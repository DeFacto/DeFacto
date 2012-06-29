package org.aksw.defacto.cache.warmer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.aksw.defacto.DefactoDemo;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.cache.SearchResultCache;
import org.aksw.defacto.search.concurrent.HtmlCrawlerCallable;
import org.aksw.defacto.search.engine.SearchEngine;
import org.aksw.defacto.search.engine.bing.BingSearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.query.QueryGenerator;
import org.aksw.defacto.search.result.SearchResult;
import org.aksw.defacto.util.TimeUtil;

import com.hp.hpl.jena.rdf.model.Model;


public class SearchResultCacheWarmer {

    public static void main(String[] args) {
        
        org.apache.log4j.PropertyConfigurator.configure("log/log4j.properties");
        List<Model> models = DefactoDemo.getTrainingData();
        int numberOfModels = models.size();
        int currentModel = 1;
        long startTime = System.currentTimeMillis();
        
        SearchEngine engine             = new BingSearchEngine();
        QueryGenerator queryGenerator   = null;
        
        for ( Model model : models ) {
            
            queryGenerator = new QueryGenerator(model);
            Map<Pattern,MetaQuery> patternToQueries = queryGenerator.getSearchEngineQueries();
            if ( patternToQueries.size() <= 0 ) {
                System.out.println("No queries for model: " + model); continue;
            }
            
            Set<SearchResult> patternToSearchResults = new HashSet<SearchResult>();

            // collect the urls
            for ( Map.Entry<Pattern, MetaQuery> entry : patternToQueries.entrySet())
                patternToSearchResults.add(engine.getSearchResults(entry.getValue(), entry.getKey()));
                    
            SearchResultCache cache = new SearchResultCache();
            ExecutorService service = Executors.newFixedThreadPool(100);
            List<HtmlCrawlerCallable> crawlerThreads = new ArrayList<HtmlCrawlerCallable>();
                    
            long start = System.currentTimeMillis();
            
            // prepare all websites for a single fact
            for ( SearchResult entry : patternToSearchResults ) {
                // only start threads if non empty
                if ( !entry.getWebSites().isEmpty() && !cache.contains(entry.getQuery().toString()))
                    for (WebSite site : entry.getWebSites()) crawlerThreads.add(new HtmlCrawlerCallable(site));
            }
            
            // crawl
            if ( !crawlerThreads.isEmpty() ) {

                try {
                    
                    for ( Future<WebSite> websiteFuture : service.invokeAll(crawlerThreads, 20, TimeUnit.SECONDS)) {
                        
                        System.out.println(String.format("\tDone [%s] - Canceled [%s]", websiteFuture.isDone() ? "x" : "o", websiteFuture.isCancelled() ? "x" : "o" )); 
                    }
                }
                catch (InterruptedException e) {
                    
                    throw new RuntimeException("What in the hell?", e);
                }
                service.shutdown();
                service.shutdownNow();
            }
            
            // save to database
            for ( SearchResult entry : patternToSearchResults ) 
                if ( !cache.contains(entry.getQuery().toString()) ) cache.add(entry);
            
            // 6. Log statistics
            System.out.println("Model " + currentModel + "/" + numberOfModels + " took " + TimeUtil.formatTime(System.currentTimeMillis() - start) +
                    " Average time: " + ( (System.currentTimeMillis() - startTime) / currentModel++ ) + "ms");
        }
    }
}
