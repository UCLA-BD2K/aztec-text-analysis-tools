/**
 * Created by Xinxin on 7/28/2016.
 * This class is a filter to get all high quality phrases
 * Input: possible phrases and frequency, word frequency.
 * Output: high quality phrases.
 */
package edu.ucla.cs.scai.aztec.keyphrase;
import com.sun.xml.internal.ws.client.sei.ResponseBuilder;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.lang.Math.*;
import java.util.regex.*;

public class PhraseFilter {
    Map<String, Integer> wordCount = new HashMap<String, Integer>();
    Map<String, Integer> phraseCount = new HashMap<String, Integer>();
    Map<String, Integer> phrase3Count = new HashMap<String, Integer>();
    Map<String, Double> phraseProb = new HashMap<String, Double>();
    LinkedList<String> phraseList3 = new LinkedList<>();
    Double threshold2_bot = 0.025;
    Double threshold2_up = 1.0;
    Double min_sup2 = 20.0;
    Double threshold3 = 0.0;
    Double min_sup3 = 20.0;
    Integer wordthreshold  = 4;
    Double t_threshold = 0.0;
    private final static HashSet<String> stopwords = new HashSet<>();
    static {
        String s = "a\n"
                + "about\n"
                + "above\n"
                + "after\n"
                + "again\n"
                + "against\n"
                + "all\n"
                + "also\n"
                + "am\n"
                + "an\n"
                + "and\n"
                + "any\n"
                + "are\n"
                + "aren't\n"
                + "as\n"
                + "at\n"
                + "be\n"
                + "because\n"
                + "been\n"
                + "before\n"
                + "being\n"
                + "below\n"
                + "between\n"
                + "both\n"
                + "but\n"
                + "by\n"
                + "can\n"
                + "can't\n"
                + "cannot\n"
                + "could\n"
                + "couldn't\n"
                + "did\n"
                + "didn't\n"
                + "do\n"
                + "does\n"
                + "doesn't\n"
                + "doing\n"
                + "don't\n"
                + "down\n"
                + "during\n"
                + "each\n"
                + "few\n"
                + "for\n"
                + "from\n"
                + "further\n"
                + "had\n"
                + "hadn't\n"
                + "has\n"
                + "hasn't\n"
                + "have\n"
                + "haven't\n"
                + "having\n"
                + "he\n"
                + "he'd\n"
                + "he'll\n"
                + "he's\n"
                + "her\n"
                + "here\n"
                + "here's\n"
                + "hers\n"
                + "herself\n"
                + "him\n"
                + "himself\n"
                + "his\n"
                + "how\n"
                + "how's\n"
                + "i\n"
                + "i'd\n"
                + "i'll\n"
                + "i'm\n"
                + "i've\n"
                + "if\n"
                + "in\n"
                + "into\n"
                + "is\n"
                + "isn't\n"
                + "it\n"
                + "it's\n"
                + "its\n"
                + "itself\n"
                + "let's\n"
                + "me\n"
                + "many\n"
                + "make\n"
                + "more\n"
                + "most\n"
                + "mustn't\n"
                + "my\n"
                + "myself\n"
                + "no\n"
                + "nor\n"
                + "not\n"
                + "of\n"
                + "off\n"
                + "on\n"
                + "once\n"
                + "only\n"
                + "or\n"
                + "other\n"
                + "ought\n"
                + "our\n"
                + "ours	ourselves\n"
                + "out\n"
                + "over\n"
                + "own\n"
                + "same\n"
                + "shan't\n"
                + "she\n"
                + "she'd\n"
                + "she'll\n"
                + "she's\n"
                + "should\n"
                + "shouldn't\n"
                + "so\n"
                + "some\n"
                + "such\n"
                + "than\n"
                + "that\n"
                + "that's\n"
                + "the\n"
                + "their\n"
                + "theirs\n"
                + "them\n"
                + "themselves\n"
                + "then\n"
                + "there\n"
                + "there's\n"
                + "these\n"
                + "they\n"
                + "they'd\n"
                + "they'll\n"
                + "they're\n"
                + "they've\n"
                + "this\n"
                + "those\n"
                + "through\n"
                + "to\n"
                + "too\n"
                + "under\n"
                + "until\n"
                + "up\n"
                + "very\n"
                + "was\n"
                + "wasn't\n"
                + "we\n"
                + "we'd\n"
                + "we'll\n"
                + "we're\n"
                + "we've\n"
                + "were\n"
                + "weren't\n"
                + "what\n"
                + "what's\n"
                + "when\n"
                + "when's\n"
                + "where\n"
                + "whereas\n"
                + "where's\n"
                + "which\n"
                + "while\n"
                + "who\n"
                + "who's\n"
                + "whom\n"
                + "why\n"
                + "why's\n"
                + "with\n"
                + "won't\n"
                + "would\n"
                + "wouldn't\n"
                + "you\n"
                + "you'd\n"
                + "you'll\n"
                + "you're\n"
                + "you've\n"
                + "your\n"
                + "yours\n"
                + "yourself\n"
                + "yourselves\n'";
        stopwords.addAll(Arrays.asList(s.split("\\\n")));
        stopwords.add("");
    }

