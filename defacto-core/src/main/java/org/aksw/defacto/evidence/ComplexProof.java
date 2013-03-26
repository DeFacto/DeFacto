package org.aksw.defacto.evidence;

import org.aksw.defacto.DefactoModel;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;

import weka.core.Instance;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class ComplexProof {

    private Instance instance = new Instance(AbstractFactFeatures.factFeatures.numAttributes());
    
    private Pattern pattern;
    private WebSite website;
    private DefactoModel model;

    private String firstLabel;
    private String secondLabel;

    private String proofPhrase;
    private String normalizedProofPhrase;

    private String shortContext;

    private double score = 0D;

	private String longContext = "";

	private String taggedLongContext;

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
    public ComplexProof(DefactoModel model, String firstLabel, String secondLabel, String occurrence, String normalizedOccurrence, WebSite site, Pattern boaPattern) {
        
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
    public ComplexProof(DefactoModel model, String firstLabel, String secondLabel, String occurrence, String normalizedOccurrence, WebSite site) {
        
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

    public DefactoModel getModel() {

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

    public void setShortContext(String leftAndRightContext) {
        
        this.shortContext = leftAndRightContext;
    }

    
    /**
     * @return the context
     */
    public String getShortContext() {
    
        return shortContext;
    }

    public void setScore(double score) {

        this.score = score;
    }
    
    public double getScore() {

        return this.score;
    }

	public void setLongContext(String longContext) {
		
		this.longContext  = longContext;
	}
	
	public String getLongContext() {
		
		return this.longContext;
	}

	/**
	 * 
	 * @param merged
	 */
	public void setTaggedLongContext(String merged) {
		
		this.taggedLongContext = merged;
	}
	
	public String getTaggedLongContext() {
		
		return this.taggedLongContext;
	}
}
