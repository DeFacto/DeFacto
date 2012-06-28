package org.aksw.provenance.ml.feature.fact;

import java.util.List;
import java.util.Set;

import org.aksw.provenance.evidence.ComplexProof;
import org.aksw.provenance.evidence.Evidence;

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
