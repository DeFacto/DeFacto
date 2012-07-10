package org.aksw.defacto.search.fact;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.defacto.DefactoModel;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.search.crawl.EvidenceCrawler;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.query.QueryGenerator;
import org.aksw.defacto.util.CSVWriter;
import org.aksw.defacto.util.LabeledTriple;
import org.aksw.defacto.util.SparqlUtil;
import org.aksw.defacto.util.TimeUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Utility script to check whether confirming pages can be found for a
 * particular triple. Its main purpose is to observe the performance of the
 * current algorithms.
 * 
 * @author Jens Lehmann
 * 
 */
public class FactSearchTester {

    // write a CSV file:
    // triple incl. labels, pattern, webpage, confirmation, excerpt
    // repeat previous values only if they are different
    static CSVWriter csv = new CSVWriter("log/factConfirmation.csv", 7);
    static BufferedWriter out;

    // the fact search we use
    // static DefaultFactSearcher searcher = (DefaultFactSearcher)
    // DefaultFactSearcher
    // .getInstance();
//    static SubjectObjectFactSearcher searcher = SubjectObjectFactSearcher.getInstance();

    static Logger logger = Logger.getLogger(FactSearchTester.class);

    public static String check(DefactoModel model) throws IOException {

        // Logger logger = Logger.getLogger(FactSearchTester.class);

        // 1. generate the search engine queries
        long start = System.currentTimeMillis();
        QueryGenerator queryGenerator = new QueryGenerator(model);
        Map<Pattern, MetaQuery> queries = queryGenerator.getSearchEngineQueries();
        if (queries.size() <= 0)
            return "0|0";
        logger.info("Preparing queries took " + TimeUtil.formatTime(System.currentTimeMillis() - start));

        // 2. download the search results in parallel
        long startCrawl = System.currentTimeMillis();
        EvidenceCrawler crawler = new EvidenceCrawler(model, queries);
        MetaQuery query = queries.values().iterator().next();
        Evidence evidence = crawler.crawlEvidence(query.getSubjectLabel(), query.getObjectLabel());
        logger.info("Crawling evidence took " + TimeUtil.formatTime(System.currentTimeMillis() - startCrawl));

        // loop over all patterns
        Set<String> proofWebsites = new TreeSet<String>();
        Set<String> possibleProofWebsites = new TreeSet<String>();
        boolean containsProof = !evidence.getComplexProofs().isEmpty();
        
        if (containsProof) {
            
            for (ComplexProof proof : evidence.getComplexProofs() ) {
                String phrase = proof.getProofPhrase();//.replace(proof.getPattern().normalize(), "___" + proof.getPattern().normalize() + "___");
                csv.addRecord(new String[] { toString(model), "proof", proof.getWebSite().getUrl(), StringEscapeUtils.escapeCsv(proof.getPattern().naturalLanguageRepresentation), proof.getSubject(), StringEscapeUtils.escapeCsv(phrase), proof.getObject() });
                
                proofWebsites.add(proof.getWebSite().getUrl());
                logger.info("Checking pattern <i>" + proof.getPattern().naturalLanguageRepresentation + "</i> on website <a href=\"" + proof.getWebSite().getUrl() + "\">" + proof.getWebSite().getUrl() + "</a>: " + "<b>success!</b>");
            }
        }
            
//        if ( !evidence.getPossibleProofs().isEmpty() ) {
//            
//            for (Map.Entry<PossibleProof,Integer> posProofs : evidence.getPossibleProofs().entrySet()) {
//                
//                out.write(posProofs.getValue() + ": " + posProofs.getKey() + " : " + posProofs.getKey().getWebSite().getUrl() + "\n");
//                csv.addRecord(new String[] { toString(model), "possible proofs", posProofs.getKey().getWebSite().getUrl(), posProofs.getValue()+ "", posProofs.getKey().getSubject(), posProofs.getKey().getPhrase(), posProofs.getKey().getObject() });
//                possibleProofWebsites.add(posProofs.getKey().getWebSite().getUrl());
//            }
//        }
        else csv.addRecord(new String[] { toString(model), "no", "no", "no", "no", "no", "no" });
        
        return proofWebsites.size() + "|" +possibleProofWebsites.size();
    }

    public static String toString(DefactoModel model) {

        LabeledTriple lt = new LabeledTriple(model);

        return  lt.getSubjectURI().replace("http://dbpedia.org/resource/", "") + " (" + lt.getSubjectLabel() + ") - " + 
                lt.getPredicateURI().replace("http://dbpedia.org/ontology/", "") + " - " + 
                lt.getObjectURI().replace("http://dbpedia.org/resource/", "") + " (" + lt.getObjectLabel() + ")";

    }

