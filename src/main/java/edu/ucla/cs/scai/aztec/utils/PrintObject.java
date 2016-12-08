package edu.ucla.cs.scai.aztec.utils;

import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.AztecEntryProviderFromJsonFile;
import edu.ucla.cs.scai.aztec.similarity.CachedData;
import edu.ucla.cs.scai.aztec.summarization.KeywordsBuilder;
import edu.ucla.cs.scai.aztec.summarization.RankedString;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Xinxin on 8/29/2016.
 */
public class PrintObject {
    static HashMap<String, List<RankedString>> keywords;
    public void printObject() throws Exception{
        System.out.println("Load Keywords path system property: " + System.getProperty("keywords.path"));
        String keywordsPath = System.getProperty("keywords.path", "src/main/data/expkeywords.data");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keywordsPath));
        keywords = (HashMap<String, List<RankedString>>) ois.readObject();
        String entriesPath = System.getProperty("entries.path", "src/main/data/resourcesWithDomain.json");
        ArrayList<AztecEntry> entryArray;
        entryArray = new AztecEntryProviderFromJsonFile(entriesPath).load();
        HashMap<String,AztecEntry> entryMap = new HashMap<>();
        for (AztecEntry e : entryArray) {
            entryMap.put(e.getId(), e);
        }
        PrintWriter pw = new PrintWriter(new FileOutputStream("src/main/data/expkeywords_domain.txt"));
        for(String id : keywords.keySet()){
            //pw.print(id+": ");
            for(RankedString rs:keywords.get(id)){
                pw.print(rs.getString()+","+Double.toString(rs.getRank())+" ");
            }
            pw.print(entryMap.get(id).getDomains());
            pw.print("\n");
        }
        pw.close();
        ois.close();
    }
    public void printEntry() throws Exception{
        ArrayList<AztecEntry> entryArray;
        PrintWriter pw = new PrintWriter(new FileOutputStream("src/main/data/tags.txt"));
        String entriesPath = System.getProperty("entries.path", "src/main/data/resourcesWithDomain.json");
        try {
            entryArray = new AztecEntryProviderFromJsonFile(entriesPath).load();
            for (AztecEntry e : entryArray) {
                pw.print(e.getId()+": ");
                pw.println(e.getTags());
            }
        } catch (Exception ex) {
            Logger.getLogger(CachedData.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public static void main(String[] args) throws Exception{
        PrintObject po = new PrintObject();
        po.printObject();
    }
}
