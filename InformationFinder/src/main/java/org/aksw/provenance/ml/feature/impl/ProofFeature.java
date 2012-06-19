/**
 * 
 */
package org.aksw.provenance.ml.feature.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.provenance.Constants;
import org.aksw.provenance.evidence.ComplexProof;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.evidence.PossibleProof;
import org.aksw.provenance.evidence.Proof;
import org.aksw.provenance.evidence.WebSite;
import org.aksw.provenance.ml.feature.AbstractFeature;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class ProofFeature extends AbstractFeature {

    /* (non-Javadoc)
     * @see org.aksw.provenance.ml.feature.Feature#extractFeature(org.aksw.provenance.evidence.Evidence)
     */
    @Override
    public void extractFeature(Evidence evidence) {

        // how many boa pattern did we find
        evidence.getFeatures().setValue(AbstractFeature.NUMBER_OF_PROOFS, evidence.getComplexProofs().size());
        
        double scorePositives = 1D;
        double scoreNegatives = 1D;
        
        // on how many sites did we find a boa pattern
        Set<String> proofWebsites = new HashSet<String>();
        for ( ComplexProof p : evidence.getComplexProofs() ) {
            
            if ( p.getScore() > Constants.CONFIRMATION_THRESHOLD ) {
                
                proofWebsites.add(p.getWebSite().getUrl());
                scorePositives *= ( 1D - p.getScore() ); 
            }
            else {
                
                scoreNegatives *= ( 1D - p.getScore());
            }
        }
        evidence.getFeatures().setValue(AbstractFeature.NUMBER_OF_CONFIRMING_PROOFS, proofWebsites.size());
        evidence.getFeatures().setValue(AbstractFeature.TOTAL_POSITIVES_EVIDENCE_SCORE, 1D - scorePositives);
        evidence.getFeatures().setValue(AbstractFeature.TOTAL_NEGATIVES_EVIDENCE_SCORE, 1D - scoreNegatives);
    }
}
