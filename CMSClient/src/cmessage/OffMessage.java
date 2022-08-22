/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmessage;

/**
 *
 * @author duc
 */
public class OffMessage extends ClientMessage{
    public OffMessage(){
        this.title = MessageType.OFF;
    }
    
    @Override
    public String toString(){
        return title.toString();
    }
}
