package org.aksw.defacto.search.query;

import org.aksw.defacto.Constants;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.topic.frequency.Word;

import java.util.ArrayList;
import java.util.List;


public class MetaQuery {

    public String subjectLabel;
    private String propertyLabel;
    public String objectLabel;
    private List<Word> topicTerms;
	private String language;

    private Pattern pattern;

    private int penalizesDifDomOnDR_Rel = 0;
    private int penalizesDifRanOnDR_Rel = 0;

    private double penalizeFactorDifDomOnDR_Rel = 0.0;
    private double penalizeFactorDifRanOnDR_Rel = 0.0;

    private Constants.EvidenceType evidenceTypeRelation;


    /**
     * 
     * @param subjectLabel
     * @param propertyLabel
     * @param objectLabel
     * @param language 
     * @param topicTerms
     */
    public MetaQuery(String subjectLabel, String propertyLabel, String objectLabel, String language, List<Word> topicTerms, Pattern pattern) {
        
        this.subjectLabel          = subjectLabel;
        this.propertyLabel         = propertyLabel;
        this.objectLabel           = objectLabel;
        this.language		       = language;
        this.topicTerms            = topicTerms != null ? topicTerms : new ArrayList<Word>();
        this.evidenceTypeRelation  = Constants.EvidenceType.POS;
        this.pattern               = pattern;
    }

    public MetaQuery(String subjectLabel, String propertyLabel, String objectLabel, String language, List<Word> topicTerms,
                     int penalizesDifDomOnDR, int penalizesDifRanOnDR, double penalizeFactorDifDomOnDR,
                     double penalizeFactorDifRanOnDR, Pattern pattern) {

        this.subjectLabel                 = subjectLabel;
        this.propertyLabel                = propertyLabel;
        this.objectLabel                  = objectLabel;
        this.language		              = language;
        this.topicTerms                   = topicTerms != null ? topicTerms : new ArrayList<Word>();
        this.penalizesDifDomOnDR_Rel      = penalizesDifDomOnDR;
        this.penalizesDifRanOnDR_Rel      = penalizesDifRanOnDR;
        this.penalizeFactorDifDomOnDR_Rel = penalizeFactorDifDomOnDR; //factor of penalization (directly related to functional property)
        this.penalizeFactorDifRanOnDR_Rel = penalizeFactorDifRanOnDR; //factor of penalization (directly related to functional property)
        this.evidenceTypeRelation         = Constants.EvidenceType.POS;
        this.pattern                      = pattern;
    }

    public MetaQuery(String metaQuery, Pattern pattern) {

        String[] parts            = metaQuery.split("\\|-\\|");
        this.subjectLabel         = parts[0];
        this.propertyLabel        = parts[1];
        this.objectLabel          = parts[2];
        this.language		      = parts[3];
        this.topicTerms           = new ArrayList<Word>();
        this.evidenceTypeRelation = Constants.EvidenceType.POS;
        this.pattern              = pattern;
    }

    /**
     * @return the subjectLabel
     */
    public String getSubjectLabel() {
    
        return subjectLabel;
    }
    
    /**
     * @return the propertyLabel
     */
    public String getPropertyLabel() {
    
        return propertyLabel;
    }
    
    /**
     * @return the objectLabel
     */
    public String getObjectLabel() {
    
        return objectLabel;
    }
    
    /**
     * @return the topicTerms
     */
    public List<Word> getTopicTerms() {
    
        return topicTerms;
    }

    
    /**
     * @param topicTerms the topicTerms to set
     */
    public void setTopicTerms(List<Word> topicTerms) {
    
        this.topicTerms = topicTerms;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return String.format("%s|-|%s|-|%s|-|%s", subjectLabel, propertyLabel, objectLabel, language);
    }

    public Pattern getPattern(){
        return this.pattern;
    }

	public String getLanguage() {
		return this.language;
	}

    public void setEvidenceTypeRelation(Constants.EvidenceType e){
        this.evidenceTypeRelation = e;
    }

    public Constants.EvidenceType getEvidenceTypeRelation(){
        return this.evidenceTypeRelation;
    }
}
