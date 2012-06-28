package org.aksw.provenance.evidence;

import org.aksw.provenance.boa.Pattern;
import org.aksw.provenance.ml.feature.fact.AbstractFactFeatures;

import com.hp.hpl.jena.rdf.model.Model;

import weka.core.Instance;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class ComplexProof {

    private Instance instance = new Instance(AbstractFactFeatures.factFeatures.numAttributes());
    
    private Pattern pattern;
    private WebSite website;
    private Model model;

    private String firstLabel;
    private String secondLabel;

    private String proofPhrase;
    private String normalizedProofPhrase;

    private String context;

    private double score = 0D;

    /**
     * boa pattern found
     * 
     * @param firstLabel
     * @param secondLabel
     * @param occurrence
     * @param normalizedOccurrence
     * @param site
     * @param boaPattern
     */
    public ComplexProof(Model model, String firstLabel, String secondLabel, String occurrence, String normalizedOccurrence, WebSite site, Pattern boaPattern) {
        
        this.model                    = model;
        this.firstLabel               = firstLabel;
        this.secondLabel              = secondLabel;
        this.proofPhrase              = occurrence;
        this.normalizedProofPhrase    = normalizedOccurrence;
        this.website                  = site;
        this.pattern                  = boaPattern;
    }

    /**
     * proof with no boa pattern
     * 
     * @param firstLabel
     * @param secondLabel
     * @param occurrence
     * @param normalizedOccurrence
     * @param site
     */
    public ComplexProof(Model model, String firstLabel, String secondLabel, String occurrence, String normalizedOccurrence, WebSite site) {
        
        this.model                    = model;
        this.firstLabel               = firstLabel;
        this.secondLabel              = secondLabel;
        this.proofPhrase              = occurrence;
        this.normalizedProofPhrase    = normalizedOccurrence;
        this.website                  = site;
    }

    /**
     * 
     * @return
     */
    public String getProofPhrase() {

        return this.proofPhrase;
    }

    public Instance getFeatures() {

        return instance;
    }

    public WebSite getWebSite() {

        return this.website;
    }

    public Pattern getPattern() {

        return this.pattern;
    }

    public Model getModel() {

        return this.model;
    }

    public String getNormalizedProofPhrase() {

        return this.normalizedProofPhrase;
    }

    public String getSubject() {

        return this.firstLabel;
    }
    
    public String getObject() {

        return this.secondLabel;
    }

    public void setContext(String leftAndRightContext) {
        
        this.context = leftAndRightContext;
    }

    
    /**
     * @return the context
     */
    public String getContext() {
    
        return context;
    }

    public void setScore(double score) {

        this.score = score;
    }
    
    public double getScore() {

        return this.score;
    }
}
