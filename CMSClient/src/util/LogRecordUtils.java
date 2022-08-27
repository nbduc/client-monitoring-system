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
public class LogRecordUtils {
    private static final String LOG_FILE_PATH = "log.csv";
    
    public static File getLogFile(){
        File logFile = new File(LOG_FILE_PATH);
        if(!logFile.exists()){
            try {
                logFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
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
    
    public static ArrayList<LogRecord> readAllLogs(){
        ArrayList<LogRecord> logs = new ArrayList<>();
        File logFile = getLogFile();
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
    
    public static void writeLog(LogRecord record){
        File logFile = getLogFile();
        try (BufferedWriter bw = new BufferedWriter(
                        new FileWriter(logFile, true))){
            bw.write(record.toCsv());
            bw.newLine();
            bw.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
