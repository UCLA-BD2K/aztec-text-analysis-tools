package edu.ucla.cs.scai.aztec.similarity;

import edu.ucla.cs.scai.aztec.AztecEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class SimilarityComputation {

    Tokenizer tokenizer;

    public SimilarityComputation() throws Exception {
        tokenizer = new Tokenizer();
    }

    public double getTagDistance(AztecEntry e1, AztecEntry e2) {
        HashSet<String> tags1 = new HashSet<>();
        HashSet<String> intersection = new HashSet<>();
        HashSet<String> union = new HashSet<>();
        if (e1.getTags() != null) {
            for (String tag : e1.getTags()) {
                tag = tag.toLowerCase();
                tags1.add(tag);
                union.add(tag);
            }
        }
        if (e2.getTags() != null) {
            for (String tag : e2.getTags()) {
                tag = tag.toLowerCase();
                if (tags1.contains(tag)) {
                    intersection.add(tag);
                }
                union.add(tag);
            }
        }
        if (intersection.size() > 0) {
            System.out.print("");
        }
        return (1.0 + intersection.size()) / (1.0 + union.size());
    }

    public ArrayList<WeightedEntry> getMostSimilarEntries(String entryId, int k, boolean useTags) {
        return getMostSimilarEntries(CachedData.entryMap.get(entryId), k, useTags);
    }

    public ArrayList<WeightedEntry> getMostSimilarEntriesToQuery(String qs, int k) {
        ArrayList<WeightedEntry> res = new ArrayList<>();
        LinkedList<String> tokens = tokenizer.tokenize(qs);
        HashMap<String, Integer> wordCount = new HashMap<>();
        int max = 1;
        for (String w : tokens) {
            Integer c = wordCount.get(w);
            if (c == null) {
                wordCount.put(w, 1);
            } else {
                wordCount.put(w, c + 1);
                max = Math.max(max, c + 1);
            }
        }
        HashMap<String, Double> queryTfidt = new HashMap<>();
        double queryLength = 0;
        for (String w : wordCount.keySet()) {
            Double val = CachedData.idf.get(w);
            if (val != null) {
                //val *= 1.0 * wordCount.get(w) / max;
                val *= 1 + Math.log(wordCount.get(w)) / Math.log(2);
                queryLength += val * val;
                queryTfidt.put(w, val);
            }
        }
        queryLength = Math.sqrt(queryLength);
        for (String entry : CachedData.tfidt.keySet()) {
            double docLength = CachedData.documentLength.get(entry);
            HashMap<String, Double> row = CachedData.tfidt.get(entry);
            double sim = 0;
            for (String w : wordCount.keySet()) {
                Double val = row.get(w);
                if (val != null) {
                    sim += val * queryTfidt.get(w);
                }
            }
            sim /= (queryLength * docLength);
            if (sim > 0) {
                res.add(new WeightedEntry(CachedData.entryMap.get(entry), sim));
            }
        }

        return res;
    }

    public ArrayList<WeightedEntry> getMostSimilarEntries(AztecEntry e, int k, boolean useTags) {
        ArrayList<WeightedEntry> res = new ArrayList<>();
        String desc = e.getDescription();
        if (desc != null) {

            LinkedList<String> tokens = tokenizer.tokenize(desc);
            HashMap<String, Integer> wordCount = new HashMap<>();
            int max = 1;
            for (String w : tokens) {
                Integer c = wordCount.get(w);
                if (c == null) {
                    wordCount.put(w, 1);
                } else {
                    wordCount.put(w, c + 1);
                    max = Math.max(max, c + 1);
                }
            }
            HashMap<String, Double> queryTfidt = new HashMap<>();
            double queryLength = 0;
            for (String w : wordCount.keySet()) {
                Double val = CachedData.idf.get(w);
                if (val != null) {
                    val *= 0.5 + 0.5 * wordCount.get(w) / max;
                    queryLength += val * val;
                    queryTfidt.put(w, val);
                }
            }
            queryLength = Math.sqrt(queryLength);
            for (String entry : CachedData.tfidt.keySet()) {
                double docLength = CachedData.documentLength.get(entry);
                HashMap<String, Double> row = CachedData.tfidt.get(entry);
                double product = 0;
                for (String w : wordCount.keySet()) {
                    Double val = row.get(w);
                    if (val != null) {
                        product += val * queryTfidt.get(w);
                    }
                }
                double sim = product / (queryLength * docLength);
                if (sim > 0) {
                    if (useTags) {
                        res.add(new WeightedEntry(CachedData.entryMap.get(entry), Math.sqrt(sim * getTagDistance(CachedData.entryMap.get(entry), e))));
                    } else {
                        res.add(new WeightedEntry(CachedData.entryMap.get(entry), sim));
                    }
                }
            }

            /*
             HashMap<String, Double> queryRow = CachedData.tfidt.get(e.getId());
             double queryLength = CachedData.documentLength.get(e.getId());

             for (String entry : CachedData.tfidt.keySet()) {
             HashMap<String, Double> row = CachedData.tfidt.get(entry);
             double length = CachedData.documentLength.get(entry);
             double sim = 0;
             for (String term : row.keySet()) {
             Double val = queryRow.get(term);
             if (val != null) {
             sim += val * row.get(term);
             }
             }
             if (sim > 0) {
             sim /= (length * queryLength);
             res.add(new WeightedEntry(CachedData.entryMap.get(entry), Math.sqrt(sim * getTagDistance(CachedData.entryMap.get(entry), e))));
             }
             }
             */
            Collections.sort(res);
            ArrayList<WeightedEntry> resk = new ArrayList<>();
            for (WeightedEntry we : res) {
                if (we.entry.getId().equals(e.getId())) {
                    continue;
                }
                if (resk.size() == k) {
                    break;
                }
                //System.out.println(we.entry.getName() + " " + we.weight);
                resk.add(we);
            }
            return resk;
        }
        return res;
    }

    public static String truncate(String s, int length) {
        if (s == null) {
            return "";
        }
        s = s.replaceAll("\n", " ");
        if (s.length() <= length + 3) {
            return s;
        }
        return s.substring(0, length) + "...";
    }

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        SimilarityComputation sim = new SimilarityComputation();
        for (AztecEntry e : CachedData.entryMap.values()) {            
            System.out.println("=======");
            System.out.println(e.getId() + " " + e.getName() + ": " + truncate(e.getDescription(), 100));
            System.out.println("Using tags");
            ArrayList<WeightedEntry> res = sim.getMostSimilarEntries(e, 5, true);            
            for (WeightedEntry we : res) {
                System.out.println(we.weight + " " + we.entry.getId() + " " + we.entry.getName() + ": " + truncate(we.entry.getDescription(), 100));
            }
            System.out.println("Not using tags");
            res = sim.getMostSimilarEntries(e, 5, false);
            for (WeightedEntry we : res) {
                System.out.println(we.weight + " " + we.entry.getId() + " " + we.entry.getName() + ": " + truncate(we.entry.getDescription(), 100));
            }
        }
        long end = System.currentTimeMillis();
        long sec = (end - start) / 1000;
        System.out.println("Running time: " + (sec / 60) + "'" + (sec % 60) + "\"");
    }

}
