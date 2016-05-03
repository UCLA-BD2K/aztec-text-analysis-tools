package edu.ucla.cs.scai.aztec.grants;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class AckLoader {

    public ArrayList<String> loadAcks(String path) throws FileNotFoundException {
        ArrayList<String> res = new ArrayList<>();
        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new FileReader(path));
        Set<Map.Entry<String, JsonElement>> es = root.getAsJsonObject().entrySet();
        for (Map.Entry<String, JsonElement> e : es) {
            JsonObject jo = e.getValue().getAsJsonObject();
            JsonElement ack = jo.get("ack");
            if (ack != null) {
                String ackS = ack.getAsString().trim();
                if (ackS.length() > 0) {
                    res.add(ackS);
                }
            }
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Publication extracts path system property: "+System.getProperty("publications.path"));
        String solrPublicationExtracts=System.getProperty("publications.path", "/home/massimo/Downloads/solrPublicationExtracts.json");
        ArrayList<String> acks = new AckLoader().loadAcks(solrPublicationExtracts);
        int i = 1;
        Tokenizer tokenizer = new Tokenizer();
        GrantExtractor extractor = new GrantExtractor(new MockGrantSentenceClassifier());
        for (String ack : acks) {
            System.out.println(i + ": " + ack);
            LinkedList<LinkedList<ExtendedToken>> tokenLists = tokenizer.tokenize(ack);
            for (LinkedList<ExtendedToken> tokens : tokenLists) {
                for (ExtendedToken token : tokens) {
                    System.out.print(token.word() + "/" + token.lemma() + (token.isGrantCandidate() ? "/*" : "") + "/");
                }
                System.out.println();
                LinkedList<String> grants = extractor.extractGrants(tokens);
                if (grants.isEmpty()) {
                    System.out.println("No grants found");
                } else {
                    for (String grant : grants) {
                        System.out.println(grant);
                    }
                }
                LinkedList<String> agencies = extractor.extractAgency(tokens);
                if (agencies.isEmpty()) {
                    System.out.println("No agencies found");
                } else {
                    for (String agency : agencies) {
                        System.out.println(agency);
                    }
                }
            }
            System.out.println();
            System.out.println();
            System.out.println();
            i++;
        }
    }

}
