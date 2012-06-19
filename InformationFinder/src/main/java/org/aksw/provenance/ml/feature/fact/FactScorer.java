/**
 * 
 */
package org.aksw.provenance.ml.feature.fact;

import java.io.BufferedReader;
import java.io.FileReader;

import org.aksw.provenance.evidence.ComplexProof;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.evidence.WebSite;
import org.aksw.provenance.ml.feature.AbstractFeature;
import org.aksw.provenance.util.ModelUtil;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class FactScorer {

    private Classifier classifier = null;
    
    public FactScorer() {
        
        this.classifier = loadClassifier();
    }
    
    public void scoreEvidence(Evidence evidence) {

        for ( ComplexProof proof : evidence.getComplexProofs() )
            try {
                
                Instances withoutStrings = new Instances(new BufferedReader(new FileReader("resources/training/defacto_fact_word.arff")));
                withoutStrings.setClassIndex(11);
//                withoutStrings.deleteAttributeAt(10);
//                withoutStrings.deleteAttributeAt(11);
//                withoutStrings.deleteAttributeAt(12);
//                withoutStrings.deleteAttributeAt(12);
//                withoutStrings.attribute(s)
                
                Instance newInstance = new Instance(proof.getFeatures());
                newInstance.deleteAttributeAt(10);
                newInstance.deleteAttributeAt(11);
                newInstance.deleteAttributeAt(12);
                newInstance.deleteAttributeAt(12);
                
                for ( int i = newInstance.numAttributes() ; i < withoutStrings.numAttributes(); i++) {
                    
                    String name = withoutStrings.attribute(i).name();
                    newInstance.insertAttributeAt(i);
                    newInstance.setValue(withoutStrings.attribute(i), proof.getProofPhrase().contains(name) ? 1D : 0D);
                }
                
                newInstance.setDataset(withoutStrings);
                withoutStrings.add(newInstance);
                
//                StringToWordVector filter = new StringToWordVector();
//                filter.setInputFormat(withoutStrings);
//                Instances dataFiltered = Filter.useFilter(withoutStrings, filter);
                
//                System.out.println(withoutStrings.toSummaryString());
//                System.out.println(newInstance.toString());
                
//                System.out.println(ModelUtil.getPropertyUri(proof.getModel()));
//                System.out.println(proof.getProofPhrase());
//                System.out.println(proof.getScore());
                
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
