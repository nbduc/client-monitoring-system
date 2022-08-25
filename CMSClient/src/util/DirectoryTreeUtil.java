/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import model.CustomFolder;

/**
 *
 * @author duc
 */
public class DirectoryTreeUtil {
    public static void getWholeDirectoryTree(String path){
        //
        File folder = new File(path);
        ArrayList<File> parents = new ArrayList<>();
        parents.add(folder);
        while(folder.getParent() != null){
            parents.add(folder.getParentFile());
            folder = folder.getParentFile();
        }
        
//        CustomFolder root = new CustomFolder();
//        root.setName(folder.getAbsolutePath());
        
        File root = folder.getAbsoluteFile();
        CustomFolder cfolder = new CustomFolder(root.getAbsolutePath());
        try {
            Files.walkFileTree(root.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) throws IOException {
                    //khác root
                    if(!directory.toString().equals(root.getAbsolutePath())){
                        if(attrs.isDirectory()){
                            cfolder.getFolders().add(new CustomFolder(directory.getFileName().toString()));
                            System.out.println(directory);
                        }
                        return FileVisitResult.CONTINUE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {

                    System.out.println("Access Permissions denied for " + file);
                    return FileVisitResult.SKIP_SUBTREE;
                }
            }); 
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
