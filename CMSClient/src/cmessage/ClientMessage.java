/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmessage;
import com.google.gson.Gson;
import logrecord.LogRecord;

/**
 *
 * @author duc
 */
public class ClientMessage {
    protected MessageType title;
    protected LogRecord payload;
    public static enum MessageType{
        SENDING_LOG_RECORD,
        DIRECTORY_TREE_RESPONSE;
    }
    
    public MessageType getTitle(){
        return this.title;
    };
    
    public LogRecord getPayload(){
        return this.payload;
    }
    
    public String toJsonString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
