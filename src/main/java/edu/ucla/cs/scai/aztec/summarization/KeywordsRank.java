/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.aztec.summarization;

import edu.ucla.cs.scai.aztec.similarity.Tokenizer;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
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

    public KeywordsRank(String text, int windowSize) throws JWNLException, FileNotFoundException {
        Tokenizer tokenizer = new Tokenizer();
        LinkedList<String> tokens = tokenizer.simpleTokenization(text);
        HashSet<String> distinctTokens=new HashSet<>(tokens);
        int n=0;
        for (String t:distinctTokens) {
            keywords.put(n, t);
            keywordIds.put(t, n);
            n++;
        }
        g = new WeightedEdgeGraph(n);
        if (windowSize > tokens.size()) {
            windowSize = tokens.size();
        }
        LinkedList<Integer> window = new LinkedList<>();
        Iterator<String> it=tokens.iterator();
        //init window
        for (int i=0; i<windowSize; i++) {
            String w=it.next();
            int idw=keywordIds.get(w);
            for (int idw2:window) {
                g.addWeight(idw, idw2, 1);
            }
            window.addLast(idw);
        }
        //advance window
        while (it.hasNext()) {
            String w=it.next();
            int idw=keywordIds.get(w);
            window.removeFirst();
            for (int idw2:window) {
                g.addWeight(idw, idw2, 1);
            }
            window.addLast(idw);            
        }
        rank = g.computeNodeRank(0.85, 1, 0.001);
        ordered = new Integer[n];
        for (int i = 0; i < n; i++) {
            ordered[i] = i;
        }
        Arrays.sort(ordered, new RankComparator(rank));
    }

    //returns the keywords with the top-k rank
    public List<String> topKeywords(int k) {
        if (k > keywords.size()) {
            k = keywords.size();
        }
        LinkedList<String> res = new LinkedList<>();
        for (int i = 0; i < k; i++) {
            res.add(keywords.get(ordered[i]));
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

    public static void main(String[] args) throws JWNLException, FileNotFoundException {
        KeywordsRank kr = new KeywordsRank("In this paper we present SYST, a question answering (QA) system over RDF cubes.\n" +
"The system first tags chunks of text with elements of the knowledge base, and then leverages the well-defined structure of data cubes to create the SPARQL query from the tags.\n" +
"For each class of questions with the same structure a SPARQL template is defined.\n" +
"The correct template is chosen by using a set of regular-expression-like regex-like patterns, based on both syntactical and semantic features of the tokens extracted from the question.\n" +
"Preliminary results are encouraging and suggest a number of improvements.\n" +
"SYST can currently provide a correct answer to 51 of the 100 questions of the training set.", 10);
        List<String> kw = kr.topKeywords(10);
        for (String s : kw) {
            System.out.println(s);
        }

    }
}
