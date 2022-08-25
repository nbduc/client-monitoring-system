/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smessage;

/**
 *
 * @author duc
 */
public class DirectoryTreeRequest extends ServerMessage{
    private String requestedPath;
    public DirectoryTreeRequest(String requestedPath){
        this.title = MessageType.DIRECTORY_TREE_REQUEST;
        this.requestedPath = requestedPath;
    }
    
    public String getRequestedPath(){
        return this.requestedPath;
    }
}
