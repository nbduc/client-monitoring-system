/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.DocumentListener;
import logrecord.CreateLogRecord;
import logrecord.DeleteLogRecord;
import logrecord.LogRecord;
import logrecord.RenameLogRecord;
import logrecord.UpdateLogRecord;
import util.ServerComunicator;

/**
 *
 * @author duc
 */

public final class CMainFrame extends JFrame{
    private final String DEFAULT_IP = "127.0.0.1";
    private final Integer DEFAULT_PORT = 3210;
    private final String DEFAULT_DIRECTORY = "C:/ClientMonitoringSystem/Data";
    
    private JTextField ipTextField;
    private JTextField portTextField;
    private JLabel connectionStatusLabel;
    private JButton startButton;
    private JButton stopButton;
    
    private JPanel watchDirectoryPanel;
    
    private JTextField directoryPathTextField;
    private DirectoryChooserButton directoryChooserButton;
    private JPanel logPanel;
    
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
            "Time",
            "Action",
            "Desciption"
        };

        @Override
        public int getRowCount() {
            return logRecordList.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(logRecordList.isEmpty()){
                return null;
            } 
            LogRecord record = logRecordList.get(rowIndex);
            switch(columnIndex){
                case 0: return record.getTime();
                case 1: return record.getAction();
                case 2: return record.getDescription();
                default: return null;
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
    private class DirectoryChangeListener implements PropertyChangeListener{
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if(source == directoryChooserButton){
                setCurrentDirectory(directoryChooserButton.getCurrentDirectory());
            } else if(source == getInstance()){
                updateCurrentDirectoryOnUI();
            }
        }
    }
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
        initPropertyValues();
        initWatchService();
        createAndShowGUI();
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
            System.out.println(result);
        }
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
    
    private void createAndShowGUI(){
        this.propertyChangeSupport.addPropertyChangeListener("currentDirectory", new DirectoryChangeListener());
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
        directoryChooserButton = new DirectoryChooserButton("Choose...", currentDirectory);
        directoryChooserButton.getPropertyChangeSupport()
                .addPropertyChangeListener("currentDirectory", new DirectoryChangeListener());
        
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
        c = new GridBagConstraints();
        c.insets = new Insets(3,3,3,3);
        c.gridy = 0;
        c.gridx = 2;
        watchDirectoryPanel.add(directoryChooserButton, c);
        
        //connect + watchDirectory panel
        JPanel connectAndWatchDirectoryWrapper = new JPanel();
        connectAndWatchDirectoryWrapper.setLayout(new BoxLayout(connectAndWatchDirectoryWrapper, BoxLayout.Y_AXIS));
        connectAndWatchDirectoryWrapper.add(connectPanel);
        connectAndWatchDirectoryWrapper.add(watchDirectoryPanel);
        
        //log panel
        logPanel = new JPanel();
        logPanel.setBorder(BorderFactory.createTitledBorder("Change Logs"));
        
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
                    stopConnection();
                    System.exit(0);
                }
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    //update methods
    private void updateConnectionStatusOnUI(){
        startButton.setEnabled(!isConnected);
        stopButton.setEnabled(isConnected);
        connectionStatusLabel.setForeground(isConnected? Color.BLUE : Color.RED);
        connectionStatusLabel.setText(isConnected? "Connected" : "Not Connected");
    }
    private void updateCurrentDirectoryOnUI(){
        if(currentDirectory != null){
            directoryPathTextField.setText(currentDirectory.toPath().toString());
        } else {
            directoryPathTextField.setText(null);
        }
    }
    
    //server comunication
    private void startConnection(){
        System.out.println("Connect to: " + ip);
        Socket socket = getSocket();
        CompletableFuture<Boolean> sendingGreetingFuture = CompletableFuture.supplyAsync(() -> {
            return ServerComunicator.sendGreeting(socket);
        });
        sendingGreetingFuture.thenAccept(successful -> {
            if(successful) {
                System.out.println("Connected: " + socket);
                setIsConnected(true);
            } else {
                JOptionPane.showMessageDialog(null, 
                    "Cannot connect to server!", "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    private void stopConnection(){
        Socket socket = getSocket();
        CompletableFuture<Boolean> sendingOffFuture = CompletableFuture.supplyAsync(() -> {
            return ServerComunicator.sendOff(socket);
        });
        sendingOffFuture.thenAccept(successful -> {
            if(successful) {
                setIsConnected(false);
            } else {
                JOptionPane.showMessageDialog(null, 
                    "Cannot connect to server. Going to stop connecting anyway.", "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    private void sendLogRecord(LogRecord logRecord){
        Socket socket = getSocket();
        CompletableFuture<Boolean> sendingLogRecordFuture = CompletableFuture.supplyAsync(() -> {
            return ServerComunicator.sendLogRecord(socket, logRecord);
        });
        sendingLogRecordFuture.thenAccept(successful -> {
            if(!successful){
                JOptionPane.showMessageDialog(null, 
                    "Cannot connect to the server!", "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    //get socket
    private Socket getSocket(){
        try {
            Socket socket = new Socket(ip, port);
            socket.setSoTimeout(10*1000);
            return socket;
        }catch (IOException ex) {
            JOptionPane.showMessageDialog(null, 
                "Cannot connect to server!", "Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        return null;
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
