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
public class PossessiveFeature implements FactFeature {

    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

    	proof.getFeatures().setValue(AbstractFactFeatures.POSSESSIVE_FEATURE, proof.getTinyContext().contains("'s") ? 1 : 0);
    }
}
