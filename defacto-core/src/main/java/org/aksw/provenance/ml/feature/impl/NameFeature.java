package org.aksw.provenance.ml.feature.impl;

import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.ml.feature.AbstractFeature;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NameFeature extends AbstractFeature {

    @Override
    public void extractFeature(Evidence evidence) {

        String name = evidence.getModel().getNsPrefixURI("name");
        evidence.getFeatures().setValue(AbstractFeature.MODEL_NAME, name);
    }
}
