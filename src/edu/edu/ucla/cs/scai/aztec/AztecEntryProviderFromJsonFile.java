package edu.ucla.cs.scai.aztec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class AztecEntryProviderFromJsonFile implements edu.ucla.cs.scai.aztec.AztecEntryProvider {

    String fileName;

    public AztecEntryProviderFromJsonFile(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public ArrayList<AztecEntry> load() throws Exception {
        StringBuilder json = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
            String l;
            while ((l = in.readLine()) != null) {
                json.append(l).append(" ");
            }
        }
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        EntryWrapper1 w = gson.fromJson(json.toString(), EntryWrapper1.class);
        System.out.println("Loaded "+w.getdocs().size()+" entries");
        return w.getdocs();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Entries path system property: "+System.getProperty("entries.path"));
        String entriesPath = System.getProperty("entries.path", "/Users/patricktan/Desktop/aztec-text-analysis-tools/data/data.json");
        System.out.println(entriesPath);
        ArrayList<AztecEntry> entries=new AztecEntryProviderFromJsonFile(entriesPath).load();
        System.out.println(entries.size());
    }

}
