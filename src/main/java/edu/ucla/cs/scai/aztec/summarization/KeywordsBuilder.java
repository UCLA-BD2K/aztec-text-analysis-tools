package edu.ucla.cs.scai.aztec.summarization;

import edu.ucla.cs.scai.aztec.keyphrase.Tokenizer;
import edu.ucla.cs.scai.aztec.textexpansion.TextParser;
import edu.ucla.cs.scai.aztec.AztecEntry;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

    public KeywordsBuilder() throws JWNLException, FileNotFoundException {
        textparser = new TextParser();
    }

    public HashMap<String, List<RankedString>> buildKeywords(Collection<AztecEntry> entries, String outputPath) throws IOException {

        HashMap<String, List<RankedString>> res = new HashMap<>();
        for (AztecEntry entry : entries) {
            LinkedList<RankedString> l = new LinkedList<>();
            res.put(entry.getId(), l);
            if (entry.getDescription() != null && entry.getDescription().trim().length() > 0) {
                try {
                    KeywordsRank kr = new KeywordsRank(entry.getDescription(), 10);
                    l.addAll(kr.topRankedKeywords(20));
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

    public HashMap<String, List<RankedString>> buildKeywordsTest(String infile, String outputPath) throws IOException {
        // The one I am currently using.
        // input contains all the sbatracts and description

        HashMap<String, List<RankedString>> res = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        Integer id = 0;
        while((line = reader.readLine()) != null){
            LinkedList<RankedString> l = new LinkedList<>();
            res.put(Integer.toString(id), l);
            if (line.trim().length() > 0) {
                try {
                    KeywordsRank kr = new KeywordsRank(line.trim(), 10);
                    l.addAll(kr.topRankedKeywords(20));
                    //System.out.println(entry.getDescription() + " -> " + l);
                } catch (Exception ex) {
                    Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            id ++;
        }

        System.out.println("Writing keywords to file " + outputPath);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputPath))) {
            out.writeObject(res);
        } catch (Exception ex) {
            Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }

        return res;
    }

//    public static void main (String[] args)throws IOException, JWNLException{
//        String infile = "src/main/data/abstract_removeurl.txt";
//        String outfile = "src/main/data/textrank.data";
//        KeywordsBuilder KB = new KeywordsBuilder();
//        HashMap<String, List<RankedString>> res = KB.buildKeywordsTest(infile,outfile);
//    }
}
