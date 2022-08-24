/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import cmessage.ClientMessage;
import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import logrecord.LogRecord;
import model.Client;
import smessage.ServerStatusNotification;
import util.ClientCommunicator;
import util.ClientUtils;

/**
 *
 * @author duc
 */
public class SMainFrame extends JFrame{
    public static final String CONNECTED_CLIENTS = "connectedClients";
    public static final String IS_ALL_CLIENTS = "isAllClients";
    public final Integer NUMBER_OF_THREAD = 10;
    public final Integer PORT = 3210;
    private Gson gson = new Gson();
    
    private JLabel notificationLabel;
    private JLabel connectedClientCountLabel;
    private JTextField searchClientTextField;
    private JButton searchClientButton;
    private JTextField portTextField;
    private JTable logTable;
    private JList clientJList;
    private ButtonGroup clientDisplayButtonGroup;
    
    private ServerSocket serverSocket;
    private Map<String, String> serverIfNetwordList;
    private ClientUtils clientUtils;
    
    //property change
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    //connected clients
    private ArrayList<Client> connectedClients;
    public void addConnectedClient(Client client){
        connectedClients.add(client);
        propertyChangeSupport.firePropertyChange(SMainFrame.CONNECTED_CLIENTS, null, connectedClients);
    }
    public void removeConnectedClient(Client client){
        connectedClients.remove(client);
        propertyChangeSupport.firePropertyChange(SMainFrame.CONNECTED_CLIENTS, connectedClients, null);
    }
    
    //client displaymode
    private Boolean isAllClients = false;
    public void setIsAllClients(Boolean value){
        Boolean oldValue = isAllClients;
        isAllClients = value;
        propertyChangeSupport.firePropertyChange(SMainFrame.IS_ALL_CLIENTS, oldValue, value);
    }
    
