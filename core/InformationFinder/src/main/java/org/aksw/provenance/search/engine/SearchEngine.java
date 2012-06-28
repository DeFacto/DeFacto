package org.aksw.provenance.search.engine;

import org.aksw.provenance.boa.Pattern;
import org.aksw.provenance.search.query.MetaQuery;
import org.aksw.provenance.search.result.SearchResult;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface SearchEngine {

    /**
     * 
     * @param query
     * @param pattern 
     * @return
     */
    public SearchResult getSearchResults(MetaQuery query, Pattern pattern);
    
    /**
     * 
     */
    public SearchResult query(MetaQuery query, Pattern pattern);

    /**
     * 
     * @param query
     * @return
     */
    public String generateQuery(MetaQuery query);

    /**
     * 
     * @param query
     * @return
     */
    public Long getNumberOfResults(MetaQuery query);
}
