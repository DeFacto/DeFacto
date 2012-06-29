package org.aksw.defacto.ml.feature;

import org.aksw.defacto.evidence.Evidence;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public interface Feature {
    
    public void extractFeature(Evidence evidence);
}
