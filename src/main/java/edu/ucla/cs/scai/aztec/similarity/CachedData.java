package edu.ucla.cs.scai.aztec.similarity;

import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.AztecEntryProviderFromJsonFile;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
    static HashMap<String, AztecEntry> entryMap;
    static ArrayList<AztecEntry> entryArray;

    static {
        System.out.println("TF/IDF path system property: "+System.getProperty("tfidt.path"));
        String tfidtPath = System.getProperty("tfidf.path","/home/massimo/aztec/tfidf.data");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tfidtPath))) {
            documentLength = (HashMap<String, Double>) ois.readObject();
            tfidt = (HashMap<String, HashMap<String, Double>>) ois.readObject();
            idf = (HashMap<String, Double>) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(CachedData.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Entries path system property: "+System.getProperty("entries.path"));
        String entriesPath = System.getProperty("entries.path", "/home/massimo/Downloads/solrResources.json");
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

    static HashMap<String, Double> getDocumentLength() {
        return documentLength;
    }

    static HashMap<String, HashMap<String, Double>> getTfidt() {
        return tfidt;
    }

    static HashMap<String, Double> getIdf() {
        return idf;
    }

    static HashMap<String, AztecEntry> getEntries() {
        return entryMap;
    }

}
