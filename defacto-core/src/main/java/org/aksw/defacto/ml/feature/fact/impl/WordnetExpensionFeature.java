/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import java.io.File;
import java.util.List;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;
import org.aksw.defacto.wordnet.WordNetExpansion;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class WordnetExpensionFeature implements FactFeature {

//    WordNetExpansion wordnetExpansion = new WordNetExpansion(new File(WordnetExpensionFeature.class.getResource("/wordnet/dict").getFile()).getAbsolutePath());
    WordNetExpansion wordnetExpansion = new WordNetExpansion(Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "WORDNET_DICTIONARY"));
    
    
    BoaPatternSearcher searcher = new BoaPatternSearcher();
    
    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.fact.FactFeature#extractFeature(org.aksw.defacto.evidence.ComplexProof)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {
        
        double similarity = 0;

        List<Pattern> patterns = searcher.querySolrIndex(evidence.getModel().getPropertyUri(), 20, 0, proof.getLanguage());
        
        for ( Pattern pattern : patterns ) {
        	similarity = Math.max(similarity, wordnetExpansion.getExpandedJaccardSimilarity(proof.getProofPhrase(), pattern.getNormalized()));
        }
        
        if ( Double.isInfinite(similarity) || Double.isNaN(similarity) ) proof.getFeatures().setValue(AbstractFactFeatures.WORDNET_EXPANSION, 0D);
        else proof.getFeatures().setValue(AbstractFactFeatures.WORDNET_EXPANSION, similarity);
    }
    
    public static void main(String[] args) {

    	System.out.println(new File(WordnetExpensionFeature.class.getResource("/wordnet/dict").getFile()).getAbsolutePath());
        System.out.println(new WordNetExpansion(new File(WordnetExpensionFeature.class.getResource("/wordnet/dict").getFile()).getAbsolutePath()).getExpandedJaccardSimilarity(", the director of", "the director of"));
    }
}
