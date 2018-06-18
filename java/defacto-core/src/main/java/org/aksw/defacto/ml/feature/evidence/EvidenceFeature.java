package org.aksw.defacto.ml.feature.evidence;

import org.aksw.defacto.evidence.Evidence;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public interface EvidenceFeature {
    
    public void extractFeature(Evidence evidence);
}
