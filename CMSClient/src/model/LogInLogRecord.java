/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author duc
 */
public class LogInLogRecord extends LogRecord{
    public LogInLogRecord(){
        super(Action.LOG_IN);
        this.description = String.format("Log in");
    }
}
