package org.aksw.defacto.ml.feature.evidence;

import java.io.File;
import java.util.Random;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.FactScorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class DummyEvidenceScorer implements Scorer {


    /**
     * 
     * @return
     */
	@Override
    public Classifier loadClassifier() {
		return null;
    }

    /**
     * 
     * @param evidence
     * @return
     */
	@Override
    public Double scoreEvidence(Evidence evidence) {

		return new Random().nextDouble();
    }
}
