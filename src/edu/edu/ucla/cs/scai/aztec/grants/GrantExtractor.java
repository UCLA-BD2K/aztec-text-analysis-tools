package edu.ucla.cs.scai.aztec.grants;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.extjwnl.JWNLException;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class GrantExtractor {

    GrantSentenceClassifier classifier;
    Tokenizer tokenizer;

    public GrantExtractor(GrantSentenceClassifier classifier) {
        this.classifier = classifier;
        try {
            tokenizer = new Tokenizer();
        } catch (JWNLException ex) {
            Logger.getLogger(GrantExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GrantExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public GrantExtractor(GrantSentenceClassifier classifier, Tokenizer tokenizer) {
        this.classifier = classifier;
        this.tokenizer = tokenizer;
    }

    public LinkedList<String> extractGrants(String text) {
        LinkedList<LinkedList<ExtendedToken>> tokenLists = tokenizer.tokenize(text);
        LinkedList<String> res = new LinkedList<>();
        for (LinkedList<ExtendedToken> tokens : tokenLists) {
            res.addAll(extractGrants(tokens));
        }
        return res;
    }

    public LinkedList<String> extractGrants(List<ExtendedToken> tokens) {
        LinkedList<String> candidates = new LinkedList<>();
        Iterator<ExtendedToken> it = tokens.iterator();
        while (it.hasNext()) {
            ExtendedToken token = it.next();
            if (token.isGrantCandidate()) {
                String candidate = token.word();
                while (it.hasNext()) {
                    token = it.next();
                    if (token.isGrantCandidate()) {
                        candidate += " " + token.word();
                    } else {
                        break;
                    }
                }
                candidates.add(candidate);
            }
        }
        if (!candidates.isEmpty() && classifier.containsGrant(tokens) > 0.5) {
            return candidates;
        } else {
            return new LinkedList<>();
        }
    }

    public LinkedList<String> extractAgency(String text) {
        LinkedList<LinkedList<ExtendedToken>> tokenLists = tokenizer.tokenize(text);
        LinkedList<String> res = new LinkedList<>();
        for (LinkedList<ExtendedToken> tokens : tokenLists) {
            res.addAll(extractAgency(tokens));
        }
        return res;
    }

    public LinkedList<String> extractAgency(List<ExtendedToken> tokens) {
        LinkedList<String> candidates = new LinkedList<>();
        Iterator<ExtendedToken> it = tokens.iterator();
        while (it.hasNext()) {
            ExtendedToken token = it.next();
            if (token.ner().equals("ORGANIZATION")) {
                String candidate = token.word();
                while (it.hasNext()) {
                    token = it.next();
                    if (token.ner().equals("ORGANIZATION")) {
                        candidate += " " + token.word();
                    } else {
                        break;
                    }
                }
                candidates.add(candidate);
            }
        }
        if (!candidates.isEmpty() && classifier.containsGrant(tokens) > 0.5) {
            return candidates;
        } else {
            return new LinkedList<>();
        }
    }

}
