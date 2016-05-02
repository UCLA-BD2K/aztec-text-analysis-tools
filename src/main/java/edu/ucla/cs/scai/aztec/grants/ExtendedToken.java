package edu.ucla.cs.scai.aztec.grants;

import edu.stanford.nlp.ling.CoreLabel;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class ExtendedToken {
    
    CoreLabel token;
    boolean grantCandidate;
    
    public ExtendedToken(CoreLabel token) {
        this.token=token;
        boolean containsLetter=false;
        boolean containsDigit=false;
        String word=token.word();
        int i=0;
        while (!(containsDigit && containsLetter) && i<word.length()) {
            char c=word.charAt(i);
            if ('A' <= c && c <='Z') {
                containsLetter=true;
            } else if ('0' <= c && c <='9') {
                containsDigit=true;
            }
            i++;
        }
        grantCandidate = containsDigit && containsLetter;
    }
    
    public String word() {
        return token.word();
    }
    
    public String lemma() {
        return token.lemma();
    }
    
    public String ner() {
        return token.ner();
    }
    
    public String tag() {
        return token.tag();
    }
    
    public boolean isGrantCandidate() {
        return grantCandidate;
    }
    
}
