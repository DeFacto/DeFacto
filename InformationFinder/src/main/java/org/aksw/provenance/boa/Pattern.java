package org.aksw.provenance.boa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Transient;

/**
 * Only used inside this class to encapsulate the Solr query results.
 */
public class Pattern {
    
    public Map<String,Double> features = new HashMap<String,Double>();
    public String naturalLanguageRepresentationNormalized = "";
    public String naturalLanguageRepresentationWithoutVariables = "";
    public String naturalLanguageRepresentation = "";
    public Double boaScore = 0D;
    public Double naturalLanguageScore = 0D;
    public String posTags = "";
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("Pattern [factFeatures=");
        builder.append(features);
        builder.append(", naturalLanguageRepresentation=");
        builder.append(naturalLanguageRepresentation);
        builder.append(", boaScore=");
        builder.append(boaScore);
        builder.append(", naturalLanguageScore=");
        builder.append(naturalLanguageScore);
        builder.append(", POS=");
        builder.append(posTags);
        builder.append("]");
        return builder.toString();
    }
    
    /**
     * 
     * @return
     */
    public String normalize() {
        
        if ( this.naturalLanguageRepresentationNormalized.isEmpty() ) {
            
            this.naturalLanguageRepresentationNormalized = 
                        naturalLanguageRepresentationWithoutVariables.
                        replaceAll(",", "").replace("`", "").replace(" 's", "'s").replaceAll("  ", " ").replaceAll("'[^s]", "").
                        replaceAll("-LRB-", "").replaceAll("-RRB-", "").trim();
            // ensure that we match the pattern and nothing more
            
            if ( this.naturalLanguageRepresentationNormalized.equals("'s") )
                this.naturalLanguageRepresentationNormalized = this.naturalLanguageRepresentationNormalized + " ";
            else
                this.naturalLanguageRepresentationNormalized = " " + this.naturalLanguageRepresentationNormalized + " ";
                
        }
        
        return this.naturalLanguageRepresentationNormalized;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((naturalLanguageRepresentation == null) ? 0 : naturalLanguageRepresentation.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pattern other = (Pattern) obj;
        if (naturalLanguageRepresentation == null) {
            if (other.naturalLanguageRepresentation != null)
                return false;
        }
        else
            if (!naturalLanguageRepresentation.equals(other.naturalLanguageRepresentation))
                return false;
        return true;
    }
    
    /**
     * @return true if the pattern starts with ?D?
     */
    public boolean isDomainFirst() {
        
        return this.naturalLanguageRepresentation.startsWith("?D?") ? true : false;
    }
}