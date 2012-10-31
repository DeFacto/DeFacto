package org.aksw.defacto.ml.feature.impl;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.AbstractFeature;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NameFeature extends AbstractFeature {

    @Override
    public void extractFeature(Evidence evidence) {

        evidence.getFeatures().setValue(AbstractFeature.MODEL_NAME, evidence.getModel().getName());
    }
}
