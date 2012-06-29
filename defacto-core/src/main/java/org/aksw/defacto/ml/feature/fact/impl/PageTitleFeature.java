/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;
import org.aksw.defacto.util.ModelUtil;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PageTitleFeature implements FactFeature {

    AbstractStringMetric metric = new QGramsDistance();

    /* (non-Javadoc)
     * @see org.aksw.defacto.ml.feature.fact.FactFeature#extractFeature(org.aksw.defacto.evidence.ComplexProof)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        String pageTitle = proof.getWebSite().getTitle();
        float subjectSimilarity = metric.getSimilarity(pageTitle, ModelUtil.getLabel(ModelUtil.getSubjectUri(proof.getModel()), proof.getModel()));
        float objectSimilarity = metric.getSimilarity(pageTitle, ModelUtil.getLabel(ModelUtil.getObjectUri(proof.getModel()), proof.getModel()));
        
        proof.getFeatures().setValue(AbstractFactFeatures.PAGE_TITLE_SUBJECT, subjectSimilarity);
        proof.getFeatures().setValue(AbstractFactFeatures.PAGE_TITLE_OBJECT, objectSimilarity);
    }
}
