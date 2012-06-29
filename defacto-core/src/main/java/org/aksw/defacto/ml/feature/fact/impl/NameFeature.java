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
public class NameFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.fact.FactFeature#extractFeature(org.aksw.defacto.evidence.ComplexProof, java.util.Set)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        proof.getFeatures().setValue(AbstractFactFeatures.SUBJECT, proof.getSubject());
        proof.getFeatures().setValue(AbstractFactFeatures.PHRASE, proof.getProofPhrase().replaceAll("\\n", ""));
        proof.getFeatures().setValue(AbstractFactFeatures.OBJECT, proof.getObject());
        proof.getFeatures().setValue(AbstractFactFeatures.CONTEXT, proof.getContext().replaceAll("\\n", ""));
    }
}
