/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmessage;

import model.CustomFolder;

/**
 *
 * @author duc
 */
public class DirectoryTreeResponse extends ClientMessage{
    private CustomFolder folder;
    public DirectoryTreeResponse(CustomFolder folder){
        this.title = MessageType.DIRECTORY_TREE_RESPONSE;
        this.folder = folder;
    }
    
    public CustomFolder getFolder(){
        return this.folder;
    }
}
