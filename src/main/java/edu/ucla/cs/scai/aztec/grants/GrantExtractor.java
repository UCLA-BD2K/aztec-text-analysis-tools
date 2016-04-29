/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

    public GrantExtractor(GrantSentenceClassifier classifier) {
        this.classifier = classifier;
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
        if (!candidates.isEmpty() && classifier.containsGrant(tokens)>0.5) {
            return candidates;
        } else {
            return new LinkedList<>();
        }
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
        if (!candidates.isEmpty() && classifier.containsGrant(tokens)>0.5) {
            return candidates;
        } else {
            return new LinkedList<>();
        }
    }

}
