/**
 * 
 */
package org.aksw.provenance.ml.feature.fact.impl;

import java.util.List;
import java.util.Set;

import org.aksw.provenance.evidence.ComplexProof;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.ml.feature.Feature;
import org.aksw.provenance.ml.feature.fact.AbstractFactFeatures;
import org.aksw.provenance.ml.feature.fact.FactFeature;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class TokenDistanceFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.provenance.ml.feature.Feature#extractFeature(org.aksw.provenance.evidence.Evidence)
     */
    @Override
    public void extractFeature(ComplexProof complexProof, Evidence evidence) {

        complexProof.getFeatures().setValue(AbstractFactFeatures.TOKEN_DISTANCE, complexProof.getProofPhrase().split(" ").length);
    }
}
