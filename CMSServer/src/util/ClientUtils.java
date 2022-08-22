/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import model.Client;

/**
 *
 * @author duc
 */
public class ClientUtils {
    public static final String CLIENT_LIST_PATH = "clients.txt";
    private ArrayList<Client> clientList;
    
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
    
    private ArrayList<Client> getClientList(){
        if(clientList == null){
            clientList = new ArrayList<>();
        }
        return clientList;
    }
    
    public ArrayList<Client> getAllClients(){
        clientList = getClientList();
        File clientListFile = getClientFile();
        try(BufferedReader br = new BufferedReader(new FileReader(clientListFile))){
            String line = br.readLine();
            while(line != null){
                String[] parts = line.split(",");
                Client client = new Client(parts[0], false, parts[1]);
                clientList.add(client);
            }
        } catch(IOException ex){
            ex.printStackTrace();
        }
        return clientList;
    }
    
    public Boolean addNewClient(Client client){
        clientList = getClientList();
        clientList.add(client);
        File clientListFile = new File(CLIENT_LIST_PATH);
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(clientListFile))){
            bw.write(client.toCsv());
            bw.newLine();
            bw.flush();
        } catch (IOException ex){
            ex.printStackTrace();
        }
        return true;
    }
}