    public static String checkTriple(String subject, String predicate, String object) throws IOException {

        // SparqlUtil is only used to get the labels
        SparqlUtil sparql = new SparqlUtil("http://live.dbpedia.org/sparql", "http://dbpedia.org");
        Model model = ModelFactory.createDefaultModel();
        model.add(ResourceFactory.createStatement(ResourceFactory.createResource(subject), ResourceFactory.createProperty(predicate), ResourceFactory.createResource(object)));
        model.add(ResourceFactory.createStatement(ResourceFactory.createResource(subject), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),
                model.createLiteral(sparql.getEnLabel(subject), "en")));
        model.add(ResourceFactory.createStatement(ResourceFactory.createResource(object), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),
                model.createLiteral(sparql.getEnLabel(object), "en")));
        
        return check(new DefactoModel(model, "undefined", false));
    }

    public static void checkPositiveExamples() throws IOException {

        Map<String,Integer> propertiesToConfirmingPages = new HashMap<String,Integer>();
        
        // loop over all training files
        int successCount = 0;
        int noSuccessCount = 0;
        int total = 0;
        int totalPossibleProofs = 0;
        List<File> modelFiles = new ArrayList<File>(Arrays.asList(new File("resources/training/data/true").listFiles()));
        int i = 1;
        for (File modelFile : modelFiles) {
            
            String propertyName = modelFile.getName().substring(0,modelFile.getName().lastIndexOf("_"));

            String name = modelFile.getName();
            boolean isFiltered = name.startsWith("industry_1");
            // boolean isFiltered = name.startsWith("industry")
            // || name.startsWith("recorded") ;

            if (!modelFile.isHidden() && !isFiltered) {
                if (!propertiesToConfirmingPages.containsKey(propertyName)) propertiesToConfirmingPages.put(propertyName, 0);
                logger.info("Checking " + modelFile.getName() + ".");
                String[] parts = checkModelFile(modelFile.getAbsolutePath()).split("\\|");
                
                int confPages = Integer.valueOf(parts[0]);
                int possibleProofs = Integer.valueOf(parts[1]);
                total += confPages;
                totalPossibleProofs += possibleProofs;
                if (confPages > 0 || possibleProofs > 0) {
                    successCount++;
                    propertiesToConfirmingPages.put(propertyName, propertiesToConfirmingPages.get(propertyName) + 1);
                }
                else {
                    
                    noSuccessCount++;
                    logger.info("Could not find anything for " + modelFile.getName());
                }
                logger.info("Intermediate summary:");
                logger.info("examples which have at least one confirming page or a possible proof: " + successCount);
                logger.info("examples without any confirming page: " + noSuccessCount);
                logger.info("total confirming pages: " + total);
                logger.info("total possible confirming pages: " + totalPossibleProofs);
            }
        }

        logger.info("Overall summary:");
        logger.info("examples which have at least one confirming page or a possible proof: " + successCount);
        logger.info("examples without any confirming page: " + noSuccessCount);
        logger.info("total confirming pages: " + total);
        for ( Map.Entry<String, Integer> entry : propertiesToConfirmingPages.entrySet()) {
            
            out.write(entry.getKey() + ": " + entry.getValue() + "\n");
        }
    }

    public static String checkModelFile(String modelFile) throws IOException {

        Model model = ModelFactory.createDefaultModel();
        model.read(new FileReader(modelFile), "", "TTL");
        DefactoModel defactoModel = new DefactoModel(model, "undefined", false);
        
        logger.info(defactoModel.getFact().toString());
        out.write("Checking: " + defactoModel.getFact().toString() + "." + "\n");
        return check(defactoModel);
    }

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {

        out = new BufferedWriter(new FileWriter("/Users/gerb/defacto-possible-proof.csv",true));
        
        PropertyConfigurator.configure("log/log4j.properties");
        System.out.println("Please check log/factConfirmation.html for logging output!");

        // pick one of the three options to test fact confirmation (manual
        // triple, model file, all training data)
        // checkTriple("http://dbpedia.org/resource/Barack_Obama",
        // "http://dbpedia.org/ontology/birthPlace",
        // "http://dbpedia.org/resource/Honolulu,_Hawaii");

        // checkModelFile("resources/training/data/true/alma_mater_0.ttl");
        // checkModelFile("resources/training/data/true/recorded_in_5.ttl");
        // checkModelFile("resources/training/data/true/industry_4.ttl");

        checkPositiveExamples();
        
        out.close();
    }

}
