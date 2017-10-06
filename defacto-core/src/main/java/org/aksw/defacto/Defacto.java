package org.aksw.defacto;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.ml.feature.evidence.EvidenceFeatureExtractor;
import org.aksw.defacto.ml.feature.evidence.EvidenceScorer;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeatureExtraction;
import org.aksw.defacto.ml.feature.fact.FactScorer;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.crawl.EvidenceCrawler;
import org.aksw.defacto.search.engine.SearchEngine;
import org.aksw.defacto.search.engine.bing.AzureBingSearchEngine;
import org.aksw.defacto.search.engine.localcorpora.wikipedia.WikiSearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.query.QueryGenerator;
import org.aksw.defacto.util.BufferedFileWriter;
import org.aksw.defacto.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.defacto.util.Encoder.Encoding;
import org.aksw.defacto.util.TimeUtil;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instance;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class Defacto {

    public enum TIME_DISTRIBUTION_ONLY{
    	
    	YES,
    	NO;
    }
    
    public static DefactoConfig DEFACTO_CONFIG;
    public static TIME_DISTRIBUTION_ONLY onlyTimes;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Defacto.class);
    public static org.apache.log4j.Logger LOGDEV    = org.apache.log4j.Logger.getLogger("developer");
    private static final boolean searchCounterargument = false;
    private static boolean foundCounterargument = true;

    /**
     * @param model the model to check. this model may only contain the link between two resources
     * which needs to be checked and the labels (Constants.RESOURCE_LABEL) for the resources which means it
     * needs to contain only these three triples
     * 
     * @return
     */
    public static Evidence checkFact(DefactoModel model, TIME_DISTRIBUTION_ONLY onlyTimes) throws Exception {
    	
    	init();
    	LOGGER.info("Checking fact: " + model);
    	Defacto.onlyTimes = onlyTimes;

        /*********************************************************************************************************************
         [1] generate the search engine queries
         *********************************************************************************************************************/

        LOGGER.debug("[1] -> starting generating the search engines queries");

        long start = System.currentTimeMillis();
        QueryGenerator queryGenerator = new QueryGenerator(model);
        Map<Pattern,MetaQuery> queries = new HashMap<>();
        for ( String language : model.languages ) {
            Map<Pattern,MetaQuery> q = queryGenerator.getSearchEngineQueries(language);
            queries.putAll(q);
        }
        if ( queries.size() <= 0 ) {
            LOGDEV.debug("none query has been generated for the model!");
            return new Evidence(model);
        }
        LOGDEV.debug("-> Preparing queries took " + TimeUtil.formatTime(System.currentTimeMillis() - start));

        Map<Pattern, MetaQuery> queries2 = new HashMap<>();
        if (searchCounterargument) {
            // counterargument
            for (String language : model.languages) {
                if (language.equals("en")) { //currently just english
                    Map<Pattern, MetaQuery> q = queryGenerator.getCounterExampleSearchEngineQueries(language);
                    queries2.putAll(q);
                }
            }
        }

        /*********************************************************************************************************************
         [2] download the search results in parallel
        *********************************************************************************************************************/

        LOGDEV.debug("[2] -> downloading the search results in parallel");

        // crawl evidence using a defined search engine designed for a corpora (internet or local corpora)
        SearchEngine engine = new AzureBingSearchEngine();
        //SearchEngine engine2 = new WikiSearchEngine(); //local corpora tests

        // download the search results in parallel
        long startCrawl = System.currentTimeMillis();
        EvidenceCrawler crawler = new EvidenceCrawler(model, queries);
        Evidence evidence = crawler.crawlEvidence(engine);
        evidence.setQueries(queries);
        LOGDEV.debug(" -> crawling evidence took " + TimeUtil.formatTime(System.currentTimeMillis() - startCrawl));

        Evidence evidence2;
        if (searchCounterargument) {
            // counterargument
            startCrawl = System.currentTimeMillis();
            EvidenceCrawler crawler2 = new EvidenceCrawler(model, queries2);
            evidence2 = crawler2.crawlEvidence(engine);
            LOGGER.info("Crawling (counter argument) evidence took " + TimeUtil.formatTime(System.currentTimeMillis() - startCrawl));

            //short cut to avoid unnecessary computation
            foundCounterargument = (evidence2.getAllWebSites().size() > 0);
        }

        //nothing has been found to corroborate with the negated claim
        if (!foundCounterargument){
            evidence.setDeFactoCounterargumentScore(0.0d);
        }
        // short cut to avoid unnecessary computation
        if ( onlyTimes.equals(TIME_DISTRIBUTION_ONLY.YES) ) return evidence;

        /*********************************************************************************************************************
         [1] extracting structured linked data content
         *********************************************************************************************************************/


        /*********************************************************************************************************************
         [3] confirm the facts
         *********************************************************************************************************************/

        LOGDEV.debug("[3] -> confirming the facts");

        long startFactConfirmation = System.currentTimeMillis();
        FactFeatureExtraction factFeatureExtraction = new FactFeatureExtraction();
        factFeatureExtraction.extractFeatureForFact(evidence);
        LOGGER.info("Fact Feature extraction took " + TimeUtil.formatTime(System.currentTimeMillis() - startFactConfirmation));


        if (searchCounterargument && foundCounterargument) {
            startFactConfirmation = System.currentTimeMillis();
            FactFeatureExtraction factFeatureExtraction2 = new FactFeatureExtraction();
            factFeatureExtraction2.extractFeatureForFact(evidence2);
            LOGGER.info("Fact Feature (counterargument) extraction took " + TimeUtil.formatTime(System.currentTimeMillis() - startFactConfirmation));
        }

        /*********************************************************************************************************************
         [4] score the facts
         *********************************************************************************************************************/

        LOGDEV.debug("[3] -> scoring the facts");

        // score the facts
        long startFactScoring = System.currentTimeMillis();
        FactScorer factScorer = new FactScorer();
        factScorer.scoreEvidence(evidence);
        LOGGER.info("Fact Scoring took " + TimeUtil.formatTime(System.currentTimeMillis() - startFactScoring));

        if (searchCounterargument && foundCounterargument) {
            startFactScoring = System.currentTimeMillis();
            FactScorer factScorer2 = new FactScorer();
            factScorer2.scoreEvidence(evidence2);
            LOGGER.info("Fact Scoring (counterargument) took " + TimeUtil.formatTime(System.currentTimeMillis() - startFactScoring));
        }
        /*********************************************************************************************************************
         [5] calculate the factFeatures for the model
         *********************************************************************************************************************/

        LOGDEV.debug("[3] -> computing the fact features");

    	// calculate the factFeatures for the model
        long startFeatureExtraction = System.currentTimeMillis();
        EvidenceFeatureExtractor featureCalculator = new EvidenceFeatureExtractor();
        featureCalculator.extractFeatureForEvidence(evidence);
        LOGGER.info("Evidence feature extraction took " + TimeUtil.formatTime(System.currentTimeMillis() - startFeatureExtraction));

        if (searchCounterargument && foundCounterargument) {
            long startFeatureExtraction2 = System.currentTimeMillis();
            EvidenceFeatureExtractor featureCalculator2 = new EvidenceFeatureExtractor();
            featureCalculator2.extractFeatureForEvidence(evidence2);
            LOGGER.info("Evidence Feature extraction (counterargument) took " + TimeUtil.formatTime(System.currentTimeMillis() - startFeatureExtraction2));
        }


        if ( !Defacto.DEFACTO_CONFIG.getBooleanSetting("settings", "TRAINING_MODE") ) {

            /*********************************************************************************************************************
             [6] score the evidences
             *********************************************************************************************************************/

            long startScoring = System.currentTimeMillis();
            EvidenceScorer scorer = new EvidenceScorer();
            scorer.scoreEvidence(evidence);
            LOGGER.info("Evidence Scoring took " + TimeUtil.formatTime(System.currentTimeMillis() - startScoring));

            if (searchCounterargument && foundCounterargument) {
                long startScoring2 = System.currentTimeMillis();
                EvidenceScorer scorer2 = new EvidenceScorer();
                scorer2.scoreEvidence(evidence2);
                LOGGER.info("Evidence Scoring (counterargument) took " + TimeUtil.formatTime(System.currentTimeMillis() - startScoring2));
            }
        }

        if (searchCounterargument && foundCounterargument) {
            evidence.setDeFactoCounterargumentScore(evidence2.getDeFactoScore());
            evidence.setNegativeEvidenceObject(evidence2);
        }

        LOGGER.info("Overall time for fact: " +  TimeUtil.formatTime(System.currentTimeMillis() - start));

        //returning enhanced evidence (we should later merge the positive and negative evidences in one object)
        return evidence;
    }
    
    public static void writeFactTrainingFiles(String filename) {
    	
        // rewrite the fact training file after every proof
        if ( DEFACTO_CONFIG.getBooleanSetting("fact", "OVERWRITE_FACT_TRAINING_FILE") ) writeFactTrainingDataFile(filename);
    }
    
    public static void writeEvidenceTrainingFiles(String filename) {
    	
    	// rewrite the training file after every checked triple
        if ( DEFACTO_CONFIG.getBooleanSetting("evidence", "OVERWRITE_EVIDENCE_TRAINING_FILE")  ) writeEvidenceTrainingDataFile(filename);
    }
    
    public static void init(){
    	
    	try {
    		
    		if ( Defacto.DEFACTO_CONFIG  == null )
    			Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(new File(Defacto.class.getResource("/defacto.ini").getFile())));
    		
		} catch (InvalidFileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    

    /**
     * 
     * @param defactoModel
     * @return
     */
    public static Map<DefactoModel,Evidence> checkFacts(List<DefactoModel> defactoModel, TIME_DISTRIBUTION_ONLY onlyTimeDistribution) throws Exception {

    	init();
        
        Map<DefactoModel,Evidence> evidences = new HashMap<DefactoModel, Evidence>();
        
        for (DefactoModel model : defactoModel) {
        	
            Evidence evidence = checkFact(model, onlyTimeDistribution);
            evidences.put(model, evidence);
            
            // we want to print the score of the classifier 
            if ( !Defacto.DEFACTO_CONFIG.getBooleanSetting("settings", "TRAINING_MODE") ) 
                LOGGER.info("Defacto: " + new DecimalFormat("0.00").format(evidence.getDeFactoScore()) + " % that this fact is true! Actual: " + model.isCorrect() +"\n");

            // rewrite the fact training file after every proof
            if ( DEFACTO_CONFIG.getBooleanSetting("fact", "OVERWRITE_FACT_TRAINING_FILE") ) 
            	writeFactTrainingDataFile(DEFACTO_CONFIG.getStringSetting("evidence", "FACT_TRAINING_DATA_FILENAME"));
            
            // rewrite the training file after every checked triple
            if ( DEFACTO_CONFIG.getBooleanSetting("evidence", "OVERWRITE_EVIDENCE_TRAINING_FILE")  ) 
            	writeEvidenceTrainingDataFile(DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_TRAINING_DATA_FILENAME"));
        }
        
        return evidences;
    }
    
	/**
     * 
     */
    private static void writeEvidenceTrainingDataFile(String filename) {

        BufferedFileWriter writer = new BufferedFileWriter(DefactoConfig.DEFACTO_DATA_DIR + filename, Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
        writer.write(AbstractEvidenceFeature.provenance.toString());
        writer.close();
    }
    
    /**
     * this tries to write an arff file which is also compatible with google docs spreadsheets
     */
    private static void writeFactTrainingDataFile(String filename) {

        try {
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(DefactoConfig.DEFACTO_DATA_DIR + filename));
            writer.write(AbstractFactFeatures.factFeatures.toString().substring(0, AbstractFactFeatures.factFeatures.toString().indexOf("@data")));
            writer.write("\n@data\n");

//            // add all instances to a list to shuffle them
//            List<Instance> instances = new ArrayList<Instance>();
//            for ( int i = 0; i < AbstractFactFeatures.factFeatures.numInstances() ; i++ ) instances.add(AbstractFactFeatures.factFeatures.instance(i));
////            Collections.shuffle(instances);
//            
//            // temp collections
//            List<Instance> pickedInstances = new ArrayList<Instance>();
//            Set<Integer> randoms = new HashSet<Integer>();
//            Map<String,Integer> modelsToProofsSize = new HashMap<String,Integer>();
//            
//            int numberOfProofsPerRelation = Integer.MAX_VALUE;
////            int numberOfProofsPerRelation = 50;
//            int maxNumberOfFacts = Integer.MAX_VALUE;
//            
//            for ( Instance instance : instances ) {
//            	
//            	String type = instance.stringValue(AbstractFactFeatures.FILE_NAME).substring(0, instance.stringValue(AbstractFactFeatures.FILE_NAME).lastIndexOf("/"));
//            	type = type.replace("property/", "");
//            	type = type.replace("domainrange/", "");
//            	type = type.replace("domain/", "");
//            	type = type.replace("range/", "");
//            	type = type.replace("random/", "");
//            	
//            	if ( modelsToProofsSize.containsKey(type) ) {
//                    
//                    if ( modelsToProofsSize.get(type) < numberOfProofsPerRelation ) {
//                        
//                        pickedInstances.add(instance);
//                        modelsToProofsSize.put(type, modelsToProofsSize.get(type) + 1);
//                    }
//                }
//                else {
//                    
//                    pickedInstances.add(instance);
//                    modelsToProofsSize.put(type, 1);
//                }
//            }
            
//            System.out.println("\n----------");
//            System.out.println(modelsToProofsSize.size());
//            for ( Map.Entry<String, Integer> entry : modelsToProofsSize.entrySet()) {
//            	
//            	System.out.println(entry.getKey() + ": " + entry.getValue());
//            }
//            
//            System.out.println("----------\n");
            
//            while ( pickedInstances.size() <= maxNumberOfFacts && pickedInstances.size() < AbstractFactFeatures.factFeatures.numInstances()) {
//
//                Integer random = new Integer((int)((AbstractFactFeatures.factFeatures.numInstances()) * Math.random()));
//                Instance instance = AbstractFactFeatures.factFeatures.instance(random);
//                
//                if ( !randoms.contains(random) ) {
//                    
//                    randoms.add(random);
//                    String type = instance.stringValue(AbstractFactFeatures.FILE_NAME).substring(0, instance.stringValue(AbstractFactFeatures.FILE_NAME).lastIndexOf("_"));
//                    type += String.valueOf(instance.stringValue(AbstractFactFeatures.factFeatures.attribute("class")));
//                    
//                    if ( modelsToProofsSize.containsKey(type) ) {
//                        
//                        if ( modelsToProofsSize.get(type) < numberOfProofsPerRelation ) {
//                            
//                            pickedInstances.add(instance);
//                            modelsToProofsSize.put(type, modelsToProofsSize.get(type) + 1);
//                        }
//                    }
//                    else {
//                        
//                        pickedInstances.add(instance);
//                        modelsToProofsSize.put(type, 1);
//                    }
//                }
//            }
//            Collections.shuffle(pickedInstances);
            
            Enumeration<Instance> enumerateInstances = AbstractFactFeatures.factFeatures.enumerateInstances();
//            for (Instance instance : pickedInstances) {
            while ( enumerateInstances.hasMoreElements() ) {
//            	writer.write(instance.toString() + "\n");
            	writer.write(enumerateInstances.nextElement().toString() + "\n");
            }
            
            writer.flush();
            writer.close();
        }
        catch (IOException e) {

            e.printStackTrace();
        }        
    }
    
    public static void main(String[] args) {
		
    	int max = 0;
    	int min = 1000;
    	
    	for ( int i = 0; i < 1000 ; i++) {

    		int j = new Integer(0 + (int)((9 - 0 + 1) * Math.random()));
    		
    		max = Math.max(max, j);
    		min = Math.min(min, j);
    	}

        LOGGER.debug(("MAX: " + max));
        LOGGER.debug(("MIN: " + min));
	}
}