    public Double[] confidence_support(String phrase){
        Double prob = 0.0;
        Double[] conf_sup = {0.0, 0.0};
        String words[] = phrase.split("_");
        Integer phraseNum = phraseCount.get(phrase);
        try {
            Integer word1Num = wordCount.get(words[0]);
            //Integer word2Num = wordCount.get(words[1]);
            prob = (double) (phraseNum) / (double)(word1Num);
            conf_sup[0] = prob;
            conf_sup[1] = (double)(phraseNum);
        }catch(NullPointerException e){
            System.out.println("I can't find word: "+phrase);
        }
        return conf_sup;
    }

    public Double[] confidence_support_2(String phrase){
        Double prob = 0.0;
        Double[] conf_sup = new Double[2];
        String words[] = phrase.split("_");
        Integer phraseNum = phraseCount.get(phrase);
        try {
            //Integer word1Num = wordCount.get(words[0]);
            Integer word2Num = wordCount.get(words[1]);
            prob = (double) (phraseNum) / (double)(word2Num);
            conf_sup[0] = prob;
            conf_sup[1] = (double)(phraseNum);
        }catch(NullPointerException e){
            System.out.println("I can't find word: "+phrase);
        }
        return conf_sup;
    }

    public Double[] confidence_support_3(String phrase){
        Double prob = 0.0;
        Double[] conf_sup = {0.0,0.0};
        String words[] = phrase.split("_");
        Integer phraseNum = phraseCount.get(phrase);
        try {
            Integer word1Num = wordCount.get(words[0]);
            Integer word2Num = wordCount.get(words[1]);
            //Integer minNum = Math.min(word1Num,word2Num);
            prob = (double) (phraseNum) / (double) (Math.min(word1Num, word2Num));
            conf_sup[0] = prob;
            conf_sup[1] = (double) (phraseNum);
        }catch(NullPointerException e){
            System.out.println("I can't find word: "+phrase);
        }
        return conf_sup;
    }

    public Double[] confidence_support_4(String phrase){
        Double prob = 0.0;
        Double[] conf_sup = {0.0,0.0};
        String words[] = phrase.split("_");
        try {
            Integer phraseNum = phrase3Count.get(phrase);
            Integer prephraseNum = phraseCount.get(words[0]+"_"+words[1]);
            Integer sufphraseNum = phraseCount.get(words[1]+"_"+words[2]);
            prob = (double) (phraseNum) / (double)Math.min(prephraseNum,sufphraseNum);
            conf_sup[0] = prob;
            conf_sup[1] = (double)(phraseNum);
        }catch(NullPointerException e){
            System.out.println("I can't find word: "+phrase);
        }
        return conf_sup;
    }

    public Double[] t_test(String phrase){
        Double phrase_prob = 0.0;
        Double null_hypo_mean = 0.0;
        Double[] t_value_sup = new Double[2];
        String words[] = phrase.split("_");
        Integer phraseNum = phraseCount.get(phrase);
        try{
            Integer word1Num = wordCount.get(words[0]);
            Integer word2Num = wordCount.get(words[1]);
            phrase_prob = (double) phraseNum/ phraseCount.size();
            null_hypo_mean = (double)(word1Num*word2Num)/(double)(wordCount.size()*wordCount.size());
            t_value_sup[0] = Math.abs(phrase_prob-null_hypo_mean)/(Math.sqrt(phrase_prob/wordCount.size()));
            t_value_sup[1] = (double)(phraseNum);
        }catch(NullPointerException e){
            System.out.println("I can't find word: "+phrase);
        }
        return t_value_sup;
    }

