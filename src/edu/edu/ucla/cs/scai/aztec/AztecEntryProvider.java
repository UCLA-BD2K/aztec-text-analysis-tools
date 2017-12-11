package edu.ucla.cs.scai.aztec;

import java.util.ArrayList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public interface AztecEntryProvider {

    public ArrayList<AztecEntry> load() throws Exception;

}
