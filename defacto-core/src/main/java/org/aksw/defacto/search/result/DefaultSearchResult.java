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
    private Long totalHitCount;
    private MetaQuery query;
    private Pattern pattern;

    public DefaultSearchResult(List<WebSite> websites, Long totalHitCount, MetaQuery query, Pattern pattern) {
        
        this.hits = websites;
        this.totalHitCount = totalHitCount;
        this.query = query;
        this.pattern = pattern;
    }
    
    public DefaultSearchResult(List<WebSite> websites, Long hitCount, MetaQuery metaQuery) {

        this.hits = websites;
        this.totalHitCount = hitCount;
        this.query = metaQuery;
    }

    @Override
    public Long getTotalHitCount() {

        return this.totalHitCount;
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
}
