/**
 * 
 */
package org.aksw.provenance.ml.feature.fact.impl;

import java.util.List;
import java.util.Set;

import org.aksw.provenance.evidence.ComplexProof;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.ml.feature.fact.AbstractFactFeatures;
import org.aksw.provenance.ml.feature.fact.FactFeature;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class TotalOccurrenceFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.provenance.ml.feature.fact.FactFeature#extractFeature(org.aksw.provenance.evidence.ComplexProof)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        int numberOfOccurrences = 0;
        
        for ( ComplexProof complexProof : evidence.getComplexProofs())
            if ( complexProof.getNormalizedProofPhrase().equals(proof.getNormalizedProofPhrase()) ) numberOfOccurrences++;

        proof.getFeatures().setValue(AbstractFactFeatures.TOTAL_OCCURRENCE, numberOfOccurrences);
    }
}
