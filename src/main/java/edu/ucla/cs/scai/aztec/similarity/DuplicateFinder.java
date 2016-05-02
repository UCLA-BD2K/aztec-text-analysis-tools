package edu.ucla.cs.scai.aztec.similarity;

import edu.ucla.cs.scai.aztec.AztecEntry;
import edu.ucla.cs.scai.aztec.AztecEntryProviderFromJsonFile;
import edu.ucla.cs.scai.aztec.PossibleDuplicates;
import edu.ucla.cs.scai.aztec.utils.ImageUtils;
import edu.ucla.cs.scai.aztec.utils.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DuplicateFinder {

    public final static double NAME_EDIT_DISTANCE_THRESHOLD = 0.2;
    public final static double LOGO_HISTOGRAM_DISTANCE_THRESHOLD = 0.2;
    public final static double SOURCE_CODE_URL_EDIT_DISTANCE_THRESHOLD = 0.2;

    StringUtils su = new StringUtils();
    ImageUtils iu = new ImageUtils();

    public DuplicateFinder() {
    }

    public boolean duplicatesForName(AztecEntry e1, AztecEntry e2) { //uses the edit distance
        String n1 = e1.getName();
        String n2 = e2.getName();
        if (n1 == null || n2 == null) {
            return false;
        }
        n1 = n1.trim();
        n2 = n2.trim();
        if (n1.length() == 0 || n2.length() == 0) {
            return false;
        }
        if (1.0 * Math.abs(n1.length() - n2.length()) / Math.max(n1.length(), n2.length()) > NAME_EDIT_DISTANCE_THRESHOLD) {
            return false;
        }
        int ed = su.editDistance(n1, n2);
        return 1.0 * ed / Math.max(n1.length(), n2.length()) <= NAME_EDIT_DISTANCE_THRESHOLD;
    }

    public boolean duplicatesForLogo(AztecEntry e1, AztecEntry e2) {
        if (e1.getLogo() == null || e2.getLogo() == null) {
            return false;
        }
        if (e1.getLogo().equals(e2.getLogo())) {
            return true;
        }
        double[] h1 = e1.getLogoHistogram();
        if (h1 == null) {
            return false;
        }
        double[] h2 = e2.getLogoHistogram();
        if (h2 == null) {
            return false;
        }
        double hs = iu.histogramSimilarity(h1, h2);
        if (hs >= 1 - LOGO_HISTOGRAM_DISTANCE_THRESHOLD) {
            System.out.println();
        }
        return hs >= 1 - LOGO_HISTOGRAM_DISTANCE_THRESHOLD;
    }

    public boolean duplicatesForSourceCodeUrl(AztecEntry e1, AztecEntry e2) {
        String n1 = e1.getSourceCodeURL();
        String n2 = e2.getSourceCodeURL();
        if (n1 == null || n2 == null) {
            return false;
        }
        n1 = n1.trim();
        n2 = n2.trim();
        if (n1.length() == 0 || n2.length() == 0) {
            return false;
        }
        String[] n1s = n1.toLowerCase().split(":\\/\\/");
        n1 = n1s[n1s.length - 1];
        if (n1.endsWith("/")) {
            n1 = n1.substring(0, n1.length() - 1);
        }
        String[] n2s = n2.toLowerCase().split(":\\/\\/");
        n2 = n2s[n2s.length - 1];
        if (n2.endsWith("/")) {
            n2 = n2.substring(0, n2.length() - 1);
        }
        return n1.equals(n2);
        /*
         if (1.0 * Math.abs(n1.length() - n2.length()) / Math.max(n1.length(), n2.length()) > SOURCE_CODE_URL_EDIT_DISTANCE_THRESHOLD) {
         return false;
         }
         int ed = su.editDistance(n1, n2);
         return 1.0 * ed / Math.max(n1.length(), n2.length()) <= SOURCE_CODE_URL_EDIT_DISTANCE_THRESHOLD;
         */
    }

    public ArrayList<AztecEntry> find(AztecEntry e1) {
        ArrayList<AztecEntry> res = new ArrayList<>();
        for (int j = 0; j < CachedData.entryArray.size(); j++) {
            AztecEntry e2 = CachedData.entryArray.get(j);
            if (e2.equals(e1)) {
                continue;
            }
            if (duplicatesForName(e1, e2)) {
                res.add(e2);
            } else if (duplicatesForSourceCodeUrl(e1, e2)) {
                res.add(e2);
            }
        }
        return res;
    }

    public ArrayList<AztecEntry> find(String entryId) {
        return find(CachedData.entryMap.get(entryId));
    }

    public ArrayList<PossibleDuplicates> findAll() {
        ArrayList<PossibleDuplicates> res = new ArrayList<>();
        for (int i = 0; i < CachedData.entryArray.size() - 1; i++) {
            AztecEntry e1 = CachedData.entryArray.get(i);
            for (int j = i + 1; j < CachedData.entryArray.size(); j++) {
                AztecEntry e2 = CachedData.entryArray.get(j);
                if (duplicatesForName(e1, e2)) {
                    PossibleDuplicates pd = new PossibleDuplicates(e1, e2, "Name");
                    res.add(pd);
                    System.out.println(pd);
                    //} else if (duplicatesForLogo(e1, e2)) {
                    //res.add(new PossibleDuplicates(e1, e2, "Their logo is very similar"));
                } else if (duplicatesForSourceCodeUrl(e1, e2)) {
                    PossibleDuplicates pd = new PossibleDuplicates(e1, e2, "Source code url");
                    res.add(pd);
                    System.out.println(pd + " " + e1.getSourceCodeURL() + " " + e2.getSourceCodeURL());
                }
            }
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        String entriesPath = System.getProperty("entries.path", "/home/massimo/Downloads/solrResources.json");
        ArrayList<AztecEntry> entries = new AztecEntryProviderFromJsonFile(entriesPath).load();
        HashMap<String, AztecEntry> entryById = new HashMap<>();
        for (AztecEntry e : entries) {
            entryById.put(e.getId(), e);
        }
        ArrayList<PossibleDuplicates> possibileDuplicates = new DuplicateFinder().findAll();
        System.out.println(possibileDuplicates.size() + " possibile duplicate pairs");
        HashMap<String, ArrayList<String>> edges = new HashMap<>();
        for (PossibleDuplicates pd : possibileDuplicates) {
            String id1 = pd.getE1().getId();
            String id2 = pd.getE2().getId();
            ArrayList<String> l1 = edges.get(id1);
            if (l1 == null) {
                l1 = new ArrayList<>();
                edges.put(id1, l1);
            }
            ArrayList<String> l2 = edges.get(id2);
            if (l2 == null) {
                l2 = new ArrayList<>();
                edges.put(id2, l2);
            }
            l1.add(id2);
            l2.add(id1);
        }
        HashSet<String> visited = new HashSet<>();
        int possibleDeletion = 0;
        for (int i = 0; i < entries.size(); i++) {
            String start = entries.get(i).getId();
            if (visited.contains(start)) {
                continue;
            }
            LinkedList<String> queue = new LinkedList<>();
            queue.addLast(start);
            HashSet<String> component = new HashSet<>();
            while (!queue.isEmpty()) {
                String n = queue.removeFirst();
                component.add(n);
                visited.add(n);
                if (edges.get(n) != null) {
                    for (String m : edges.get(n)) {
                        if (!visited.contains(m)) {
                            queue.addLast(m);
                            visited.add(m);
                        }
                    }
                }
            }
            if (component.size() > 1) {
                component.remove(start);
                System.out.print("Possible entries equal to " + entryById.get(start).getName() + "(id=" + start + ") :");
                for (String id : component) {
                    System.out.print(" " + entryById.get(id).getName() + " (id=" + id + ")");
                }
                System.out.println();
                possibleDeletion += component.size();
            }
        }
        System.out.println(possibleDeletion + " entries could be deleted/merged");
    }

}
