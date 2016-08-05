package edu.ucla.cs.scai.aztec.keyphrase;

/**
 * Created by Xinxin on 8/1/2016.
 */
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import net.sf.extjwnl.JWNLException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

// need to map all the words to its lemma
public class Tokenizer {
    StanfordCoreNLP pipelineTokens;
    public Tokenizer() throws JWNLException, FileNotFoundException {
        Properties propToken = new Properties();
        propToken.put("annotators","tokenize,ssplit,pos,lemma");
        propToken.setProperty("tokenize.options", "ptb3Escaping=false,normalizeParentheses=false");
        pipelineTokens = new StanfordCoreNLP(propToken);
        //propsTokens.setProperty("tokenize.ptb3Escaping","false");
//        pipelineTokens = new StanfordCoreNLP(
//                PropertiesUtils.asProperties(
//                        "annotators", "tokenize,ssplit,pos,lemma,",
//                        "tokenize.ptb3Escaping","false",
//                        "tokenize.normalizeParentheses", "false",
//                        "tokenize.language", "en"));
    }

    public LinkedList<String> tokenize(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("/"," ");
        text = text.replaceAll("_","-");
        LinkedList<String> wordList = new LinkedList<String>();
        if (text != null) {
            Annotation qaTokens = new Annotation(text);
            pipelineTokens.annotate(qaTokens);
            List<CoreMap> qssTokens = qaTokens.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap sentenceTokens : qssTokens) {
                ArrayList<CoreLabel> tokens = (ArrayList<CoreLabel>) sentenceTokens.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel t : tokens) {
                    String lemma = t.lemma();
                    wordList.add(lemma);
                }
            }
        }
        return wordList;
    }
}
