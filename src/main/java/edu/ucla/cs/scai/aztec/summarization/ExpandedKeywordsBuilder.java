package edu.ucla.cs.scai.aztec.summarization;

import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.AbsEntry;
import edu.ucla.cs.scai.aztec.AztecEntryProviderFromJsonFile;
import edu.ucla.cs.scai.aztec.similarity.AbsCachedData;
import edu.ucla.cs.scai.aztec.textexpansion.TextExpansion;
import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Xinxin on 8/5/2016.
 */
public class ExpandedKeywordsBuilder {

    public HashMap<String, List<RankedString>> buildExpKeywords(Collection<AztecEntry> entries, String outputPath) throws JWNLException, IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("src/main/data/keywords.data"));
        HashMap<String, LinkedList<RankedString>> keywords = (HashMap<String, LinkedList<RankedString>>) ois.readObject();
        HashMap<String, List<RankedString>> expendedKeywords = new HashMap<>();
        TextExpansion TE = new TextExpansion();
        for (AztecEntry entry : entries) {
            LinkedList<RankedString> l = new LinkedList<>();
            expendedKeywords.put(entry.getId(), l);
            if (entry.getDescription() != null && entry.getDescription().trim().length() > 0) {
                LinkedList<RankedString> rankedToken = keywords.get(entry.getId());
                List<RankedString> expendedToken = TE.docExpansion(rankedToken);
                expendedKeywords.put(entry.getId(),expendedToken);
            }
        }

        System.out.println("Writing keywords to file " + outputPath);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputPath))) {
            out.writeObject(expendedKeywords);
        } catch (Exception ex) {
            Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return expendedKeywords;
    }
    public HashMap<String, List<RankedString>> buildExpKeywordsAbstract(Collection<AbsEntry> entries, String outputPath) throws JWNLException, IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("src/main/data/abskeywords.data"));
        HashMap<String, LinkedList<RankedString>> keywords = (HashMap<String, LinkedList<RankedString>>) ois.readObject();
        HashMap<String, List<RankedString>> expendedKeywords = new HashMap<>();
        TextExpansion TE = new TextExpansion();
        //Integer total_abs = keywords.size();
//        for (String i:keywords.keySet()) {
//            LinkedList<RankedString> l = new LinkedList<>();
//            expendedKeywords.put(i, l);
//            if (keywords.get(i) != null) {
//                List<RankedString> rankedToken = keywords.get(i);
//                List<RankedString> expendedToken = TE.docExpansion(rankedToken);
//                expendedKeywords.put(i,expendedToken);
//            }
//        }
        for (AbsEntry entry : entries) {
            LinkedList<RankedString> l = new LinkedList<>();
            expendedKeywords.put(entry.getId(), l);
            if (entry.getDescription() != null && entry.getDescription().trim().length() > 0) {
                LinkedList<RankedString> rankedToken = keywords.get(entry.getId());
                List<RankedString> expendedToken = TE.docExpansion(rankedToken);
                expendedKeywords.put(entry.getId(),expendedToken);
            }
        }

        System.out.println("Writing keywords to file " + outputPath);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputPath))) {
            out.writeObject(expendedKeywords);
        } catch (Exception ex) {
            Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return expendedKeywords;
    }

    public static void main(String[] args) throws Exception{
//        ArrayList<AztecEntry> entryArray = new ArrayList<>();
//        HashMap<String, AztecEntry> entryMap;
//        System.out.println("Entries path system property: " + System.getProperty("entries.path"));
//        String entriesPath = System.getProperty("entries.path", "src/main/data/solrResources.json");
//        try {
//            entryArray = new AztecEntryProviderFromJsonFile(entriesPath).load();
//            entryMap = new HashMap<>();
//            for (AztecEntry e : entryArray) {
//                entryMap.put(e.getId(), e);
//            }
//        } catch (Exception ex) {
//        }
//        HashMap<String, List<RankedString>> expkeywords;
//        ExpandedKeywordsBuilder builder = new ExpandedKeywordsBuilder();
//        expkeywords = builder.buildExpKeywords(entryArray, "src/main/data/expkeywords.data");
        String infile = "src/main/data/keywords.data";
        String outfile = "src/main/data/expkeywords.data";
        ExpandedKeywordsBuilder EKB = new ExpandedKeywordsBuilder();
        //HashMap<String, List<RankedString>> res = EKB.buildExpKeywordsAbstract(infile,outfile);
    }

}
