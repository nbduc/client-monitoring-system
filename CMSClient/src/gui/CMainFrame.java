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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.nio.file.WatchKey;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.StandardWatchEventKinds;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import logrecord.CreateLogRecord;
import logrecord.DeleteLogRecord;
import logrecord.LogInLogRecord;
import logrecord.LogOutLogRecord;
import logrecord.LogRecord;
import logrecord.RenameLogRecord;
import logrecord.UpdateLogRecord;
import model.CustomFolder;
import smessage.DirectoryTreeRequest;
import smessage.ServerMessage;
import util.DirectoryTreeUtil;
import util.LogRecordUtils;
import util.ServerComunicator;

/**
 *
 * @author duc
 */

public final class CMainFrame extends JFrame{
    private final String DEFAULT_IP = "127.0.0.1";
    private final Integer DEFAULT_PORT = 3210;
    private final String DEFAULT_DIRECTORY = "C:/ClientMonitoringSystem/Data";
    private final Integer DEFAULT_CLIENT_PORT = 3211;
    private ServerSocket serverSocket;
    private Gson gson = new Gson();
    
    private JTextField ipTextField;
    private JTextField portTextField;
    private JLabel connectionStatusLabel;
    private JButton startButton;
    private JButton stopButton;
    
    private JPanel watchDirectoryPanel;
    
    private JTextField directoryPathTextField;
//    private DirectoryChooserButton directoryChooserButton;
    private JTable logTable;
    private JComboBox actionComboBox;
    private JTextField startDateTextField;
    private JTextField endDateTextField;
    
    private TableRowSorter<LogRecordTableModel> sorter;
    private List<RowFilter<Object, Object>> filters;
    private TableCellRenderer logTableCellRenderer;
    
    private WatchService watchService;
    private Map<WatchKey, Path> watchKeys;
    
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    //ip
    private String ip;
    public String getIp() {
        return ip;
    }
    public void setIp(String newIp) {
        String oldIp = this.ip;
        this.ip = newIp;
        propertyChangeSupport.firePropertyChange("ip", oldIp, newIp);
    }
    
    //port
    private Integer port;
    public Integer getPort(){
        return this.port;
    }
    public void setPort(Integer newPort){
        Integer oldPort = this.port;
        this.port = newPort;
        propertyChangeSupport.firePropertyChange("port", oldPort, newPort);
    }
    
    //connection status
    private Boolean isConnected;
    public Boolean getIsConnected() {
        return isConnected;
    }
    public void setIsConnected(Boolean isConnected) {
        Boolean oldValue = this.isConnected;
        this.isConnected = isConnected;
        propertyChangeSupport.firePropertyChange("isConnected", oldValue, isConnected);
    }
    
    //current directory
    private File currentDirectory;
    public File getCurrentDirectory() {
        return currentDirectory;
    }
    public void setCurrentDirectory(File newCurrentDirectory) {
        File oldCurrentDirectory = this.currentDirectory;
        this.currentDirectory = newCurrentDirectory;
        propertyChangeSupport.firePropertyChange("currentDirectory",
                                   oldCurrentDirectory, newCurrentDirectory);
    }
    
    //custom component
    private class DirectoryChooserButton extends JButton{
        private JFileChooser directoryChooser;
        private File currentDirectory;
        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
        public PropertyChangeSupport getPropertyChangeSupport(){
            return this.propertyChangeSupport;
        }
        private void createUI(){
            directoryChooser = new JFileChooser();
            directoryChooser.setCurrentDirectory(getCurrentDirectory() != null? getCurrentDirectory() : new File(System.getProperty("user.dir")));
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            addActionListener((ActionEvent e) -> {
                int returnVal = directoryChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    setCurrentDirectory(directoryChooser.getSelectedFile());
                }
            });
        }
        public DirectoryChooserButton(String text, File currentDirectory){
            super(text);
            this.currentDirectory = currentDirectory;
            createUI();
        }

        public File getCurrentDirectory() {
            return currentDirectory;
        }

