/**
 * 
 */
package org.aksw.defacto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aksw.defacto.Defacto.TIME_DISTRIBUTION_ONLY;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.model.DefactoModel;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import weka.core.Instances;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class DefactoEval {

    private static Logger logger = Logger.getLogger(DefactoDemo.class);
    public static BufferedWriter writer = null;

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        
        writer = new BufferedWriter(new FileWriter("log/progess.txt"));
        org.apache.log4j.PropertyConfigurator.configure("log/log4j.properties");
        
        List<String> pathToFalseData = new ArrayList<String>(Arrays.asList("domain", "range", "domain_range", "property", "random", "true"));

        for (String falseDataDir : pathToFalseData) {
            
            AbstractEvidenceFeature.provenance = new Instances("defacto", AbstractEvidenceFeature.attributes, 0);
            Defacto.DEFACTO_CONFIG.setStringSetting("evidence", "EVIDENCE_TRAINING_DATA_FILENAME", "resources/training/arff/evidence/" + falseDataDir + "_defacto_evidence.arff");
            logger.info("Checking facts for from: " + falseDataDir);
            writer.write("Checking facts from: " + falseDataDir + " (" + (pathToFalseData.indexOf(falseDataDir) + 1) + " of " + pathToFalseData.size() + " testsets)\n");
            Defacto.checkFacts(getTrainingData(falseDataDir), TIME_DISTRIBUTION_ONLY.NO);
        }
        writer.close();
        
//        CacheManager.getInstance().closeConnection();
    }

    private static List<DefactoModel> getTrainingData(String pathToFalseTrainingDirectory) throws IOException {

        List<File> modelFiles = new ArrayList<File>();//Arrays.asList(new File("resources/training/data/true").listFiles()));
        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/" + pathToFalseTrainingDirectory).listFiles()));
        Collections.sort(modelFiles);

        List<String> confirmedFilenames = FileUtils.readLines(new File("resources/properties/confirmed_properties_master.txt"));
        List<DefactoModel> models = new ArrayList<DefactoModel>();
        
        for (File mappingFile : modelFiles) {

            // dont use svn files
            if (!mappingFile.isHidden() && confirmedFilenames.contains(mappingFile.getName())) {
                
                try {
                    
                    Model model = ModelFactory.createDefaultModel();
                    model.read(new FileReader(mappingFile), "", "TTL");
                    String name = mappingFile.getParent().replace("resources/training/data/", "") + "/" + mappingFile.getName();
                    boolean isCorrect = false;

                    if (mappingFile.getAbsolutePath().contains("false/true")) isCorrect = true;
                    logger.info("Loading "+isCorrect+" triple from file: " + mappingFile.getName() + " in directory: " +pathToFalseTrainingDirectory );

                    models.add(new DefactoModel(model, name, isCorrect, Arrays.asList("en")));
                }
                catch (FileNotFoundException e) {

                    e.printStackTrace();
                }
            }
        }
        Collections.shuffle(models);
        return models;
    }
}