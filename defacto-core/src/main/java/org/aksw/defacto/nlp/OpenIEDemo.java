package org.aksw.defacto.nlp;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.*;

import java.util.Collection;
import java.util.Properties;

/**
 * A demo illustrating how to call the OpenIE system programmatically.
 */
public class OpenIEDemo {

    private static void test(){
        Document doc = new Document("Obama was born in Hawaii. He is our president.");

        // Iterate over the sentences in the document
        for (Sentence sent : doc.sentences()) {
            // Iterate over the triples in the sentence
            for (RelationTriple triple : sent.openieTriples()) {
                // Print the triple
                System.out.println(triple.confidence + "\t" +
                        triple.subjectLemmaGloss() + "\t" +
                        triple.relationLemmaGloss() + "\t" +
                        triple.objectLemmaGloss());
            }
        }
    }

    private static void test2(){

        // Create the Stanford CoreNLP pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Annotate an example document.
        Annotation doc = new Annotation("Diego was born in Rio de Janeiro");
        pipeline.annotate(doc);

        // Loop over sentences in the document
        for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
            // Get the OpenIE triples for the sentence
            Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
            // Print the triples
            for (RelationTriple triple : triples) {
                System.out.println(triple.confidence + "\t" +
                        triple.subjectLemmaGloss() + "\t" +
                        triple.relationLemmaGloss() + "\t" +
                        triple.objectLemmaGloss());
            }
        }

    }
    public static void main(String[] args) throws Exception {

        test();

    }
}