        public void setCurrentDirectory(File newCurrentDirectory) {
            File oldCurrentDirectory = this.currentDirectory;
            this.currentDirectory = newCurrentDirectory;
            propertyChangeSupport.firePropertyChange("currentDirectory",
                                       oldCurrentDirectory, newCurrentDirectory);
        }

    }
    private class LogRecordTableModel extends AbstractTableModel{
        public ArrayList<LogRecord> logRecordList;
        public LogRecordTableModel(ArrayList<LogRecord> logRecordList){
            this.logRecordList = logRecordList;
        }
        private final String[] columnNames = {
            "#",
            "Time",
            "Action",
            "Desciption"
        };
        
        public final Object[] longValues = 
            {10, new Date(), "*".repeat(10), "*".repeat(100)};

        @Override
        public int getRowCount() {
            return logRecordList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(logRecordList.isEmpty()){
                return null;
            } 
            LogRecord record = logRecordList.get(rowIndex);
            switch(columnIndex){
                case 0: return logRecordList.indexOf(record);
                case 1: return new Date(record.getTime() * 1000);
                case 2: return record.getAction();
                case 3: return record.getDescription();
                default: return null;
            }
        }
        
        public Class getColumnClass(int c) {
            if(logRecordList.size() == 0){
                return Object.class;
            }
            return getValueAt(0, c).getClass();
        }
        
        public void initColumnSizes(JTable table) {
            TableColumn column;
            Component comp;
            int headerWidth;
            int cellWidth;
            Object[] longValues = this.longValues;
            TableCellRenderer headerRenderer =
                table.getTableHeader().getDefaultRenderer();

            for (int i = 0; i < this.getColumnCount(); i++) {
                column = table.getColumnModel().getColumn(i);

                comp = headerRenderer.getTableCellRendererComponent(
                    null, column.getHeaderValue(), false, false, 0, 0);
                headerWidth = comp.getPreferredSize().width;

                if(i == 1){
                    System.out.println(this.getColumnClass(i));
                }
                comp = table.getDefaultRenderer(this.getColumnClass(i))
                    .getTableCellRendererComponent(table, longValues[i], false, false, 0, i);
                cellWidth = comp.getPreferredSize().width;

                column.setPreferredWidth(Math.max(headerWidth, cellWidth));
            }
        }
    }
    
    //listeners
    private class ConnectionStatusChangeListener implements PropertyChangeListener{
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if(source == getInstance()){
                updateConnectionStatusOnUI();
            }
        }
    }
