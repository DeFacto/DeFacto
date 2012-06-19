package org.aksw.provenance.ml.feature.impl;

import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.ml.feature.AbstractFeature;

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
