/**
 * 
 */
package org.aksw.defacto.search.engine;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.cache.Cache;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.cache.solr.Solr4SearchResultCache;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public abstract class DefaultSearchEngine implements SearchEngine {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSearchEngine.class);
    protected Cache<SearchResult> searchResultsCache = new Solr4SearchResultCache();

    /* (non-Javadoc)
     * @see org.aksw.defacto.search.engine.SearchEngine#getSearchResults(org.aksw.defacto.search.query.MetaQuery)
     */
    @Override
    public SearchResult getSearchResults(MetaQuery query, Pattern pattern) {

        if ( searchResultsCache.contains(query.toString()) ) {
            
            // search results will be identified by the string we used to search in search engine
        	LOGGER.info(String.format("Query: '%s' cached! Starting to get from cache!", query.toString()));
            SearchResult result = searchResultsCache.getEntry(query.toString());
            result.setPattern(pattern);
            return result;
        }
        return query(query, pattern);
    }
    
    public static void main(String[] args) {
		
    	Defacto.init();
    	
    	Cache<SearchResult> searchResultsCache = new Solr4SearchResultCache();

        Pattern p = new Pattern();
        p.naturalLanguageRepresentation = "?D? won the ?R?";

    	SearchResult result = searchResultsCache.getEntry(new MetaQuery("Philipp Lenard|-|?D? won the ?R?|-|Nobel Prize in Physics|-|en", p).toString());
    	
    	for ( WebSite site : result.getWebSites()) {
    		System.out.println(site.getText());
    	}
	}
}
