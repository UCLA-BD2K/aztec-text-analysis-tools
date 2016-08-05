package edu.ucla.cs.scai.aztec.similarity;

import edu.ucla.cs.scai.aztec.AztecEntry;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class WeightedEntry implements Comparable<WeightedEntry> {

    public AztecEntry entry;
    public double weight;

    public WeightedEntry(AztecEntry entry, double weight) {
        this.entry = entry;
        this.weight = weight;
    }

    @Override
    public int compareTo(WeightedEntry o) {
        return Double.compare(o.weight, weight);
    }

}
