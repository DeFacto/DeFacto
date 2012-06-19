/**
 * 
 */
package org.aksw.provenance.ml.feature.fact.impl;

import java.util.List;
import java.util.Set;

import org.aksw.provenance.evidence.ComplexProof;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.ml.feature.fact.AbstractFactFeatures;
import org.aksw.provenance.ml.feature.fact.FactFeature;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class EndOfSentenceCharacterFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.provenance.ml.feature.fact.FactFeature#extractFeature(org.aksw.provenance.evidence.ComplexProof)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        proof.getFeatures().setValue(AbstractFactFeatures.END_OF_SENTENCE_DOT, proof.getProofPhrase().contains(".") ? 1 : 0 );
        proof.getFeatures().setValue(AbstractFactFeatures.END_OF_SENTENCE_EXCLAMATION_MARK, proof.getProofPhrase().contains("!") ? 1 : 0 );
        proof.getFeatures().setValue(AbstractFactFeatures.END_OF_SENTENCE_QUESTION_MARK, proof.getProofPhrase().contains("?") ? 1 : 0 );
//        proof.getFeatures().setValue(AbstractFactFeatures.NUMBER_OF_NON_ALPHA_NUMERIC_CHARACTERS, proof.getProofPhrase().replaceAll("[A-z0-9]",     "").length() );
    }
}
