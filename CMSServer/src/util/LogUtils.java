/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import logrecord.LogRecord;

/**
 *
 * @author duc
 */
public class LogUtils {
    public static final String LOG_FOLDER = "log";
    
    private static File getLogFile(String ip){
        File logFolder = new File(LOG_FOLDER);
        if(!logFolder.exists()){
            logFolder.mkdir();
        }
        File logFile = new File(LOG_FOLDER+"/"+ip+".txt");
        synchronized(LogUtils.class){
            if(!logFile.exists()){
                try {
                    logFile.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return logFile;
    }
    
    public static LogRecord toLogRecord(String recordString){
        String[] parts = recordString.split(",");
        LogRecord record = new LogRecord();
        record.setTime(Long.parseLong(parts[0]));
        record.setAction(LogRecord.Action.valueOf(parts[1]));
        record.setDescription(parts[2]);
        return record;
    }
    
    private static ArrayList<LogRecord> readLog(File logFile){
        ArrayList<LogRecord> logs = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(logFile))){
            String line = br.readLine();
            while(line != null){
                logs.add(toLogRecord(line));
                line = br.readLine();
            }
        } catch(IOException ex){
            ex.printStackTrace();
        }
        return logs;
    }
    
    public static ArrayList<LogRecord> getLogsByIp(String ip){
        File logFolder = new File(LOG_FOLDER);
        if(logFolder.isDirectory() && logFolder.exists()){
            return readLog(getLogFile(ip));
        } else {
            logFolder.mkdir();
        }
        return null;
    }
    
    public static Boolean writeLog(String ip, LogRecord log){
        try {
            File logFile = getLogFile(ip);
            try (BufferedWriter bw = new BufferedWriter(
                            new FileWriter(logFile, true))){
                bw.write(log.toCsv());
                bw.newLine();
                bw.flush();
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
