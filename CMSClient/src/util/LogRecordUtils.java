/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import logrecord.LogRecord;

/**
 *
 * @author duc
 */
public class LogRecordUtils {
    private String path = "log.csv";
    public LogRecordUtils(String path){
        this.path = path;
    }
    public void writeLog(LogRecord record){
        try {
            File logFile = new File(path);
            if(!logFile.exists()){
                logFile.createNewFile();
            }
            try (PrintWriter pw = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(logFile, true)))){
                pw.println(convertLogRecordToCsv(record));
                pw.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(LogRecordUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    public String convertLogRecordToCsv(LogRecord record){
        return record.toString();
    }
}
