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
public class LogRecord {
    private LocalDateTime time;
    private Action action;
    private String description;
    
    public enum Action {
        RENAME_CURRENT_DIR,
        RENAME_SUB_DIR,
        RENAME_FILE,
        CREATE_SUB_DIR,
        CREATE_FILE,
        UPDATE_FILE,
        DELETE_SUB_DIR,
        DELETE_FILE,
        LOG_IN,
        LOG_OUT;
    }
    
    public LogRecord(Action action){
        this.time = LocalDateTime.now();
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
}
