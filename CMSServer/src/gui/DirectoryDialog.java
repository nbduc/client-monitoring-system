/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.io.File;
import javax.swing.JDialog;
import javax.swing.*;
import javax.swing.tree.*; 
import model.CustomFolder;

/**
 *
 * @author duc
 */
public class DirectoryDialog extends JDialog{
    private JTree dirTree;
    private String returnedValue;
    private CustomFolder tree;
    
    public String getReturnedValue(){
        return returnedValue;
    }
    
    public void createUI(){
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        createNodes(root, tree);
        dirTree = new JTree(root);
        dirTree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        JScrollPane treeView = new JScrollPane(dirTree);
        getContentPane().add(treeView);
        setTitle("Choose watched directory");
        setSize(330, 430);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void createNodes(DefaultMutableTreeNode root, CustomFolder tree){
        for(CustomFolder folder: tree.getFolders()){
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(folder.getName());
            root.add(node);
            if(folder.getFolders().size() != 0){
                createNodes(node, folder);
            }
        }
    }
    
    public DirectoryDialog(JFrame parent, CustomFolder tree){
        super(parent);
        this.tree = tree;
        createUI();
    }
}
