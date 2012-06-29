/**
 * 
 */
package org.aksw.provenance.ml.feature.fact.impl;

import java.util.List;
import java.util.Set;

import org.aksw.expansion.WordNetExpansion;
import org.aksw.provenance.boa.Pattern;
import org.aksw.provenance.evidence.ComplexProof;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.ml.feature.fact.AbstractFactFeatures;
import org.aksw.provenance.ml.feature.fact.FactFeature;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class WordnetExpensionFeature implements FactFeature {

    WordNetExpansion wordnetExpansion = new WordNetExpansion("resources/wordnet/dict");
    
    /* (non-Javadoc)
     * @see org.aksw.provenance.ml.feature.fact.FactFeature#extractFeature(org.aksw.provenance.evidence.ComplexProof)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {
        
        double similarity = 0;
        
        if ( proof.getPattern() != null ) {
        
            similarity = wordnetExpansion.getExpandedJaccardSimilarity(proof.getProofPhrase(), proof.getPattern().normalize());
        }
        else {
            
            double maximum = 1;
            for (Pattern pattern : evidence.getBoaPatterns()) 
                maximum = Math.min(maximum, wordnetExpansion.getExpandedJaccardSimilarity(proof.getProofPhrase(), pattern.normalize()));
                    
            similarity = maximum;
        }
        
        if ( Double.isInfinite(similarity) || Double.isNaN(similarity) ) proof.getFeatures().setValue(AbstractFactFeatures.WORDNET_EXPANSION, 0D);
        else proof.getFeatures().setValue(AbstractFactFeatures.WORDNET_EXPANSION, similarity);
    }
}
