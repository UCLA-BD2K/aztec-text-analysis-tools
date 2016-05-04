package edu.ucla.cs.scai.aztec.summarization;

import edu.ucla.cs.scai.aztec.similarity.*;
import edu.ucla.cs.scai.aztec.AztecEntry;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.extjwnl.JWNLException;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class KeywordsBuilder {

    Tokenizer tokenizer;

    static final double log2 = Math.log(2);

    public KeywordsBuilder() throws JWNLException, FileNotFoundException {
        tokenizer = new Tokenizer();
    }

    public HashMap<String, List<RankedString>> buildKeywords(Collection<AztecEntry> entries, String outputPath) throws IOException {

        HashMap<String, List<RankedString>> res = new HashMap<>();
        for (AztecEntry entry : entries) {
            LinkedList<RankedString> l = new LinkedList<>();
            res.put(entry.getId(), l);
            if (entry.getDescription() != null && entry.getDescription().trim().length() > 0) {
                try {
                    KeywordsRank kr = new KeywordsRank(entry.getDescription(), 10);
                    l.addAll(kr.topRankedKeywords(20));
                } catch (Exception ex) {
                    Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        System.out.println("Writing keywords to file "+outputPath);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputPath))) {
            out.writeObject(res);
        } catch (Exception ex) {
            Logger.getLogger(KeywordsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }

        return res;
    }
}
