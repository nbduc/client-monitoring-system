/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smessage;

/**
 *
 * @author duc
 */
public class SendingLogRecordReponseMessage extends ServerMessage{
    public SendingLogRecordReponseMessage(){
        this.title = MessageType.SENDING_LOG_RECORD_RESPONSE;
    }
}
