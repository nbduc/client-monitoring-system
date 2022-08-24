/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import cmessage.ClientMessage;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import smessage.ServerStatusNotification;

/**
 *
 * @author duc
 */
public class ClientCommunicator {
    private static Gson gson = new Gson();
    public static ClientMessage readClientMessage(Socket socket){
        try(BufferedReader br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));){
            // Receive data from client
            String messageJson = br.readLine();
            return gson.fromJson(messageJson, ClientMessage.class);
        } catch (IOException ex){
            ex.printStackTrace();
        }
        return null;
    }
    
    public static Boolean sendServerStatus(Socket socket, Boolean serverStatus){
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8));){
            //gửi phản hồi
            ServerStatusNotification response = new ServerStatusNotification(serverStatus);
            bw.write(response.toJsonString());
            bw.newLine();
            bw.flush();
            return true;
        } catch (IOException ex){
            ex.printStackTrace();
        }
        return false;
    }
}
