package edu.ucla.cs.scai.aztec.textexpansion;

import edu.stanford.nlp.ling.tokensregex.types.Expressions;
import edu.ucla.cs.scai.aztec.similarity.AbsCachedData;
import edu.ucla.cs.scai.aztec.textexpansion.TextParser;
import edu.ucla.cs.scai.aztec.summarization.RankedString;
import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

/**
 * Created by Xinxin on 8/1/2016.
 * Input: unitRank list from textrank in List<RankedString> pairList= new ArrayList<>();
 * Output: List<RankedString> pairList= new ArrayList<>();
 */
public class TextExpansion {
    private final static HashSet<String> phraseList = new HashSet<>();
    private static Double confidence = 0.5;  // confidence of similar words
    private static Double sum_sim_conf = 2.0; // total similar word weight = 2.0*original word weight, should design a better weight schema
    private static Double sum_sub_conf = 1.0; // total sub sequence weight = 2.0*original word weight
    private static Double conf_phrase = 0.5; // confidence of sub words in phrase
    private static Double min_sim = 0.7;  // min similarity score to be considered
    private static Integer max_num = 10; // max similar units to be considered
    private static Integer vec_size = 200; // length of each vector in Hash Map
    private final static Map<String, ArrayList<Float>> word2vec = new HashMap<>();
    private final static Map<String, List<RankedString>> similarity = new HashMap<>();



