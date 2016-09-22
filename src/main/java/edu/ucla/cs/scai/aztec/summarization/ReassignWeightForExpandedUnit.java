package edu.ucla.cs.scai.aztec.summarization;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Xinxin on 8/19/2016.
 */
public class ReassignWeightForExpandedUnit {
    private final static Map<String, List<RankedString>> similarity = new HashMap<>();
    public void loadMap(String infile) throws IOException { // input format -- Key: simUnit1,score simUnit2,score simUnit3,score ....
        BufferedReader reader = new BufferedReader(new FileReader(infile));
        String line;
        while((line = reader.readLine()) != null){
            List<RankedString> simPair = new ArrayList<>();
            String key = line.trim().split(":")[0].trim();
            String simString = line.trim().split(":")[1].trim();
            String[] simList = simString.split(" ");
            for(String pair: simList){
                String unit = pair.split(",")[0].trim();
                Double score = Double.parseDouble(pair.split(",")[1].trim());
                simPair.add(new RankedString(unit,score));
            }
            similarity.put(key,simPair);
            //simPair.clear();
        }
        reader.close();
    }
    public ReassignWeightForExpandedUnit() throws IOException{
        this.loadMap("src/main/data/SimilarityFile.txt");
    }
    public LinkedList<RankedString> ReassignWeight(LinkedList<RankedString> expandeddoc){
        LinkedList<RankedString> reassignExpandedDoc = new LinkedList<>();
        WeightedEdgeGraph WG = new WeightedEdgeGraph(expandeddoc.size());
        HashMap<String, Integer> UnitId = new HashMap<>();
        for (RankedString r: expandeddoc){
            List<RankedString> simList = similarity.get(r.getString());
            for(RankedString sr:simList){

            }
        }
        return reassignExpandedDoc;
    }
}
