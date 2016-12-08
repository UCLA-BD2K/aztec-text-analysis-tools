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
public class TextRank {

    WeightedEdgeGraph g;
    HashMap<Integer, String> sentences = new HashMap<>();
    HashMap<Integer, HashSet<String>> sets = new HashMap<>();
    double[] rank;
    Integer[] ordered;
    private WeightedEdgeGraph dependencyGraph(String sen) throws JWNLException, FileNotFoundException {
        Tokenizer tokenizer = new Tokenizer();
        WeightedEdgeGraph g = new WeightedEdgeGraph(1);
        return g;
    }

    private double similarity(int i, int j) {
        if (sets.get(i).size() > sets.get(j).size()) {
            int temp = i;
            i = j;
            j = temp;
        }
        HashSet<String> si = sets.get(i);
        HashSet<String> sj = sets.get(j);
        int intersection = 0;
        int difference = 0;
        for (String s : si) {
            if (sj.contains(s)) {
                intersection++;
            } else {
                difference++;
            }
        }
        //return 1.0 * intersection / (Math.log(si.size()) + Math.log(sj.size()));
        return 1.0 * intersection / (si.size()+difference);
    }

    public TextRank(String text) throws JWNLException, FileNotFoundException {
        Tokenizer tokenizer = new Tokenizer();
        LinkedList<int[]> boundaries = new LinkedList<>();
        LinkedList<LinkedList<String>> tokens = tokenizer.tokenizeBySentence(text, boundaries);
        int n = 0;
        Iterator<int[]> it = boundaries.iterator();
        for (LinkedList<String> s : tokens) {
            int[] bounds=it.next();
            this.sentences.put(n, text.substring(bounds[0], bounds[1]));
            HashSet<String> set = new HashSet<>();
            for (String t:s) {
                set.add(t);
            }
            if (set.size() > 1) {
                sets.put(n, set);
                n++;
            }
        }
        g = new WeightedEdgeGraph(n);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                g.setWeight(i, j, similarity(i, j));
            }
        }
        rank = g.computeNodeRank(0.85, 1, 0.001);
        ordered = new Integer[n];
        for (int i = 0; i < n; i++) {
            ordered[i] = i;
        }
        Arrays.sort(ordered, new RankComparator(rank));
    }

    public TextRank(List<String> sentences) {
        int n = 0;
        for (String s : sentences) {
            this.sentences.put(n, s);
            StringTokenizer st = new StringTokenizer(s, " ");
            HashSet<String> set = new HashSet<>();
            while (st.hasMoreTokens()) {
                set.add(st.nextToken());
            }
            if (set.size() > 1) {
                sets.put(n, set);
                n++;
            }
        }
        g = new WeightedEdgeGraph(n);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                g.setWeight(i, j, similarity(i, j));
            }
        }
        rank = g.computeNodeRank(0.85, 1, 0.001);
        ordered = new Integer[n];
        for (int i = 0; i < n; i++) {
            ordered[i] = i;
        }
        Arrays.sort(ordered, new RankComparator(rank));
    }
    
    //returns the sentences with a rank at least equal to 0.9 times the maximum rank
    public List<String> topSentences() {
        ArrayList<Integer> ids=new ArrayList<>();
        double minRank=rank[ordered[0]]*0.9;
        int i=0;
        while (i<ordered.length && rank[ordered[i]]>=minRank) {
            ids.add(ordered[i]);
            i++;
        }
        Collections.sort(ids);        
        LinkedList<String> res = new LinkedList<>();
        for (int k:ids) {
            res.add(sentences.get(k));
        }
        return res;
    }
    

    //returns the sentences with the top-k rank, in the order in which they appear in the text
    public List<String> topSentences(Integer k) {
        if (k==null) {
            return topSentences();
        }
        if (k > sentences.size()) {
            k = sentences.size();
        }        
        ArrayList<Integer> ids=new ArrayList<>();
        for (int i=0; i<k; i++) {
            ids.add(ordered[i]);
        }
        Collections.sort(ids);        
        LinkedList<String> res = new LinkedList<>();
        for (int i:ids) {
            res.add(sentences.get(i));
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
        String[] text = new String[]{"In this paper we present SYST, a question answering (QA) system over RDF cubes.",
            "The system first tags chunks of text with elements of the knowledge base, and then leverages the well-defined structure of data cubes to create the SPARQL query from the tags.",
            "For each class of questions with the same structure a SPARQL template is defined.",
            "The correct template is chosen by using a set of regular-expression-like regex-like patterns, based on both syntactical and semantic features of the tokens extracted from the question.",
            "Preliminary results are encouraging and suggest a number of improvements.",
            "SYST can currently provide a correct answer to 51 of the 100 questions of the training set."};
        List<String> input = new LinkedList<>();
        for (String s : text) {
            input.add(s);
        }
        TextRank tr = new TextRank(input);
        List<String> summary = tr.topSentences(2);
        for (String s : summary) {
            System.out.println(s);
        }
        
        StringBuilder fullText=new StringBuilder();
        for (String s:text) {
            fullText.append(s).append(" ");
        }
        tr=new TextRank(fullText.toString());
        summary = tr.topSentences(2);
        for (String s : summary) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println();
        tr=new TextRank("Meso-scale structural analysis, like core decomposition has uncovered groups of nodes that play important roles in the underlying complex systems. The existing core decomposition approaches generally focus on node properties like degree and strength. The node centric approaches can only capture a limited information about the local neighborhood topology. In the present work, we propose a group density based core analysis approach that overcome the drawbacks of the node centric approaches. The proposed algorithmic approach focuses on weight density, cohesiveness, and stability of a substructure. The method also assigns an unique score to every node that rank the nodes based on their degree of core-ness. To determine the correctness of the proposed method, we propose a synthetic benchmark with planted core structure. A performance test on the null model is carried out using a weighted lattice without core structures. We further test the stability of the approach against random noise. The experimental results prove the superiority of our algorithm over the state-of-the-arts. We finally analyze the core structures of several popular weighted network models and real life weighted networks. The experimental results reveal important node ranking and hierarchical organization of the complex networks, which give us better insight about the underlying systems.");
        summary = tr.topSentences(4);
        for (String s : summary) {
            System.out.println(s);
        }
        
        
    }
}
