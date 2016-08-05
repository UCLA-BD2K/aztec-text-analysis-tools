/**
 * Created by Xinxin on 7/28/2016.
 * Not in use now
 */
package edu.ucla.cs.scai.aztec.keyphrase;
import edu.ucla.cs.scai.aztec.keyphrase.Tokenizer;
import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.*;


public class WordCooccurrence {

    public static void main(String[] args)throws JWNLException,IOException{
        Tokenizer token = new Tokenizer();
        String fileName = "data/abstract_all.txt";
        FileReader fileReader =
                new FileReader(fileName);
        // Always wrap FileReader in BufferedReader.
        BufferedReader br =
                new BufferedReader(fileReader);
        LinkedList<String> words = new LinkedList<String>();
        String sentences[] = null;
        String line;
        String phrase;
        Map<String, Integer> phraseCount = new HashMap<String, Integer>();     //Creates an Hash Map for storing the words and its count
        Integer linecount = 0;
        while((line = br.readLine()) != null) {
            //words = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
            //sentences = line.replaceAll("(?!-)\\p{P}", "\\|").toLowerCase().split("\\|");
            sentences = line.replaceAll("[^A-Za-z0-9\\-\\s+]","\\|").toLowerCase().split("\\|");
            for (String sen : sentences){
                //sen = sen.replaceAll("-"," ");
                //words = sen.trim().split("\\s+");
                words = token.tokenize(line);
                if (words.size()>1){                            // if it possible to contain a phrase
                    Integer i = 0;
                    while (i<words.size()-1) {
                        phrase = words.get(i) + "_" + words.get(i+1);
                        Integer freq = phraseCount.get(phrase);
                        phraseCount.put(phrase, (freq == null) ? 1 : freq + 1); //For Each phrases the count will be incremented in the Hashmap
                        i += 1;
                    }
                }
            }
            linecount+=1;
            if ((linecount%500) == 0 ){
                System.out.println(linecount/500);
            }

        }
        br.close();
        System.out.println(phraseCount.size() + " distinct words:");     //Prints the Number of Distinct words found in the files read
        //System.out.println(wordCount);
        Properties properties = new Properties();

        for (Map.Entry<String,Integer> entry : phraseCount.entrySet()) {
            properties.put(entry.getKey(), Integer.toString(entry.getValue()));
        }
        try{
            File fileOne=new File("data/phraseFrequency_keep");
            FileOutputStream fos=new FileOutputStream(fileOne);
            properties.store(fos, null);
            fos.close();
        }catch (NullPointerException | IOException e) {
            System.out.println("I can't write the file:" + e);
        }
    }
}
