/**
 * 
 */
package org.aksw.defacto.search.engine;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.cache.Cache;
import org.aksw.defacto.search.cache.SearchResultCache;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.SearchResult;
import org.apache.log4j.Logger;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public abstract class DefaultSearchEngine implements SearchEngine {

    private Logger logger = Logger.getLogger(DefaultSearchEngine.class);
    protected Cache<SearchResult> searchResultsCache = new SearchResultCache();

    /* (non-Javadoc)
     * @see org.aksw.defacto.search.engine.SearchEngine#getSearchResults(org.aksw.defacto.search.query.MetaQuery)
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
