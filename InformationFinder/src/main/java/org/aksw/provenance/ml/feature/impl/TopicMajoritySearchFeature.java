/**
 * 
 */
package org.aksw.provenance.ml.feature.impl;

import java.util.ArrayList;
import java.util.List;

import org.aksw.provenance.Constants;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.evidence.WebSite;
import org.aksw.provenance.ml.feature.AbstractFeature;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class TopicMajoritySearchFeature extends AbstractFeature {

    /* (non-Javadoc)
     * @see org.aksw.provenance.ml.feature.Feature#extractFeature(org.aksw.provenance.evidence.Evidence)
     */
    @Override
    public void extractFeature(Evidence evidence) {

        List<WebSite> allWebsites = new ArrayList<WebSite>();
        for ( List<WebSite> entry : evidence.getWebSites().values() ) allWebsites.addAll(entry);
        
        int numberOfConfirmingWebSites = 0;
        int numberOfNonConfirmingWebSites = 0;

        double sumScore = 0D;
        double maxScore = 0D;
        
        // i is the index of the website in the row
        for ( int i = 0; i < evidence.getSimilarityMatrix().length; i++ ) {
            
            WebSite site = allWebsites.get(i);
            
            // j is the index of the websites in the columns
            for ( int j = 0; j < evidence.getSimilarityMatrix()[i].length ; j++ ) { 

                // TODO do we want to count the identity, if not j and i need to be different                
                if ( evidence.getSimilarityMatrix()[i][j] > Constants.WEBSITE_SIMILARITY_THRESHOLD && (i != j) ) {
                    
                    site.setTopicMajoritySearchFeature(site.getScore() * evidence.getSimilarityMatrix()[i][j]);
                    maxScore = Math.max(maxScore, site.getTopicMajoritySearchFeature());
                    sumScore += site.getTopicMajoritySearchFeature();
                    
//                    if ( site.getScore() > Constants.CONFIRMATION_THRESHOLD ) 
//                        numberOfConfirmingWebSites++;
//                    else 
//                        numberOfNonConfirmingWebSites++;
                }
            }
        }
        
        evidence.getFeatures().setValue(AbstractFeature.TOPIC_MAJORITY_SEARCH_RESULT_SUM, sumScore);
        evidence.getFeatures().setValue(AbstractFeature.TOPIC_MAJORITY_SEARCH_RESULT_MAX, maxScore);
        
//        // average the topic majority search of confirming pages
//        Double similarWebSitesAverageConfirming = (double) numberOfConfirmingWebSites / (double) allWebsites.size();
//        evidence.getFeatures().setValue(AbstractFeature.TOPIC_MAJORITY_SEARCH_CONFIRMING_FEATURE, 
//                !similarWebSitesAverageConfirming.isNaN() && !similarWebSitesAverageConfirming.isInfinite() ? similarWebSitesAverageConfirming : 0D);
//        
//        // average the topic majority search of non confirming pages
//        Double similarWebSitesAverageNonConfirming = (double) numberOfNonConfirmingWebSites / (double) allWebsites.size();
//        evidence.getFeatures().setValue(AbstractFeature.TOPIC_MAJORITY_SEARCH_NON_CONFIRMING_FEATURE, 
//                !similarWebSitesAverageNonConfirming.isNaN() && !similarWebSitesAverageNonConfirming.isInfinite() ? similarWebSitesAverageNonConfirming : 0D);
    }
}
