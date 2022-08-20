/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smessage;

import com.google.gson.Gson;

/**
 *
 * @author duc
 */
public class ServerMessage {
    protected MessageType title;
    protected Boolean status;
    
    public enum MessageType{
        GREETING_RESPONSE,
        SENDING_LOG_RECORD_RESPONSE
    }
    
    public MessageType getTitle(){
        return this.title;
    };
    
    public Boolean getStatus() {
        return status;
    }
    
    public void setStatus(Boolean status){
        this.status = status;
    }
    
    public String toJsonString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
