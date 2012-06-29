/**
 * 
 */
package org.aksw.defacto.ml.feature.fact;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;

import weka.classifiers.Classifier;
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
            
            this.trainingInstances = new Instances(new BufferedReader(new FileReader("resources/training/arff/fact/defacto_fact_word.arff")));
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

        for ( ComplexProof proof : evidence.getComplexProofs() )
            try {
                
                Instances withoutStrings = new Instances(trainingInstances);
                withoutStrings.setClassIndex(11);
                
                // create new instance and delete debugging features
                Instance newInstance = new Instance(proof.getFeatures());
                newInstance.deleteAttributeAt(10);
                newInstance.deleteAttributeAt(11);
                newInstance.deleteAttributeAt(12);
                newInstance.deleteAttributeAt(12);
                
                // insert all the words which occur
                for ( int i = newInstance.numAttributes() ; i < withoutStrings.numAttributes(); i++) {
                    
                    String name = withoutStrings.attribute(i).name();
                    newInstance.insertAttributeAt(i);
                    newInstance.setValue(withoutStrings.attribute(i), proof.getProofPhrase().contains(name) ? 1D : 0D);
                }
                
                newInstance.setDataset(withoutStrings);
                withoutStrings.add(newInstance);
                
                proof.setScore(this.classifier.classifyInstance(newInstance));
            }
            catch (Exception e) {

                e.printStackTrace();
                System.exit(0);
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
            
            return (Classifier) weka.core.SerializationHelper.read("resources/classifier/fact/fact.model");
        }
        catch (Exception e) {

            throw new RuntimeException("Could not load classifier from: " + "resources/classifier/fact/fact.model", e);
        }
    }
}
