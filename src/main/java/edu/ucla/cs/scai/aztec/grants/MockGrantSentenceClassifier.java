/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.aztec.grants;

import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class MockGrantSentenceClassifier implements GrantSentenceClassifier {
    
    static final HashSet<String> dictionary;
    
    static {
        dictionary=new HashSet<>();
        dictionary.add("grant");
        dictionary.add("fund");
        dictionary.add("support");
    }
    
    @Override
    public double containsGrant(List<ExtendedToken> tokens) {
        for (ExtendedToken t:   tokens) {
            if (dictionary.contains(t.lemma())) {
                return 1;
            }
        }
        return 0;
    }
    
    
}
