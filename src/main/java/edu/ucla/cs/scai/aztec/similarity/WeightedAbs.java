package edu.ucla.cs.scai.aztec.similarity;

import edu.ucla.cs.scai.aztec.AbsEntry;


/**
 * Created by Xinxin on 8/11/2016.
 */
public class WeightedAbs implements Comparable<WeightedAbs>{
    public AbsEntry entry;
    public double weight;
    public WeightedAbs(AbsEntry entry, double weight) {
        this.entry = entry;
        this.weight = weight;
    }
    @Override
    public int compareTo(WeightedAbs o) {
        return Double.compare(o.weight, weight);
    }
}
