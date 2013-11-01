package org.aksw.defacto.search.result;

import java.util.List;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.query.MetaQuery;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class DefaultSearchResult implements SearchResult {

    private List<WebSite> hits;
    private Long totalHitCount = 0L;
    private MetaQuery query;
    private Pattern pattern;
	private boolean cached = false;

    public DefaultSearchResult(List<WebSite> websites, Long totalHitCount, MetaQuery query, Pattern pattern, boolean cached) {
        
        this.hits = websites;
        this.totalHitCount = totalHitCount;
        this.query = query;
        this.cached  = cached;
        this.pattern = pattern;
    }
    
    public DefaultSearchResult(List<WebSite> websites, Long hitCount, MetaQuery metaQuery) {

        this.hits = websites;
        this.totalHitCount = hitCount;
        this.query = metaQuery;
    }
    public DefaultSearchResult(List<WebSite> websites, Long hitCount, MetaQuery metaQuery, boolean cached) {

        this.hits = websites;
        this.totalHitCount = hitCount;
        this.query = metaQuery;
        this.cached  = cached;
    }

    @Override
    public Long getTotalHitCount() {

        return this.totalHitCount != null ? this.totalHitCount : 0L;
    }
    
    public String getLanguage() {
    	
    	return this.query.getLanguage();
    }

    @Override
    public List<WebSite> getWebSites() {

        return this.hits;
    }

    /**
     * @return the query
     */
    public MetaQuery getQuery() {

        return query;
    }

    @Override
    public void setPattern(Pattern pattern) {

        this.pattern = pattern;
    }

    @Override
    public Pattern getPattern() {

        return this.pattern;
    }

	@Override
	public boolean isCached() {
		// TODO Auto-generated method stub
		return this.cached;
	}
}