    public boolean ifStopWords(String phrase){
        String words[] = phrase.split("_");
        for (String word:words){
            if (stopwords.contains(word)){
                return true;
            }
        }
        return false;
    }

    public boolean ifNotCharacter(String phrase){
        String words[] = phrase.split("_");
        for(String word:words){
            if((word).matches("^[0-9,.]+$")){
                return true;
            }
        }
        return false;
    }

    public boolean ifSpecialCharacter(String phrase){ // if contains return true now: hope if all contains return true
        String words[] = phrase.split("_");
        for (String word:words){
            if(word.replaceAll("[^A-Za-z0-9]"," ").trim().isEmpty()){
                return true;
            }
        }
        return false;
    }

    public boolean validPhrase(String phrase){
        PhraseFilter pf = new PhraseFilter();
        if(!(pf.ifNotCharacter(phrase) || pf.ifStopWords(phrase) || pf.ifSpecialCharacter(phrase))){
            return true;
        }
        else{
            return false;
        }
    }

    public void selectPhrase() throws IOException, NullPointerException {
        String infile = "src/main/data/wordFrequency";
        String infile2 = "src/main/data/phrase2Frequency";
        Properties properties = new Properties();
        try{
            properties.load(new FileInputStream(infile));

            for (String key : properties.stringPropertyNames()) {
                wordCount.put(key, Integer.valueOf(properties.get(key).toString()));
            }
            properties.clear();
            properties.load(new FileInputStream(infile2));
            for (String key : properties.stringPropertyNames()) {
                phraseCount.put(key, Integer.valueOf(properties.get(key).toString()));
            }
        }catch(NullPointerException | IOException e){
            System.out.println("I can't read the file");
        }
        Integer linecount = 0;
        try{
            PrintWriter outString = new PrintWriter("src/main/data/invalid_phrase.txt");
            PrintWriter outPhrase = new PrintWriter("src/main/data/phrase2List_20.txt");
            for ( String phrase : phraseCount.keySet() ) {
                if (validPhrase(phrase)){
                    Double[] conf_sup;
                    conf_sup = confidence_support_3(phrase);
                    //Double[] t_value_sup;
                    //t_value_sup = t_test(phrase);
                    //Double[] conf_sup_2 = confidence_support_2(phrase);
                    //if ((conf_sup[0] > threshold || conf_sup_2[0] > threshold) && conf_sup[1] > min_sup) {
                    if(conf_sup[0]>= threshold2_bot &&conf_sup[0]< threshold2_up && conf_sup[1]>= min_sup2){
                        //phraseProb.put(phrase, Math.max(conf_sup[0],conf_sup_2[0]));
                        phraseProb.put(phrase,conf_sup[0]);
                        outPhrase.println(phrase);
                    }
//                    if(t_value_sup[0]> threshold && t_value_sup[1]> min_sup){
//                        //phraseProb.put(phrase, Math.max(conf_sup[0],conf_sup_2[0]));
//                        phraseProb.put(phrase,t_value_sup[0]);
//                        outPhrase.println(phrase);
//                    }
                }
                else{
                    outString.println(phrase);
                }
//                linecount+=1;
//                if ((linecount%3000) == 0 ){
//                    System.out.println(linecount/3000);
//                }
            }
            System.out.println(linecount);
            System.out.println(phraseProb.size() + " distinct phrases:");     //Prints the Number of Distinct words found in the files read
            //System.out.println(wordCount);
            outString.close();
            outPhrase.close();

        }catch(IOException e) {
            System.out.println("I can't write test file");
        }
        properties = new Properties();

        for (Map.Entry<String,Double> entry : phraseProb.entrySet()) {
            properties.put(entry.getKey(), Double.toString(entry.getValue()));
        }
        try{
            File fileOne=new File("src/main/data/phraseProbability_sup_20_c_min_0.025_max_1");
            FileOutputStream fos=new FileOutputStream(fileOne);
            properties.store(fos, null);
            fos.close();
        }catch (NullPointerException | IOException e) {
            System.out.println("I can't write the file:" + e);
        }
        properties.clear();
        for (String key : phraseProb.keySet()) {
            properties.put(key, Double.toString(phraseCount.get(key)));
        }
        try{
            File fileOne=new File("src/main/data/phraseFrequency_sup_20_c_min_0.025_max_1");
            FileOutputStream fos=new FileOutputStream(fileOne);
            properties.store(fos, null);
            fos.close();
        }catch (NullPointerException | IOException e) {
            System.out.println("I can't write the file:" + e);
        }
    }
    public void mergePhrase() throws IOException{
        String infile = "src/main/data/phrase2List_20.txt";
        BufferedReader br = new BufferedReader(new FileReader(infile));
        Map<String,HashSet<String>> phraseList2 = new HashMap<>();
        String line;
        String phrase3;
        HashSet<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            String[] words = line.split("_");
            set = phraseList2.get(words[0]);
            if(set == null){
                set = new HashSet<>();
            }
            set.add(words[1]);
            phraseList2.put(words[0],set);
        }
        HashSet<String> keySet = new HashSet<String>(phraseList2.keySet());
        for(String key: keySet){
            set = phraseList2.get(key);
            for(String word:set){
                if(keySet.contains(word)){
                    for (String rightword: phraseList2.get(word)) {
                        phrase3 = key + "_" + word + "_"+ rightword;
                        phraseList3.add(phrase3);
                    }
                }
            }
        }
    }

    public void selectPhrase3() throws IOException{
        String infile = "src/main/data/phrase3Frequency";
        String outfile = "src/main/data/phrase3List_20";
        PrintWriter outString = new PrintWriter("src/main/data/invalid_phrase_3.txt");
        PrintWriter outPhrase = new PrintWriter(outfile);
        Properties properties = new Properties();
        try{
            properties.load(new FileInputStream(infile));

            for (String key : properties.stringPropertyNames()) {
                phrase3Count.put(key, Integer.valueOf(properties.get(key).toString()));
            }
            properties.clear();
        }catch(NullPointerException | IOException e){
            System.out.println("I can't read the file");
        }
        for ( String phrase : phraseList3 ) {
            if (validPhrase(phrase)) {
                Double[] conf_sup;
                conf_sup = confidence_support_4(phrase);
                //Double[] t_value_sup;
                //t_value_sup = t_test(phrase);

                //if ((conf_sup[0] > threshold || conf_sup_2[0] > threshold) && conf_sup[1] > min_sup) {
                if(conf_sup[0]> threshold3 && conf_sup[1]> min_sup3){
                    phraseProb.put(phrase,conf_sup[0]);
                    outPhrase.println(phrase+" "+ Double.toString(conf_sup[1]));
                }

            } else {
                outString.println(phrase);
            }
        }
        outPhrase.close();
    }

    public void writePhraseList() throws IOException{
        String outfile = "src/main/data/phraseList_20.txt";
        PrintWriter outString = new PrintWriter(outfile);
        for(String key:phraseProb.keySet()){
            outString.println(key);
        }
        outString.close();
    }
    public void writeProperties() throws IOException{
        Properties properties = new Properties();
        for (Map.Entry<String,Double> entry : phraseProb.entrySet()) {
            properties.put(entry.getKey(), Double.toString(entry.getValue()));
        }
        try{
            File fileOne=new File("src/main/data/phraseProbability_all_20");
            FileOutputStream fos=new FileOutputStream(fileOne);
            properties.store(fos, null);
            fos.close();
        }catch (NullPointerException | IOException e) {
            System.out.println("I can't write the file:" + e);
        }
    }

    public static void main(String[] args) throws IOException{
        PhraseFilter PF = new PhraseFilter();
        System.out.println("Selecting bigram phrases");
        PF.selectPhrase();
        PF.wordCount.clear();
        System.out.println("Generate possible trigram phrases for bigram phrases");
        PF.mergePhrase();
        System.out.println("Selecting trigram phrases");
        PF.selectPhrase3();
        System.out.println("Writing all high quality phrases into file");
        PF.writePhraseList();
        PF.writeProperties();

    }
}
