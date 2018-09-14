package org.aksw.defacto.ml.feature.evidence.impl;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class TotalHitCountFeature extends AbstractEvidenceFeature {

    @Override
    public void extractFeature(Evidence evidence) {

        evidence.getFeatures().setValue(AbstractEvidenceFeature.TOTAL_HIT_COUNT_FEATURE, evidence.getTotalHitCount());
    }
}
