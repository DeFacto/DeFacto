/**
 * 
 */
package org.aksw.defacto.ml.feature.fact;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class FactScorer {

    private Classifier classifier       = null;
    private Instances trainingInstances = null;

    /**
     * 
     */
    public FactScorer() {
        
        this.classifier = loadClassifier();
        try {
            
//            this.trainingInstances = new Instances(new BufferedReader(new FileReader(
//            		loadFileName("/training/arff/fact/defacto_fact_word.arff"))));
        	this.trainingInstances = new Instances(new BufferedReader(new FileReader(
        			DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("fact", "ARFF_TRAINING_DATA_FILENAME"))));
        }
        catch (FileNotFoundException e) {

            throw new RuntimeException(e);
        }
        catch (IOException e) {
            
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 
     * @param evidence
     */
    public void scoreEvidence(Evidence evidence) {

    	Instances instancesWithStringVector = new Instances(trainingInstances);
        instancesWithStringVector.setClassIndex(26);
    	
        for ( ComplexProof proof : evidence.getComplexProofs() ) {
        	
            try {
                
                // create new instance and delete debugging features
                Instance newInstance = new Instance(proof.getFeatures());
                newInstance.deleteAttributeAt(27);
                newInstance.deleteAttributeAt(27);
                newInstance.deleteAttributeAt(27);
                newInstance.deleteAttributeAt(27);
                newInstance.deleteAttributeAt(27);
                
                // insert all the words which occur
                for ( int i = 26 + 1 ; i < instancesWithStringVector.numAttributes(); i++) {
                    
                	List<String> parts = Arrays.asList(proof.getTinyContext().split(" "));
                    newInstance.insertAttributeAt(i);
                    Attribute attribute = instancesWithStringVector.attribute(i);
                    newInstance.setValue(attribute, parts.contains(attribute.name()) ? 1D : 0D);
                }
                newInstance.setDataset(instancesWithStringVector);
                instancesWithStringVector.add(newInstance);
                
                proof.setScore(this.classifier.distributionForInstance(newInstance)[0]);
//                System.out.println(proof.getScore() + " -> " + this.classifier.classifyInstance(newInstance) + " -> " + proof.getTinyContext());
                
                // remove the new instance again
                instancesWithStringVector.delete();
            }
            catch (Exception e) {

                e.printStackTrace();
                System.exit(0);
            }
        }
        
        // set for each website the score by multiplying the proofs found on this site
        for ( WebSite website : evidence.getAllWebSites() ) {
            
            double score = 1D;
            
            for ( ComplexProof proof : evidence.getComplexProofs(website)) {

                score *= ( 1D - proof.getScore() );
            }
            website.setScore(1 - score);
        }
    }
    
    /**
     * 
     * @return
     */
    private Classifier loadClassifier() {

        try {
            
            return (Classifier) weka.core.SerializationHelper.read(
            		DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("fact", "FACT_CLASSIFIER_TYPE"));
        }
        catch (Exception e) {

            throw new RuntimeException("Could not load classifier from: " + 
            		DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("fact", "FACT_CLASSIFIER_TYPE"), e);
        }
    }
    
    public String loadFileName(String name){
    	
    	return new File(FactScorer.class.getResource(name).getFile()).getAbsolutePath(); 
    }
    
    public static void main(String[] args) {
		
    	System.out.println("-"+ "".split(";").length + "-");
	}
}
