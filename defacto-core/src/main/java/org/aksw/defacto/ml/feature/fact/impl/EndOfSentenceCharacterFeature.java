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
public class EndOfSentenceCharacterFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.fact.FactFeature#extractFeature(org.aksw.defacto.evidence.ComplexProof)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        proof.getFeatures().setValue(AbstractFactFeatures.END_OF_SENTENCE_DOT, proof.getProofPhrase().contains(".") ? 1 : 0 );
        proof.getFeatures().setValue(AbstractFactFeatures.END_OF_SENTENCE_EXCLAMATION_MARK, proof.getProofPhrase().contains("!") ? 1 : 0 );
        proof.getFeatures().setValue(AbstractFactFeatures.END_OF_SENTENCE_QUESTION_MARK, proof.getProofPhrase().contains("?") ? 1 : 0 );
//        proof.getFeatures().setValue(AbstractFactFeatures.NUMBER_OF_NON_ALPHA_NUMERIC_CHARACTERS, proof.getProofPhrase().replaceAll("[A-z0-9]",     "").length() );
    }
}
