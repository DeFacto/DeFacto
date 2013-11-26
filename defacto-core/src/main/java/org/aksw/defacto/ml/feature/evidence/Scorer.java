package org.aksw.defacto.ml.feature.evidence;

import org.aksw.defacto.evidence.Evidence;

import weka.classifiers.Classifier;

public interface Scorer {

	public Classifier loadClassifier();
	
    public Double scoreEvidence(Evidence evidence);
}
