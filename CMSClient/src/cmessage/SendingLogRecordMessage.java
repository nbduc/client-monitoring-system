/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmessage;

import logrecord.LogRecord;


/**
 *
 * @author duc
 */
public class SendingLogRecordMessage extends ClientMessage{
    public SendingLogRecordMessage(LogRecord record){
        this.title = MessageType.SENDING_LOG_RECORD;
        this.payload = record;
    }

    @Override
    public MessageType getTitle() {
        return this.title;
    }
    
    @Override
    public String toString(){
        return title + "," + payload;
    }
}
