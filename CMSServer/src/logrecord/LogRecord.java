/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logrecord;

import java.time.Instant;


/**
 *
 * @author duc
 */
public class LogRecord {
    protected long time;
    protected Action action;
    protected String description;
    
    public static enum Action {
        RENAME,
        CREATE,
        UPDATE,
        DELETE,
        LOG_IN,
        LOG_OUT;
    }
    
    public LogRecord(){
        this.time = Instant.now().getEpochSecond();
    }
    
    public LogRecord(Action action){
        this();
        this.action = action;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the action
     */
    public Action getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String toString(){
        return this.toCsv();
    }
    
    public String toCsv(){
        return this.time + "," + this.action + "," + this.description;
    }
}
