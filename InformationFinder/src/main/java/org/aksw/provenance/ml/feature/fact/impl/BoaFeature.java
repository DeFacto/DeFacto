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
public class BoaFeature implements FactFeature {

    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        // we set this to 0 and over write it if we find a pattern
        proof.getFeatures().setValue(AbstractFactFeatures.BOA_BOOLEAN, 0);
        proof.getFeatures().setValue(AbstractFactFeatures.BOA_SCORE, 0);

        if ( proof.getPattern() != null ) {
            
            proof.getFeatures().setValue(AbstractFactFeatures.BOA_BOOLEAN, 1);
            proof.getFeatures().setValue(AbstractFactFeatures.BOA_SCORE, proof.getPattern().boaScore);
        }
    }
}