    //listeners
    private class IsAllClientsChangeListener implements PropertyChangeListener{
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(() -> {
                    propertyChange(evt);
                });
                return;
            }
            if(evt.getSource() == getInstance()){
                if(SMainFrame.IS_ALL_CLIENTS.equals(evt.getPropertyName())){
                    updateClientJList();
                }
            }
        }
    }
    
    private class ConnectedClientsChangeListener implements PropertyChangeListener{
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(() -> {
                    propertyChange(evt);
                });
                return;
            }
            if(evt.getSource() == getInstance()){
                if(SMainFrame.CONNECTED_CLIENTS.equals(evt.getPropertyName())){
                    updateClientJList();
                }
            }
        }
    }
    
    private class AllClientsChangeListener implements PropertyChangeListener{
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            //thêm client mới vào JList
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(() -> {
                    propertyChange(evt);
                });
                return;
            }
            if (evt.getSource() == clientUtils) {
                if (ClientUtils.CLIENT_LIST.equals(evt.getPropertyName())) {
                    updateClientJList();
                }
            }
        }
    }
    
    private class SearchClientDocumentListener implements DocumentListener{

        @Override
        public void insertUpdate(DocumentEvent e) {
            action();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            action();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            action();
        }
        
        public void action() {
            if ("".equals(searchClientTextField.getText())){
//                    createNewWordList(originWordList);
            }
        }
        
    }
    
    //table model
    private class LogTableModel extends AbstractTableModel {
        private Client client;
        private ArrayList<LogRecord> logRecords;
        public LogTableModel(Client client, ArrayList<LogRecord> logRecords){
            super();
            this.client = client;
            this.logRecords = logRecords;
        }
        
        private final String[] columnNames = {
            "#",
            "Time",
            "Action",
            "IP",
            "Description"
        };
        
        public final Object[] longValues = 
            {10, "*".repeat(20), "*".repeat(10), "*".repeat(50), 
                "*".repeat(100)};

        @Override
        public int getRowCount() {
            return logRecords.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(logRecords.size() == 0){
                return null;
            }
            LogRecord record = logRecords.get(rowIndex);
            switch(columnIndex){
                case 0: return logRecords.indexOf(record);
                case 1: return record.getTime();
                case 2: return record.getAction();
                case 3: return client.getIp();
                case 4: return record.getDescription();
                default: return null;
            }
        }
    }
    
    //frame constructor
    public SMainFrame(){
        this.serverIfNetwordList = getServerIp();
        this.connectedClients = new ArrayList<>();
        this.propertyChangeSupport
                .addPropertyChangeListener(SMainFrame.IS_ALL_CLIENTS, new IsAllClientsChangeListener());
        this.propertyChangeSupport
                .addPropertyChangeListener(SMainFrame.CONNECTED_CLIENTS, new ConnectedClientsChangeListener());
        this.clientUtils = new ClientUtils();
        this.clientUtils.getPropertyChangeSupport()
                .addPropertyChangeListener(ClientUtils.CLIENT_LIST, new AllClientsChangeListener());
        initServerSocket();
        createAndShowGUI();
    }
    
    private Map<String, String> getServerIp(){
        TreeMap<String, String> ipTreeMap = new TreeMap<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters inactive interfaces
                if (!iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet6Address) continue;
                    ipTreeMap.put(iface.getDisplayName(), addr.getHostAddress());
                }
            }
            return ipTreeMap;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
    
    private class RequestHandler implements Runnable{
        private final Socket socket;
        private InetAddress clientAddress;
        public RequestHandler(Socket socket){
            this.socket = socket;
            this.clientAddress = socket.getInetAddress();
        }

        @Override
        public void run() {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8));){
                // Receive data from client
                String messageJson = br.readLine();
                ClientMessage message = gson.fromJson(messageJson, ClientMessage.class);
                
                Client client = clientUtils.getClientByIp(clientAddress.getHostAddress());
                if(message.getTitle() == ClientMessage.MessageType.GREETING){
                    if (client == null){
                        //chưa có (client mới)
                        client = new Client(clientAddress.getHostAddress(), true);
                        clientUtils.addNewClient(client);
                    }
                    clientConnecting(client);
                }

                if(message.getTitle() == ClientMessage.MessageType.OFF){
                    if (client == null){
                        System.err.println("Cannot find this client: " + client.toString());
                    }
                    clientLeaving(client);
                }
                
                ServerStatusNotification response = new ServerStatusNotification(true);
                bw.write(response.toJsonString());
                bw.newLine();
                bw.flush();
            } catch (IOException ex){
                ex.printStackTrace();
            }
            System.out.println("Complete processing: " + socket);
        }
    }
    
    private void initServerSocket(){
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREAD);
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started: " + serverSocket);
            Thread thread = new Thread(() -> {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        System.out.println("Client accepted: " + socket);

                        RequestHandler handler = new RequestHandler(socket);
                        executor.execute(handler);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, 
                            "Connection Error: " + ex, "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            thread.start();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, 
                "Connection Error: " + ex, "Error", 
                JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(SMainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //
    private void createAndShowGUI(){
        //set notify panel
        JPanel notificationPanel = new JPanel();
        notificationPanel.setBorder(BorderFactory.createTitledBorder("Notification"));
        notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));
        
        notificationLabel = new JLabel("Server just started.");
        connectedClientCountLabel = new JLabel();
        connectedClientCountLabel.setText("("+connectedClients.size()+" client(s) connected.)");
        
        notificationPanel.add(notificationLabel);
        notificationPanel.add(connectedClientCountLabel);
        
        //set client display mode
        class ClientDisplayModeActionListener implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                setIsAllClients(Boolean.parseBoolean(event.getActionCommand()));
            }
        }
        ClientDisplayModeActionListener clientDisplayModeActionListener = new ClientDisplayModeActionListener();
        JRadioButton connectedClientRadButton = new JRadioButton("Connected Clients");
        connectedClientRadButton.setSelected(true);
        connectedClientRadButton.setActionCommand("false");
        connectedClientRadButton.addActionListener(clientDisplayModeActionListener);
        
        JRadioButton allClientRadButton = new JRadioButton("All Clients");
        allClientRadButton.setActionCommand("true");
        allClientRadButton.addActionListener(clientDisplayModeActionListener);
        
        clientDisplayButtonGroup = new ButtonGroup();
        clientDisplayButtonGroup.add(connectedClientRadButton);
        clientDisplayButtonGroup.add(allClientRadButton);
        
        //set search text field
        searchClientTextField = new JTextField();
//        searchClientTextField.addActionListener((ActionEvent e) -> 
//                searchAndDisplayResult(searchClientTextField.getText())
//        );
        searchClientTextField.getDocument().addDocumentListener(new SearchClientDocumentListener());
        
        //set search button
        searchClientButton = new JButton("Search");
