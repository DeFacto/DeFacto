package org.aksw.defacto.ml.feature.impl;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.AbstractFeature;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class TotalHitCountFeature extends AbstractFeature {

    @Override
    public void extractFeature(Evidence evidence) {

        evidence.getFeatures().setValue(AbstractFeature.TOTAL_HIT_COUNT_FEATURE, evidence.getTotalHitCount());
    }
}
