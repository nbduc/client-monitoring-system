/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.Client;

/**
 *
 * @author duc
 */
public class SMainFrame extends JFrame{
    private JTextField searchClientTextField;
    private JButton searchClientButton;
    
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
        createAndShowGUI();
    }
    
    //
    private void createAndShowGUI(){
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
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 1.0; //take all extra space
        c.fill = GridBagConstraints.BOTH; //fill all the space
        searchClientPane.add(searchClientTextField, c);
        c = new GridBagConstraints();
        c.gridy = 0;
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

        JPanel leftPane = new JPanel();
        leftPane.setPreferredSize(new Dimension(250, -1));
        leftPane.setLayout(new BorderLayout());
        leftPane.add(searchClientPane, BorderLayout.NORTH);
        leftPane.add(clientPane, BorderLayout.CENTER);
        
        JPanel rightPane = new JPanel();
        
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
            } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                // handle exception
            }
            SMainFrame window = SMainFrame.getInstance();
            window.setVisible(true);
        });
    }
}
