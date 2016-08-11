package edu.ucla.cs.scai.aztec.summarization;

import edu.ucla.cs.scai.aztec.similarity.Tokenizer;
import edu.ucla.cs.scai.aztec.textexpansion.TextParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.sf.extjwnl.JWNLException;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class KeywordsRank {

    WeightedEdgeGraph g;
    HashMap<Integer, String> keywords = new HashMap<>();
    HashMap<String, Integer> keywordIds = new HashMap<>();
    double[] rank;
    Integer[] ordered;

    public KeywordsRank(String text, int... windowSizes) throws JWNLException, FileNotFoundException, IOException {
        //Tokenizer tokenizer = new Tokenizer();
        TextParser TP = new TextParser();
        //LinkedList<String> tokens = tokenizer.tokenize(text);
        LinkedList<String> tokens = TP.docParser(text);
        HashSet<String> distinctTokens = new HashSet<>(tokens);
        int n = 0;
        for (String t : distinctTokens) {
            keywords.put(n, t);
            keywordIds.put(t, n);
            n++;
        }
        g = new WeightedEdgeGraph(n);
        for (int windowSize : windowSizes) {
            if (windowSize > tokens.size()) {
                windowSize = tokens.size();
            }
            LinkedList<Integer> window = new LinkedList<>();
            Iterator<String> it = tokens.iterator();
            //init window
            int i = 0;
            for (; i < windowSize; i++) {
                String w = it.next();
                int idw = keywordIds.get(w);
                int j = 0;
                for (int idw2 : window) {
                    g.addWeight(idw, idw2, 1.0 / (i - j));
                    j++;
                }
                window.addLast(idw);
            }
            //advance window
            while (it.hasNext()) {
                String w = it.next();
                int idw = keywordIds.get(w);
                window.removeFirst();
                int j = i - windowSize + 1; //i is increase from previous iteration
                for (int idw2 : window) {
                    g.addWeight(idw, idw2, 1.0 / (i - j));
                    j++;
                }                
                window.addLast(idw);
                i++;
            }
        }
        rank = g.computeNodeRank(0.85, 1, 0.001);
        ordered = new Integer[n];
        for (int i = 0; i < n; i++) {
            ordered[i] = i;
        }
        Arrays.sort(ordered, new RankComparator(rank));
    }

    //returns the keywords with the top-k rank
    public List<String> topKeywords(Integer k) {
        if (k == null) {
            return topKeywords();
        }
        if (k > keywords.size()) {
            k = keywords.size();
        }
        LinkedList<String> res = new LinkedList<>();
        for (int i = 0; i < k; i++) {
            res.add(keywords.get(ordered[i]));
        }
        return res;
    }

    public List<String> topKeywords() {
        LinkedList<String> res = new LinkedList<>();
        double minRank = rank[ordered[0]] * 0.9;
        int i = 0;
        while (i < ordered.length && rank[ordered[i]] >= minRank) {
            res.add(keywords.get(ordered[i]));
            i++;
        }
        return res;
    }

    public List<RankedString> topRankedKeywords(Integer k) {
        if (k == null) {
            return topRankedKeywords();
        }
        if (k > keywords.size()) {
            k = keywords.size();
        }
        LinkedList<RankedString> res = new LinkedList<>();
        for (int i = 0; i < k; i++) {
            res.add(new RankedString(keywords.get(ordered[i]), rank[ordered[i]]));
        }
        return res;

    }

    public List<RankedString> topRankedKeywords() {
        LinkedList<RankedString> res = new LinkedList<>();
        double minRank = (rank[ordered[0]]-rank[ordered[ordered.length-1]]) * 0.2+rank[ordered[ordered.length-1]];// change from 0.9 to 0.6
        int i = 0;
        while (i < ordered.length && rank[ordered[i]] >= minRank) {
            res.add(new RankedString(keywords.get(ordered[i]), rank[ordered[i]]));
            i++;
        }
        return res;
    }

    class RankComparator implements Comparator<Integer> {

        double[] rank;

        public RankComparator(double[] rank) {
            this.rank = rank;
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            return Double.compare(rank[o2], rank[o1]);
        }
    }

    public static void main(String[] args) throws JWNLException, FileNotFoundException, IOException{
        String test = "We introduce Sailfish, a computational method for quantifying the abundance of previously annotated RNA isoforms from RNA-seq data. "
                +"Because Sailfish entirely avoids mapping reads, a time-consuming step in all current methods, "
                +"it provides quantification estimates much faster than do existing approaches (typically 20 times faster) without loss of accuracy. "
                +"By facilitating frequent reanalysis of data and reducing the need to optimize parameters, "
                +"Sailfish exemplifies the potential of lightweight algorithms for efficiently processing sequencing reads.\n";
        KeywordsRank kr = new KeywordsRank(test, 10);
        List<RankedString> kw = kr.topRankedKeywords();
        for (RankedString s : kw) {
            System.out.println(s);
        }
    }
}
