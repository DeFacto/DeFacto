/**
 * 
 */
package org.aksw.provenance.ml.feature.fact.impl;

import java.util.Set;

import org.aksw.provenance.evidence.ComplexProof;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.ml.feature.fact.AbstractFactFeatures;
import org.aksw.provenance.ml.feature.fact.FactFeature;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class ClassFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.provenance.ml.feature.fact.FactFeature#extractFeature(org.aksw.provenance.evidence.ComplexProof, java.util.Set)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        if ( evidence.getModel().getNsPrefixURI("name").contains("true") )
            proof.getFeatures().setValue(AbstractFactFeatures.CLASS, "true");
        else
            proof.getFeatures().setValue(AbstractFactFeatures.CLASS, "false");
    }
}
