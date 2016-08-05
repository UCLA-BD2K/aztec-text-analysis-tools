package edu.ucla.cs.scai.aztec.utils;

import java.util.HashSet;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class StringUtils {

    public int editDistance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        int[][] f = new int[a.length() + 1][b.length() + 1];
        for (int i = 1; i <= a.length(); i++) {
            f[i][0] = i;
        }
        for (int j = 1; j <= b.length(); j++) {
            f[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    f[i][j] = f[i - 1][j - 1];
                } else {
                    f[i][j] = 1 + Math.min(f[i - 1][j - 1], Math.min(f[i - 1][j], f[i][j - 1]));
                }
            }
        }
        return f[a.length()][b.length()];
    }

    public double jaccardIndex(String[] a, String[] b) {
        if (a == null || b == null) {
            return 0;
        }
        HashSet<String> union = new HashSet<>();
        HashSet<String> intersection = new HashSet<>();
        for (String s : a) {
            union.add(s);
        }
        for (String s : b) {
            if (union.contains(s)) {
                intersection.add(s);
            } else {
                union.add(s);
            }
        }
        if (union.isEmpty()) {
            return 0;
        }
        return 1.0 * intersection.size() / union.size();
    }
}
