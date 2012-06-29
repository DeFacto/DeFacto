/**
 * 
 */
package org.aksw.provenance.search.engine;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.provenance.boa.Pattern;
import org.aksw.provenance.cache.Cache;
import org.aksw.provenance.evidence.WebSite;
import org.aksw.provenance.search.cache.SearchResultCache;
import org.aksw.provenance.search.query.BingQuery;
import org.aksw.provenance.search.query.MetaQuery;
import org.aksw.provenance.search.result.DefaultSearchResult;
import org.aksw.provenance.search.result.SearchResult;
import org.apache.log4j.Logger;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public abstract class DefaultSearchEngine implements SearchEngine {

    private Logger logger = Logger.getLogger(DefaultSearchEngine.class);
    protected Cache<SearchResult> searchResultsCache = new SearchResultCache();

    /* (non-Javadoc)
     * @see org.aksw.provenance.search.engine.SearchEngine#getSearchResults(org.aksw.provenance.search.query.MetaQuery)
     */
    @Override
    public SearchResult getSearchResults(MetaQuery query, Pattern pattern) {

        if ( searchResultsCache.contains(query.toString()) ) {
            
            // search results will be identified by the string we used to search in search engine
            SearchResult result = searchResultsCache.getEntry(query.toString());
            result.setPattern(pattern);
            return result;
        }
        logger.info(String.format("Query: '%s' was not found in the cache, starting to query!", query.toString()));
        
        return query(query, pattern);
    }
}
