package edu.ucla.cs.scai.aztec;

import java.util.ArrayList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class EntryWrapper1 {

    EntryWrapper2 response;

    public EntryWrapper2 getResponse() {
        return response;
    }

    public void setResponse(EntryWrapper2 response) {
        this.response = response;
    }

    public ArrayList<AztecEntry> getdocs() {
        return response.getDocs();
    }

}
