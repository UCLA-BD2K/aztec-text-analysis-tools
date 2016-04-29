/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.aztec.similarity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class RepresentativeProvider {

    private static final int UPDATE_FREQUENCY = 100;
    HashMap<String, String> representative;
    Dictionary dictionary;
    int newEntries = 0;
    String representativePath;

    public RepresentativeProvider(String representativePath, String dictionaryPath) throws JWNLException, FileNotFoundException {
        this.representativePath = representativePath;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(representativePath))) {
            representative = (HashMap<String, String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            representative = new HashMap<>();
        }
        dictionary = Dictionary.getInstance(new FileInputStream(dictionaryPath));
    }

    private void saveRepresentatives() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(representativePath))) {
            oos.writeObject(representative);
        } catch (Exception ex) {
            Logger.getLogger(RepresentativeProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getRepresentative(String w, String pos) {
        POS posw;
        if (pos.equals("NN") || pos.equals("NNS") || pos.equals("NNP") || pos.equals("NNPS")) {
            posw = POS.NOUN;
        } else if (pos.equals("VBZ") || pos.equals("VBG") || pos.equals("VBP") || pos.equals("VBN") || pos.equals("VBD") || pos.equals("VB")) {
            posw = POS.VERB;
        } else if (pos.equals("JJ")) {
            posw = POS.ADJECTIVE;
        }else {
            if (pos.startsWith("V") || pos.startsWith("N")) {
                System.out.println("POS discarded: " + pos);
            }
            return "";
        }
        if (!representative.containsKey(w)) {
            IndexWord iw1 = null;
            try {
                iw1 = dictionary.getIndexWord(posw, w);
            } catch (Exception e) {
            }
            String r = null;
            if (iw1 != null) {
                long[] offsets = iw1.getSynsetOffsets();
                if (offsets.length > 0) {
                    //try only the first meaning
                    try {
                        Synset synset = dictionary.getSynsetAt(posw, offsets[0]);
                        for (Word word : synset.getWords()) {
                            String lemma = word.getLemma();
                            if (representative.containsKey(lemma)) {
                                r = lemma;
                                break;
                            }
                        }
                    } catch (JWNLException ex) {
                        Logger.getLogger(RepresentativeProvider.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if (r != null) {
                representative.put(w, representative.get(r));
            } else {
                representative.put(w, w);
            }
            newEntries++;
        }
        if (newEntries == UPDATE_FREQUENCY) {
            saveRepresentatives();
            newEntries = 0;
        }
        return representative.get(w);
    }

    public static void main(String[] args) throws JWNLException, FileNotFoundException {
        RepresentativeProvider p = new RepresentativeProvider("/home/massimo/aztec/representatives.data", "/home/massimo/wordnet/properties.xml");
        System.out.println(p.getRepresentative("car", "NN"));
        System.out.println(p.getRepresentative("automobile", "NN"));
        System.out.println(p.getRepresentative("dog", "NN"));
        System.out.println(p.getRepresentative("hound", "NN"));
    }
}
