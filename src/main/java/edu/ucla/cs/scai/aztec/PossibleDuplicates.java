package edu.ucla.cs.scai.aztec;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class PossibleDuplicates {

    AztecEntry e1, e2;
    String reason;

    public PossibleDuplicates(AztecEntry e1, AztecEntry e2, String reason) {
        this.e1 = e1;
        this.e2 = e2;
        this.reason = reason;
    }

    public AztecEntry getE1() {
        return e1;
    }

    public void setE1(AztecEntry e1) {
        this.e1 = e1;
    }

    public AztecEntry getE2() {
        return e2;
    }

    public void setE2(AztecEntry e2) {
        this.e2 = e2;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return e1.name + " (id=" + e1.id + ") could be a duplicate of " + e2.name + " (id=" + e2.id + ") - reason: " + reason;
    }

}
