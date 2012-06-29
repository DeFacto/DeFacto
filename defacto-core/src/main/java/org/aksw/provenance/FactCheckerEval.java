/**
 * 
 */
package org.aksw.provenance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aksw.provenance.cache.CacheManager;
import org.aksw.provenance.ml.feature.AbstractFeature;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import weka.core.Instance;
import weka.core.Instances;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class FactCheckerEval {

    private static Logger logger = Logger.getLogger(FactCheckerDemo.class);

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        
        org.apache.log4j.PropertyConfigurator.configure("log/log4j.properties");
        List<String> pathToFalseData = new ArrayList<String>(Arrays.asList("domain", "range", "domain_range", "property", "random"));

        for (String falseDataDir : pathToFalseData) {
            
            AbstractFeature.provenance = new Instances("defacto", AbstractFeature.attributes, 0);
            Constants.TRAINING_DATA_FILENAME = falseDataDir + "_defacto_evidence.arff"; 
            FactChecker.checkFacts(getTrainingData(falseDataDir));
        }
        
        CacheManager.getInstance().closeConnection();
    }

    private static List<Model> getTrainingData(String pathToFalseTrainingDirectory) throws IOException {

        List<File> modelFiles = new ArrayList<File>(Arrays.asList(new File("resources/training/data/true").listFiles()));
        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/" + pathToFalseTrainingDirectory).listFiles()));
        Collections.sort(modelFiles);
        
        List<String> confirmedFilenames = FileUtils.readLines(new File("resources/training/confirmed_positives.txt"));
        List<Model> models = new ArrayList<Model>();
        
        for (File mappingFile : modelFiles) {

            // dont use svn files
            if (!mappingFile.isHidden() && confirmedFilenames.contains(mappingFile.getName())) {
                
                try {

                    Model model = ModelFactory.createDefaultModel();
                    model.read(new FileReader(mappingFile), "", "TTL");
                    model.setNsPrefix("name", mappingFile.getParent().replace("resources/training/data/", "") + "/" + mappingFile.getName());

                    if (mappingFile.getAbsolutePath().contains("data/true")) {

                        logger.info("Loading true triple from file: " + mappingFile.getName());
                        model.setNsPrefix("correct", "http://this.uri.is.useless/just_to_mark_model_as_correct");
                    }
                    else
                        logger.info("Loading false triple from file: " + mappingFile.getName());

                    models.add(model);
                }
                catch (FileNotFoundException e) {

                    e.printStackTrace();
                }
            }
        }
        return models;
    }

}
