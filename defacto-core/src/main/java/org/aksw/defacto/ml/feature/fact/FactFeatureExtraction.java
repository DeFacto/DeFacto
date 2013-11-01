/**
 * 
 */
package org.aksw.defacto.ml.feature.fact;

import java.util.HashSet;
import java.util.Set;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.impl.GoodnessFeature;
import org.aksw.defacto.ml.feature.fact.impl.BoaFeature;
import org.aksw.defacto.ml.feature.fact.impl.ClassFeature;
import org.aksw.defacto.ml.feature.fact.impl.EndOfSentenceCharacterFeature;
import org.aksw.defacto.ml.feature.fact.impl.NameFeature;
import org.aksw.defacto.ml.feature.fact.impl.PageTitleFeature;
import org.aksw.defacto.ml.feature.fact.impl.PropertyFeature;
import org.aksw.defacto.ml.feature.fact.impl.SubjectObjectSimilarityFeature;
import org.aksw.defacto.ml.feature.fact.impl.TokenDistanceFeature;
import org.aksw.defacto.ml.feature.fact.impl.TotalOccurrenceFeature;
import org.aksw.defacto.ml.feature.fact.impl.WordnetExpensionFeature;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class FactFeatureExtraction {

    public static Set<FactFeature> factFeatures = new HashSet<FactFeature>();
    
    static {

        FactFeatureExtraction.factFeatures.add(new BoaFeature());
        FactFeatureExtraction.factFeatures.add(new EndOfSentenceCharacterFeature());
        FactFeatureExtraction.factFeatures.add(new PageTitleFeature());
        FactFeatureExtraction.factFeatures.add(new SubjectObjectSimilarityFeature());
        FactFeatureExtraction.factFeatures.add(new TokenDistanceFeature());
        FactFeatureExtraction.factFeatures.add(new TotalOccurrenceFeature());
        FactFeatureExtraction.factFeatures.add(new WordnetExpensionFeature());
        FactFeatureExtraction.factFeatures.add(new NameFeature());
        FactFeatureExtraction.factFeatures.add(new PropertyFeature());
        FactFeatureExtraction.factFeatures.add(new ClassFeature());
    }
    
    /**
     * 
     * @param evidence
     */
    public void extractFeatureForFact(Evidence evidence) {

        // score the collected evidence with every feature extractor defined
        for ( ComplexProof proof : evidence.getComplexProofs() ) {
            
            for ( FactFeature feature : FactFeatureExtraction.factFeatures ) {
                
                feature.extractFeature(proof, evidence);
            }
            // all features for this proof are completed so add it to the instances data
            // we only need to add the feature vector to the weka instances object if we plan to write the training file
            if ( Defacto.DEFACTO_CONFIG.getBooleanSetting("fact", "OVERWRITE_FACT_TRAINING_FILE") )
                AbstractFactFeatures.factFeatures.add(proof.getFeatures());
        }
    }
}