//        searchClientButton.addActionListener((ActionEvent e) -> 
//                searchAndDisplayResult(searchClientTextField.getText()));

        //set search pane
        JPanel searchClientPane = new JPanel();
        searchClientPane.setLayout(new GridBagLayout());
        GridBagConstraints c;
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;
        c.gridx = 0;
        searchClientPane.add(connectedClientRadButton, c);
        c.gridy = 1;
        searchClientPane.add(allClientRadButton, c);
        c = new GridBagConstraints();
        c.gridy = 2;
        c.gridx = 0;
        c.weightx = 1.0; //take all extra space
        c.fill = GridBagConstraints.BOTH; //fill all the space
        searchClientPane.add(searchClientTextField, c);
        c = new GridBagConstraints();
        c.gridy = 2;
        c.gridx = 1;
        c.gridwidth = 1;
        searchClientPane.add(searchClientButton, c);
        
        //client JList
        clientJList = new JList();
        clientJList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        clientJList.setLayoutOrientation(JList.VERTICAL);
        clientJList.setVisibleRowCount(-1);
        clientJList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) 
            {
                if(SwingUtilities.isLeftMouseButton(e)){
                    Client client = (Client)clientJList.getSelectedValue();
                }
            }
        });
        
        //set client list pane
        JScrollPane clientPane = new JScrollPane(clientJList);

        //set left pane
        JPanel leftPane = new JPanel();
        leftPane.setPreferredSize(new Dimension(250, -1));
        leftPane.setLayout(new BorderLayout());
        leftPane.add(searchClientPane, BorderLayout.NORTH);
        leftPane.add(clientPane, BorderLayout.CENTER);
        leftPane.add(notificationPanel, BorderLayout.SOUTH);
        
        //set server infor pane
        JPanel serverInfoPanel = new JPanel();
        serverInfoPanel.setBorder(BorderFactory.createTitledBorder("Connection information"));
        serverInfoPanel.setLayout(new BoxLayout(serverInfoPanel, BoxLayout.Y_AXIS));
        
        portTextField = new JTextField(5);
        portTextField.setEditable(false);
        portTextField.setText(PORT.toString());
        JPanel portPanel = new JPanel();
        portPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        portPanel.add(new JLabel("Port:"));
        portPanel.add(portTextField);
        serverInfoPanel.add(portPanel);
        serverIfNetwordList.forEach((name, address) -> {
            JPanel inetPanel = new JPanel();
            inetPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JTextField addressTextField = new JTextField(25);
            addressTextField.setEditable(false);
            addressTextField.setText(address);
            inetPanel.add(addressTextField);
            inetPanel.add(new JLabel("("+name+")"));
            serverInfoPanel.add(inetPanel);
        });
        
        //client information pane
        JPanel clientInfoPanel = new JPanel();
        clientInfoPanel.setLayout(new BoxLayout(clientInfoPanel, BoxLayout.Y_AXIS));
        
        JPanel clientIpPanel = new JPanel();
        clientIpPanel.setBackground(Color.WHITE);
        clientIpPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        clientIpPanel.add(new JLabel("IP:"));
        JTextField clientIpTextField = new JTextField(25);
        clientIpTextField.setEditable(false);
        clientIpPanel.add(clientIpTextField);
        
        JPanel clientWatchedDirPanel = new JPanel();
        clientWatchedDirPanel.setBackground(Color.WHITE);
        clientWatchedDirPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        clientWatchedDirPanel.add(new JLabel("Watched directory:"));
        JTextField clientWatchedDirTextField = new JTextField(40);
        clientWatchedDirTextField.setEditable(false);
        clientWatchedDirPanel.add(clientWatchedDirTextField);
        JButton changeWatchedDirButton = new JButton("Change...");
        clientWatchedDirPanel.add(changeWatchedDirButton);
        
        clientInfoPanel.add(clientIpPanel);
        clientInfoPanel.add(clientWatchedDirPanel);
        
        //log table
        logTable = new JTable();
        
        //client log pane
        JPanel clientLogPanel = new JPanel();
        
        // set tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Client information", clientInfoPanel);
        tabbedPane.add("Change logs", clientLogPanel);
        
        //set right pane
        JPanel rightPane = new JPanel();
        rightPane.setLayout(new BorderLayout());
        rightPane.add(serverInfoPanel, BorderLayout.NORTH);
        JPanel rightBottomPanel = new JPanel();
        rightBottomPanel.setLayout(new BorderLayout());
        rightBottomPanel.add(tabbedPane, BorderLayout.NORTH);
        rightPane.add(rightBottomPanel, BorderLayout.CENTER);
        
        JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);

        //contentPane
        getContentPane().add(mainPane);
        setTitle("Client Monitoring System (Server)");
        pack();
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int option = JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to close Client Monitoring System?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE);
                if (option == JOptionPane.YES_OPTION){
                    System.exit(0);
                }
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    //frame singleton
    private static SMainFrame instance;
    public static SMainFrame getInstance(){
       if(instance == null){
           instance = new SMainFrame();
       }
       return instance;
    }
    
    //update ui funtions
    private void updateNotification(String notification){
        notificationLabel.setText(notification);
    }
    
    private void updateClientCount(){
        connectedClientCountLabel.setText("("+connectedClients.size()+" client(s) connected.)");
    }
    
    private void clientConnecting(Client client){
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                addConnectedClient(client);
                updateClientCount();
                updateNotification(client.getIp()+" just connected.");
            });
        }
    }
    
    private void clientLeaving(Client client){
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                removeConnectedClient(client);
                updateClientCount();
                updateNotification(client.getIp()+" just left.");
            });
        }
    }
    
    private void updateClientJList(){
        DefaultListModel newModel = new DefaultListModel();
        if(!isAllClients){
            newModel.addAll(connectedClients);
        } else {
            newModel.addAll(clientUtils.getAllClients());
        }
        clientJList.setModel(newModel);
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
            SMainFrame window = SMainFrame.getInstance();
            window.setVisible(true);
        });
    }
}
