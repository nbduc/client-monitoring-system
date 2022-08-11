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
public class RenameLogRecord extends LogRecord{
    public RenameLogRecord(Path oldPath, Path newPath){
        super(Action.RENAME);
        this.description = String.format("%s: '%s' -> '%s'", 
                action, 
                oldPath.toString(), 
                newPath.toString());
    }
}
