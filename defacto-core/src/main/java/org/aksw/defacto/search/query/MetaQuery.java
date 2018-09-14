package org.aksw.defacto.search.query;

import java.util.ArrayList;
import java.util.List;

import org.aksw.defacto.topic.frequency.Word;


public class MetaQuery {

    public String subjectLabel;
    private String propertyLabel;
    public String objectLabel;
    private List<Word> topicTerms;
	private String language;

    /**
     * 
     * @param subjectLabel
     * @param propertyLabel
     * @param objectLabel
     * @param language 
     * @param topicTerms
     */
    public MetaQuery(String subjectLabel, String propertyLabel, String objectLabel, String language, List<Word> topicTerms) {
        
        this.subjectLabel   = subjectLabel;
        this.propertyLabel  = propertyLabel;
        this.objectLabel    = objectLabel;
        this.language		= language;
        this.topicTerms     = topicTerms != null ? topicTerms : new ArrayList<Word>();
    }
    
    public MetaQuery(String metaQuery) {

        String[] parts      = metaQuery.split("\\|-\\|");
        this.subjectLabel   = parts[0];
        this.propertyLabel  = parts[1];
        this.objectLabel    = parts[2];
        this.language		= parts[3];
        this.topicTerms     = new ArrayList<Word>();
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

	public String getLanguage() {
		return this.language;
	}
}
