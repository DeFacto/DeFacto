package org.aksw.defacto.search.query;

import java.util.ArrayList;
import java.util.List;

import org.aksw.defacto.topic.frequency.Word;


public class MetaQuery {

    private String subjectLabel;
    private String propertyLabel;
    private String objectLabel;
    private List<Word> topicTerms;

    /**
     * 
     * @param subjectLabel
     * @param propertyLabel
     * @param objectLabel
     * @param topicTerms
     */
    public MetaQuery(String subjectLabel, String propertyLabel, String objectLabel, List<Word> topicTerms) {
        
        this.subjectLabel   = subjectLabel;
        this.propertyLabel  = propertyLabel;
        this.objectLabel    = objectLabel;
        this.topicTerms     = topicTerms != null ? topicTerms : new ArrayList<Word>();
    }
    
    public MetaQuery(String metaQuery) {

        String[] parts      = metaQuery.split("\\|-\\|");
        this.subjectLabel   = parts[0];
        this.propertyLabel  = parts[1];
        this.objectLabel    = parts[2];
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

        return String.format("%s|-|%s|-|%s", subjectLabel, propertyLabel, objectLabel);
    }
}
