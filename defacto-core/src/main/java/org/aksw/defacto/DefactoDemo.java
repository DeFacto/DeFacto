package org.aksw.defacto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aksw.defacto.config.DefactoConfig;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class DefactoDemo {

    private static Logger logger = Logger.getLogger(DefactoDemo.class);
    
    /**
     * @param args
     * @throws IOException 
     * @throws InvalidFileFormatException 
     */
    public static void main(String[] args) throws InvalidFileFormatException, IOException {

        org.apache.log4j.PropertyConfigurator.configure("log/log4j.properties");
        Defacto.checkFacts(new DefactoConfig(new Ini(new File("defacto.ini"))), getTrainingData());
    }
    
    public static List<DefactoModel> getTrainingData() {

        List<DefactoModel> models = new ArrayList<DefactoModel>();
        List<File> modelFiles = new ArrayList<File>(Arrays.asList(new File("resources/training/data/true").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/domain").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/range").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/domain_range").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/property").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/random").listFiles()));
        Collections.sort(modelFiles);
//        Collections.shuffle(modelFiles);
        List<String> confirmedFilenames = null;
        try {
            
            confirmedFilenames = FileUtils.readLines(new File("resources/properties/confirmed_properties.txt"));
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
                    String name = mappingFile.getParent().replace("resources/training/data/", "") + "/" +mappingFile.getName();
                    boolean isCorrect = false;

                    if ( mappingFile.getAbsolutePath().contains("data/true") ) isCorrect = true;
                    logger.info("Loading "+isCorrect+" triple from file: " + mappingFile.getName());
                        
                    models.add(new DefactoModel(model, name, isCorrect));
                }
                catch (FileNotFoundException e) {
                    
                    e.printStackTrace();
                }
            }
        }
        Collections.shuffle(models);
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
