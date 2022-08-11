/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.time.LocalDateTime;

/**
 *
 * @author duc
 */
public abstract class LogRecord {
    protected LocalDateTime time;
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
        this.time = LocalDateTime.now();
    }
    
    public LogRecord(Action action){
        this();
        this.action = action;
    }

    /**
     * @return the time
     */
    public LocalDateTime getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(LocalDateTime time) {
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
        return this.time + "| " + this.action + "| " + this.description;
    }
}
