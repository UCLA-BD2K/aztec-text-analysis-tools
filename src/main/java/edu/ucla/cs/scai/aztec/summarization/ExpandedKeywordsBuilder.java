package edu.ucla.cs.scai.aztec.summarization;

import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.textexpansion.TextExpansion;
import net.sf.extjwnl.JWNLException;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Xinxin on 8/5/2016.
 */
public class ExpandedKeywordsBuilder {

    public HashMap<String, List<RankedString>> buildExpKeywords(Collection<AztecEntry> entries, String outputPath) throws JWNLException, IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("src/main/data/keywords.data"));
        HashMap<String, List<RankedString>> keywords = (HashMap<String, List<RankedString>>) ois.readObject();
        HashMap<String, List<RankedString>> expendedKeywords = new HashMap<>();
        TextExpansion TE = new TextExpansion();
        for (AztecEntry entry : entries) {
            LinkedList<RankedString> l = new LinkedList<>();
            expendedKeywords.put(entry.getId(), l);
            if (entry.getDescription() != null && entry.getDescription().trim().length() > 0) {
                List<RankedString> rankedToken = keywords.get(entry.getId());
                List<RankedString> expendedToken = TE.docExpansion(rankedToken);
                expendedKeywords.put(entry.getId(),expendedToken);
            }
        }

        System.out.println("Writing keywords to file " + outputPath);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputPath))) {
            out.writeObject(expendedKeywords);
        } catch (Exception ex) {
            Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return expendedKeywords;
    }

}
