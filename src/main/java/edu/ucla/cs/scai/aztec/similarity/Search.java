/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.aztec.similarity;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import edu.ucla.cs.scai.aztec.summarization.RankedString;
import edu.ucla.cs.scai.aztec.textexpansion.TextExpansion;
import edu.ucla.cs.scai.aztec.textexpansion.TextParser;
import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.dto.SearchResultPage;
import jdk.nashorn.internal.ir.CatchNode;
import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.*;
import java.lang.*;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Search {

    //Tokenizer tokenizer;
    TextParser textparser;
    TextExpansion textexpansion;
    HashMap<String,HashSet<String>> id_domain = new HashMap<>();

    public Search() throws Exception {
        textparser = new TextParser();
        textexpansion = new TextExpansion();
    }
    public void loadMap() throws IOException{
        String infile = "src/main/data/LDA_result.txt";
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        while((line = reader.readLine()) != null){
            String[] id_do = line.split("\t");
            HashSet<String> domain = new HashSet<>();
            id_domain.put(id_do[0],domain);
            for(int i = 1; i<id_do.length;i++){
                domain.add(id_do[i]);
            }
        }
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
            //Tokenizer t = new Tokenizer();
            TextParser tp = new TextParser();
            TextExpansion te = new TextExpansion();
            //LinkedList<String> tokens = t.tokenize(query);
            LinkedList<String> origintokens = tp.queryParser(query);
            LinkedList<RankedString> tokens  = te.queryExpansion(origintokens);
            if (!tokens.isEmpty()) {
                for (String entry : CachedData.tfidtK.keySet()) {
                    HashMap<String, Double> row2 = CachedData.tfidtK.get(entry);
                    double product = 0;
                    for (RankedString w : tokens) {
                        Double val = row2.get(w.getString());
                        if (val != null) {
                            product += val*w.getRank(); // suppose the query has already been normalized
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
        for (int i = offset; i < Math.min(res.size(), limit + offset); i++) {
            WeightedEntry we = res.get(i);
            resk.add(we.entry);
        }
        return new SearchResultPage(resk, res.size());
    }

    public ArrayList<WeightedEntry> getMostSimilarEntriesToQuery(String qs, String qs_domain, int k, String infile) throws Exception{
        PrintWriter pw = new PrintWriter(new FileOutputStream(infile));
        ArrayList<WeightedEntry> res = new ArrayList<>();
        ArrayList<WeightedEntry> res_sub = new ArrayList<>();
        LinkedList<String> origintokens = new LinkedList<>();
        LinkedList<RankedString> tokens = new LinkedList<>();
        origintokens = textparser.queryParser(qs);
        for(String w:origintokens){
            Double val = 0.0;
            if(CachedData.idf.get(w)!=null) {
                val = CachedData.idf.get(w);
            }
            tokens.add(new RankedString(w,val));
        }
//        origintokens = textparser.queryParser(qs);
//        LinkedList<RankedString> scoredtoken = new LinkedList<>();
//        for(String w:origintokens){
//            Double val = 0.0;
//            if(CachedData.idfK.get(w)!=null) {
//                val = CachedData.idfK.get(w);
//            }
//            scoredtoken.add(new RankedString(w,val));
//        }
//        LinkedList<RankedString> tokens = textexpansion.docExpansion(scoredtoken);
        HashMap<String, Double> wordCount = new HashMap<>();
        int max = 1;
        Double max_s = 0.0;
        Double min_s = 100.0;
        for (RankedString w : tokens) {
            //now calculating tf for words in query;
//            Integer c = wordCount.get(w.getString());
//            if (c == null) {
//                wordCount.put(w.getString(), 1);
//            } else {
//                wordCount.put(w.getString(), c + 1);
//                max = Math.max(max, c + 1);
//            }
//            Double c = wordCount.get(w.getString());
//            if( c == null){
//                wordCount.put(w.getString(),w.getRank());
//            }
//            else{
//                Double pre = wordCount.get(w.getString());
//                Double max_score = Math.max(pre,c);
//                wordCount.put(w.getString(),max_score);
//            }
            Double c = wordCount.get(w.getString());
            if (c == null) {
                wordCount.put(w.getString(), w.getRank());
            } else {
                //Double max_c = Math.max(c,w.getRank());
                //wordCount.put(w.getString(), max_c);
                wordCount.put(w.getString(), c + w.getRank());
            }
        }
        for(String w:wordCount.keySet()){
            Double val = wordCount.get(w);//*CachedData.idfK.get(w);
            if(val>max_s){
                max_s = val;
            }
            if(val<min_s){
                min_s = val;
            }
        }
        HashMap<String, Double> queryTfidt = new HashMap<>();
        double queryLength = 0;

        for (String w : wordCount.keySet()) {
            Double val = wordCount.get(w);
            //val = (val-min_s)/(max_s-min_s); //do the min max scaler as the documents
            if (val != null) {
                //val *= 1.0 * wordCount.get(w) / max;
//                val = val/max_s;
//                if(CachedData.idfK.get(w)!=null) {
//                    val = val * CachedData.idfK.get(w);
//                }
//                else{
//                    val = val * 0;
//                }
                // the calculation methods have problem, log(w) will get to 0
                //val *= 1 + Math.log(wordCount.get(w)) / Math.log(2);
                queryLength += val * val;
                queryTfidt.put(w, val);
//                System.out.print(" "+w+" ");
//                pw.print(" "+w+" ");
//                System.out.print(val);
//                pw.print(val);
            }
//            System.out.print("\n");
//            pw.print("\n");
        }

        queryLength = Math.sqrt(queryLength);
        for (String entry : CachedData.tfidtK.keySet()) {
            double docLength = CachedData.documentLengthK.get(entry);
            HashMap<String, Double> row = CachedData.tfidtK.get(entry);
            double sim = 0;
            for (String w : wordCount.keySet()) {
                Double val = row.get(w);
                if (val != null) {
                    sim += val * queryTfidt.get(w);
                }
            }
            sim /= (queryLength * docLength); // calculate cos similarity
            //sim /=(queryLength);
            Boolean domain_b = false;
            if (sim > 0) {
                HashSet domain =  id_domain.get(entry);
                if(domain_b && (!qs_domain.equals("null")) && domain!=null && domain.contains(qs_domain)) {
                    res.add(new WeightedEntry(CachedData.entryMap.get(entry), sim + 0.5));
                }
                else {
                    res.add(new WeightedEntry(CachedData.entryMap.get(entry), sim));
                }

            }
        }
        Collections.sort(res);
        if(res.size()>=k) {
            res_sub = new ArrayList<>(res.subList(0, k));
        }
        else{
            res_sub = new ArrayList<>(res);
        }
        HashSet<String> namelist = new HashSet<>();

        for (WeightedEntry w: res_sub){
            HashMap<String,Double> tfidf = CachedData.tfidtK.get(w.entry.getId());
//            if(namelist.contains(w.entry.getName())) continue;
//            else{
//                namelist.add(w.entry.getName());
//            }
            Double docLenth = CachedData.documentLengthK.get(w.entry.getId());
            System.out.print(w.weight);
            pw.print(w.entry.getId()+"\t");
            pw.print(w.weight);
            pw.print("\t");
            System.out.println(w.entry.getDescription());
            pw.print(w.entry.getId()+": ");
            pw.println(w.entry.getDescription());
            pw.println(w.entry.getTags());
            pw.println(id_domain.get(w.entry.getId()));
            for(String key:tfidf.keySet()){
                System.out.print(" "+key+" ");
                System.out.print(tfidf.get(key));
                pw.print(" "+key+" ");
                pw.print(tfidf.get(key)/docLenth);
            }
            System.out.print("\n---------------------\n");
            pw.print("\n---------------------\n");
        }
        pw.close();
        return res_sub;
    }
    public ArrayList<WeightedEntry> getMostSimilarEntriesToQueryTFIDF(String qs, String qs_domain, int k, String infile) throws Exception{
        PrintWriter pw = new PrintWriter(new FileOutputStream(infile));
        ArrayList<WeightedEntry> res = new ArrayList<>();
        ArrayList<WeightedEntry> res_sub = new ArrayList<>();
        LinkedList<String> origintokens = new LinkedList<>();
        origintokens = textparser.queryParser(qs);
        LinkedList<RankedString> tokens = new LinkedList<>();
        for(String w:origintokens){
            Double val = 0.0;
            if(CachedData.idf.get(w)!=null) {
                val = CachedData.idf.get(w);
            }
            tokens.add(new RankedString(w,val));
        }
        HashMap<String, Double> wordCount = new HashMap<>();

        for (RankedString w : tokens) {
            Double c = wordCount.get(w.getString());
            if (c == null) {
                wordCount.put(w.getString(), w.getRank());
            } else {
                //Double max_c = Math.max(c,w.getRank());
                //wordCount.put(w.getString(), max_c);
                wordCount.put(w.getString(), c + w.getRank());
            }
        }
        HashMap<String, Double> queryTfidt = new HashMap<>();
        Double queryLength = 0.0;
        for (String w : wordCount.keySet()) {
            Double val = wordCount.get(w);
            //val = (val-min_s)/(max_s-min_s); //do the min max scaler as the documents
            if (val != null) {
                //val *= 1.0 * wordCount.get(w) / max;
                queryLength += val * val;
                queryTfidt.put(w, val);
                System.out.print(" "+w+" ");
                pw.print(" "+w+" ");
                System.out.print(val);
                pw.print(val);
            }
            System.out.print("\n");
            pw.print("\n");
        }


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
             sim /= (queryLength*docLength); // calculate cos similarity
            Boolean domain_b = false;
            if (sim > 0) {
                HashSet domain =  id_domain.get(entry);
                if(domain_b && (!qs_domain.equals("null")) && domain!=null && domain.contains(qs_domain)) {
                    res.add(new WeightedEntry(CachedData.entryMap.get(entry), sim + 0.5));
                }
                else {
                    res.add(new WeightedEntry(CachedData.entryMap.get(entry), sim));
                }

            }
        }
        Collections.sort(res);
        if(res.size()>=k) {
            res_sub = new ArrayList<>(res.subList(0, k));
        }
        else{
            res_sub = new ArrayList<>(res);
        }
        HashSet<String> namelist = new HashSet<>();

        for (WeightedEntry w: res_sub){
            HashMap<String,Double> tfidf = CachedData.tfidt.get(w.entry.getId());
//            if(namelist.contains(w.entry.getName())) continue;
//            else{
//                namelist.add(w.entry.getName());
//            }
            Double docLenth = CachedData.documentLength.get(w.entry.getId());
            System.out.print(w.weight);
            pw.print(w.entry.getId()+"\t");
//            pw.print(w.weight);
//            pw.print("\t");
//            System.out.println(w.entry.getDescription());
//            pw.print(w.entry.getId()+": ");
//            pw.println(w.entry.getDescription());
//            pw.println(w.entry.getTags());
//            pw.println(id_domain.get(w.entry.getId()));
//            for(String key:tfidf.keySet()){
//                System.out.print(" "+key+" ");
//                System.out.print(tfidf.get(key));
//                pw.print(" "+key+" ");
//                pw.print(tfidf.get(key)/docLenth);
//            }
//            System.out.print("\n---------------------\n");
//            pw.print("\n---------------------\n");
        }
        pw.close();
        return res_sub;
    }
    public ArrayList<WeightedAbs> getMostSimilarAbstract(String qs, Integer k) throws JWNLException, IOException{
        PrintWriter pw = new PrintWriter(new FileOutputStream("src/main/data/search_proteomics data analysis pipeline.txt"));
        ArrayList<WeightedAbs> res = new ArrayList<>();
        ArrayList<WeightedAbs> res_sub = new ArrayList<>();
        LinkedList<String> origintokens = new LinkedList<>();
        origintokens = textparser.queryParser(qs);
        LinkedList<RankedString> tokens = textexpansion.queryExpansion(origintokens);
        HashMap<String, Double> wordCount = new HashMap<>();
        int max = 1;
        Double max_s = 0.0;
        Double min_s = 100.0;
        for (RankedString w : tokens) {
            //now calculating tf for words in query;
//            Integer c = wordCount.get(w.getString());
//            if (c == null) {
//                wordCount.put(w.getString(), 1);
//            } else {
//                wordCount.put(w.getString(), c + 1);
//                max = Math.max(max, c + 1);
//            }
//            Double c = wordCount.get(w.getString());
//            if( c == null){
//                wordCount.put(w.getString(),w.getRank());
//            }
//            else{
//                Double pre = wordCount.get(w.getString());
//                Double max_score = Math.max(pre,c);
//                wordCount.put(w.getString(),max_score);
//            }
            Double c = wordCount.get(w.getString());
            if (c == null) {
                wordCount.put(w.getString(), w.getRank());
            } else {
                wordCount.put(w.getString(), c + w.getRank());
            }
        }
        for(String w:wordCount.keySet()){
            Double val = wordCount.get(w)*AbsCachedData.idfK.get(w); // calculate the max score after multiple with idfk
            if(val>max_s){
                max_s = val;
            }
            if(val<min_s){
                min_s = val;
            }
        }
        HashMap<String, Double> queryTfidt = new HashMap<>();
        double queryLength = 0;

        for (String w : wordCount.keySet()) {
            Double val = wordCount.get(w);
            //val = (val-min_s)/(max_s-min_s); //do the min max scaler as the documents
            if (val != null) {
                val = val/max_s; //do the min max scaler as the documents
                val *= AbsCachedData.idfK.get(w);
                //val *= 1.0 * wordCount.get(w) / max;
                // the calculation methods have problem, log(w) will get to 0
                //val *= 1 + Math.log(wordCount.get(w)) / Math.log(2);
                queryLength += val * val;
                queryTfidt.put(w, val);
                System.out.print(" "+w+" ");
                System.out.print(val);
            }
            System.out.print("\n");
        }

        queryLength = Math.sqrt(queryLength);
        for (String entry : AbsCachedData.tfidtK.keySet()) {
            double docLength = AbsCachedData.documentLengthK.get(entry);
            HashMap<String, Double> row = AbsCachedData.tfidtK.get(entry);
            double sim = 0;
            for (String w : wordCount.keySet()) {
                Double val = row.get(w);
                if (val != null) {
                    sim += val * queryTfidt.get(w);
                }
            }
            //sim /= (queryLength * docLength); // calculate cos similarity
            sim /=(queryLength);
            if (sim > 0) {
                res.add(new WeightedAbs(AbsCachedData.entryMap.get(entry), sim));
            }
        }
        Collections.sort(res);
        if(res.size()>=k) {
            res_sub = new ArrayList<>(res.subList(0, k));
        }
        else{
            res_sub = new ArrayList<>(res);
        }

        for (WeightedAbs w: res_sub){
            HashMap<String,Double> tfidf = AbsCachedData.tfidtK.get(w.entry.getId());
            System.out.print(w.weight);
            pw.print(w.weight);
            pw.print("\t");
            System.out.println(w.entry.getDescription());

            pw.print(w.entry.getId()+": ");
            pw.println(w.entry.getDescription());
            for(String key:tfidf.keySet()){
                System.out.print(" "+key+" ");
                System.out.print(tfidf.get(key));
                pw.print(" "+key+" ");
                pw.print(tfidf.get(key));
            }
            System.out.print("\n---------------------\n");
            pw.print("\n---------------------\n");
        }
        pw.close();

        return res_sub;
    }

    public static void main(String[] args) throws Exception{
        Search handle = new Search();
        handle.loadMap();
        String infile = "src/main/data/search_result_v9_2/query.txt";
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        Integer count = 1;
        while((line = reader.readLine()) != null) {
            String query = line.split("\t")[0];
            String qs_domain = line.split("\t")[1];
            //String outfile = "src/main/data/search_result_TK3/search_" + query + ".txt";
            String outfile = "src/main/data/search_result_TK3/search_Q" + count.toString() + ".txt";

            //ArrayList<WeightedAbs> res = handle.getMostSimilarAbstract(query,10);
            ArrayList<WeightedEntry> res = handle.getMostSimilarEntriesToQuery(query, qs_domain,30, outfile);
            count+=1;
//        for (WeightedEntry r:res){
//            System.out.print(r.weight);
//            System.out.println(r.entry.getDescription());
//        }
        }
    }

}
