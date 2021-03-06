package edu.ucla.cs.scai.aztec.similarity;

import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.summarization.RankedString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

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
        if (intersection.isEmpty()) {
            return 0;
        }
        return (1.0 * intersection.size()) / (1.0 * union.size());
    }

    public ArrayList<WeightedEntry> getMostSimilarEntries(String entryId, int k, boolean useTags) {
        return getMostSimilarEntries(CachedData.entryMap.get(entryId), k, useTags);
    }

    public ArrayList<WeightedEntry> getMostSimilarEntriesWithSeparateTags(String entryId, int k) {
        return getMostSimilarEntriesWithSeparateTags(CachedData.entryMap.get(entryId), k);
    }

    public ArrayList<WeightedEntry> getMostSimilarEntriesWithOnlyTags(AztecEntry e, int k) {
        ArrayList<WeightedEntry> res = new ArrayList<>();
        for (String entry : CachedData.tfidt.keySet()) {
            double simTags = getTagDistance(CachedData.entryMap.get(entry), e);
            res.add(new WeightedEntry(CachedData.entryMap.get(entry), simTags));
        }

        Collections.sort(res);
        ArrayList<WeightedEntry> resk = new ArrayList<>();
        for (WeightedEntry we : res) {
            if (we.entry.getId().equals(e.getId())) {
                continue;
            }
            if (resk.size() == k) {
                break;
            }
            resk.add(we);
        }
        return resk;
    }

    public double getKeywordDistance(Collection<RankedString> l1, Collection<RankedString> l2) {
        HashMap<String, Double> w1 = new HashMap<>();
        HashMap<String, Double> w2 = new HashMap<>();
        double totW1 = 0;
        for (RankedString s : l1) {
            w1.put(s.getString(), s.getRank());
            totW1 += s.getRank();
        }
        double totW2 = 0;
        for (RankedString s : l2) {
            w2.put(s.getString(), s.getRank());
            totW2 += s.getRank();
        }
        double intersectionW = 0;
        for (String s : w1.keySet()) {
            if (w2.containsKey(s)) {
                intersectionW = w1.get(s) / totW1 + w2.get(s) / totW2;
            }
        }
        return intersectionW / 2;
    }

    public ArrayList<WeightedEntry> getMostSimilarEntriesWithOnlyKeywords(String entryId, int k) {
        return getMostSimilarEntriesWithOnlyKeywords(CachedData.entryMap.get(entryId), k);
    }

    public ArrayList<WeightedEntry> getMostSimilarEntriesWithOnlyKeywords(AztecEntry e, int k) {
        ArrayList<WeightedEntry> res = new ArrayList<>();
        List<RankedString> l1 = CachedData.keywords.get(e.getId());
        for (String entry : CachedData.keywords.keySet()) {
            if (entry.equals(e.getId())) {
                continue;
            }
            double simTags = getKeywordDistance(l1, CachedData.keywords.get(entry));
            res.add(new WeightedEntry(CachedData.entryMap.get(entry), simTags));
        }

        Collections.sort(res);
        ArrayList<WeightedEntry> resk = new ArrayList<>();
        for (WeightedEntry we : res) {
            if (we.entry.getId().equals(e.getId())) {
                continue;
            }
            if (resk.size() == k) {
                break;
            }
            resk.add(we);
        }
        return resk;
    }

    public ArrayList<WeightedEntry> getMostSimilarEntriesWithOnlyKeywordsTFIDF(String eId, int k, Double threshold) {
        return getMostSimilarEntriesWithOnlyKeywordsTFIDF(CachedData.entryMap.get(eId), k, threshold);
    }

    public ArrayList<WeightedEntry> getMostSimilarEntriesWithOnlyKeywordsTFIDF(AztecEntry e, int k, Double threshold) {
        if (threshold == null) {
            threshold = 0.2;
        }
        ArrayList<WeightedEntry> res = new ArrayList<>();
        HashMap<String, Double> row1 = CachedData.tfidtK.get(e.getId());
        if (row1 != null) {
            double length1 = CachedData.documentLengthK.get(e.getId());
            for (String entry : CachedData.tfidtK.keySet()) {
                double length2 = CachedData.documentLengthK.get(entry);
                HashMap<String, Double> row2 = CachedData.tfidtK.get(entry);
                double product = 0;
                for (String w : row1.keySet()) {
                    Double val = row2.get(w);
                    if (val != null) {
                        product += val * row1.get(w);
                    }
                }
                double sim = product / (length1 * length2);
                if (Double.isFinite(sim)) {
                    res.add(new WeightedEntry(CachedData.entryMap.get(entry), sim));
                }
            }
        }

        Collections.sort(res);
        ArrayList<WeightedEntry> resk = new ArrayList<>();
        for (WeightedEntry we : res) {
            if (we.entry.getId().equals(e.getId())) {
                continue;
            }
            if (resk.size() == k) {
                break;
            }
            if (we.weight < threshold) {
                break;
            }
            resk.add(we);
        }
        return resk;
    }

    public ArrayList<WeightedEntry> getMostSimilarEntriesWithOnlyDescription(AztecEntry e, int k) {
        ArrayList<WeightedEntry> res = new ArrayList<>();
        String desc = e.getDescription();
        if (desc != null) {
            LinkedList<String> tokens = tokenizer.tokenize(desc);
            HashMap<String, Integer> wordCount = new HashMap<>();
            CachedData.keywords.get(e.getId());
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
                double simDesc = product / (queryLength * docLength);
                if (Double.isFinite(simDesc)) {
                    res.add(new WeightedEntry(CachedData.entryMap.get(entry), simDesc));
                }
            }
        } else {
            return getMostSimilarEntriesWithOnlyTags(e, k);
        }

        Collections.sort(res);
        ArrayList<WeightedEntry> resk = new ArrayList<>();
        for (WeightedEntry we : res) {
            if (we.entry.getId().equals(e.getId())) {
                continue;
            }
            if (resk.size() == k) {
                break;
            }
            resk.add(we);
        }
        return resk;
    }

    public ArrayList<WeightedEntry> getMostSimilarEntriesWithSeparateTags(AztecEntry e, int k) {
        ArrayList<WeightedEntry> res1 = getMostSimilarEntriesWithOnlyDescription(e, k);
        ArrayList<WeightedEntry> res2 = getMostSimilarEntriesWithOnlyTags(e, k);
        ArrayList<WeightedEntry> resk = new ArrayList<>();
        int i = 0;
        int j = 0;
        HashSet<String> ids = new HashSet<>();
        while (resk.size() < k && (i < res1.size() || j < res2.size())) {
            if (i >= res1.size() || j < res2.size() && res1.get(i).weight < res2.get(j).weight) {
                if (!ids.contains(res2.get(j).entry.getId())) {
                    resk.add(res2.get(j));
                    ids.add(res2.get(j).entry.getId());
                }
                j++;
            } else {
                if (!ids.contains(res1.get(i).entry.getId())) {
                    resk.add(res1.get(i));
                    ids.add(res1.get(i).entry.getId());
                }
                i++;
            }
        }
        return resk;
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

    public ArrayList<WeightedEntry> getMostSimilarEntries(String eId, int k, Double simW, Double tagW) {
        return getMostSimilarEntries(CachedData.getEntries().get(eId), k, simW, tagW);
    }

    public ArrayList<WeightedEntry> getMostSimilarEntries(AztecEntry e, int k, Double simW, Double tagW) {
        if (simW == null || tagW == null) {
            simW = 0.5;
            tagW = 0.5;
        }
        ArrayList<WeightedEntry> res = new ArrayList<>();
        if (e == null) {
            return res;
        }
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
                if (row != null && !row.isEmpty()) {
                    double product = 0;
                    for (String w : wordCount.keySet()) {
                        Double val = row.get(w);
                        if (val != null) {
                            product += val * queryTfidt.get(w);
                        }
                    }
                    double sim = product / (queryLength * docLength);
                    if (CachedData.entryMap.get(entry).getTags() != null && CachedData.entryMap.get(entry).getTags().size() > 0
                            && e.getTags() != null && e.getTags().size() > 0) {
                        res.add(new WeightedEntry(CachedData.entryMap.get(entry),
                                (sim * simW + getTagDistance(CachedData.entryMap.get(entry), e) * tagW) / (simW + tagW)
                        ));
                    } else {
                        res.add(new WeightedEntry(CachedData.entryMap.get(entry), sim));
                    }
                } else {
                    res.add(new WeightedEntry(CachedData.entryMap.get(entry), getTagDistance(CachedData.entryMap.get(entry), e)));
                }
            }
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
        } else {
            return getMostSimilarEntriesWithOnlyTags(e, k);
        }
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
            System.out.println("Combining description and tags");
            ArrayList<WeightedEntry> res = sim.getMostSimilarEntries(e, 5, true);
            for (WeightedEntry we : res) {
                System.out.println(we.weight + " " + we.entry.getId() + " " + we.entry.getName() + ": " + truncate(we.entry.getDescription(), 100));
            }
            System.out.println("Only description");
            res = sim.getMostSimilarEntriesWithOnlyDescription(e, 5);
            for (WeightedEntry we : res) {
                System.out.println(we.weight + " " + we.entry.getId() + " " + we.entry.getName() + ": " + truncate(we.entry.getDescription(), 100));
            }
            System.out.println("Description and tags separately");
            res = sim.getMostSimilarEntries(e, 5, 0.5, 0.5);
            for (WeightedEntry we : res) {
                System.out.println(we.weight + " " + we.entry.getId() + " " + we.entry.getName() + ": " + truncate(we.entry.getDescription(), 100));
            }
            System.out.println("TFIDF computed on keywords obtained through TextRank");
            res = sim.getMostSimilarEntriesWithOnlyKeywordsTFIDF(e, 5, 0.2);
            for (WeightedEntry we : res) {
                System.out.println(we.weight + " " + we.entry.getId() + " " + we.entry.getName() + ": " + truncate(we.entry.getDescription(), 100));
            }

        }
        long end = System.currentTimeMillis();
        long sec = (end - start) / 1000;
        System.out.println("Running time: " + (sec / 60) + "'" + (sec % 60) + "\"");
    }
}
