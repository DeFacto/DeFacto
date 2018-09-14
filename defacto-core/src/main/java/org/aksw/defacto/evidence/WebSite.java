package org.aksw.defacto.evidence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.search.cache.solr.TopicTermSolr4Cache;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.topic.frequency.Word;
import org.apache.commons.lang.StringUtils;


public class WebSite {

    private String text                     = "";
    private String title                    = "";
    private String url                      = "";
    private int pagerank                    = Defacto.DEFACTO_CONFIG.getIntegerSetting("evidence", "UNASSIGNED_PAGE_RANK");
    private double score                    = 0D;
    private MetaQuery query                 = null;
    
    private Map<Word,Integer> topicTermsOccurrences    = new LinkedHashMap<Word,Integer>();
    private int rank;
    private boolean cached;
    private Double topicMajorityWeb = 0D;
    private Double topicMajoritySearch = 0D;
    private Double pageRankScore = 0D;
    private Double topicCoverageScore = 0D;
	private String annotatedSentences;
	private String language ="";
	private String lowerCaseText = null;
	private String lowerCaseTitle = null; 
    
    /**
     * 
     * @param query
     * @param url
     */
    public WebSite(MetaQuery query, String url) {
        
        this.query = query;
        this.url = url;
    }
    
    /**
     * 
     * @param text
     */
    public void setText(String text) {

        this.text = text;
    }
    
    public void setTitle(String title) {
        
        this.title = title;
    }

    /**
     * 
     * @return
     */
    public int getPageRank() {

        return this.pagerank;
    }

    /**
     * 
     * @return
     */
    public double getScore() {

        return this.score;
    }

    /**
     * 
     * @param score
     */
    public void setScore(double score) {

        this.score = score;
    }

    /**
     * 
     * @return
     */
    public String getText() {

        return this.text;
    }
    
    /**
     * 
     * @param topicTermsOccurrences
     */
    public void setTopicTerms(String language, Collection<Word> topicTerms) {
        
    	String text = this.text.toLowerCase();
    	
        for ( Word topicTerm : topicTerms )
            this.topicTermsOccurrences.put(topicTerm, StringUtils.countMatches(text, topicTerm.getWord().toLowerCase()));
    }

    /**
     * 
     * @return
     */
    public List<Integer> getTopicTerms() {

        return new ArrayList<Integer>(this.topicTermsOccurrences.values());
    }

    public void setPageRank(int pagerank) {

        this.pagerank = pagerank;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("WebSite [text.length=");
        builder.append(text.length());
        builder.append(", url=");
        builder.append(url);
        builder.append(", pagerank=");
        builder.append(pagerank);
        builder.append(", score=");
        builder.append(score);
        builder.append(", topicTermsOccurrences=");
        builder.append(topicTermsOccurrences);
        builder.append("]");
        return builder.toString();
    }

    public String getTitle() {

        return this.title;
    }

    
    /**
     * @return the url
     */
    public String getUrl() {
    
        return url;
    }
    
    /**
     * @return the query
     */
    public MetaQuery getQuery() {
    
        return query;
    }

    /**
     * @return returns all topic terms which appear in the website's body
     */
    public List<Word> getOccurringTopicTerms() {

        List<Word> words = new ArrayList<Word>();
        for ( Map.Entry<Word, Integer> wordToOccurrence : this.topicTermsOccurrences.entrySet()) {
        
            if ( wordToOccurrence.getValue() > 0 ) words.add(wordToOccurrence.getKey());
        }
        return words;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
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
        WebSite other = (WebSite) obj;
        if (url == null) {
            if (other.url != null)
                return false;
        }
        else
            if (!url.equals(other.url))
                return false;
        return true;
    }

    public void setRank(int rank) {

        this.rank = rank;
    }
    
    public int getSearchRank() {

        return this.rank;
    }

    public void setCached(boolean cached) {

        this.cached = cached;
    }

    
    /**
     * @return the cached
     */
    public boolean isCached() {
    
        return cached;
    }

    public void setTopicMajorityWebFeature(double topicMajority) {

        this.topicMajorityWeb  = topicMajority;
    }

    public Double getTopicMajorityWebFeature() {

        return this.topicMajorityWeb;
    }

    public void setTopicMajoritySearchFeature(Double topicMajoritySearch) {

        this.topicMajoritySearch  = topicMajoritySearch;
    }

    public Double getTopicMajoritySearchFeature() {

        return this.topicMajoritySearch;
    }

    public void setPageRankScore(Double score) {

        this.pageRankScore = score;
    }

    public Double getPageRankScore() {

        return this.pageRankScore;
    }

    public void setTopicCoverageScore(Double score) {

        this.topicCoverageScore = score;
    }

    public Double getTopicCoverageScore() {

        return this.topicCoverageScore;
    }

    public String getTaggedText() {
		
		return this.annotatedSentences;
	}
    
	public void setTaggedText(String annotatedSentences) {
		
		this.annotatedSentences = annotatedSentences;
	}

	public void setLanguage(String language) {
		
		this.language  = language;
	}

	public String getLanguage() {
		return this.language;
	}

	public String getLowerCaseText() {
		
		if ( this.lowerCaseText  == null ) this.lowerCaseText = this.text.toLowerCase();
		
		return this.lowerCaseText;
	}

	public String getLowerCaseTitle() {
		
		if ( this.lowerCaseTitle  == null ) this.lowerCaseTitle  = this.title.toLowerCase();
		
		return this.lowerCaseTitle;
	}
}
