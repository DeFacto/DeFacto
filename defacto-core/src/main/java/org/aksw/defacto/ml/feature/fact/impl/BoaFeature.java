/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import java.util.Arrays;
import java.util.List;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.BlockDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.DiceSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.EuclideanDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Jaro;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;
import uk.ac.shef.wit.simmetrics.similaritymetrics.OverlapCoefficient;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class BoaFeature implements FactFeature {

    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        // we set this to 0 and over write it if we find a pattern
        proof.getFeatures().setValue(AbstractFactFeatures.BOA_BOOLEAN, 0);
        proof.getFeatures().setValue(AbstractFactFeatures.BOA_SCORE, 0);

        float similarity = new SmithWaterman().getSimilarity(proof.getPattern().normalize(), proof.getProofPhrase());
        proof.getFeatures().setValue(AbstractFactFeatures.SMITH_WATERMAN, similarity);
        
        if ( similarity >= 0.5 ) {
            
            proof.getFeatures().setValue(AbstractFactFeatures.BOA_BOOLEAN, 1);
            proof.getFeatures().setValue(AbstractFactFeatures.BOA_SCORE, proof.getPattern().boaScore);
        }
    }
    
    
    public static void main(String[] args) {
		
    	String longTest = "oubleCli . . . Aprimo 05/09/05 Selectica Acquires Determine Software Products - Selectica announced the acquisition of the contract managem".toLowerCase();
//    	String pattern = "was acquires by";
    	String pattern = "acquires";
    	
    	List<? extends AbstractStringMetric> metrics = Arrays.asList(
    			new Levenshtein(), new QGramsDistance(), new BlockDistance(), new OverlapCoefficient(), new DiceSimilarity(),
    			new JaccardSimilarity(), new EuclideanDistance(), new Jaro(), new JaroWinkler(), new SmithWaterman(), new NeedlemanWunch());
    	
    	for ( AbstractStringMetric metric : metrics) {
    		
    		System.out.println(metric.getShortDescriptionString() + ":\t" + metric.getSimilarity(longTest, pattern));
    	}
	}
}
