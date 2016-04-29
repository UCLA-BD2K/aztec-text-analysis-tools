package edu.ucla.cs.scai.aztec.grants;

import java.util.List;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public interface GrantSentenceClassifier {
    
    double containsGrant(List<ExtendedToken> tokens);
    
}
