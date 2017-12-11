package edu.ucla.cs.scai.aztec;

import java.util.ArrayList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class EntryWrapper2 {

    int numFound;
    ArrayList<AztecEntry> docs;

    public int getNumFound() {
        return numFound;
    }

    public void setNumFound(int numFound) {
        this.numFound = numFound;
    }

    public ArrayList<AztecEntry> getDocs() {
        return docs;
    }

    public void setDocs(ArrayList<AztecEntry> docs) {
        this.docs = docs;
    }

}
