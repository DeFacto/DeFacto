package org.aksw.provenance;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.provenance.boa.Pattern;
import org.aksw.provenance.cache.CacheManager;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.ml.feature.AbstractFeature;
import org.aksw.provenance.ml.feature.EvidenceFeatureExtractor;
import org.aksw.provenance.ml.feature.fact.AbstractFactFeatures;
import org.aksw.provenance.ml.feature.fact.FactFeatureExtraction;
import org.aksw.provenance.ml.feature.fact.FactScorer;
import org.aksw.provenance.ml.score.EvidenceScorer;
import org.aksw.provenance.search.crawl.EvidenceCrawler;
import org.aksw.provenance.search.fact.DefaultFactSearcher;
import org.aksw.provenance.search.fact.SubjectObjectFactSearcher;
import org.aksw.provenance.search.query.MetaQuery;
import org.aksw.provenance.search.query.QueryGenerator;
import org.aksw.provenance.util.TimeUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.LineSeparator;

import weka.core.Instance;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class FactChecker {

    private static int numberOfModels;
    private static int currentModel;
    private static long startTime;
    
    /**
     * @param model the model to check. this model may only contain the link between two resources
     * which needs to be checked and the labels (Constants.RESOURCE_LABEL) for the resources which means it
     * needs to contain only these three triples
     * 
     * @return
     */
    public static Evidence checkFact(Model model) {
        
        Logger logger = Logger.getLogger(FactChecker.class);

        // 1. generate the search engine queries
        long start = System.currentTimeMillis();
        QueryGenerator queryGenerator = new QueryGenerator(model);
        Map<Pattern,MetaQuery> queries = queryGenerator.getSearchEngineQueries();
        if ( queries.size() <= 0 ) return new Evidence(model); 
        logger.info("Preparing queries took " + TimeUtil.formatTime(System.currentTimeMillis() - start));
        
        // 2. download the search results in parallel
        long startCrawl = System.currentTimeMillis();
        EvidenceCrawler crawler = new EvidenceCrawler(model, queries);
        MetaQuery query = queries.values().iterator().next(); // every metaquery has the 
        Evidence evidence = crawler.crawlEvidence(query.getSubjectLabel(), query.getObjectLabel());
        logger.info("Crawling evidence took " + TimeUtil.formatTime(System.currentTimeMillis() - startCrawl));
        
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
        
//        // 7. score the model
//        long startScoring = System.currentTimeMillis();
//        EvidenceScorer scorer = new EvidenceScorer();
//        scorer.scoreEvidence(evidence);
//        logger.info("Scoring took " + TimeUtil.formatTime(System.currentTimeMillis() - startScoring));
        
        // 8. Log statistics
        System.out.println("Model " + currentModel + "/" + numberOfModels + " took " + TimeUtil.formatTime(System.currentTimeMillis() - start) +
                " Average time: " + ( (System.currentTimeMillis() - startTime) / currentModel++ ) + "ms");
        
        return evidence;
    }
    
    private static void writeFactTrainingDataFile() {

        try {
            
            BufferedWriter writer = new BufferedWriter(new FileWriter("resources/training/" + Constants.FACT_TRAINING_DATA_FILENAME));
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

    /**
     * 
     * @param models
     * @return
     */
    public static void checkFacts(List<Model> models) {

        // hack to get surface forms before timing
        SubjectObjectFactSearcher.getInstance();
        startTime = System.currentTimeMillis();
        numberOfModels = models.size();
        currentModel = 1;
        
        for (Model model : models) {
            
            Evidence evidence = checkFact(model);
            StmtIterator iter = model.listStatements();
            while (iter.hasNext()) System.out.println(iter.nextStatement());
            System.out.println("FactChecker: " + new DecimalFormat("0.00").format(evidence.getDeFactoScore()) + " % that this fact is true\n");
        }
        // rewrite the fact training file after every proof
        if ( Constants.WRITE_FACT_TRAINING_FILE ) writeFactTrainingDataFile();
        // rewrite the training file after every check triple
        if ( Constants.WRITE_TRAINING_FILE ) writeTrainingDataFile();
    }
    
    public static void main(String[] args) {

           System.out.println("\"\"\"\" asdjas \"\" \"".replaceAll("\"*", ""));
    }

    /**
     * 
     */
    private static void writeTrainingDataFile() {

        try {
            
            BufferedWriter writer = new BufferedWriter(new FileWriter("resources/training/arff/nominal/" + Constants.TRAINING_DATA_FILENAME));
            writer.write(AbstractFeature.provenance.toString());
            writer.flush();
            writer.close();
        }
        catch (IOException e) {

            e.printStackTrace();
        }        
    }
}
