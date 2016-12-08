package edu.ucla.cs.scai.aztec.textexpansion;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import edu.ucla.cs.scai.aztec.keyphrase.Tokenizer;
import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.*;
/**
 * Created by Xinxin on 9/23/2016.
 */
public class SynonymFilter {
    private final static HashSet<String> vocab = new HashSet<>();
    private Tokenizer tk;
    public SynonymFilter() throws FileNotFoundException, JWNLException{
        tk = new Tokenizer();
    }
    public void loadVoc(String infile) throws FileNotFoundException, IOException{
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        while((line = reader.readLine()) != null){
           vocab.add(line.trim());
        }
        reader.close();
    }
    public ArrayList<String> removeDuplication (ArrayList<String> synlist){
        ArrayList<String> uniquelist = new ArrayList<>();

        return uniquelist;
    }
    public void synFilter1(String infile, String outfile) throws FileNotFoundException, IOException{
        // if one word of the list is in the vocabulary , then the entire list is added into the synfile;
        // May include words that are not included in the vocabulary
        PrintWriter writer = new PrintWriter(new FileOutputStream(outfile));
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        Integer count = 0;
        Integer success = 0;
        Set<String> synset;
        while((line = reader.readLine()) != null){
            synset = new HashSet<String>();
            boolean label = false;
            String[] words = line.trim().split(",");
            for(String word:words) {
                LinkedList<String> token = tk.tokenize(word);
                if (token.size() > 1) {
                    //if this item is a phrase
                    word = String.join("_", token);
                } else if (token.size() == 1) {
                    word = token.get(0);
                } else continue;
                if (vocab.contains(word)&&!label) {
                    label = true;
                }
                synset.add(word);
            }
            count+=1;
            if(label){
                writer.println(String.join(",",synset));
                success+=1;
            }
            if(count%10000 == 0){
                System.out.println(count);
            }
        }
        System.out.print("Final success item: ");
        System.out.println(success);
        writer.close();
    }
    public void synFilter2(String infile, String outfile) throws FileNotFoundException, IOException{
        // Build a synonym file based on our vocabulary. All the words in the synonym file is from our vocabulary
        PrintWriter writer = new PrintWriter(new FileOutputStream(outfile));
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        Integer count = 0;
        Integer success = 0;
        Set<String> synset;
        while((line = reader.readLine()) != null){
            synset = new HashSet<>();
            String[] words = line.trim().split(",");

            for(String word:words){
//                LinkedList<String> token = tk.tokenize(word);
//                if(token.size()>1){
//                    //if this item is a phrase
//                    word = String.join("_",token);
//                }
//                else if(token.size() == 1){
//                    word = token.get(0);
//                }
//                else continue;
                if(vocab.contains(word)){
                    synset.add(word);
                }
            }
            count+=1;
            if(!synset.isEmpty()){
                writer.println(String.join(",",synset));
                success+=1;
            }
            if(count%10000 == 0){
                System.out.println(count);
            }
        }
        System.out.print("Final success item: ");
        System.out.println(success);
        reader.close();
        writer.close();
    }
    public void MergeDupicationInRow(String infile,String outfile) throws IOException{ // simple merge, keep the first one of each line as the key
        PrintWriter writer = new PrintWriter(new FileOutputStream(outfile));
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        HashMap<String,HashSet<String>> synmap = new HashMap<>();
        String line;
        while((line = reader.readLine()) != null){
            String[] words = line.split(",");
            String key = words[0];
            if(synmap.containsKey(key)){
                for(Integer i = 1;i<words.length;i++){
                    synmap.get(key).add(words[i]);
                }
            }else{
                HashSet<String> synlist = new HashSet<>();
                for(Integer i=1;i<words.length;i++){
                    synlist.add(words[i]);
                }
                synmap.put(key,synlist);
            }
        }
        for(String key:synmap.keySet()){
            String write = key+","+String.join(",",synmap.get(key));
            writer.println(write);
        }
        reader.close();
        writer.close();
    }
    public static void main(String[] args) throws IOException, JWNLException{
        String vocfile = "D:/AztecSearch/Xinxin/data/word2vec_data/Synonyms/Vocabulary.txt";
        String infile = "D:/AztecSearch/Xinxin/data/word2vec_data/Synonyms/Synonyms2.txt";
        String outfile = "D:/AztecSearch/Xinxin/data/word2vec_data/Synonyms/Synonyms3.txt";
        SynonymFilter SF = new SynonymFilter();
        //SF.loadVoc(vocfile);
        //SF.synFilter2(infile,outfile);
        SF.MergeDupicationInRow(infile,outfile);

    }
}
