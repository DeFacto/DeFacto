/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;

import weka.core.Utils;


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

        proof.getFeatures().setValue(AbstractFactFeatures.SUBJECT, Utils.quote(proof.getSubject()));
        proof.getFeatures().setValue(AbstractFactFeatures.PHRASE, Utils.quote(proof.getProofPhrase()));
        proof.getFeatures().setValue(AbstractFactFeatures.OBJECT, Utils.quote(proof.getObject()));
        proof.getFeatures().setValue(AbstractFactFeatures.CONTEXT, Utils.quote(proof.getTinyContext()));
        proof.getFeatures().setValue(AbstractFactFeatures.FILE_NAME, proof.getModel().getName());
        proof.getFeatures().setValue(AbstractFactFeatures.LANGUAGE, proof.getLanguage());
    }
}
