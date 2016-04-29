/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.aztec.similarity;

import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.EntryLoader;
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

        String tfidtPath = System.getProperty("tfidt.path");
        if (tfidtPath == null) {
            tfidtPath = "/home/massimo/aztec/tdidf.data";
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tfidtPath))) {
            documentLength = (HashMap<String, Double>) ois.readObject();
            tfidt = (HashMap<String, HashMap<String, Double>>) ois.readObject();
            idf = (HashMap<String, Double>) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(CachedData.class.getName()).log(Level.SEVERE, null, ex);
        }

        String entriesPath = System.getProperty("entries.path");
        if (entriesPath == null) {
            entriesPath = "/home/massimo/Downloads/solrResources.json";
        }
        try {
            entryArray = new EntryLoader(entriesPath).load();
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
