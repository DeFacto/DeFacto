package org.aksw.provenance.ml.feature;

import java.util.Arrays;

import org.aksw.provenance.evidence.Evidence;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public interface Feature {
    
    public void extractFeature(Evidence evidence);
}
