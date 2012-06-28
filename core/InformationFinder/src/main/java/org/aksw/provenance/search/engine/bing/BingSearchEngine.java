package org.aksw.provenance.search.engine.bing;

import com.google.code.bing.search.client.BingSearchClient;
import com.google.code.bing.search.client.BingSearchClient.SearchRequestBuilder;
import com.google.code.bing.search.client.BingSearchServiceClientFactory;
import com.google.code.bing.search.schema.AdultOption;
import com.google.code.bing.search.schema.SearchOption;
import com.google.code.bing.search.schema.SearchResponse;
import com.google.code.bing.search.schema.SourceType;
import com.google.code.bing.search.schema.web.DeepLink;
import com.google.code.bing.search.schema.web.WebResponse;
import com.google.code.bing.search.schema.web.WebResult;
import com.google.code.bing.search.schema.web.WebSearchOption;
import org.aksw.helper.KeywordExtractor;
import org.aksw.provenance.Constants;
import org.aksw.provenance.boa.Pattern;
import org.aksw.provenance.evidence.WebSite;
import org.aksw.provenance.search.engine.DefaultSearchEngine;
import org.aksw.provenance.search.engine.SearchEngine;
import org.aksw.provenance.search.query.BingQuery;
import org.aksw.provenance.search.query.MetaQuery;
import org.aksw.provenance.search.result.DefaultSearchResult;
import org.aksw.provenance.search.result.SearchResult;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Date: 2/6/12
 * Time: 7:11 PM
 * Class BingSearchEngine contains the facilities required to contact Bing search engine to get the search results for a
 * set of keywords
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Mohamed Morsey <morsey@informatik.uni-leipzig.de>
 */
public class BingSearchEngine extends DefaultSearchEngine {

    private static Logger logger =  Logger.getLogger(BingSearchEngine.class);
    
    @Override
    public Long getNumberOfResults(MetaQuery query) {
        
        BingSearchServiceClientFactory factory = BingSearchServiceClientFactory.newInstance();
        BingSearchClient client = factory.createBingSearchClient();

        SearchRequestBuilder builder = client.newSearchRequestBuilder();
        builder.withAppId(Constants.BING_API_KEY);
        builder.withQuery(this.generateQuery(query));
        builder.withSourceType(SourceType.WEB);
        builder.withVersion("2.0");
        builder.withMarket("en-us");
        builder.withAdultOption(AdultOption.OFF);

        builder.withWebRequestCount(Integer.valueOf(Constants.NUMBER_OF_SEARCH_RESULTS).longValue());
        builder.withWebRequestOffset(0L);
        builder.withWebRequestSearchOption(WebSearchOption.DISABLE_HOST_COLLAPSING);
        builder.withWebRequestSearchOption(WebSearchOption.DISABLE_QUERY_ALTERATIONS);
        
        Long numberOfResults = client.search(builder.getResult()).getWeb().getTotal();
        logger.info("Querying Bing for query: '" + this.generateQuery(query) + "' returned " + numberOfResults + " results.");
        
        return numberOfResults;
    }
    
    @Override
    public SearchResult query(MetaQuery query, Pattern pattern) {

//        throw new RuntimeException("We have this is the cache! Why do we query "+ query +" again!?");
        
        BingSearchServiceClientFactory factory = BingSearchServiceClientFactory.newInstance();
        BingSearchClient client = factory.createBingSearchClient();

        SearchRequestBuilder builder = client.newSearchRequestBuilder();
        builder.withAppId(Constants.BING_API_KEY);
        builder.withQuery(this.generateQuery(query));
        builder.withSourceType(SourceType.WEB);
        builder.withVersion("2.0");
        builder.withMarket("en-us");
        builder.withAdultOption(AdultOption.OFF);

        builder.withWebRequestCount(Integer.valueOf(Constants.NUMBER_OF_SEARCH_RESULTS).longValue());
        builder.withWebRequestOffset(0L);
        builder.withWebRequestSearchOption(WebSearchOption.DISABLE_HOST_COLLAPSING);
        builder.withWebRequestSearchOption(WebSearchOption.DISABLE_QUERY_ALTERATIONS);
        
        int i = 1;
        
        // query bing and get only the urls and the total hit count back
        List<WebSite> results = new ArrayList<WebSite>();
        WebResponse response = client.search(builder.getResult()).getWeb();
        for (WebResult result : response.getResults()) { 
            
            WebSite website = new WebSite(query, result.getUrl());
            website.setTitle(result.getTitle());
            website.setRank(i++);
            results.add(website);
        }
            
        logger.info("Querying Bing for query: '" + this.generateQuery(query) + "' returned " + results.size() + " results.");        
                
        return new DefaultSearchResult(results, response.getTotal(), query, pattern);
    }

    @Override
    public String generateQuery(MetaQuery query) {

        BingQuery bingQuery = new BingQuery();
        return bingQuery.generateQuery(query);
    }
}
