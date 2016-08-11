package edu.ucla.cs.scai.aztec;

/**
 * Created by Xinxin on 8/11/2016.
 */
public class AbsEntry {
    String Id;
    String description;
    public AbsEntry(String id,String des){
        this.Id = id;
        this.description = des;
    }
    public String getId() {
        return Id;
    }
    public void setId(String id) {
        this.Id= id;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
