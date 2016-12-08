package edu.ucla.cs.scai.aztec.summarization;

import edu.ucla.cs.scai.aztec.AztecEntryProviderFromJsonFile;
import edu.ucla.cs.scai.aztec.keyphrase.Tokenizer;
import edu.ucla.cs.scai.aztec.textexpansion.TagExpansion;
import edu.ucla.cs.scai.aztec.textexpansion.TextParser;
import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.AbsEntry;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.extjwnl.JWNLException;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class KeywordsBuilder {

//    Tokenizer tokenizer;
    TextParser textparser = new TextParser();

    static final double log2 = Math.log(2);

    public KeywordsBuilder() throws JWNLException, FileNotFoundException, IOException {
        textparser = new TextParser();
    }

    public HashMap<String, LinkedList<RankedString>> buildKeywords(Collection<AztecEntry> entries, String outputPath) throws JWNLException,IOException {

        HashMap<String, LinkedList<RankedString>> res = new HashMap<>();
        TagExpansion TE = new TagExpansion();
        for (AztecEntry entry : entries) {
            LinkedList<RankedString> tl = new LinkedList<>();
            LinkedList<RankedString> l = new LinkedList<>();
            res.put(entry.getId(), l);
            //use tag to buid key words
            if (entry.getDescription() != null && entry.getDescription().trim().length() > 0 && entry.getTags() !=null) {
                try {
                    KeywordsRank kr = new KeywordsRank(entry.getDescription().replace("-","_"), 10);
                    //KeywordsRank kr = new KeywordsRank(entry.getDescription(), 10);
                    tl.addAll(kr.topRankedKeywords(30));
                    tl.addAll(TE.tagExpansion(entry.getTags()));
                    HashMap<String,Double> key_score = new HashMap<>();
                    for(RankedString rs:tl){ // merge the duplication
                        Double score = key_score.get(rs.getString());
                        if(score!=null){
                            score = score+rs.getRank();
                            key_score.put(rs.getString(),score);
                        }
                        else{
                            key_score.put(rs.getString(),rs.getRank());
                        }
                    }
                    for(String key:key_score.keySet()){
                        l.add(new RankedString(key,key_score.get(key)));
                    }
                    //System.out.println(entry.getDescription() + " -> " + l);
                } catch (Exception ex) {
                    Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(entry.getDescription() != null && entry.getDescription().trim().length() > 0){
                try {
                    KeywordsRank kr = new KeywordsRank(entry.getDescription(), 10);
                    //KeywordsRank kr = new KeywordsRank(entry.getDescription(), 10);
                    l.addAll(kr.topRankedKeywords(20));
                    //System.out.println(entry.getDescription() + " -> " + l);
                } catch (Exception ex) {
                    Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            // build without tags
//            if (entry.getDescription() != null && entry.getDescription().trim().length() > 0) {
//                try {
//                    KeywordsRank kr = new KeywordsRank(entry.getDescription(), 10);
//                    //KeywordsRank kr = new KeywordsRank(entry.getDescription(), 10);
//                    l.addAll(kr.topRankedKeywords(20));
//                    //System.out.println(entry.getDescription() + " -> " + l);
//                } catch (Exception ex) {
//                    Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
        }

        System.out.println("Writing keywords to file " + outputPath);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputPath))) {
            out.writeObject(res);
        } catch (Exception ex) {
            Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }

        return res;
    }

    public HashMap<String, List<RankedString>> buildKeywordsAbstract(Collection<AbsEntry> entries, String outputPath) throws IOException {
        // The one I am currently using.
        // input contains all the sbatracts and description

        HashMap<String, List<RankedString>> res = new HashMap<>();
        //BufferedReader reader = new BufferedReader(new FileReader(infile));
        //String line;
        //Integer id = 0;
//        while((line = reader.readLine()) != null){
//            LinkedList<RankedString> l = new LinkedList<>();
//            res.put(Integer.toString(id), l);
//            if (line.trim().length() > 0) {
//                try {
//                    KeywordsRank kr = new KeywordsRank(line.trim(), 10);
//                    l.addAll(kr.topRankedKeywords());
//                    //System.out.println(entry.getDescription() + " -> " + l);
//                } catch (Exception ex) {
//                    Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            id ++;
//        }
        for (AbsEntry entry : entries) {
            LinkedList<RankedString> l = new LinkedList<>();
            res.put(entry.getId(), l);
            if (entry.getDescription() != null && entry.getDescription().trim().length() > 0) {
                try {
                    KeywordsRank kr = new KeywordsRank(entry.getDescription(), 10);
                    l.addAll(kr.topRankedKeywords());
                    //System.out.println(entry.getDescription() + " -> " + l);
                } catch (Exception ex) {
                    Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        System.out.println("Writing keywords to file " + outputPath);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputPath))) {
            out.writeObject(res);
        } catch (Exception ex) {
            Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }

        return res;
    }

    public static void main (String[] args)throws Exception{

        String outfile = "src/main/data/keywords.data";
        KeywordsBuilder KB = new KeywordsBuilder();
        PrintWriter pw = new PrintWriter(new FileOutputStream("src/main/data/exptags.txt"));
        String entriesPath = System.getProperty("entries.path", "src/main/data/solrResources.json");
        ArrayList<AztecEntry> entryArray = new AztecEntryProviderFromJsonFile(entriesPath).load();
        HashMap<String, LinkedList<RankedString>> res = KB.buildKeywords(entryArray,outfile);
    }
}
