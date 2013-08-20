package org.aksw.defacto;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.AbstractFeature;
import org.aksw.defacto.ml.feature.EvidenceFeatureExtractor;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeatureExtraction;
import org.aksw.defacto.ml.feature.fact.FactScorer;
import org.aksw.defacto.ml.score.EvidenceScorer;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.concurrent.NlpModelManager;
import org.aksw.defacto.search.crawl.EvidenceCrawler;
import org.aksw.defacto.search.fact.SubjectObjectFactSearcher;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.query.QueryGenerator;
import org.aksw.defacto.util.TimeUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import weka.core.Instance;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class Defacto {

    public enum TIME_DISTRIBUTION_ONLY{
    	
    	YES,
    	NO;
    }
	private static int numberOfModels;
    private static int currentModel;
    private static long startTime;
    public static DefactoConfig DEFACTO_CONFIG;
    public static TIME_DISTRIBUTION_ONLY onlyTimes;
    
    /**
     * @param model the model to check. this model may only contain the link between two resources
     * which needs to be checked and the labels (Constants.RESOURCE_LABEL) for the resources which means it
     * needs to contain only these three triples
     * 
     * @return
     */
    public static Evidence checkFact(DefactoModel model, TIME_DISTRIBUTION_ONLY onlyTimes) {
    	
    	init();
    	Defacto.onlyTimes = onlyTimes;
    	
    	// hack to get surface forms before timing
        SubjectObjectFactSearcher.getInstance();
        NlpModelManager.getInstance();
        
        Logger logger = Logger.getLogger(Defacto.class);
        logger.info("Checking fact: " + model);

        // 1. generate the search engine queries
        long start = System.currentTimeMillis();
        QueryGenerator queryGenerator = new QueryGenerator(model);
        Map<Pattern,MetaQuery> queries = new HashMap<Pattern,MetaQuery>();
        for ( String language : model.getLanguages() )
          queries.putAll(queryGenerator.getSearchEngineQueries(language));
          
        if ( queries.size() <= 0 ) return new Evidence(model); 
        logger.info("Preparing queries took " + TimeUtil.formatTime(System.currentTimeMillis() - start));
        
        // 2. download the search results in parallel
        long startCrawl = System.currentTimeMillis();
        EvidenceCrawler crawler = new EvidenceCrawler(model, queries);
        Evidence evidence = crawler.crawlEvidence();
        logger.info("Crawling evidence took " + TimeUtil.formatTime(System.currentTimeMillis() - startCrawl));
        
        // short cut to avoid 
        if ( onlyTimes.equals(TIME_DISTRIBUTION_ONLY.YES) ) return evidence;
        
        // 3. confirm the facts
        long startFactConfirmation = System.currentTimeMillis();
        FactFeatureExtraction factFeatureExtraction = new FactFeatureExtraction();
        factFeatureExtraction.extractFeatureForFact(evidence);
        logger.info("Fact feature extraction took " + TimeUtil.formatTime(System.currentTimeMillis() - startFactConfirmation));
        
        // 4. score the facts
        long startFactScoring = System.currentTimeMillis();
        FactScorer factScorer = new FactScorer();
        factScorer.scoreEvidence(evidence);
        logger.info("Scoring took " + TimeUtil.formatTime(System.currentTimeMillis() - startFactScoring));
        
        // 5. calculate the factFeatures for the model
        long startFeatureExtraction = System.currentTimeMillis();
        EvidenceFeatureExtractor featureCalculator = new EvidenceFeatureExtractor();
        featureCalculator.extractFeatureForEvidence(evidence);
        logger.info("Feature extraction took " + TimeUtil.formatTime(System.currentTimeMillis() - startFeatureExtraction));
        
        // 7. score the model
        if ( !Defacto.DEFACTO_CONFIG.getBooleanSetting("settings", "TRAINING_MODE") ) {

            long startScoring = System.currentTimeMillis();
            EvidenceScorer scorer = new EvidenceScorer();
            scorer.scoreEvidence(evidence);
            logger.info("Scoring took " + TimeUtil.formatTime(System.currentTimeMillis() - startScoring));
        }
        
//        String output = "Model " + currentModel + "/" + numberOfModels + " took " + TimeUtil.formatTime(System.currentTimeMillis() - start) +
//                " Average time: " + ( (System.currentTimeMillis() - startTime) / currentModel++ ) + "ms";
        
        // 8. Log statistics
//        System.out.println(output);
        
//        try {
//
//            DefactoEval.writer.write(output);
//            DefactoEval.writer.write("\n");
//            DefactoEval.writer.flush();
//        }
//        catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        
        return evidence;
    }
    
    public static void init(){
    	
    	try {
    		
			Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(new File("defacto.ini")));
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
     * @param models
     * @return
     */
    public static void checkFacts(List<DefactoModel> defactoModel) {

    	init();
        startTime       = System.currentTimeMillis();
        numberOfModels  = defactoModel.size();
        currentModel    = 1;
        
        for (DefactoModel model : defactoModel) {
            
            Evidence evidence = checkFact(model, TIME_DISTRIBUTION_ONLY.NO);
            
            // we want to print the score of the classifier 
            if ( !Defacto.DEFACTO_CONFIG.getBooleanSetting("settings", "TRAINING_MODE") ) 
                System.out.println("Defacto: " + new DecimalFormat("0.00").format(evidence.getDeFactoScore()) + " % that this fact is true! Actual: " + model.isCorrect() +"\n");
            
            // rewrite the training file after every checked triple
            if ( DEFACTO_CONFIG.getBooleanSetting("evidence", "OVERWRITE_EVIDENCE_TRAINING_FILE")  ) writeEvidenceTrainingDataFile();
        }
        // rewrite the fact training file after every proof
        if ( DEFACTO_CONFIG.getBooleanSetting("fact", "OVERWRITE_FACT_TRAINING_FILE") ) writeFactTrainingDataFile();
    }
    
    /**
     * 
     */
    private static void writeEvidenceTrainingDataFile() {

        try {
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_TRAINING_DATA_FILENAME") ));
            writer.write(AbstractFeature.provenance.toString());
            writer.flush();
            writer.close();
        }
        catch (IOException e) {

            e.printStackTrace();
        }        
    }
    
    /**
     * this tries to write an arff file which is also compatible with google docs spreadsheets
     */
    private static void writeFactTrainingDataFile() {

        try {
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(DEFACTO_CONFIG.getStringSetting("fact", "FACT_TRAINING_DATA_FILENAME")));
            writer.write(AbstractFactFeatures.factFeatures.toString().substring(0, AbstractFactFeatures.factFeatures.toString().indexOf("@data")));
            writer.write("\n@data\n");
            
            // add all instances to a list to shuffle them
            List<Instance> instances = new ArrayList<Instance>();
            for ( int i = 0; i < AbstractFactFeatures.factFeatures.numInstances() ; i++ ) instances.add(AbstractFactFeatures.factFeatures.instance(i));
            Collections.shuffle(instances);
            
            // temp collections
            List<Instance> pickedInstances = new ArrayList<Instance>();
            Set<Integer> randoms = new HashSet<Integer>();
            Map<String,Integer> modelsToProofsSize = new HashMap<String,Integer>();
            
            for ( Instance instance : instances ) {
                
                if ( pickedInstances.size() == 550) break;

                Integer random = (int)(Math.random() * ((AbstractFactFeatures.factFeatures.numInstances()) + 1)) - 1;
                if ( !randoms.contains(random) ) {
                    
                    randoms.add(random);
                    String type = instance.stringValue(AbstractFactFeatures.TRUE_FALSE_TYPE)
                            .substring(0, instance.stringValue(AbstractFactFeatures.TRUE_FALSE_TYPE).lastIndexOf("_"));
                    
                    if ( modelsToProofsSize.containsKey(type) ) {
                        
                        if ( modelsToProofsSize.get(type) < 10 ) {
                            
                            pickedInstances.add(instance);
                            modelsToProofsSize.put(type, modelsToProofsSize.get(type) + 1);
                        }
                    }
                    else {
                        
                        pickedInstances.add(instance);
                        modelsToProofsSize.put(type, 1);
                    }
                }
            }
            Collections.shuffle(pickedInstances);
            
            for (Instance instance : pickedInstances) {
                
                List<String> lines = new ArrayList<String>();
                for ( int i = 0; i < instance.numAttributes() ; i++ ) {
                    
                    if ( instance.attribute(i).isString() ) {
                        
                        String field = StringEscapeUtils.escapeCsv(instance.stringValue(instance.attribute(i)).replaceAll("\\n", ""));
                        field = field.replace("\"\"\"", "'");
                        field = field.replace("\"\"", "'");
                        field = field.replace("\"", "'");
                        if ( !field.startsWith("\"") ) field = "\"" + field;
                        if ( !field.endsWith("\"")) field = field + "\"";
                        lines.add(field);
                    }
                    else {
                        
                        if ( instance.attribute(i).isNumeric() )
                            lines.add(StringEscapeUtils.escapeCsv(instance.value(instance.attribute(i))+ ""));
                        else
                            lines.add(instance.stringValue(instance.attribute(i)) + "");
                    }
                }
                
                writer.write(StringUtils.join(lines, ",") + "\n");
            }
            
            writer.flush();
            writer.close();
        }
        catch (IOException e) {

            e.printStackTrace();
        }        
    }
}
