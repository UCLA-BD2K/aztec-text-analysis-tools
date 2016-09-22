package edu.ucla.cs.scai.aztec.similarity;

import edu.ucla.cs.scai.aztec.AbsEntry;
import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.summarization.KeywordsBuilder;
import edu.ucla.cs.scai.aztec.summarization.RankedString;

import java.io.*;
import java.util.*;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class TfIdfBuilderKeywords {

    static final double log2 = Math.log(2);

    //the value of td-ift is constructed using the "Best fully weighted system" - Salton and Buckley, 1988
    //tf-idf(i,j) =tf(i,j) * log N/n_j / sqrt(sum_k (tf(k,j) * log (N/n_j))^2) 

    public void buildTfIdfMatrix(Collection<AztecEntry> entries, String outputPath) throws IOException {

        HashMap<String, Integer> nOfDocsWithWord = new HashMap<>();
        HashMap<String, HashMap<String, Double>> fOfWordsInDocuments = new HashMap<>();
        for (AztecEntry entry : entries) {
            List<RankedString> rankedTokens = CachedData.expkeywords.get(entry.getId()); // change to expanded keywords as input
            HashMap<String, Double> wordCount = new HashMap<>();
            fOfWordsInDocuments.put(entry.getId(), wordCount);
            for (RankedString w : rankedTokens) {
                Double c = wordCount.get(w.getString());
                if (c == null) {
                    wordCount.put(w.getString(), w.getRank());
                } else {
                      //Double max_c = Math.max(c,w.getRank());
//                    wordCount.put(w.getString(), max_c);
                    wordCount.put(w.getString(), c + w.getRank());
                }
            }
            // for each words, how many document contains the words.

            for (String w : wordCount.keySet()) {
                // for origin units in the words list
                Integer c = nOfDocsWithWord.get(w);
                if (c == null) {
                    nOfDocsWithWord.put(w, 1);
                } else {
                    nOfDocsWithWord.put(w, c + 1);
                }
                // new codes to calculate the idf of sub_phrases
                String[] subwords = w.split("_");
                Integer win_size = subwords.length-1;
                while(win_size>1){
                    Integer start_pos = 0;
                    Integer end_pos = start_pos+win_size;
                    while(end_pos<subwords.length){
                        String[] subphrase = Arrays.copyOfRange(subwords, start_pos, end_pos);
                        String sub_phrase = String.join("_",subphrase);
                        c=nOfDocsWithWord.get(sub_phrase);
                        if(c == null) {
                            nOfDocsWithWord.put(sub_phrase, 1);
                        }else{
                            nOfDocsWithWord.put(sub_phrase, c+1);
                        }
                        start_pos++;
                        end_pos = start_pos+win_size;
                    }
                    win_size --;
                }
                if(win_size == 1){
                    for (String sub : subwords) { // only do this for origin units
                        c = nOfDocsWithWord.get(sub);
                        if(c == null) {
                            nOfDocsWithWord.put(sub, 1);
                        }else{
                            nOfDocsWithWord.put(sub, c+1);
                        }
                    }
                }
                //////// end of new part
            }
        }

        HashMap<String, Double> documentLength = new HashMap<>(); // the norm of each vector
        HashMap<String, HashMap<String, Double>> tfidt = new HashMap<>(); // tfidf matrix
        HashMap<String, Double> idf = new HashMap<>(); //
        HashMap<String, Double> row_max = new HashMap<>();
        HashMap<String, Double> row_min = new HashMap<>();
        double N = fOfWordsInDocuments.keySet().size();
        for (String w : nOfDocsWithWord.keySet()) { // for each word: calculate idf value
            double val = Math.log(N / nOfDocsWithWord.get(w)) / log2;
            idf.put(w, val);
        }
        HashMap<String, Double> columnLengths = new HashMap<>();
        //multiply tf by idf
        for (String entry : fOfWordsInDocuments.keySet()) {
            Double max_s = 0.0;
            Double min_s = 100.0;
            HashMap<String, Double> row = new HashMap<>();
            HashMap<String, Double> wordCount = fOfWordsInDocuments.get(entry);
            tfidt.put(entry, row);
            for (String w : wordCount.keySet()) {
                double val = wordCount.get(w)*idf.get(w); // do idf for documents do not do it again for query
                row.put(w, val);
                Double colLength = columnLengths.get(w);
                if (colLength == null) {
                    colLength = 0d;
                }
                colLength += val;
                columnLengths.put(w, colLength);
                if(val>max_s){
                    max_s = val;
                }
                if(val<min_s){
                    min_s = val;
                }
            }

            row_max.put(entry,max_s);
            row_min.put(entry,min_s);
        }

//        for (Map.Entry<String, Double> e : columnLengths.entrySet()) {
//            e.setValue(Math.sqrt(e.getValue())); // do sqrt later
//        }

        //now divide each cell by the length of its column
        //and compute the length of the document
        for (String entry : tfidt.keySet()) {
            HashMap<String, Double> row = tfidt.get(entry);
            double length = 0;
            Double min_score = row_min.get(entry);
            Double max_score = row_max.get(entry);
            for (Map.Entry<String, Double> e : row.entrySet()) {
                Double value = e.getValue(); // / columnLengths.get(e.getKey());
                //value = (value-min_score)/(max_score-min_score);
                //value = 0.5+0.5*value/max_score;
                value = value/max_score;
                value *= idf.get(e.getKey());
                //e.setValue(value); // normalize each column already involve a kind of idf
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
    public void buildTfIdfMatrixAbstract(Collection<AbsEntry> entries, String outputPath) throws Exception, IOException {
        // The one I am currently using
        System.out.println("Start to calculate tfidf");
        HashMap<String, Integer> nOfDocsWithWord = new HashMap<>();
        HashMap<String, List<RankedString>> keywords;
        HashMap<String, HashMap<String, Double>> fOfWordsInDocuments = new HashMap<>();
        HashMap<String, Double> row_max = new HashMap<>();
        HashMap<String, Double> row_min = new HashMap<>();

        for (AbsEntry entry : entries) {
            //List<RankedString> rankedTokens = CachedData.keywords.get(entry.getId());
            List<RankedString> rankedTokens = AbsCachedData.expkeywords.get(entry.getId());
            HashMap<String, Double> wordCount = new HashMap<>();
            fOfWordsInDocuments.put(entry.getId(), wordCount);
            for (RankedString w : rankedTokens) {
                Double c = wordCount.get(w.getString());
                if (c == null) {
                    wordCount.put(w.getString(), w.getRank());
                } else {
//                    Double max_c = Math.max(c,w.getRank());
//                    wordCount.put(w.getString(), max_c);
                    wordCount.put(w.getString(), c + w.getRank());
                }
            }
            // count how many documents contain a word

            for (String w : wordCount.keySet()) {
                Integer c = nOfDocsWithWord.get(w);
                if (c == null) {
                    nOfDocsWithWord.put(w, 1);
                } else {
                    nOfDocsWithWord.put(w, c + 1);
                }
                // new codes to calculate the idf of sub_phrases
                String[] subwords = w.split("_");
                Integer win_size = subwords.length-1;
                while(win_size>1){
                    Integer start_pos = 0;
                    Integer end_pos = start_pos+win_size;
                    while(end_pos<subwords.length){
                        String[] subphrase = Arrays.copyOfRange(subwords, start_pos, end_pos);
                        String sub_phrase = String.join("_",subphrase);
                        // in this case we do not check the subphrase is a real phrase or not, we just keep the record of it
                        c=nOfDocsWithWord.get(sub_phrase);
                        if(c == null) {
                            nOfDocsWithWord.put(sub_phrase, 1);
                        }else{
                            nOfDocsWithWord.put(sub_phrase, c+1);
                        }
                        start_pos++;
                        end_pos = start_pos+win_size;
                    }
                    win_size --;
                }
                if(win_size == 1){
                    for (String sub : subwords) { // only do this for origin units
                        c = nOfDocsWithWord.get(sub);
                        if(c == null) {
                            nOfDocsWithWord.put(sub, 1);
                        }else{
                            nOfDocsWithWord.put(sub, c+1);
                        }
                    }
                }
                //////// end of new part
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
            HashMap<String, Double> wordCount = fOfWordsInDocuments.get(entry);
            tfidt.put(entry, row);
            Double max_s = 0.0;
            Double min_s = 100.0;
            for (String w : wordCount.keySet()) {
                //double val = 1+ Math.log(wordCount.get(w)) / log2; // do some change to text rank score before put into tfidf matrix
                double val = wordCount.get(w);
                //val *= idf.get(w);  // do not multiple idf here do not need to check if null for document
                row.put(w, val);
                Double colLength = columnLengths.get(w);
                if (colLength == null) {
                    colLength = 0d;
                }
                colLength += val * val;
                columnLengths.put(w, colLength); // column have some problem but we do not use it now
                if(val>max_s){
                    max_s = val;
                }
                if(val<min_s){
                    min_s = val;
                }
            }
            row_max.put(entry,max_s);
            row_min.put(entry,min_s);
        }

        for (Map.Entry<String, Double> e : columnLengths.entrySet()) {
            e.setValue(Math.sqrt(e.getValue()));
        }

        //now divide each cell by the length of its column
        //and compute the length of the document
        for (String entry : tfidt.keySet()) {
            HashMap<String, Double> row = tfidt.get(entry);
            double length = 0;
            Double max_score = row_max.get(entry);
            for (Map.Entry<String, Double> e : row.entrySet()) {
                double val = e.getValue();
//                val = 0.5+0.5*val/max_score;
//                val *= idf.get(e.getKey());
                val = val/max_score;
                e.setValue(val);
                //e.setValue( e.getValue()/ columnLengths.get(e.getKey())); // divide the column length here
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
    public static void main(String[] args) throws Exception{
        String infile = "src/main/data/absExpkeywords.data";
        String outfile = "src/main/data/abstfidf.data";
        TfIdfBuilderKeywords TBK = new TfIdfBuilderKeywords();
        //TBK.buildTfIdfMatrixAbstract(infile,outfile);


    }
}
