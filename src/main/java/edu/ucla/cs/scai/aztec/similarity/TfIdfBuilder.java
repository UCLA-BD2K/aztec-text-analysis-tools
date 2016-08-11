package edu.ucla.cs.scai.aztec.similarity;

import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.AztecEntryProviderFromJsonFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import net.sf.extjwnl.JWNLException;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class TfIdfBuilder {

    Tokenizer tokenizer;

    static final double log2 = Math.log(2);

    public TfIdfBuilder() throws JWNLException, FileNotFoundException {
        tokenizer = new Tokenizer();
    }

    public void buildTfIdfMatrixOld(Collection<AztecEntry> entries, String outputPath) throws IOException {

        HashMap<String, Integer> nOfDocsWithWord = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> fOfWordsInDocuments = new HashMap<>();
        for (AztecEntry entry : entries) {
            LinkedList<String> tokens = tokenizer.tokenize(entry.getDescription());
            HashMap<String, Integer> wordCount = new HashMap<>();
            fOfWordsInDocuments.put(entry.getId(), wordCount);
            for (String w : tokens) {
                Integer c = wordCount.get(w);
                if (c == null) {
                    wordCount.put(w, 1);
                } else {
                    wordCount.put(w, c + 1);
                }
            }

            for (String w : wordCount.keySet()) {
                Integer c = nOfDocsWithWord.get(w);
                if (c == null) {
                    nOfDocsWithWord.put(w, 1);
                } else {
                    nOfDocsWithWord.put(w, c + 1);
                }
            }
        }
        HashMap<String, Double> documentLength = new HashMap<>();
        HashMap<String, HashMap<String, Double>> tfidt = new HashMap<>();
        HashMap<String, Double> idf = new HashMap<>();
        double N = fOfWordsInDocuments.keySet().size();
        for (String w : nOfDocsWithWord.keySet()) {
            double val = Math.log(N / nOfDocsWithWord.get(w)) / log2;
            idf.put(w, val);
        }
        for (String entry
                : fOfWordsInDocuments.keySet()) {
            HashMap<String, Double> row = new HashMap<>();
            HashMap<String, Integer> wordCount = fOfWordsInDocuments.get(entry);
            tfidt.put(entry, row);
            double length = 0;
            for (String w : wordCount.keySet()) {
                double val = (1 + Math.log(wordCount.get(w)) / log2);
                row.put(w, val);
                length += val * val;
            }
            documentLength.put(entry, Math.sqrt(length));
        }
        System.out.println("Writing matrix to file");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputPath))) {
            out.writeObject(documentLength);
            out.writeObject(tfidt);
            out.writeObject(idf);
        }
    }
    
    //the value of td-ift is constructed using the "Best fully weighted system" - Salton and Buckley, 1988
    //tf-idf(i,j) =tf(i,j) * log N/n_j / sqrt(sum_k (tf(k,j) * log (N/n_j))^2) 

    public void buildTfIdfMatrix(Collection<AztecEntry> entries, String outputPath) throws IOException {

        HashMap<String, Integer> nOfDocsWithWord = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> fOfWordsInDocuments = new HashMap<>();
        for (AztecEntry entry : entries) {
            LinkedList<String> tokens = tokenizer.tokenize(entry.getDescription());
            HashMap<String, Integer> wordCount = new HashMap<>();
            fOfWordsInDocuments.put(entry.getId(), wordCount);
            for (String w : tokens) {
                Integer c = wordCount.get(w);
                if (c == null) {
                    wordCount.put(w, 1);
                } else {
                    wordCount.put(w, c + 1);
                }
            }

            for (String w : wordCount.keySet()) {
                Integer c = nOfDocsWithWord.get(w);
                if (c == null) {
                    nOfDocsWithWord.put(w, 1);
                } else {
                    nOfDocsWithWord.put(w, c + 1);
                }
            }
        }

        HashMap<String, Double> documentLength = new HashMap<>();
        HashMap<String, HashMap<String, Double>> tfidt = new HashMap<>();
        HashMap<String, Double> idf = new HashMap<>();
        double N = fOfWordsInDocuments.keySet().size();
        for (String w : nOfDocsWithWord.keySet()) {
            double val = Math.log(N / nOfDocsWithWord.get(w)) / log2;
            idf.put(w, val);
        }
        HashMap<String, Double> columnLengths = new HashMap<>();
        //multiply tf by idf
        for (String entry : fOfWordsInDocuments.keySet()) {
            HashMap<String, Double> row = new HashMap<>();
            HashMap<String, Integer> wordCount = fOfWordsInDocuments.get(entry);
            tfidt.put(entry, row);
            for (String w : wordCount.keySet()) {
                double val = wordCount.get(w) * idf.get(w);
                row.put(w, val);
                Double colLength = columnLengths.get(w);
                if (colLength == null) {
                    colLength = 0d;
                }
                colLength += val * val;
                columnLengths.put(w, colLength);
            }
        }

        for (Map.Entry<String, Double> e : columnLengths.entrySet()) {
            e.setValue(Math.sqrt(e.getValue()));
        }

        //now divide each cell by the length of its column
        //and compute the length of the document
        for (String entry : tfidt.keySet()) {
            HashMap<String, Double> row = tfidt.get(entry);
            double length = 0;
            for (Map.Entry<String, Double> e : row.entrySet()) {
                e.setValue(e.getValue() / columnLengths.get(e.getKey()));
                length += e.getValue() * e.getValue();
            }
            documentLength.put(entry, Math.sqrt(length));
        }

        System.out.println("Writing matrix to file");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputPath))) {
            out.writeObject(documentLength);
            out.writeObject(tfidt);
            out.writeObject(idf);
        }
    }


    public static void main(String[] args) throws Exception {
        System.out.println("Entries path system property: "+System.getProperty("entries.path"));
        String entriesPath = System.getProperty("entries.path", "/home/massimo/Downloads/solrResources.json");
        System.out.println("TF/IDF path system property: "+System.getProperty("tfidf.path"));
        System.out.println("TF/IDF on keykors path system property: "+System.getProperty("tfidfk.path"));
        String tfidtPath = System.getProperty("tfidf.path","/home/massimo/aztec/tfidf.data");
        ArrayList<AztecEntry> entries = new AztecEntryProviderFromJsonFile(entriesPath).load();
        TfIdfBuilder builder = new TfIdfBuilder();
        builder.buildTfIdfMatrix(entries, tfidtPath);
    }
}