//    private class DirectoryChangeListener implements PropertyChangeListener{
//        @Override
//        public void propertyChange(PropertyChangeEvent evt) {
//            Object source = evt.getSource();
//            if(source == directoryChooserButton){
//                setCurrentDirectory(directoryChooserButton.getCurrentDirectory());
//            } else if(source == getInstance()){
//                updateCurrentDirectoryOnUI();
//            }
//        }
//    }
    private class ipTextFieldChangeListener implements DocumentListener{

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
            setIp(ipTextField.getText());
        }
    }
    private class portTextFieldChangeListener implements DocumentListener{
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
            setPort(Integer.parseInt(portTextField.getText()));
        }
    }
    
    //frame singleton
    private static CMainFrame instance;
    public static CMainFrame getInstance(){
       if(instance == null){
           instance = new CMainFrame();
       }
       return instance;
    }
    
    //frame constructor
    public CMainFrame(){
        initServerSocket();
        initPropertyValues();
        initWatchService();
        createAndShowGUI();
    }
    
    private class RequestHandler implements Runnable{
        private final Socket socket;
        public RequestHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try(BufferedReader br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8));){
                // Receive request from server
                String messageJson = br.readLine();
                ServerMessage message = gson.fromJson(messageJson, ServerMessage.class);
                if(message.getTitle() == ServerMessage.MessageType.DIRECTORY_TREE_REQUEST){
                    String path = (gson.fromJson(messageJson, DirectoryTreeRequest.class)).getRequestedPath();
                    bw.newLine();
                    bw.flush();
                }
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }
    
    private void initServerSocket(){
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            serverSocket = new ServerSocket(DEFAULT_CLIENT_PORT);
            System.out.println("Client server started: " + serverSocket);
            Thread thread = new Thread(() -> {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        System.out.println("Server accepted: " + socket);
                        
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
        }
    }
    
    private void initPropertyValues(){
        //init ip
        ip = DEFAULT_IP;
        
        //init port
        port = DEFAULT_PORT;
        
        //init connection status
        setIsConnected((Boolean) false);
        
        //init current directory
        currentDirectory = new File(DEFAULT_DIRECTORY);
        if(!currentDirectory.exists()){
            boolean result = currentDirectory.mkdirs();
        }
        
        logTableCellRenderer = new DefaultTableCellRenderer(){
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column){
                return super.getTableCellRendererComponent(table, formatter.format(value), isSelected, hasFocus, row, column);
            }
        };
    }
    private void walkAndRegisterDirectories(final Path start, Map<WatchKey, Path> watchKeys) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) throws IOException {
                WatchKey key = directory.register(watchService, 
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                watchKeys.put(key, directory);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    private class WatchServiceRunnable implements Runnable{
        private Thread thread;
        private Map<WatchKey, Path> watchKeys;
        public WatchServiceRunnable(Map<WatchKey, Path> watchKeys){
            this.watchKeys = watchKeys;
        }
        
        @Override
        public void run() {
            for(;;){
                WatchKey key;
                try {
                    key = watchService.take();
                    Path dir = watchKeys.get(key);
                    if (dir == null) {
                        System.err.println("WatchKey not recognized!!");
                        continue;
                    }

                    List<WatchEvent<?>> eventList = key.pollEvents();

                    boolean valid = key.reset();
                    if (!valid) {
                        watchKeys.remove(key);
                        if (watchKeys.isEmpty()) {
                            break;
                        }
                    }
                    
                    LogRecord newLogRecord = null;
                    //
                    if(eventList.size() == 1){
                        WatchEvent<?> event = eventList.get(0);
                        WatchEvent.Kind kind = event.kind();

                        Path path = dir.resolve(((WatchEvent<Path>) event).context());
                        if(kind == StandardWatchEventKinds.ENTRY_CREATE){
                            newLogRecord = new CreateLogRecord(path);
                            try {
                                if (Files.isDirectory(path)) {
                                    walkAndRegisterDirectories(path, watchKeys);
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE){
                            newLogRecord = new DeleteLogRecord(path);
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY){
                            newLogRecord = new UpdateLogRecord(path);
                        }
                    }
                    
                    //rename
                    if(eventList.size() == 2){
                        WatchEvent<?> event1 = eventList.get(0);
                        WatchEvent<?> event2 = eventList.get(1);
                        if(event1.kind() == StandardWatchEventKinds.ENTRY_DELETE 
                                && event2.kind() == StandardWatchEventKinds.ENTRY_CREATE){
                            Path oldPath = dir.resolve(((WatchEvent<Path>) event1).context());
                            Path newPath = dir.resolve(((WatchEvent<Path>) event2).context());
                            newLogRecord = new RenameLogRecord(oldPath, newPath);
                        }
                    }
                    
                    LogRecordUtils.writeLog(newLogRecord);
                    updateLogTable();

                    if(newLogRecord != null){
                        sendLogRecord(newLogRecord);
                    }

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        public void start(){
            if (thread == null){
                thread = new Thread(this);
                thread.start();
            }
        }
        
        public void stop(){
            if(thread != null){
                thread = null;
            }
        }
    }
    private void initWatchService(){
        //References: https://howtodoinjava.com/java8/java-8-watchservice-api-tutorial/
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            watchKeys = new HashMap<>();
            walkAndRegisterDirectories(currentDirectory.toPath(), watchKeys);
            WatchServiceRunnable watchServiceRunnable = new WatchServiceRunnable(watchKeys);
            watchServiceRunnable.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
    
    private void createAndShowGUI(){
//        this.propertyChangeSupport.addPropertyChangeListener("currentDirectory", new DirectoryChangeListener());
        this.propertyChangeSupport.addPropertyChangeListener("isConnected", new ConnectionStatusChangeListener());
        //connect panel
        ipTextField = new JTextField(25);
        ipTextField.setText(ip != null? ip : "");
        ipTextField.getDocument().addDocumentListener(new ipTextFieldChangeListener());
        portTextField = new JTextField(5);
        portTextField.setText(port != null? port.toString() : "");
        connectionStatusLabel = new JLabel("Not connected");
        startButton = new JButton("Start");
        startButton.addActionListener((ActionEvent evt) -> startConnection());
        stopButton = new JButton("Stop");
        stopButton.addActionListener((ActionEvent evt) -> stopConnection());
        updateConnectionStatusOnUI();
        
        JPanel wrapper1 = new JPanel();
        wrapper1.setLayout(new FlowLayout(FlowLayout.LEADING));
        wrapper1.add(new JLabel("Server IP:"));
        wrapper1.add(ipTextField);
        wrapper1.add(new JLabel("Port:"));
        wrapper1.add(portTextField);
        
        JPanel wrapper2 = new JPanel();
        wrapper2.setLayout(new FlowLayout(FlowLayout.LEADING));
        wrapper2.add(new JLabel("Status:"));
        wrapper2.add(connectionStatusLabel);
        wrapper2.add(startButton);
        wrapper2.add(stopButton);
        
        JPanel connectPanel = new JPanel();
        connectPanel.setBorder(BorderFactory.createTitledBorder("Connection"));
        connectPanel.setLayout(new BorderLayout());
        connectPanel.add(wrapper1, BorderLayout.NORTH);
        connectPanel.add(wrapper2, BorderLayout.SOUTH);
        
        //watch directory panel
        directoryPathTextField = new JTextField();
        directoryPathTextField.setEditable(false);
        updateCurrentDirectoryOnUI();
//        directoryChooserButton = new DirectoryChooserButton("Choose...", currentDirectory);
//        directoryChooserButton.getPropertyChangeSupport()
//                .addPropertyChangeListener("currentDirectory", new DirectoryChangeListener());
        
        watchDirectoryPanel = new JPanel();
        watchDirectoryPanel.setBorder(BorderFactory.createTitledBorder("Watched Directory"));
        watchDirectoryPanel.setLayout(new GridBagLayout());
        GridBagConstraints c;
        c = new GridBagConstraints();
        c.insets = new Insets(3,3,3,3);
        c.gridy = 0;
        c.gridx = 0;
        watchDirectoryPanel.add(new JLabel("Directory:"), c);
        c = new GridBagConstraints();
        c.insets = new Insets(3,3,3,3);
        c.gridy = 0;
        c.gridx = 1;
        c.weightx = 1.0; //take all extra space
        c.fill = GridBagConstraints.BOTH; //fill all the space
        watchDirectoryPanel.add(directoryPathTextField, c);
//        c = new GridBagConstraints();
//        c.insets = new Insets(3,3,3,3);
//        c.gridy = 0;
//        c.gridx = 2;
//        watchDirectoryPanel.add(directoryChooserButton, c);
        
        //connect + watchDirectory panel
        JPanel connectAndWatchDirectoryWrapper = new JPanel();
        connectAndWatchDirectoryWrapper.setLayout(new BoxLayout(connectAndWatchDirectoryWrapper, BoxLayout.Y_AXIS));
        connectAndWatchDirectoryWrapper.add(connectPanel);
        connectAndWatchDirectoryWrapper.add(watchDirectoryPanel);
        
        //log table
        logTable = new JTable();
        updateLogTable();
        JScrollPane logScrollPanel = new JScrollPane(logTable);
        
        //filter panel
        filters = new ArrayList<>();
        RowFilter<Object, Object> defaultActionFilter = RowFilter.regexFilter("", 2);
        RowFilter<Object, Object> defaultStartDateFilter = RowFilter.dateFilter(
                RowFilter.ComparisonType.AFTER, 
                parseDate("01/01/1990"), 1);
        RowFilter<Object, Object> defaultEndDateFilter = RowFilter.dateFilter(
                RowFilter.ComparisonType.BEFORE, 
                new Date(), 1);
        filters.add(defaultActionFilter);
        filters.add(defaultStartDateFilter);
        filters.add(defaultEndDateFilter);
        DefaultComboBoxModel actionModel = new DefaultComboBoxModel();
        actionModel.addElement("All actions");
        actionModel.addAll(Arrays.asList(LogRecord.Action.values()));
        actionComboBox = new JComboBox(actionModel);
        actionComboBox.addActionListener((evt) -> {
            RowFilter<Object, Object> rf = null;
            try {
                LogRecord.Action action = (LogRecord.Action)actionComboBox.getSelectedItem();
                rf = RowFilter.regexFilter(action.toString(), 2);
            } catch (PatternSyntaxException | ClassCastException e) {
                rf = defaultActionFilter;
            }
            filters.set(0, rf);
            sorter.setRowFilter(RowFilter.andFilter(filters));
        });
        
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        startDateTextField = new JTextField(10);
        startDateTextField.setText("01/01/2022");
        startDateTextField.addActionListener((event) -> {
            RowFilter<Object, Object> rf = null;
            try{
                Date startDate = formatter.parse(startDateTextField.getText());
                if(startDate.compareTo(new Date()) <= 0){
                    rf = RowFilter.dateFilter(RowFilter.ComparisonType.AFTER, startDate, 1);
                } else {
                    rf = defaultStartDateFilter;
                }
            } catch(ParseException ex){
                rf = null;
            }
            filters.set(1, rf);
            sorter.setRowFilter(RowFilter.andFilter(filters));
        });
        endDateTextField = new JTextField(10);
        endDateTextField.setText(formatter.format(new Date()));
        endDateTextField.addActionListener((event) -> {
            RowFilter<Object, Object> rf = null;
            try{
                Date endDate = formatter.parse(startDateTextField.getText());
                if(endDate.compareTo(new Date()) <= 0){
                    rf = RowFilter.dateFilter(RowFilter.ComparisonType.BEFORE, endDate, 1);
                } else {
                    rf = defaultEndDateFilter;
                }
            } catch(ParseException ex){
                rf = null;
            }
            filters.set(2, rf);
            sorter.setRowFilter(RowFilter.andFilter(filters));
        });
        
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        filterPanel.add(new JLabel("Action:"));
        filterPanel.add(actionComboBox);
        filterPanel.add(new JLabel("   Time:"));
        filterPanel.add(startDateTextField);
        filterPanel.add(new JLabel("-"));
        filterPanel.add(endDateTextField);
        
        //log panel
        JPanel logPanel = new JPanel();
        logPanel.setBorder(BorderFactory.createTitledBorder("Change Logs"));
        logPanel.setLayout(new BorderLayout());
        
        logPanel.add(filterPanel, BorderLayout.NORTH);
        logPanel.add(logScrollPanel, BorderLayout.CENTER);
        
        //contentPane
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(connectAndWatchDirectoryWrapper, BorderLayout.NORTH);
        getContentPane().add(logPanel, BorderLayout.CENTER);
        setTitle("Client Monitoring System (Client)");
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
                    if(isConnected){
                        stopConnection();
                    }
                    System.exit(0);
                }
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    //update methods
    private void updateConnectionStatusOnUI(){
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                updateConnectionStatusOnUI();
            });
            return;
        }
        startButton.setEnabled(!isConnected);
        stopButton.setEnabled(isConnected);
        connectionStatusLabel.setForeground(isConnected? Color.BLUE : Color.RED);
        connectionStatusLabel.setText(isConnected? "Connected" : "Not Connected");
    }
    private void updateCurrentDirectoryOnUI(){
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                updateCurrentDirectoryOnUI();
            });
            return;
        }
        if(currentDirectory != null){
            directoryPathTextField.setText(currentDirectory.toPath().toString());
        } else {
            directoryPathTextField.setText(null);
        }
    }
    private void updateLogTable(){
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                updateLogTable();
            });
            return;
        }
        LogRecordTableModel model = new LogRecordTableModel(LogRecordUtils.readAllLogs());
        logTable.setModel(model);
        logTable.getColumnModel().getColumn(1).setCellRenderer(logTableCellRenderer);
        model.initColumnSizes(logTable);
        sorter = new TableRowSorter<>(model);
        logTable.setRowSorter(sorter);
    }
    
    //server comunication
    private void startConnection(){
        LogInLogRecord logInLogRecord = new LogInLogRecord();
        
        Socket socket = getSocket();
        if(socket != null) {
            CompletableFuture<Boolean> sendingGreetingFuture = CompletableFuture.supplyAsync(() -> {
            return ServerComunicator.sendLogRecord(socket, logInLogRecord);
            });
            sendingGreetingFuture.thenAccept(successful -> {
                if(successful) {
                    System.out.println("Connected: " + socket);
                    setIsConnected(true);
                    LogRecordUtils.writeLog(logInLogRecord);
                    updateLogTable();
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "Cannot connect to server!", "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            JOptionPane.showMessageDialog(null, 
                "Cannot connect to server!", "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    private void stopConnection(){
        LogOutLogRecord logOutLogRecord = new LogOutLogRecord();
        
        Socket socket = getSocket();
        if(socket != null){
            CompletableFuture<Boolean> sendingOffFuture = CompletableFuture.supplyAsync(() -> {
                return ServerComunicator.sendLogRecord(socket, logOutLogRecord);
            });
            sendingOffFuture.thenAccept(successful -> {
                if(successful) {
                    setIsConnected(false);
                    LogRecordUtils.writeLog(logOutLogRecord);
                    updateLogTable();
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "Cannot connect to server. Going to stop connecting anyway.", "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        
    }
    private void sendLogRecord(LogRecord logRecord){
        Socket socket = getSocket();
        if(socket != null){
            CompletableFuture<Boolean> sendingLogRecordFuture = CompletableFuture.supplyAsync(() -> {
                return ServerComunicator.sendLogRecord(socket, logRecord);
            });
            sendingLogRecordFuture.thenAccept(successful -> {
                if(successful){
                    System.out.println("Successful sending log");
                } else {
                    System.err.println("Server refused to receive log.");
                }
            });
        }
    }
    
    //get socket
    private Socket getSocket(){
        try {
            Socket socket = new Socket(ip, port);
            socket.setSoTimeout(10*1000);
            return socket;
        }catch (IOException ex) {
            //server ngỏm rồi
            return null;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                // handle exception
                ex.printStackTrace();
            }
            CMainFrame window = CMainFrame.getInstance();
            window.setVisible(true);
        });
    }
}
