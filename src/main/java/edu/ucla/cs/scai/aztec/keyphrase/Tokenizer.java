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
import java.util.*;

// need to map all the words to its lemma
public class Tokenizer {
    StanfordCoreNLP pipelineTokens;
    private final static HashSet<String> stopwords = new HashSet<>();
    static {
        String s = "a\n"
                + "about\n"
                + "above\n"
                + "after\n"
                + "again\n"
                + "against\n"
                + "all\n"
                + "am\n"
                + "an\n"
                + "and\n"
                + "any\n"
                + "are\n"
                + "aren't\n"
                + "as\n"
                + "at\n"
                + "be\n"
                + "because\n"
                + "been\n"
                + "before\n"
                + "being\n"
                + "below\n"
                + "between\n"
                + "both\n"
                + "but\n"
                + "by\n"
                + "can't\n"
                + "cannot\n"
                + "could\n"
                + "couldn't\n"
                + "did\n"
                + "didn't\n"
                + "do\n"
                + "does\n"
                + "doesn't\n"
                + "doing\n"
                + "don't\n"
                + "down\n"
                + "during\n"
                + "each\n"
                + "few\n"
                + "for\n"
                + "from\n"
                + "further\n"
                + "had\n"
                + "hadn't\n"
                + "has\n"
                + "hasn't\n"
                + "have\n"
                + "haven't\n"
                + "having\n"
                + "he\n"
                + "he'd\n"
                + "he'll\n"
                + "he's\n"
                + "her\n"
                + "here\n"
                + "here's\n"
                + "hers\n"
                + "herself\n"
                + "him\n"
                + "himself\n"
                + "his\n"
                + "how\n"
                + "how's\n"
                + "i\n"
                + "i'd\n"
                + "i'll\n"
                + "i'm\n"
                + "i've\n"
                + "if\n"
                + "in\n"
                + "into\n"
                + "is\n"
                + "isn't\n"
                + "it\n"
                + "it's\n"
                + "its\n"
                + "itself\n"
                + "let's\n"
                + "me\n"
                + "more\n"
                + "most\n"
                + "mustn't\n"
                + "my\n"
                + "myself\n"
                + "no\n"
                + "nor\n"
                + "not\n"
                + "of\n"
                + "off\n"
                + "on\n"
                + "once\n"
                + "only\n"
                + "or\n"
                + "other\n"
                + "ought\n"
                + "our\n"
                + "ours	ourselves\n"
                + "out\n"
                + "over\n"
                + "own\n"
                + "same\n"
                + "shan't\n"
                + "she\n"
                + "she'd\n"
                + "she'll\n"
                + "she's\n"
                + "should\n"
                + "shouldn't\n"
                + "so\n"
                + "some\n"
                + "such\n"
                + "than\n"
                + "that\n"
                + "that's\n"
                + "the\n"
                + "their\n"
                + "theirs\n"
                + "them\n"
                + "themselves\n"
                + "then\n"
                + "there\n"
                + "there's\n"
                + "these\n"
                + "they\n"
                + "they'd\n"
                + "they'll\n"
                + "they're\n"
                + "they've\n"
                + "this\n"
                + "those\n"
                + "through\n"
                + "to\n"
                + "too\n"
                + "under\n"
                + "until\n"
                + "up\n"
                + "very\n"
                + "was\n"
                + "wasn't\n"
                + "we\n"
                + "we'd\n"
                + "we'll\n"
                + "we're\n"
                + "we've\n"
                + "were\n"
                + "weren't\n"
                + "what\n"
                + "what's\n"
                + "when\n"
                + "when's\n"
                + "where\n"
                + "where's\n"
                + "which\n"
                + "while\n"
                + "who\n"
                + "who's\n"
                + "whom\n"
                + "why\n"
                + "why's\n"
                + "with\n"
                + "won't\n"
                + "would\n"
                + "wouldn't\n"
                + "you\n"
                + "you'd\n"
                + "you'll\n"
                + "you're\n"
                + "you've\n"
                + "your\n"
                + "yours\n"
                + "yourself\n"
                + "yourselves\n'";
        stopwords.addAll(Arrays.asList(s.split("\\\n")));
        stopwords.add("");
    }


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
    public LinkedList<LinkedList<String>> tokenizeBySentence(String text, LinkedList<int[]> boundaries) {
        LinkedList<LinkedList<String>> res = new LinkedList<>();
        if (text != null) {
            Annotation qaTokens = new Annotation(text);
            pipelineTokens.annotate(qaTokens);
            List<CoreMap> qssTokens = qaTokens.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap sentenceTokens : qssTokens) {
                ArrayList<CoreLabel> tokens = (ArrayList<CoreLabel>) sentenceTokens.get(CoreAnnotations.TokensAnnotation.class);
                LinkedList<String> sentence = new LinkedList<>();
                boolean first = true;
                int[] bounds = new int[2];
                CoreLabel last = null;
                for (CoreLabel t : tokens) {
                    if (first) {
                        bounds[0] = t.beginPosition();
                        first = false;
                    }
                    last = t;
                    String lemma = t.lemma();
                    String pos = t.tag();
                    if (!stopwords.contains(lemma)) {
                        sentence.add(lemma);
                    }
                }
                bounds[1] = last.endPosition();
                if (sentence.size() > 0) {
                    res.add(sentence);
                    boundaries.add(bounds);
                }
            }
        }
        return res;
    }

}
