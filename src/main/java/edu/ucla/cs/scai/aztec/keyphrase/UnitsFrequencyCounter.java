/**
 * Created by Xinxin on 7/28/2016.
 * Now can generate both words and phrases;
 */
package edu.ucla.cs.scai.aztec.keyphrase;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import edu.ucla.cs.scai.aztec.keyphrase.Tokenizer;
import net.sf.extjwnl.JWNLException;
import java.io.*;
import java.util.*;
import java.util.regex.*;


public class UnitsFrequencyCounter {
    public Map<String, Integer> wordCount = new HashMap<String, Integer>();     //Creates an Hash Map for storing the words and its count
    public Map<String, Integer> phrase2Count = new HashMap<String, Integer>();
    public Map<String, Integer> phrase3Count = new HashMap<>();

    public void countWord(LinkedList<String> words, Integer delta){
        // if delta == 0, count all frequency.
        // delta == 1 ignore the last one (count as left word)
        // delat == -1 ignore the first one (count as right word)
        Integer startidx = 0;
        Integer endidx = words.size();
        if(delta == 1){
            endidx -=1;
        }
        if(delta == -1){
            startidx +=1;
        }
        Integer total = words.size();
        List subwords = words.subList(0,total-delta);
        for (Integer i = startidx;i<endidx;i++) {
            String read = words.get(i);
            if (!(Pattern.matches("(?!-)\\p{Punct}", read))) {
                //System.out.println(read);
                read = read.trim();
                Integer freq = wordCount.get(read);
                wordCount.put(read, (freq == null) ? 1 : freq + 1); //For Each word the count will be incremented in the Hashmap
            }
        }
    }

    public void countPhrase2(LinkedList<String> words) {
        Integer i = 0;
        Integer final_index = words.size()-1;
        while (i<final_index) {
            String w1 = words.get(i);
            String w2 = words.get(i+1);
            if(!(Pattern.matches("(?!-)\\p{Punct}",w2))){
                String phrase = w1 + "_" + w2;
                Integer freq = phrase2Count.get(phrase);
                phrase2Count.put(phrase, (freq == null) ? 1 : freq + 1); //For Each phrases the count will be incremented in the Hashmap
                i += 1;
            }
            else{
                i += 2;
            }
        }
    }
    public void countPhrase3(LinkedList<String> words) {
        Integer i = 0;
        Integer final_index = words.size()-2;
        if(final_index>0) {
            if (Pattern.matches("(?!-)\\p{Punct}", words.get(1))) {
                i = 2;
            }
            while (i < final_index) {
                String w1 = words.get(i);
                String w2 = words.get(i + 1);
                String w3 = words.get(i + 2);
                if (!(Pattern.matches("(?!-)\\p{Punct}", w3))) {
                    String phrase = w1 + "_" + w2 + "_" + w3;
                    Integer freq = phrase3Count.get(phrase);
                    phrase3Count.put(phrase, (freq == null) ? 1 : freq + 1); //For Each phrases the count will be incremented in the Hashmap
                    i += 1;
                } else {
                    i += 3;
                }
            }
        }
    }
    public void writeFrequency(String name) throws IOException{
        Map<String, Integer> freqCount = new HashMap<>();
        if(name == "word") {
            freqCount = this.wordCount;
        }
        if(name == "phrase2"){
            freqCount = this.phrase2Count;
        }
        if(name == "phrase3"){
            freqCount = this.phrase3Count;
        }
        Properties properties = new Properties();
        for (Map.Entry<String,Integer> entry : freqCount.entrySet()) {
            properties.put(entry.getKey(), Integer.toString(entry.getValue()));
        }
        try{
            File fileOne=new File("src/main/data/"+name+"Frequency");
            FileOutputStream fos=new FileOutputStream(fileOne);
            properties.store(fos, null);
            fos.close();
        }catch (NullPointerException | IOException e) {
            System.out.println("I can't write the file:" + e);
        }
    }

    public static void main(String[] args) throws JWNLException,FileNotFoundException{
        Tokenizer token = new Tokenizer();
        UnitsFrequencyCounter UFcounter = new UnitsFrequencyCounter();
        String fileName = "src/main/data/abstract_removeurl.txt";
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader br = new BufferedReader(fileReader);
            //String words[] = null;
            LinkedList<String> words = new LinkedList<String>();
            String line;
            Integer linecount = 0;
            while((line = br.readLine()) != null) {
                //words = line.replaceAll("\\p{P}", " ").toLowerCase().split("\\s+");
                //words = line.replaceAll("(?!-)\\p{P}", " ").toLowerCase().split("\\s+");// remove punctuation except -
                //words = line.replaceAll("[^A-Za-z0-9\\-\\+]"," ").toLowerCase().split("\\s+"); // only keep words and digital
                words = token.tokenize(line);
////                    if(read.split("-").length > 1) {
////                        for (String subword : read.split("-")) {
////                            freq = wordCount.get(subword);
////                            wordCount.put(subword, (freq == null) ? 1 : freq + 1); //For Each word the count will be incremented in the Hashmap
////                        }
////                    }
//                }
                UFcounter.countWord(words,0);
                UFcounter.countPhrase2(words);
                UFcounter.countPhrase3(words);
                linecount+=1;
                if ((linecount%300) == 0 ){
                    System.out.println(linecount/300);
                }
            }
            br.close();
            System.out.println(UFcounter.wordCount.size() + " distinct words;");     //Prints the Number of Distinct words found in the files read
            System.out.println(UFcounter.phrase2Count.size() + " possible phrases;");
            System.out.println(UFcounter.phrase3Count.size() + " possible phrases;");
            UFcounter.writeFrequency("word");
            UFcounter.writeFrequency("phrase2");
            UFcounter.writeFrequency("phrase3");
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }
    }
}



