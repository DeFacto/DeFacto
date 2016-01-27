package org.aksw.defacto.boa;

import org.aksw.defacto.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Only used inside this class to encapsulate the Solr query results.
 */
public class Pattern {
    
    public Map<String,Double> features = new HashMap<String,Double>();
    public String naturalLanguageRepresentationNormalized = "";
    public String naturalLanguageRepresentationWithoutVariables = "";
    public String naturalLanguageRepresentation = "";
    public String language = "";
    public Double boaScore = 0D;
    public Double naturalLanguageScore = 0D;
    public String posTags = "";
	private String normalizedPattern = null;
	public String generalized ="";
    public String NER = "";
    
    public Pattern(String naturalLanguageRepresentation, String language) {
    	
    	this.naturalLanguageRepresentation = naturalLanguageRepresentation;
    	this.language = language;
	}

	public Pattern() {
		// TODO Auto-generated constructor stub
	}

    public Pattern(String nlr){
        this.naturalLanguageRepresentation = nlr;
    }

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
        
    	if ( this.normalizedPattern == null ) {
    		
    		if ( this.naturalLanguageRepresentationNormalized.isEmpty() ) {
                
                this.naturalLanguageRepresentationNormalized = 
                            naturalLanguageRepresentationWithoutVariables.
                            replaceAll(",", "").replace("`", "").replace(" 's", "'s").replaceAll("  ", " ").replaceAll("'[^s]", "").
                            replaceAll("-LRB-", "").replaceAll("-RRB-", "").replaceAll("[0-9]{4}", "").trim();
                // ensure that we match the pattern and nothing more
                
                if ( this.naturalLanguageRepresentationNormalized.equals("'s") )
                    this.naturalLanguageRepresentationNormalized = this.naturalLanguageRepresentationNormalized + " ";
                else
                    this.naturalLanguageRepresentationNormalized = " " + this.naturalLanguageRepresentationNormalized + " ";
                    
            }
            
            Set<String> naturalLanguageRepresentationChunks = new HashSet<String>(Arrays.asList(naturalLanguageRepresentationNormalized.toLowerCase().trim().split(" ")));
            naturalLanguageRepresentationChunks.removeAll(Constants.STOP_WORDS);
            
            this.normalizedPattern  =  " " + StringUtils.join(naturalLanguageRepresentationChunks, " ") + " ";
    	}
    	
        return this.normalizedPattern;
    }
    
    public String getNormalized() {
        
    	String s = this.naturalLanguageRepresentationNormalized;
    	
    		if ( s.isEmpty() ) {
                
                s = 
                            naturalLanguageRepresentationWithoutVariables.
                            replaceAll(",", "").replace("`", "").replace(" 's", "'s").replaceAll("  ", " ").//replaceAll("'[^s]", "").
                            replaceAll("-LRB-", "").replaceAll("-RRB-", "").replaceAll("[0-9]{4}", "").trim();
                // ensure that we match the pattern and nothing more
                
                if ( s.equals("'s") )
                    s = s + " ";
                else
                    s = " " + s + " ";
            }
    		
            List<String> naturalLanguageRepresentationChunks = new ArrayList<String>(Arrays.asList(s.toLowerCase().trim().split(" ")));
            naturalLanguageRepresentationChunks.removeAll(Constants.NEW_STOP_WORDS);
            
            return " " + StringUtils.join(naturalLanguageRepresentationChunks, " ").trim().replaceAll(" +", " ") + " ";
    }
    
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((language == null) ? 0 : language.hashCode());
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
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (naturalLanguageRepresentation == null) {
			if (other.naturalLanguageRepresentation != null)
				return false;
		} else if (!naturalLanguageRepresentation.equals(other.naturalLanguageRepresentation))
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