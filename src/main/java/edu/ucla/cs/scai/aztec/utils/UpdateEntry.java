package edu.ucla.cs.scai.aztec.utils;

import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.AztecEntryProviderFromJsonFile;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Xinxin on 10/9/2016.
 */
public class UpdateEntry {
    private static HashMap<String,String> id_domain = new HashMap<>();
    public void update() throws Exception{
        String entriesPath = System.getProperty("entries.path", "src/main/data/solrResources.json");
        ArrayList<AztecEntry> entryArray = new AztecEntryProviderFromJsonFile(entriesPath).load();
        for(AztecEntry e: entryArray){

        }
    }

}
