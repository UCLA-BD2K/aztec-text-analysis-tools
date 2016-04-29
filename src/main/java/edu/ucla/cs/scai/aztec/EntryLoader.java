/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.aztec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.TypeAdapters;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class EntryLoader {

    String fileName;

    public EntryLoader(String fileName) {
        this.fileName = fileName;
    }

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
        ArrayList<AztecEntry> entries=new EntryLoader("/home/massimo/Downloads/solrResources.json").load();
        System.out.println(entries.size());
    }

}
