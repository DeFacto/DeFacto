package org.aksw.defacto.ml.training;

import com.hp.hpl.jena.graph.Triple;

/**
 * Creates a training set finding factFeatures statements.
 * 
 * @author Jens Lehmann
 *
 */
public interface TrainingSetCreator {

	public Triple generatePositives();
	
	public Triple generateNegatives();
	
}
