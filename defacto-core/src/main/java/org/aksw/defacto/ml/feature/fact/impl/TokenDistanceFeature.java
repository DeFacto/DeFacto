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
public class TokenDistanceFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.Feature#extractFeature(org.aksw.defacto.evidence.Evidence)
     */
    @Override
    public void extractFeature(ComplexProof complexProof, Evidence evidence) {

        complexProof.getFeatures().setValue(AbstractFactFeatures.TOKEN_DISTANCE, complexProof.getProofPhrase().split(" ").length);
        complexProof.getFeatures().setValue(AbstractFactFeatures.NUMBER_OF_NON_ALPHA_NUMERIC_CHARACTERS, 
        		complexProof.getProofPhrase().replaceAll("\\p{Alnum}", "").length());
    }
}
