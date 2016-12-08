package edu.ucla.cs.scai.aztec.textexpansion;

import com.sun.org.apache.bcel.internal.generic.ArrayType;
import com.sun.org.apache.xml.internal.serializer.utils.SystemIDResolver;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.AztecEntryProviderFromJsonFile;
import edu.ucla.cs.scai.aztec.keyphrase.Tokenizer;
import edu.ucla.cs.scai.aztec.summarization.RankedString;
import net.sf.extjwnl.JWNLException;
import edu.ucla.cs.scai.aztec.AztecEntry;
import org.apache.poi.ss.formula.functions.Rank;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Xinxin on 10/9/2016.
 */
public class TagExpansion {
    private final static HashSet<String> vocab = new HashSet<>();
    HashSet<String> tagset = new HashSet<>();
    PrintWriter pw = new PrintWriter(new FileOutputStream("src/main/data/superised_terms.txt"));
    Tokenizer tk;

    public void loadVoc(String infile) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        while((line = reader.readLine()) != null){
            vocab.add(line.trim());
        }
        reader.close();
    }
    public TagExpansion() throws JWNLException, FileNotFoundException, IOException{
        this.tk = new Tokenizer();
        this.loadVoc("D:/AztecSearch/Xinxin/data/word2vec_data/Synonyms/Vocabulary.txt");
        //this.loadVoc("src/main/data/Vocabulary.txt");
    }

    public String tagSplit(String tag) throws IOException{
        String final_tag;
        String pattern = "([a-z])([A-Z])([a-z])";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(tag);
        if(matcher.find()){ // split the words in phrase with space
            LinkedList<String> tk_words = tk.tokenize(tag);
            String tk_tag = tk_words.get(0);
            if (vocab.contains(tk_tag)) { // if it should not be split
                final_tag = tk_tag;
                return final_tag;
            }
            else { // if should be split, split it and come to next step
                tag = matcher.replaceAll("$1 $2$3");
            }
        }
        LinkedList<String> tk_words = tk.tokenize(tag);
        if (tk_words.size() > 1) { // check if it is a phrase in our vocabulary
            String tk_tag = String.join("-", tk_words); //join with _
            if (vocab.contains(tk_tag)) {
                final_tag = tk_tag;
            }
            else{
                tk_tag = String.join("_", tk_words); //join with -
                if (vocab.contains(tk_tag)) {
                    final_tag = tk_tag;
                }
                else{ // both condition are not satisfied
                    //vocab.add(tk_tag);
                    pw.println(tk_tag); // suppose it should be joined by_
                    final_tag = tk_tag;
                }
            }
        } else { // for term with only one word.
            String tk_tag = tk_words.get(0);
            if (vocab.contains(tk_tag)) {
                final_tag = tk_tag;
            } else {
                //vocab.add(tk_tag); // add that word into the list
                pw.println(tk_tag);
                final_tag = tk_tag;
            }
        }
        return final_tag;

    }
    public LinkedList<RankedString> tagExpansion(ArrayList<String> tags) throws IOException{
        LinkedList<RankedString> expandedtags = new LinkedList<>();
        Integer count = 0;
        for(String tag:tags){
            if(!tag.equals("")) {
                String[] error_tags = tag.split(";");
                if (error_tags.length > 1) {
                    for (String etag : error_tags) {
                        Double score = Double.max(0.1, 0.7 - 0.1 * count);
                        String tk_tag = tagSplit(etag);
                        tagset.add(tk_tag);
                        expandedtags.add(new RankedString(tk_tag, score));
                        count += 1;
                    }
                } else {
                    Double score = Double.max(0.1, 0.7 - 0.1 * count);
                    String tk_tag = tagSplit(tag);
                    tagset.add(tk_tag);
                    expandedtags.add(new RankedString(tk_tag, score));
                    count += 1;
                }
            }
        }

        return expandedtags;
    }
    public static void main(String[] args) throws Exception{
        TagExpansion TE = new TagExpansion();
        PrintWriter pw = new PrintWriter(new FileOutputStream("src/main/data/exptags.txt"));
        String entriesPath = System.getProperty("entries.path", "src/main/data/solrResources.json");
        ArrayList<AztecEntry> entryArray = new AztecEntryProviderFromJsonFile(entriesPath).load();
        LinkedList<RankedString> et = new LinkedList<>();
        for(AztecEntry e: entryArray) {
            if (e.getTags() != null) {
                et = TE.tagExpansion(e.getTags());
                for(RankedString rs:et){
                    pw.print(e.getId()+": ");
                    pw.print(rs.getString()+" ");
                    pw.print(rs.getRank()+"; ");
                }
                pw.print("\n");
            }
        }
        pw.close();
        System.out.print(TE.tagset.size());
        pw =  new PrintWriter(new FileOutputStream("src/main/data/tagset.txt"));
        for(String w:TE.tagset){
            pw.println(w);
        }
        pw.close();
    }
}
