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
public class Client {
    private String ip;
    private Boolean isConnected;
    private String watchedDirectory;
    
    public Client(String ip, Boolean isConnected, String watchedDirectory){
        this.ip = ip;
        this.isConnected = isConnected;
        this.watchedDirectory = watchedDirectory;
    }
    
    public Client(){
        this("", false, "");
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the isConnected
     */
    public Boolean getIsConnected() {
        return isConnected;
    }

    /**
     * @param isConnected the isConnected to set
     */
    public void setIsConnected(Boolean isConnected) {
        this.isConnected = isConnected;
    }

    /**
     * @return the watchedDirectory
     */
    public String getWatchedDirectory() {
        return watchedDirectory;
    }

    /**
     * @param watchedDirectory the watchedDirectory to set
     */
    public void setWatchedDirectory(String watchedDirectory) {
        this.watchedDirectory = watchedDirectory;
    }
    
    public String toCsv(){
        return ip + "," + watchedDirectory;
    }
}
