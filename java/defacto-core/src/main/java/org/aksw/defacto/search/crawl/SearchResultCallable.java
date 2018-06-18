/**
 * 
 */
package org.aksw.defacto.search.crawl;

import java.util.concurrent.Callable;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.search.engine.SearchEngine;
import org.aksw.defacto.search.engine.bing.AzureBingSearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.SearchResult;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class SearchResultCallable implements Callable<SearchResult> {

    private MetaQuery query;
    private Pattern pattern;
    private SearchEngine engine;
    
    public SearchResultCallable(MetaQuery query, Pattern pattern) {

        this.query      = query;
        this.pattern    = pattern;
        this.engine     = new AzureBingSearchEngine();
    }

    @Override
    public SearchResult call() throws Exception {

        return this.engine.getSearchResults(this.query, this.pattern);
    }

}
