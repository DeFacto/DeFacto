/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import java.util.Set;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PageTitleFeature implements FactFeature {

    AbstractStringMetric metric = new SmithWaterman();

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.fact.FactFeature#extractFeature(org.aksw.defacto.evidence.ComplexProof)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        String pageTitle = proof.getWebSite().getTitle();
        
        Set<String> subjectLabels  = proof.getModel().getSubjectLabels();
        subjectLabels.addAll(proof.getModel().getSubjectAltLabels());
        Set<String> objectLabels  = proof.getModel().getObjectLabels();
        objectLabels.addAll(proof.getModel().getObjectAltLabels());
        
        float subjectSimilarity = 0f;
        for ( String label : subjectLabels) {
        	
        	float sim = metric.getSimilarity(pageTitle, label);
        	if ( sim >= subjectSimilarity ) subjectSimilarity = sim; 
        }
        float objectSimilarity = 0f;
        for ( String label : objectLabels) {
        	
        	float sim = metric.getSimilarity(pageTitle, label);
        	if ( sim >= objectSimilarity ) objectSimilarity = sim; 
        }
        
        proof.getFeatures().setValue(AbstractFactFeatures.PAGE_TITLE_SUBJECT, subjectSimilarity);
        proof.getFeatures().setValue(AbstractFactFeatures.PAGE_TITLE_OBJECT, objectSimilarity);
    }
}