    public void loadData(String infile) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        while((line = reader.readLine()) != null){
            phraseList.add(line.trim());
        }
        reader.close();
    }
    public void loadBinData(String infile) throws IOException{
        Integer max_w = 50;
        Integer words = 26066;
        File file = new File(infile);
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(fileData);
        dis.close();
        Integer pos = 10; // first 10 characters are 2 6 0 6 6 2 0 0 \n it is the word number and vector length
        while (pos < fileData.length - 4) {
            String word = "";
            String c = "";
            while (!" ".equals(c)) {
                if (!("\n".equals(c))) {
                    word = word + c;
                }
                c = Character.toString((char)fileData[pos]);
                    //byte[] chars = Arrays.copyOfRange(fileData, pos, pos + 1);
                    //Character c = ByteBuffer.wrap(chars).asCharBuffer().read();
                    //String c = StandardCharsets.UTF_16.decode(ByteBuffer.wrap(chars)).toString();
                pos += 1;
            }
            ArrayList<Float> vec = new ArrayList<>();
            for (Integer j = 0; j < vec_size; j++) {
                byte[] nums = Arrays.copyOfRange(fileData, pos, pos + 4);
                Float num = ByteBuffer.wrap(nums).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                vec.add(num);
                pos += 4;
            }
            Float len = 0.0f;
            for(Float f:vec){
                len = len+f*f;
            }
            len = (float)Math.sqrt(len);
            for(int a = 0; a< vec.size();a++){
                vec.set(a,vec.get(a)/len);
            }
            word2vec.put(new String(word), vec);
        }
    }


    public Double termDistance(String term1, String term2){
        Float sum = 0.0f;
        ArrayList<Float> vec1 = word2vec.get(term1);
        ArrayList<Float> vec2 = word2vec.get(term2);
        if(vec1 != null && vec2 !=null) {
            for (Integer i = 0; i < vec_size; i++) {
                sum += vec1.get(i)*vec2.get(i);
            }
        }
        return (double)(sum);
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
        this.loadData("src/main/data/phraseList_Chi.txt");
        this.loadBinData("src/main/data/vectors-phrase-abstract.bin");
    }
    public LinkedList<RankedString> subPhraseExpansion (String unit,Double word_score){
        String[] winphrase = unit.split("_");
        Integer win_size = winphrase.length;
        Double sum_score = 0.0;
        LinkedList<RankedString> tmpunits = new LinkedList<>();
        while(win_size>1){
            Integer start_pos = 0;
            Integer end_pos = start_pos+win_size;
            while(end_pos<winphrase.length){
                String[] subphrase = Arrays.copyOfRange(winphrase, start_pos, end_pos);
                String sub_phrase = String.join("_",subphrase);
                if(phraseList.contains(sub_phrase)){
                    Double w_score = conf_phrase * word_score * termDistance(sub_phrase, unit);
                    tmpunits.add(new RankedString(sub_phrase, w_score));
                    sum_score += w_score;
                }
                start_pos++;
                end_pos = start_pos+win_size;
            }
            win_size --;
        }
        if(win_size == 1){
            for (String w : winphrase) { // only do this for origin units
                Double w_score = conf_phrase * word_score * termDistance(w, unit);
                //sum_score +=w_score;
                tmpunits.add(new RankedString(w, w_score));
            }
        }
        return tmpunits;
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
            Integer win_size = winphrase.length-1;
            Double sum_score = 0.0;
            tmpunits = subPhraseExpansion(unit,1.0);
//            Double next_conf = conf_phrase;
//            while(win_size>1){
//                Integer start_pos = 0;
//                Integer end_pos = start_pos+win_size;
//                while(end_pos<winphrase.length){
//                    String[] subphrase = Arrays.copyOfRange(winphrase, start_pos, end_pos);
//                    String sub_phrase = String.join("_",subphrase);
//                    if(phraseList.contains(sub_phrase)){
//                        Double w_score = 1.0 * next_conf;
//                        tmpunits.add(new RankedString(sub_phrase, w_score));
//                        sum_score += w_score;
//                    }
//                    start_pos++;
//                    end_pos = start_pos+win_size;
//                }
//                win_size --;
//                next_conf *= conf_phrase;
//            }
//            if(win_size == 1){
//                for (String w : winphrase) { // only do this for origin units
//                    Double w_score = 1.0 * next_conf;
//                    sum_score +=w_score;
//                    tmpunits.add(new RankedString(w, w_score));
//                }
//            }
            for (RankedString subUnit:tmpunits){
                //subUnit.setRank(subUnit.getRank() * 1.0 * sum_sub_conf / sum_score);
                //subUnit.setRank(subUnit.getRank());
                expendedUnits.add(subUnit);
            }
            tmpunits = new LinkedList<>();
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
                    simUnit.setRank(simUnit.getRank() * 1.0 * sum_sim_conf / sum_weight);
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
        for(RankedString unit_score: textrank) {
            LinkedList<RankedString> tmpunits = new LinkedList<>();
            expendedUnits.add(unit_score);
            String unit = unit_score.getString();
            Double score = unit_score.getRank();
            String[] winphrase = unit.split("_");
            Integer win_size = winphrase.length - 1;
            tmpunits = subPhraseExpansion(unit,score);
//            Double sum_score = 0.0;
//            Double next_conf = conf_phrase;
//            while (win_size > 1) {
//                Integer start_pos = 0;
//                Integer end_pos = start_pos + win_size;
//                while (end_pos < winphrase.length) {
//                    String[] subphrase = Arrays.copyOfRange(winphrase, start_pos, end_pos);
//                    String sub_phrase = String.join("_", subphrase);
//                    if (phraseList.contains(sub_phrase)) {
//                        Double w_score = score * next_conf;
//                        tmpunits.add(new RankedString(sub_phrase, w_score));
//                        sum_score += w_score;
//                    }
//                    start_pos++;
//                    end_pos = start_pos + win_size;
//                }
//                win_size--;
//                next_conf *= conf_phrase;
//            }
//            if (win_size == 1) {
//                for (String w : winphrase) { // only do this for origin units
//                    Double w_score = score * next_conf;
//                    sum_score += w_score;
//                    tmpunits.add(new RankedString(w, w_score));
//                }
//            }
            for (RankedString subUnit : tmpunits) {
                //subUnit.setRank(subUnit.getRank() * score * sum_sub_conf / sum_score);
                subUnit.setRank(subUnit.getRank());
                expendedUnits.add(subUnit);
            }
            tmpunits = new LinkedList<>();
            Integer sim_num = 0;
            if (similarity.containsKey(unit)) {
                simList = similarity.get(unit);
                Double sum_weight = 0.0;
                if (simList != null) {
                    for (RankedString simUnit : simList) {
                        if (sim_num >= max_num) {
                            break;
                        }
                        if (simUnit.getRank() > min_sim) {
                            simUnit.setRank(simUnit.getRank() * confidence);
                            tmpunits.add(simUnit);
                            sim_num += 1;
                            sum_weight += simUnit.getRank();
                        }
                    }
                    for (RankedString simUnit : tmpunits) {
                        simUnit.setRank(simUnit.getRank() * score * sum_sim_conf / sum_weight);
                        expendedUnits.add(simUnit);
                    }
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
        //TextParser TP = new TextParser();
//        String infile = "src/main/data/vectors-phrase-abstract.bin";
//        TE.loadBinData(infile);
//       System.out.print(TE.termDistance("rna","rna-rna"));
        String test = "clustering_algorithm";
        //List<String> pq = TP.queryParser(test);
        List<String> pq = new ArrayList<String>();
        pq.add(test);
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
