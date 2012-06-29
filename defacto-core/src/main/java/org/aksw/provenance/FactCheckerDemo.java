package org.aksw.provenance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.provenance.evidence.Evidence;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class FactCheckerDemo {

    private static Logger logger = Logger.getLogger(FactCheckerDemo.class);
    
    /**
     * @param args
     */
    public static void main(String[] args) {

        org.apache.log4j.PropertyConfigurator.configure("log/log4j.properties");
        FactChecker.checkFacts(getTrainingData());
    }
    
    public static List<Model> getTrainingData() {

        List<Model> models = new ArrayList<Model>();
        List<File> modelFiles = new ArrayList<File>(Arrays.asList(new File("resources/training/data/true").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/domain").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/range").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/domain_range").listFiles()));
        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/property").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/random").listFiles()));
        Collections.sort(modelFiles);
//        Collections.shuffle(modelFiles);
        List<String> confirmedFilenames = null;
        try {
            
            confirmedFilenames = FileUtils.readLines(new File("resources/training/confirmed_positives.txt"));
        }
        catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        for (File mappingFile : modelFiles) {
            
            // dont use svn files
            if ( !mappingFile.isHidden() && confirmedFilenames.contains(mappingFile.getName())) {//&&  models.size() < 100) {
                
                try {
                    
                    Model model = ModelFactory.createDefaultModel();
                    model.read(new FileReader(mappingFile), "", "TTL");
                    model.setNsPrefix("name", mappingFile.getParent().replace("resources/training/data/", "") + "/" +mappingFile.getName());

                    if ( mappingFile.getAbsolutePath().contains("data/true") ) {
                        
                        logger.info("Loading true triple from file: " + mappingFile.getName());
                        model.setNsPrefix("correct", "http://this.uri.is.useless/just_to_mark_model_as_correct");
                    }
                    else logger.info("Loading false triple from file: " + mappingFile.getName());
                        

                    models.add(model);
                }
                catch (FileNotFoundException e) {
                    
                    e.printStackTrace();
                }
            }
        }
        return models;
    }

    /**
     * @return a set of two models which contain each a fact and the appropriate labels for the resources
     */
    private static List<Model> getSampleData(){
        
        Model model1 = ModelFactory.createDefaultModel();
        
        Resource albert = model1.createResource("http://dbpedia.org/resource/Albert_Einstein");
        albert.addProperty(RDFS.label, "Albert Einstein");
        Resource ulm = model1.createResource("http://dbpedia.org/resource/Ulm");
        ulm.addProperty(RDFS.label, "Ulm");
        albert.addProperty(model1.createProperty("http://dbpedia.org/ontology/birthPlace"), ulm);
        
        Model model2 = ModelFactory.createDefaultModel();
        
        Resource quentin = model2.createResource("http://dbpedia.org/resource/Quentin_Tarantino");
        quentin.addProperty(RDFS.label, "Quentin Tarantino");
        Resource deathProof = model2.createResource("http://dbpedia.org/resource/Death_Proof");
        deathProof.addProperty(RDFS.label, "Death Proof");
        deathProof.addProperty(model2.createProperty("http://dbpedia.org/ontology/director"), quentin);
        
        List<Model> models = new ArrayList<Model>();
        models.add(model1);
        models.add(model2);
        
        return models;
    }
}
