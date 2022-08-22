/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import cmessage.ClientMessage;
import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
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
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.Client;
import smessage.ServerStatusNotification;

/**
 *
 * @author duc
 */
public class SMainFrame extends JFrame{
    public final Integer NUMBER_OF_THREAD = 10;
    public final Integer PORT = 3210;
    
    private JTextField searchClientTextField;
    private JButton searchClientButton;
    private JTextField portTextField;
    
    private ServerSocket serverSocket;
    private Gson gson = new Gson();
    private Map<String, String> ipList;
    private Map<String, Client> clientList;
    
    //listeners
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
    
    //custom components
    
    
    //frame constructor
    public SMainFrame(){
        this.ipList = getServerIp();
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
        public RequestHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Processing: " + socket);
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8));
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));) {
                // Receive data from client
                String messageJson = br.readLine();
                ClientMessage message = gson.fromJson(messageJson, ClientMessage.class);
                
                if(message.getTitle() == ClientMessage.MessageType.GREETING){
                    ServerStatusNotification response = new ServerStatusNotification(true);
                    bw.write(response.toJsonString());
                    bw.newLine();
                    bw.flush();
                }
            } catch (IOException ex) {
                // chỗ naày cần ghi log chứ không hiện form thông báo
                JOptionPane.showMessageDialog(null, 
                    "Request Processing Error: " + ex, "Error", 
                    JOptionPane.ERROR_MESSAGE);
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
        //set client display mode
        JRadioButton connectedClientRadButton = new JRadioButton("Connected Clients");
        connectedClientRadButton.setSelected(true);
        JRadioButton allClientRadButton = new JRadioButton("All Clients");
        
        ButtonGroup clientDisplayButtonGroup = new ButtonGroup();
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
        
        JList clientJList = new JList();
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
        ipList.forEach((name, address) -> {
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
        clientInfoPanel.add(new JLabel("Client information:"));
        
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
        rightPane.add(tabbedPane, BorderLayout.CENTER);
        
        JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);

        //contentPane
        getContentPane().add(mainPane);
        setTitle("Client Monitoring System (Server)");
        setSize(750, 600);
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
