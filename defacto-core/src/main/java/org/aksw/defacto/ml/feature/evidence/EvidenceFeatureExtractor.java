package org.aksw.defacto.ml.feature.evidence;

import java.util.HashSet;
import java.util.Set;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.impl.DomainRangeCheckFeature;
import org.aksw.defacto.ml.feature.evidence.impl.GoodnessFeature;
import org.aksw.defacto.ml.feature.evidence.impl.NameFeature;
import org.aksw.defacto.ml.feature.evidence.impl.PageRankFeature;
import org.aksw.defacto.ml.feature.evidence.impl.ProofFeature;
import org.aksw.defacto.ml.feature.evidence.impl.TopicCoverageFeature;
import org.aksw.defacto.ml.feature.evidence.impl.TopicMajoritySearchFeature;
import org.aksw.defacto.ml.feature.evidence.impl.TopicMajorityWebFeature;
import org.aksw.defacto.ml.feature.evidence.impl.TotalHitCountFeature;
import org.aksw.defacto.ml.feature.fact.FactFeatureExtraction;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class EvidenceFeatureExtractor {

    public static Set<EvidenceFeature> features = new HashSet<EvidenceFeature>();
    
    static {

    	EvidenceFeatureExtractor.features.add(new DomainRangeCheckFeature());
    	EvidenceFeatureExtractor.features.add(new GoodnessFeature());
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
        for ( EvidenceFeature feature : EvidenceFeatureExtractor.features ) 
            feature.extractFeature(evidence);
        
        // we only need to add the feature vector to the weka instances object if we plan to write the training file
        if ( Defacto.DEFACTO_CONFIG.getBooleanSetting("evidence", "OVERWRITE_EVIDENCE_TRAINING_FILE") )
            AbstractEvidenceFeature.provenance.add(evidence.getFeatures());
    }
}
