package edu.ucla.cs.scai.aztec.utils;

import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.AztecEntryProviderFromJsonFile;
import edu.ucla.cs.scai.aztec.similarity.CachedData;
import edu.ucla.cs.scai.aztec.summarization.RankedString;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Xinxin on 10/12/2016.
 */
public class EntryAnalysis {
    static HashMap<String, AztecEntry> entryMap;
    static ArrayList<AztecEntry> entryArray;
    static HashMap<String, LinkedList<RankedString>> keywords;
    public void loadEntry(){
        System.out.println("Entries path system property: " + System.getProperty("entries.path"));
        String entriesPath = System.getProperty("entries.path", "src/main/data/solrResources.json");
        try {
            entryArray = new AztecEntryProviderFromJsonFile(entriesPath).load();
            entryMap = new HashMap<>();
            for (AztecEntry e : entryArray) {
                entryMap.put(e.getId(), e);
            }
        } catch (Exception ex) {
            Logger.getLogger(CachedData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void BasicInfo(){
        Integer N= entryMap.size();
        System.out.print("number: ");
        System.out.println(N);
        Integer avgdocLength = 0;
        Integer avgtagNum = 0;
        Integer cnotag = 0;
        Integer cnoDom = 0;
        Integer cnodes = 0;
        Integer numdex_10 = 0;
        Integer tagnum = 0;
        for(AztecEntry e:entryArray){
            if(e.getDescription() == null){
                cnodes +=1;
            }
            else{
                String des = e.getDescription().replace("\\p{P}","");
                Integer num = des.trim().split("\\s+").length;
                avgdocLength += num;
                if(num>=40){
                    numdex_10+=1;
                }
            }
            if(e.getTags() == null){
                cnotag+=1;
            }
            else{
                avgtagNum += e.getTags().size();
                if(e.getTags().size()<=1){
                    tagnum+=1;
                }
            }
            if(e.getDomains() == null){
                cnoDom+=1;
            }
        }
        Double davgdocLength = avgdocLength*1.0/N;
        Double davgtagNum = avgtagNum*1.0/N;
        System.out.print("average des lengh");
        System.out.println(davgdocLength);
        System.out.print("average tag num");
        System.out.println(davgtagNum);
        System.out.print("no des num");
        System.out.println(cnodes);
        System.out.print("no tag num");
        System.out.println(cnotag);
        System.out.print("no dom num");
        System.out.println(cnoDom);
        System.out.println(numdex_10);
        System.out.println(tagnum);
    }
    public static void printentry() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader("src/main/data/search_result_v9_2/Top30_list.txt"));
        String line;
        while((line = reader.readLine())!=null){
            Integer count = 1;
            String[] id_eid = line.trim().split(":");
            String ID = id_eid[0];
            PrintWriter writer = new PrintWriter(new FileOutputStream("src/main/data/search_result_v9_2/Top30_des_"+ID+".txt"));
            String eid = id_eid[1].trim();
            String[] list_id = eid.split(" ");
            for(String entryid:list_id){
                writer.print(count);
                if(entryMap.get(entryid)!=null) {
                    if ((entryMap.get(entryid).getDescription() != null) && (entryMap.get(entryid).getName() != null)) {
                        writer.println("\t" + entryMap.get(entryid).getId() + "\t" + entryMap.get(entryid).getName() + "\t" + entryMap.get(entryid).getDescription());
                        if (entryMap.get(entryid).getTags() != null) {
                            writer.println("\t" + entryMap.get(entryid).getTags());
                        }
                    } else {
                        System.out.println(ID);
                        System.out.println(entryid);
                    }

                    if (count % 10 == 0) {
                        writer.println("-------------------");
                    }
                    count += 1;
                }
                else{
                    System.out.println(entryid);
                }
            }
            writer.close();
        }
    }

    public static void main(String[] args) throws IOException{
        EntryAnalysis EA = new EntryAnalysis();
        EA.loadEntry();
        EA.BasicInfo();
    }
}
