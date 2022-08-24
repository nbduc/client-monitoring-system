/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import model.Client;

/**
 *
 * @author duc
 */
public class ClientUtils {
    private static final String CLIENT_LIST_PATH = "clients.txt";
    private ArrayList<Client> clientList;
    public static final String CLIENT_LIST = "clientList";
    private PropertyChangeSupport propertyChangeSupport;
    
    private File getClientFile(){
        File clientListFile = new File(CLIENT_LIST_PATH);
        if(!clientListFile.exists()){
            try {
                clientListFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return clientListFile;
    }
    
    private void loadClientList(){
        clientList = new ArrayList<>();
        File clientListFile = getClientFile();
        try(BufferedReader br = new BufferedReader(new FileReader(clientListFile))){
            String line = br.readLine();
            while(line != null){
                String[] parts = line.split(",");
                Client client = new Client(parts[0], false, parts[1]);
                clientList.add(client);
                line = br.readLine();
            }
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public ClientUtils(){
        if(clientList == null){
            loadClientList();
        }
        if(this.propertyChangeSupport == null){
            this.propertyChangeSupport = new PropertyChangeSupport(this);
        }
    }
    
    public PropertyChangeSupport getPropertyChangeSupport(){
        return this.propertyChangeSupport;
    }
    
    public ArrayList<Client> getAllClients(){
        return clientList;
    }
    
    public Client getClientByIp(String ip){
        Enumeration<Client> e = Collections.enumeration(clientList);
        while(e.hasMoreElements()){
            Client client = e.nextElement();
            if (ip.equals(client.getIp())){
                return client;
            }
        }
        return null;
    }
    
    public Boolean addNewClient(Client client){
        clientList.add(client);
        File clientListFile = new File(CLIENT_LIST_PATH);
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(clientListFile))){
            bw.write(client.toCsv());
            bw.newLine();
            bw.flush();
        } catch (IOException ex){
            ex.printStackTrace();
        }
        this.propertyChangeSupport.firePropertyChange(CLIENT_LIST, null, clientList);
        return true;
    }
}
