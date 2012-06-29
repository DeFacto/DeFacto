/**
 * 
 */
package org.aksw.provenance.ml.feature.fact.impl;

import java.util.Set;

import org.aksw.provenance.evidence.ComplexProof;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.ml.feature.fact.AbstractFactFeatures;
import org.aksw.provenance.ml.feature.fact.FactFeature;
import org.aksw.provenance.util.ModelUtil;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PropertyFeature implements FactFeature {

    /* (non-Javadoc)
     * @see org.aksw.provenance.ml.feature.fact.FactFeature#extractFeature(org.aksw.provenance.evidence.ComplexProof, java.util.Set)
     */
    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {

        proof.getFeatures().setValue(AbstractFactFeatures.PROPERTY_NAME, ModelUtil.getPropertyUri(proof.getModel()).replace("http://dbpedia.org/ontology/", ""));
    }
}
