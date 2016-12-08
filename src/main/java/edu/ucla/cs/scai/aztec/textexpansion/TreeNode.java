package edu.ucla.cs.scai.aztec.textexpansion;
import java.util.*;

/**
 * Created by Xinxin on 9/27/2016.
 */
public class TreeNode {
    TreeNode parent;
    String ID;
    ArrayList<String> term;
    HashMap<String, TreeNode> children;
    public TreeNode(){
        this.children = new HashMap<>();
    }
    public void addChildren(String id,TreeNode node){
        this.children.put(id,node);
    }
    public HashMap<String, TreeNode> getChildren(){
        return this.children;
    }
    public void setTreeNode(String ID, ArrayList<String> term){
        this.ID = ID;
        this.term = term;
    }
}
