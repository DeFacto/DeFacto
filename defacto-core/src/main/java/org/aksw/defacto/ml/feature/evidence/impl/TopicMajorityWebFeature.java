/**
 * 
 */
package org.aksw.defacto.ml.feature.evidence.impl;

import java.util.Collections;
import java.util.List;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.topic.TopicTermExtractor;
import org.aksw.defacto.topic.frequency.Word;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class TopicMajorityWebFeature extends AbstractEvidenceFeature {

//    private Logger logger = Logger.getLogger(TopicMajorityWebFeature.class);
    
    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.Feature#extractFeature(org.aksw.defacto.evidence.Evidence)
     */
    @Override
    public void extractFeature(Evidence evidence) {

//        int positives = 0;
//        int positiveWebsites = 0;
//        int negatives = 0;
//        int negativeWebsites = 0;
        
        double sumScore = 0D;
        double maxScore = 0D;
        
        // prepare the callables
        for (WebSite website : evidence.getAllWebSites() ) {
            
            int topicMajority = 0;
            
            List<Word> topicTerms = website.getOccurringTopicTerms();
            Collections.sort(topicTerms, new TopicTermExtractor.WordComparator());
            
            // we want this only for the first three websites
            for ( int i = 0 ; i < 3 && i < topicTerms.size() ; i++) {
                
                // we need to compare each website with each website
                for ( WebSite allWebSite : evidence.getAllWebSites() ) {
                
                    // exclude the identity comparison
                    if ( !allWebSite.equals(website) ) {
                        
                        if ( allWebSite.getText().contains(topicTerms.get(i).getWord()) ) topicMajority++;
                    }
                }
            }
            website.setTopicMajorityWebFeature(website.getScore() * topicMajority);
            maxScore = Math.max(maxScore, website.getTopicMajorityWebFeature());
            sumScore += website.getTopicMajorityWebFeature();
            
//            if ( website.getScore() > Constants.CONFIRMATION_THRESHOLD ) {
//                
//                positives += website.getTopicMajorityWebFeature();
//                positiveWebsites++;
//            }
//            else {
//                
//                negatives += website.getTopicMajorityWebFeature();
//                negativeWebsites++;
//            }
        }
        
        evidence.getFeatures().setValue(AbstractEvidenceFeature.TOPIC_MAJORITY_WEB_SUM, sumScore);
        evidence.getFeatures().setValue(AbstractEvidenceFeature.TOPIC_MAJORITY_WEB_MAX, maxScore);
        
//        // average the topic majority of confirming pages
//        Double topicMajorityWebAverageConfirming = (double) positives / (double) positiveWebsites;// / (double) positives.size();
//        evidence.getFeatures().setValue(AbstractFeature.TOPIC_MAJORITY_WEB_CONFIRMING_FEATURE, 
//                !topicMajorityWebAverageConfirming.isNaN() && !topicMajorityWebAverageConfirming.isInfinite() ? topicMajorityWebAverageConfirming : 0D);
//        
//        // average the topic majority of non confirming pages
//        Double topicMajorityWebAverageNonConfirming = (double) negatives / (double) negativeWebsites;// / (double) negatives.size();
//        evidence.getFeatures().setValue(AbstractFeature.TOPIC_MAJORITY_WEB_NON_CONFIRMING_FEATURE, 
//                !topicMajorityWebAverageNonConfirming.isNaN() && !topicMajorityWebAverageNonConfirming.isInfinite() ? topicMajorityWebAverageNonConfirming : 0D);
    }
    
//    /**
//     * 
//     * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
//     *
//     */
//    private class TopicTermMajorityWebFeatureCallable implements Callable<Long> {
//
//        private MetaQuery query;
//        private SearchEngine engine = new BingSearchEngine();
//
//        /**
//         * 
//         * @param website
//         */
//        public TopicTermMajorityWebFeatureCallable(MetaQuery query) {
//            
//            this.query = query;
//        }
//        
//        @Override
//        public Long call() throws Exception {
//
//            return engine.getNumberOfResults(this.query);
//        }
//
//        /* (non-Javadoc)
//         * @see java.lang.Object#hashCode()
//         */
//        @Override
//        public int hashCode() {
//
//            final int prime = 31;
//            int result = 1;
//            result = prime * result + getOuterType().hashCode();
//            result = prime * result + ((query == null) ? 0 : query.hashCode());
//            return result;
//        }
//
//        /* (non-Javadoc)
//         * @see java.lang.Object#equals(java.lang.Object)
//         */
//        @Override
//        public boolean equals(Object obj) {
//
//            if (this == obj)
//                return true;
//            if (obj == null)
//                return false;
//            if (getClass() != obj.getClass())
//                return false;
//            TopicTermMajorityWebFeatureCallable other = (TopicTermMajorityWebFeatureCallable) obj;
//            if (!getOuterType().equals(other.getOuterType()))
//                return false;
//            if (query == null) {
//                if (other.query != null)
//                    return false;
//            }
//            else
//                if (!query.equals(other.query))
//                    return false;
//            return true;
//        }
//
//        private TopicMajorityWebFeature getOuterType() {
//
//            return TopicMajorityWebFeature.this;
//        }
//    }
}
