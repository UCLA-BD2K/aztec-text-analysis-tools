package edu.ucla.cs.scai.aztec.similarity;

import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.AztecEntryProviderFromJsonFile;
import edu.ucla.cs.scai.aztec.summarization.ExpandedKeywordsBuilder;
import edu.ucla.cs.scai.aztec.summarization.KeywordsBuilder;
import edu.ucla.cs.scai.aztec.summarization.RankedString;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class CachedData {

    static HashMap<String, Double> documentLength;
    static HashMap<String, HashMap<String, Double>> tfidt;
    static HashMap<String, Double> idf;
    static HashMap<String, Double> documentLengthK;
    static HashMap<String, HashMap<String, Double>> tfidtK;
    static HashMap<String, Double> idfK;
    static HashMap<String, AztecEntry> entryMap;
    static ArrayList<AztecEntry> entryArray;
    static HashMap<String, List<RankedString>> keywords;
    static HashMap<String, List<RankedString>> expkeywords;

//    private static void loadTfidfData() throws Exception {
//        System.out.println("TF/IDF path system property: " + System.getProperty("tfidf.path"));
//        String tfidtPath = System.getProperty("tfidf.path", "src/main/data/tfidf.data");
//        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tfidtPath));
//        documentLength = (HashMap<String, Double>) ois.readObject();
//        tfidt = (HashMap<String, HashMap<String, Double>>) ois.readObject();
//        idf = (HashMap<String, Double>) ois.readObject();
//    }
//
//    private static void buildTfidfData() throws Exception {
//        System.out.println("TF/IDF path system property: " + System.getProperty("tfidf.path"));
//        String tfidtPath = System.getProperty("tfidf.path", "src/main/data/tfidf.data");
//        TfIdfBuilder builder = new TfIdfBuilder();
//        builder.buildTfIdfMatrix(entryArray, tfidtPath);
//    }
    
    private static void loadTfidfKData() throws Exception {
        System.out.println("Load TF/IDF on keywords path system property: " + System.getProperty("tfidfk.path"));
        String tfidtPath = System.getProperty("tfidfk.path", "src/main/data/tfidfk.data");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tfidtPath));
        documentLengthK = (HashMap<String, Double>) ois.readObject();
        tfidtK = (HashMap<String, HashMap<String, Double>>) ois.readObject();
        idfK = (HashMap<String, Double>) ois.readObject();
    }

    private static void buildTfidfKData() throws Exception {
        System.out.println("Build TF/IDF on keywords path system property: " + System.getProperty("tfidfk.path"));
        String tfidtPath = System.getProperty("tfidfk.path", "src/main/data/tfidfk.data");
        TfIdfBuilderKeywords builder = new TfIdfBuilderKeywords();
        builder.buildTfIdfMatrix(entryArray, tfidtPath);
    }    
    
    private static void loadKeywords() throws Exception {
        System.out.println("Load Keywords path system property: " + System.getProperty("keywords.path"));
        String keywordsPath = System.getProperty("keywords.path", "src/main/data/keywords.data");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keywordsPath));
        keywords = (HashMap<String, List<RankedString>>) ois.readObject();
    }

    private static void buildKeywords() throws Exception {
        System.out.println("Build Keywords path system property: " + System.getProperty("keywords.path"));
        String keywordsPath = System.getProperty("keywords.path", "src/main/data/keywords.data");
        KeywordsBuilder builder = new KeywordsBuilder();
        keywords = builder.buildKeywords(entryArray, keywordsPath);
    }
    private static void loadexpKeywords() throws Exception {
        System.out.println("Load ExpKeywords path system property: " + System.getProperty("expkeywords.path"));
        String keywordsPath = System.getProperty("expkeywords.path", "src/main/data/expkeywords.data");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keywordsPath));
        expkeywords = (HashMap<String, List<RankedString>>) ois.readObject();
    }

    private static void buildexpKeywords() throws Exception {
        System.out.println("Build ExpKeywords path system property: " + System.getProperty("expkeywords.path"));
        String keywordsPath = System.getProperty("expkeywords.path", "src/main/data/expkeywords.data");
        ExpandedKeywordsBuilder builder = new ExpandedKeywordsBuilder();
        expkeywords = builder.buildExpKeywords(entryArray, keywordsPath);
    }

    static {
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

//        try {
//            loadTfidfData();
//        } catch (Exception ex) {
//            Logger.getLogger(CachedData.class.getName()).log(Level.SEVERE, null, ex);
//            //tfidf data failed, try to reconstruct it
//            try {
//                buildTfidfData();
//                loadTfidfData();
//            } catch (Exception ex2) {
//                Logger.getLogger(CachedData.class.getName()).log(Level.SEVERE, null, ex2);
//            }
//        }
        
        try {
            loadKeywords();
        } catch (Exception ex) {
            Logger.getLogger(CachedData.class.getName()).log(Level.SEVERE, null, ex);
            //tfidf data failed, try to reconstruct it
            try {
                buildKeywords();
            } catch (Exception ex2) {
                Logger.getLogger(CachedData.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }
        try {
            loadexpKeywords();
        } catch (Exception ex) {
            Logger.getLogger(CachedData.class.getName()).log(Level.SEVERE, null, ex);
            //tfidf data failed, try to reconstruct it
            try {
                buildexpKeywords();
            } catch (Exception ex2) {
                Logger.getLogger(CachedData.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }

        try {
            loadTfidfKData();
        } catch (Exception ex) {
            Logger.getLogger(CachedData.class.getName()).log(Level.SEVERE, null, ex);
            //tfidf data failed, try to reconstruct it
            try {
                buildTfidfKData();
                loadTfidfKData();
            } catch (Exception ex2) {
                Logger.getLogger(CachedData.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }        
    }

    static HashMap<String, Double> getDocumentLength() {
        return documentLength;
    }

    static HashMap<String, HashMap<String, Double>> getTfIdt() {
        return tfidt;
    }

    static HashMap<String, Double> getIdf() {
        return idf;
    }

    static HashMap<String, AztecEntry> getEntries() {
        return entryMap;
    }

}
