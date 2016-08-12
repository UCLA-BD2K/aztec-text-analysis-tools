package edu.ucla.cs.scai.aztec.textexpansion;

import edu.ucla.cs.scai.aztec.textexpansion.TextParser;
import edu.ucla.cs.scai.aztec.summarization.RankedString;
import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

/**
 * Created by Xinxin on 8/1/2016.
 * Input: unitRank list from textrank in List<RankedString> pairList= new ArrayList<>();
 * Output: List<RankedString> pairList= new ArrayList<>();
 */
public class TextExpansion {
    private final static HashSet<String> phraseList = new HashSet<>();
    private static Double confidence = 0.5;  // confidence of similar words
    private static Double sum_conf = 2.0; // total similar word weight = 2.0*original word weight
    private static Double conf_phrase = 0.8; // confidence of sub words in phrase
    private static Double min_sim = 0.7;  // min similarity score to be considered
    private static Integer max_num = 10; // max similar units to be considered
    private final static Map<String, List<RankedString>> similarity = new HashMap<>();



    public void loadData(String infile) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        while((line = reader.readLine()) != null){
            phraseList.add(line.trim());
        }
        reader.close();
    }

    public void loadMap(String infile) throws IOException{ // input format -- Key: simUnit1,score simUnit2,score simUnit3,score ....
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        while((line = reader.readLine()) != null){
            List<RankedString> simPair = new ArrayList<>();
            String key = line.trim().split(":")[0].trim();
            String simString = line.trim().split(":")[1].trim();
            String[] simList = simString.split(" ");
            for(String pair: simList){
                String unit = pair.split(",")[0].trim();
                Double score = Double.parseDouble(pair.split(",")[1].trim());
                simPair.add(new RankedString(unit,score));
            }
            similarity.put(key,simPair);
            //simPair.clear();
        }
        reader.close();
    }
    public TextExpansion() throws IOException{
        this.loadMap("src/main/data/SimilarityFile.txt");
    }

    public LinkedList<RankedString> queryExpansion (List<String> tokenQuery) throws IOException, JWNLException{
        //input format: [unit1,unit2,...]
        // if change to [<unit1,1.0>,<unit2 ,1.0> ... then we can use the same expansion function as document.
        //this.loadMap("src/main/data/SimilarityFile.txt");
        LinkedList<RankedString> expendedUnits = new LinkedList<>();
        List<RankedString> simList;
        Double querylen = 0.0;
        for(String unit: tokenQuery){
            LinkedList<RankedString> tmpunits = new LinkedList<>();
            expendedUnits.add(new RankedString(unit, 1.0));
            String[] winphrase = unit.split("_");
            if(winphrase.length>= 2) {
                for (String w : winphrase) { // only do this for origin units
                    Double w_score = 1.0 * conf_phrase;
                    expendedUnits.add(new RankedString(w, w_score));
                }
            }
            simList = similarity.get(unit); // only do this for origin tokens
            Integer sim_num = 0;
            Double sum_weight = 0.0;
            if(simList != null) {
                for (RankedString simUnit : simList) {
                    if (sim_num>=max_num) {
                        break;
                    }
                    if(simUnit.getRank()>min_sim) {
                        simUnit.setRank(simUnit.getRank() * confidence);
                        tmpunits.add(simUnit);
                        sim_num +=1;
                        sum_weight += simUnit.getRank();
                    }
                }
                for (RankedString simUnit:tmpunits){
                    simUnit.setRank(simUnit.getRank() * 1.0 * sum_conf / sum_weight);
                    expendedUnits.add(simUnit);
                }
            }
        }
        return expendedUnits;
    }

    public List<RankedString> docExpansion (List<RankedString> textrank) throws JWNLException, IOException{
        //this.loadMap("src/main/data/SimilarityFile.txt");
        // input format: [<unit1,score1>,<unit2,score2>...]
        List<RankedString> expendedUnits = new ArrayList<>();
        List<RankedString> simList;
        for(RankedString unit_score: textrank){
            LinkedList<RankedString> tmpunits = new LinkedList<>();
            expendedUnits.add(unit_score);
            String unit = unit_score.getString();
            Double score = unit_score.getRank();
            String[] winphrase = unit.split("_");
            if (winphrase.length >=2) {
                for (String w : winphrase) { // only do this for origin units
                    Double w_score = score * conf_phrase;
                    expendedUnits.add(new RankedString(w, w_score));
                }
            }
            Integer sim_num = 0;
            simList = similarity.get(unit);
            Double sum_weight = 0.0;
            if(simList != null) {
                for (RankedString simUnit : simList) {
                    if (sim_num>=max_num) {
                        break;
                    }
                    if(simUnit.getRank()>min_sim) {
                        simUnit.setRank(simUnit.getRank() * confidence);
                        tmpunits.add(simUnit);
                        sim_num +=1;
                        sum_weight += simUnit.getRank();
                    }
                }
                for (RankedString simUnit: tmpunits){
                    simUnit.setRank(simUnit.getRank() * score * sum_conf/ sum_weight);
                    expendedUnits.add(simUnit);
                }
            }
        }
        return expendedUnits;
    }

//    public List<String> docParser (String text) throws JWNLException,FileNotFoundException{
//        //parse document input is text, output is possibile keyphrase
//        Tokenizer token = new Tokenizer();
//        List<String> units = new LinkedList<String>();
//        if( text!=null){
//            LinkedList<String> words = token.tokenize(text);
//            // implement textrank here;
//            Integer order = 0;
//            if (words.size()>=2) {
//                for (Integer i = 0; i < words.size()-1; i++) {
//                    String phrase = words.get(i) + "_" + words.get(i + 1);
//                    if (phraseList.contains(phrase)) {
//                        units.add(order,phrase);
//                        order++;
//                        i++; // don't consider another possible phrases.
//                    }
//                    else{
//                        units.add(order,words.get(i));
//                        order++;
//                    }
//                }
//            }
//            else{
//                units.add(0,words.get(0));
//            }
//        }
//        return units;
//    }

//    public List<RankedString> queryExpansion(String query) throws JWNLException, FileNotFoundException {
//        TextExpansion te = new TextExpansion();
//        List<RankedString> expQuery = new ArrayList<RankedString>();
//        List<String> words = te.docParser(query);
//        for(String word:words) {
//            List<RankedString>  simUnits = te.Expansion(word);
//            expQuery.add(new RankedString("Hello", 1.0));
//            expQuery.addAll(simUnits);
//        }
//        return expQuery;
//    }

    public static void main(String[] args) throws IOException, JWNLException, ClassNotFoundException{
        TextExpansion TE = new TextExpansion();
        TextParser TP = new TextParser();
        String infile = "src/main/data/SimilarityFile.txt";
        TE.loadMap(infile);
        String test = "cluster gene";
        List<String> pq = TP.queryParser(test);
        List<RankedString> expendTest = TE.queryExpansion(pq);
        for (RankedString rs : expendTest ){
            System.out.println(rs);
        }
//        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("src/main/data/textrank.data"));
//        HashMap<String, List<RankedString>> keywords = (HashMap<String, List<RankedString>>) ois.readObject();
//        HashMap<String, List<RankedString>> expendedKeywords = new HashMap<>();
//        for (String id : keywords.keySet()){
//            List<RankedString> rankedToken = keywords.get(id);
//            List<RankedString> expendedToken = TE.docExpansion(rankedToken);
//            expendedKeywords.put(id,expendedToken);
//        }
//        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("src/main/data/expendedKeywords.data"))) {
//            out.writeObject(expendedKeywords);
//        }
    }
}
