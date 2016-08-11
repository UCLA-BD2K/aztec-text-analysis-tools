package edu.ucla.cs.scai.aztec.similarity;

import edu.ucla.cs.scai.aztec.AbsEntry;
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
import java.io.*;

/**
 * Created by Xinxin on 8/11/2016.
 */
public class AbsCachedData {
    static HashMap<String, Double> documentLength;
    static HashMap<String, HashMap<String, Double>> tfidt;
    static HashMap<String, Double> idf;
    static HashMap<String, Double> documentLengthK;
    static HashMap<String, HashMap<String, Double>> tfidtK;
    static HashMap<String, Double> idfK;
    static HashMap<String, AbsEntry> entryMap;
    static ArrayList<AbsEntry> entryArray;
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
        System.out.println("Load TF/IDF on keywords path system property: " + System.getProperty("abstfidfk.path"));
        String abstfidtPath = System.getProperty("abstfidfk.path", "src/main/data/abstfidfk.data");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(abstfidtPath));
        documentLengthK = (HashMap<String, Double>) ois.readObject();
        tfidtK = (HashMap<String, HashMap<String, Double>>) ois.readObject();
        idfK = (HashMap<String, Double>) ois.readObject();
    }

    private static void buildTfidfKData() throws Exception {
        System.out.println("Build TF/IDF on keywords path system property: " + System.getProperty("abstfidfk.path"));
        String abstfidtPath = System.getProperty("abstfidfk.path", "src/main/data/abstfidfk.data");
        TfIdfBuilderKeywords builder = new TfIdfBuilderKeywords();
        builder.buildTfIdfMatrixAbstract(entryMap.values(), abstfidtPath);
    }

    private static void loadKeywords() throws Exception {
        System.out.println("Load Keywords path system property: " + System.getProperty("abskeywords.path"));
        String abskeywordsPath = System.getProperty("abskeywords.path", "src/main/data/abskeywords.data");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(abskeywordsPath));
        keywords = (HashMap<String, List<RankedString>>) ois.readObject();
    }

    private static void buildKeywords() throws Exception {
        System.out.println("Build Keywords path system property: " + System.getProperty("abskeywords.path"));
        String abskeywordsPath = System.getProperty("abskeywords.path", "src/main/data/abskeywords.data");
        KeywordsBuilder builder = new KeywordsBuilder();
        keywords = builder.buildKeywordsAbstract(entryMap.values(), abskeywordsPath);
    }
    private static void loadexpKeywords() throws Exception {
        System.out.println("Load ExpKeywords path system property: " + System.getProperty("absexpkeywords.path"));
        String abskeywordsPath = System.getProperty("absexpkeywords.path", "src/main/data/absexpkeywords.data");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(abskeywordsPath));
        expkeywords = (HashMap<String, List<RankedString>>) ois.readObject();
    }

    private static void buildexpKeywords() throws Exception {
        System.out.println("Build ExpKeywords path system property: " + System.getProperty("absexpkeywords.path"));
        String abskeywordsPath = System.getProperty("absexpkeywords.path", "src/main/data/absexpkeywords.data");
        ExpandedKeywordsBuilder builder = new ExpandedKeywordsBuilder();
        expkeywords = builder.buildExpKeywordsAbstract(entryMap.values(), abskeywordsPath);
    }

    static {
        System.out.println("Entries path system property: " + System.getProperty("absentries.path"));
        String entriesPath = System.getProperty("absentries.path", "src/main/data/abstract_removeurl.txt");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(entriesPath));
            entryMap = new HashMap<>();
            String line;
            Integer id=0;
            while ((line= reader.readLine())!= null) {
                entryMap.put(Integer.toString(id), new AbsEntry(Integer.toString(id), line));
                id+=1;
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
                loadexpKeywords();
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

    static HashMap<String, AbsEntry> getEntries() {
        return entryMap;
    }
    

}
