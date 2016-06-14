/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.aztec.similarity;

import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.dto.SearchResultPage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Search {

    Tokenizer tokenizer;

    public Search() throws Exception {
        tokenizer = new Tokenizer();
    }

    public SearchResultPage searchQueryWithOnlyKeywordsTFIDF(String query, Integer offset, Integer limit) {
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = Integer.MAX_VALUE;
        }
        ArrayList<WeightedEntry> res = new ArrayList<>();
        try {
            Tokenizer t = new Tokenizer();
            LinkedList<String> tokens = t.tokenize(query);
            if (!tokens.isEmpty()) {
                for (String entry : CachedData.tfidtK.keySet()) {
                    HashMap<String, Double> row2 = CachedData.tfidtK.get(entry);
                    double product = 0;
                    for (String w : tokens) {
                        Double val = row2.get(w);
                        if (val != null) {
                            product += val;
                        }
                    }
                    double sim = product;// / length2;
                    if (Double.isFinite(sim) && sim > 0) {
                        res.add(new WeightedEntry(CachedData.entryMap.get(entry), sim));
                    }
                }
            }
        } catch (Exception e) {
        }

        Collections.sort(res);
        ArrayList<AztecEntry> resk = new ArrayList<>();
        for (int i = offset; i < Math.min(res.size(), limit - offset); i++) {
            WeightedEntry we = res.get(i);
            resk.add(we.entry);
        }
        return new SearchResultPage(resk, res.size());
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

}
