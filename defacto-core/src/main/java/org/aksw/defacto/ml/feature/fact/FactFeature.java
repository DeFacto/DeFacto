package org.aksw.defacto.ml.feature.fact;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public interface FactFeature {

    /**
     * 
     * @param evidence
     */
    void extractFeature(ComplexProof proof, Evidence evidence);
}
