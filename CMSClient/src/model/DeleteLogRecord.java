/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;
import java.nio.file.Path;

/**
 *
 * @author duc
 */
public class DeleteLogRecord extends LogRecord{
    public DeleteLogRecord(Path path){
        super(Action.DELETE);
        this.description = String.format("%s: '%s'", 
                action, 
                path.toString());
    }
}
