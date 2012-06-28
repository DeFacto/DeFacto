package org.aksw.provenance.ml.feature;

import java.util.HashSet;
import java.util.Set;

import org.aksw.provenance.Constants;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.ml.feature.impl.DomainRangeCheckFeature;
import org.aksw.provenance.ml.feature.impl.NameFeature;
import org.aksw.provenance.ml.feature.impl.PageRankFeature;
import org.aksw.provenance.ml.feature.impl.ProofFeature;
import org.aksw.provenance.ml.feature.impl.TopicCoverageFeature;
import org.aksw.provenance.ml.feature.impl.TopicMajoritySearchFeature;
import org.aksw.provenance.ml.feature.impl.TopicMajorityWebFeature;
import org.aksw.provenance.ml.feature.impl.TotalHitCountFeature;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class EvidenceFeatureExtractor {

    public static Set<Feature> features = new HashSet<Feature>();
    
    static {

    	EvidenceFeatureExtractor.features.add(new DomainRangeCheckFeature());
        EvidenceFeatureExtractor.features.add(new PageRankFeature());
        EvidenceFeatureExtractor.features.add(new TotalHitCountFeature());
        EvidenceFeatureExtractor.features.add(new TopicCoverageFeature());
        EvidenceFeatureExtractor.features.add(new TopicMajorityWebFeature());
        EvidenceFeatureExtractor.features.add(new TopicMajoritySearchFeature());
        EvidenceFeatureExtractor.features.add(new ProofFeature());
        EvidenceFeatureExtractor.features.add(new NameFeature());
    }
    
    /**
     * 
     * @param evidence
     */
    public void extractFeatureForEvidence(Evidence evidence) {

        // score the collected evidence with every feature extractor defined
        for ( Feature feature : EvidenceFeatureExtractor.features ) 
            feature.extractFeature(evidence);
        
        // we only need to add the feature vector to the weka instances object if we plan to write the training file
        if ( Constants.WRITE_TRAINING_FILE )
            AbstractFeature.provenance.add(evidence.getFeatures());
    }
}
