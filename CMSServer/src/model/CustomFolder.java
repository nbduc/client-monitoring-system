/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author duc
 */
public class CustomFolder {
    private String name;

    List<CustomFolder> folders;

    public CustomFolder() {
        folders = new ArrayList<>();
    }
    
    public CustomFolder(String path){
        this();
        File folder = new File(path);
        if(folder.exists()){
            if(folder.isDirectory()){
                this.name = folder.getName();
                try {
                    Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) throws IOException {
                            System.out.println(directory.toString());
                            if(!directory.toString().equals(folder.toString())){
                                if(attrs.isDirectory()){
                                    folders.add(new CustomFolder(directory.toString()));
                                }
                                return FileVisitResult.SKIP_SUBTREE;
                            } else {
                                return FileVisitResult.CONTINUE;
                            }
                        }
                    }); 
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CustomFolder> getFolders() {
        return folders;
    }

    public void addFolder(CustomFolder folder) {
        this.folders.add(folder);
    }

    @Override
    public String toString() {
        return "CustomFolder [name=" + name + "]";
    }
    
    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
