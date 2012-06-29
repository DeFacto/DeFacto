/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class ClassFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.fact.FactFeature#extractFeature(org.aksw.defacto.evidence.ComplexProof, java.util.Set)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        if ( evidence.getModel().getNsPrefixURI("name").contains("true") )
            proof.getFeatures().setValue(AbstractFactFeatures.CLASS, "true");
        else
            proof.getFeatures().setValue(AbstractFactFeatures.CLASS, "false");
    }
}
