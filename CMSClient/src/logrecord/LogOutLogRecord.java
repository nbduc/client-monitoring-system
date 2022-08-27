/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logrecord;

/**
 *
 * @author duc
 */
public class LogOutLogRecord extends LogRecord{
    public LogOutLogRecord(){
        super(Action.LOG_OUT);
        this.description = String.format("Log out successfully.");
    }
}
