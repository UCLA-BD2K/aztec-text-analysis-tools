package edu.ucla.cs.scai.aztec.utils;

import edu.ucla.cs.scai.aztec.summarization.KeywordsBuilder;
import edu.ucla.cs.scai.aztec.summarization.RankedString;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

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
        PrintWriter pw = new PrintWriter(new FileOutputStream("src/main/data/expkeywords_joey.txt"));
        for(String id : keywords.keySet()){
            pw.print(id+": ");
            for(RankedString rs:keywords.get(id)){
                pw.print(rs.getString()+","+Double.toString(rs.getRank())+" ");
            }
            pw.print("\n");
        }
        pw.close();
        ois.close();
    }
    public static void main(String[] args) throws Exception{
        PrintObject po = new PrintObject();
        po.printObject();
    }
}
