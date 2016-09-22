/**
 * Created by Xinxin on 7/29/2016.
 * This class is to parse sentence into the required format of word2vector.
 * Not used in our search engine.
 */
package edu.ucla.cs.scai.aztec.keyphrase;

import edu.ucla.cs.scai.aztec.textexpansion.TextParser;
import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.*;

public class ParseSentence {
    private final static HashSet<String> phraseList = new HashSet<>();
    public void loadData(String infile) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        while((line = reader.readLine()) != null){
            phraseList.add(line.trim());
        }
        reader.close();
    }
    public void writeData(HashSet<String> list, String outfile) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
        Iterator it = list.iterator();
        if(it.hasNext()){
            writer.write(it.next()+"\n");
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException, JWNLException{
        TextParser TP = new TextParser();
        String infile = "src/main/data/phraseList_Chi.txt";
        String senfile = "src/main/data/abstract_removeurl.txt";
        String outfile = "src/main/data/parsedSentence_for_train_Chi.txt";
        PrintWriter outString = new PrintWriter(outfile);
        ParseSentence parser = new ParseSentence();
        parser.loadData(infile);
        BufferedReader br = new BufferedReader(new FileReader(senfile));
        String line;
        String parserSen;
        Integer linecount = 0;
        while((line = br.readLine()) != null){
            StringBuilder parsedSen = new StringBuilder();
            for( String unit: TP.queryParser(line.trim())){
                parsedSen.append(" ").append(unit);
            }
//            String parsedSen = line.toLowerCase();
//            String[] sens = line.trim().replaceAll("[^A-Za-z0-9\\-\\s+]","\\|").toLowerCase().split("\\|");
//            for(Integer i=0;i<sens.length;i++){
//                //String sen = sens[i].replaceAll("-"," ");
//                String[] words = sens[i].trim().split("\\s+");
//                if (words.length>1){                            // if it possible to contain a phrase
//                    for (Integer j = 0;j<words.length-1;j++) {
//                        String phrase = words[j] + "_" + words[j + 1];
//                        if(phraseList.contains(phrase)){
//                            parsedSen = parsedSen.replace(words[j]+" "+words[j+1],phrase); // automatically merged phrases
//                            //System.out.println("parsed");
//                        }
//                    }
//                }
//            }
            outString.println(parsedSen);
            linecount++;
            if(linecount%1000 == 0){
                System.out.println(linecount/1000);
            }
        }

    }
}
