/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import cmessage.DirectoryTreeResponse;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import model.CustomFolder;
import smessage.DirectoryTreeRequest;

/**
 *
 * @author duc
 */
public class ClientCommunicator {
    private static Gson gson = new Gson();
    
    public static CustomFolder sendDirectoryTreeRequest(Socket socket, String requestedPath){
        try(BufferedReader br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8));){
            //sending request
            DirectoryTreeRequest request = new DirectoryTreeRequest(requestedPath);
            bw.write(request.toJsonString());
            bw.newLine();
            bw.flush();
            
            //receive reponse
            String responseJson = br.readLine();
            DirectoryTreeResponse response = gson.fromJson(responseJson, DirectoryTreeResponse.class);
            return response.getFolder();
        } catch(IOException ex){
            ex.printStackTrace();
            return null;
        }
    }
}
