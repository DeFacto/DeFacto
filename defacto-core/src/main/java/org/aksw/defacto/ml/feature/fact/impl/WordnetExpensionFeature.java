/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import java.io.File;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;
import org.aksw.defacto.wordnet.WordNetExpansion;
import org.apache.log4j.chainsaw.Main;

import com.github.gerbsen.maven.MavenUtil;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class WordnetExpensionFeature implements FactFeature {

    WordNetExpansion wordnetExpansion = new WordNetExpansion(new File(WordnetExpensionFeature.class.getResource("/wordnet/dict").getFile()).getAbsolutePath());
    
    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.fact.FactFeature#extractFeature(org.aksw.defacto.evidence.ComplexProof)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {
        
        double similarity = 0;
        
        if ( proof.getPattern() != null ) {
        
            similarity = wordnetExpansion.getExpandedJaccardSimilarity(proof.getProofPhrase(), proof.getPattern().normalize());
        }
        else {
            
            double maximum = 1;
            for (Pattern pattern : evidence.getBoaPatterns()) 
                maximum = Math.min(maximum, wordnetExpansion.getExpandedJaccardSimilarity(proof.getProofPhrase(), pattern.normalize()));
                    
            similarity = maximum;
        }
        
        if ( Double.isInfinite(similarity) || Double.isNaN(similarity) ) proof.getFeatures().setValue(AbstractFactFeatures.WORDNET_EXPANSION, 0D);
        else proof.getFeatures().setValue(AbstractFactFeatures.WORDNET_EXPANSION, similarity);
    }
    
    public static void main(String[] args) {

    	System.out.println(new File(WordnetExpensionFeature.class.getResource("/wordnet/dict").getFile()).getAbsolutePath());
    	
        System.out.println(new WordNetExpansion(new File(WordnetExpensionFeature.class.getResource("/wordnet/dict").getFile()).getAbsolutePath()).getExpandedJaccardSimilarity(", the director of", "the director of"));
    }
}
