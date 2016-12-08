package edu.ucla.cs.scai.aztec.textexpansion;

import com.sun.javaws.exceptions.JNLParseException;
import edu.ucla.cs.scai.aztec.keyphrase.Tokenizer;
//import edu.ucla.cs.scai.aztec.similarity.Tokenizer;
import edu.ucla.cs.scai.aztec.utils.StringUtils;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.list.Node;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.regex.Pattern;

/**
 * Created by Xinxin on 8/3/2016.
 */
public class TextParser{
    private final static HashSet<String> phraselist = new HashSet<>();
    private final static HashSet<String> stopwords = new HashSet<>();
    private static Integer max_phrase = 5;
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
//    public TextParser() throws JWNLException, FileNotFoundException{
//        Tokenizer token = new Tokenizer();
//    }
    public void loadData(String infile) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        while((line = reader.readLine()) != null){
            phraselist.add(line.trim());
        }
        reader.close();
    }
    public TextParser() throws IOException{
        this.loadData("src/main/data/phraseList_Chi.txt");
    }
    public LinkedList<String> queryParser_old(String text) throws JWNLException, IOException{
        LinkedList<String> unitList = new LinkedList<>();
        //String infile = "src/main/data/phraseList_20.txt";
        //TextParser parser = new TextParser();
        Tokenizer token = new Tokenizer();
        //this.loadData(infile);
        LinkedList<String> words = token.tokenize(text);
        String phrase;
        if(words.size()>1) {
            Integer final_idx = words.size() - 2;
            Integer i = 0;
            while(i < final_idx) {
                phrase = words.get(i)+"_"+words.get(i+1)+"_"+words.get(i+2); // first check three words phrase
                if(phraselist.contains(phrase)){
                    unitList.add((phrase));
                    i += 3; //skip all following words contained in phrase
                }
                else{
                    phrase = words.get(i)+"_"+words.get(i+1);
                    if (phraselist.contains(phrase)){
                        unitList.add(phrase);
                        i += 2;
                    }
                    else{
                        unitList.add(words.get(i));
                        i++;
                    }
                }
            }
            while(i<final_idx+1) { // check the last few words.
                phrase = words.get(i) + "_" + words.get(i + 1);
                if (phraselist.contains(phrase)) {
                    unitList.add(phrase);
                    i += 2;
                } else { // if not phrase, add as two separate words
                    unitList.add(words.get(i));
                    i++;
                }
            }
            while(i<final_idx+2){
                unitList.add(words.get(i));
                i++;
            }

        }
        else{
            unitList.add(words.get(0));
        }
        return  unitList;
    }
    public LinkedList<String> queryParser(String text) throws JWNLException, IOException{
        LinkedList<String> unitList = new LinkedList<>();
        //String infile = "src/main/data/phraseList_20.txt";
        //TextParser parser = new TextParser();
        Tokenizer token = new Tokenizer();
        //this.loadData(infile);
        LinkedList<String> words = token.tokenize(text);
        //String[] words = text.split(" ");
        ArrayList<String> next_words = new ArrayList<>();
        String phrase;
        Integer win_size = max_phrase;
        //Integer this_length = words.size();
        ArrayList<String> this_words = new ArrayList<>();
        for(String w:words){
            this_words.add(w);
        }
        while(win_size>=1){
            Integer start_pos = 0;
            Integer end_pos = start_pos+win_size-1;
            while(end_pos<this_words.size()){
                List<String>subphrase = this_words.subList(start_pos, end_pos+1);
                String sub_phrase = String.join("_",subphrase);
                if(phraselist.contains(sub_phrase)){
                    next_words.add(sub_phrase);
                    start_pos = end_pos+1;
                }
                else{
                    next_words.add(this_words.get(start_pos));
                    start_pos++;
                }
                end_pos = start_pos+win_size-1;
            }
            if(start_pos<this_words.size()) {
                next_words.addAll(this_words.subList(start_pos, this_words.size()));
            }
            this_words = new ArrayList<>(next_words);
            next_words = new ArrayList<>();
            win_size--;
        }
        for(String w:this_words){
            unitList.add(w);
        }
//        if(words.size()>1) {
//            Integer final_idx = words.size() - 2;
//            Integer i = 0;
//            while(i < final_idx) {
//                phrase = words.get(i)+"_"+words.get(i+1)+"_"+words.get(i+2); // first check three words phrase
//                if(phraselist.contains(phrase)){
//                    unitList.add((phrase));
//                    i += 3; //skip all following words contained in phrase
//                }
//                else{
//                    phrase = words.get(i)+"_"+words.get(i+1);
//                    if (phraselist.contains(phrase)){
//                        unitList.add(phrase);
//                        i += 2;
//                    }
//                    else{
//                        unitList.add(words.get(i));
//                        i++;
//                    }
//                }
//            }
//            while(i<final_idx+1) { // check the last few words.
//                phrase = words.get(i) + "_" + words.get(i + 1);
//                if (phraselist.contains(phrase)) {
//                    unitList.add(phrase);
//                    i += 2;
//                } else { // if not phrase, add as two separate words
//                    unitList.add(words.get(i));
//                    i++;
//                }
//            }
//            while(i<final_idx+2){
//                unitList.add(words.get(i));
//                i++;
//            }
//
//        }
//        else{
//            unitList.add(words.get(0));
//        }
        return  unitList;
    }

    public LinkedList<String> docParser(String doc) throws JWNLException, IOException {
        LinkedList<String> unitList = new LinkedList<>();
        LinkedList<String> tmpunitList = new LinkedList<>();
        tmpunitList = this.queryParser(doc);
        for (String unit:tmpunitList){ // remove single punctuation unit
            if((!(Pattern.matches("(?!-_)\\p{Punct}", unit))) && (!stopwords.contains(unit))){
                unitList.add(unit);
            }
        }
        return  unitList;
    }
    public static void main(String[] args) throws JWNLException, IOException{
        String test = "metabolomics database";
        TextParser TP = new TextParser();
        LinkedList<String> units = TP.docParser(test);
        System.out.print(units);
    }
}
