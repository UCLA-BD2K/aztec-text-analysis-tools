/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.aztec.dto;

import edu.ucla.cs.scai.aztec.AztecEntry;
import java.util.ArrayList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class SearchResultPage {

    ArrayList<AztecEntry> entries;
    int totalResults;

    public SearchResultPage(ArrayList<AztecEntry> entries, int totalResults) {
        this.entries = entries;
        this.totalResults = totalResults;
    }

    public ArrayList<AztecEntry> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<AztecEntry> entries) {
        this.entries = entries;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

}
