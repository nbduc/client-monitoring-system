/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import cmessage.GreetingMessage;
import logrecord.LogRecord;
import cmessage.SendingLogRecordMessage;
import com.google.gson.Gson;
import smessage.GreetingResponseMessage;
import smessage.SendingLogRecordReponseMessage;

/**
 *
 * @author duc
 */
public class ServerComunicator {
    public static Gson gson = new Gson();
    
    public final static Boolean sendLogRecord(Socket socket, LogRecord record){
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream(), StandardCharsets.UTF_8));
                BufferedReader br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));) {
            //sending message
            SendingLogRecordMessage message = new SendingLogRecordMessage(record);
            bw.write(message.toJsonString());
            
            //receiving message
            String responseJson = br.readLine();
            SendingLogRecordReponseMessage response = gson.fromJson(responseJson, SendingLogRecordReponseMessage.class);
            return response.getStatus();
            
        } catch (IOException ex){
            ex.printStackTrace();
            return false;
        }
    }
    
    public final static Boolean sendGreeting(Socket socket){
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream(), StandardCharsets.UTF_8));
                BufferedReader br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));) {
            //sending message
            GreetingMessage message = new GreetingMessage();
            bw.write(message.toJsonString());
            bw.newLine();
            bw.flush();
            
            //receiving message
            String responseJson = br.readLine();
            GreetingResponseMessage response = gson.fromJson(responseJson, GreetingResponseMessage.class);
            return response.getStatus();
            
        } catch (IOException ex){
            ex.printStackTrace();
            return false;
        }
    }
    
    
}
