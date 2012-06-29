/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class TotalOccurrenceFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.fact.FactFeature#extractFeature(org.aksw.defacto.evidence.ComplexProof)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        int numberOfOccurrences = 0;
        
        for ( ComplexProof complexProof : evidence.getComplexProofs())
            if ( complexProof.getNormalizedProofPhrase().equals(proof.getNormalizedProofPhrase()) ) numberOfOccurrences++;

        proof.getFeatures().setValue(AbstractFactFeatures.TOTAL_OCCURRENCE, numberOfOccurrences);
    }
}
