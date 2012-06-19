package org.aksw.provenance.search.crawl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.aksw.provenance.Constants;
import org.aksw.provenance.boa.BoaPatternSearcher;
import org.aksw.provenance.boa.Pattern;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.evidence.PossibleProof;
import org.aksw.provenance.evidence.Proof;
import org.aksw.provenance.evidence.WebSite;
import org.aksw.provenance.search.cache.SearchResultCache;
import org.aksw.provenance.search.concurrent.HtmlCrawlerCallable;
import org.aksw.provenance.search.concurrent.WebSiteScoreCallable;
import org.aksw.provenance.search.engine.SearchEngine;
import org.aksw.provenance.search.engine.bing.BingSearchEngine;
import org.aksw.provenance.search.query.MetaQuery;
import org.aksw.provenance.search.result.SearchResult;
import org.aksw.provenance.topic.TopicTermExtractor;
import org.aksw.provenance.util.ModelUtil;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class EvidenceCrawler {

    private Logger logger = Logger.getLogger(EvidenceCrawler.class);
    private Map<Pattern,MetaQuery> patternToQueries;
    private Model model;
    private String modelName;
    
    /**
     * 
     * @param model
     * @param patternToQueries
     */
    public EvidenceCrawler(Model model, Map<Pattern, MetaQuery> queries) {

        this.patternToQueries = queries;
        this.model            = model;
        this.modelName        = this.model.getNsPrefixURI("name");
    }

    /**
     * 
     * @return
     */
    public Evidence crawlEvidence(String subjectLabel, String objectLabel) {

        SearchEngine engine = new BingSearchEngine();
        Set<SearchResult> searchResults = new HashSet<SearchResult>();

        // collect the urls for a particular pattern
        // could be done in parallel 
        for ( Map.Entry<Pattern, MetaQuery> entry : this.patternToQueries.entrySet())
            searchResults.add(engine.getSearchResults(entry.getValue(), entry.getKey()));
        
        // multiple pattern bring the same results but we dont want that
        this.filterSearchResults(searchResults);

        Long totalHitCount = 0L; // sum over the n*m query results        
        for ( SearchResult result : searchResults ) totalHitCount += result.getTotalHitCount();  
                
        Evidence evidence = new Evidence(model, totalHitCount, subjectLabel, objectLabel);
        // basically downloads all websites in parallel
        crawlAndCacheSearchResults(searchResults, model, evidence);
        // tries to find proofs and possible proofs and scores those
        scoreSearchResults(searchResults, model, evidence);
                
        // start multiple threads to download the text of the websites simultaneously
        for ( SearchResult result : searchResults ) 
            evidence.addWebSites(result.getPattern(), result.getWebSites());
        
        evidence.setTopicTerms(TopicTermExtractor.getTopicTerms(evidence));
        evidence.setTopicTermVectorForWebsites();
        evidence.calculateSimilarityMatrix();
        
        return evidence;
    }
    
    private void scoreSearchResults(Set<SearchResult> searchResults, Model model, Evidence evidence) {

        evidence.setBoaPatterns(new BoaPatternSearcher().getNaturalLanguageRepresentations(ModelUtil.getPropertyUri(model), 200, 0.5));
        
        // prepare the scoring 
        List<WebSiteScoreCallable> scoreCallables =  new ArrayList<WebSiteScoreCallable>();
        for ( SearchResult result : searchResults ) 
            for (WebSite site : result.getWebSites() )
                scoreCallables.add(new WebSiteScoreCallable(site, evidence, model));
        
        // nothing found, nothing to score
        if ( scoreCallables.isEmpty() ) return;
                    
        // wait als long as the scoring needs, and score every website in parallel        
        ExecutorService executor = Executors.newFixedThreadPool(scoreCallables.size());
        try {
            
            executor.invokeAll(scoreCallables);
            executor.shutdownNow();
        }
        catch (InterruptedException e) {

            e.printStackTrace();
        }
    }

    /**
     * This filters out all duplicate websites which then improves crawling speed!
     * 
     * @param patternToSearchResults
     */
    private void filterSearchResults(Set<SearchResult> searchResults) {

        // this should remove the duplicates from the list
        Set<String> alreadyKnowUrls = new HashSet<String>();
        
        for ( SearchResult searchResult : searchResults ) {

            // since there might be also duplicates in the cache, we want to remove them two
            Iterator<WebSite> iterator = searchResult.getWebSites().iterator();
            while ( iterator.hasNext() ) {
                
                WebSite site = iterator.next();
                if ( alreadyKnowUrls.contains(site.getUrl()) ) iterator.remove();
                else alreadyKnowUrls.add(site.getUrl());
            }
        }
    }

    private void crawlAndCacheSearchResults(Set<SearchResult> searchResults, Model model, Evidence evidence) {
        
        // prepare the result variables
        List<HtmlCrawlerCallable> htmlCrawlers = new ArrayList<HtmlCrawlerCallable>();
        
        // prepare the crawlers for simultanous execution
        for ( SearchResult searchResult : searchResults)
            for ( WebSite site : searchResult.getWebSites() ) 
                htmlCrawlers.add(new HtmlCrawlerCallable(site));
                    
        // nothing found. nothing to crawl
        if ( !htmlCrawlers.isEmpty() ) {

            // get the text from the urls
            ExecutorService executor = Executors.newFixedThreadPool(htmlCrawlers.size());
            this.logger.info(String.format("Creating thread pool for %s html crawlers!", Constants.NUMBER_OF_SEARCH_THREADS));
                        
            try {
                
                // this sets the text of all web-sites and cancels each task if it's not finished
                for ( Future<WebSite> future : executor.invokeAll(htmlCrawlers, Constants.WEB_SEARCH_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS) )
                    logger.debug(String.format("\tDone [%s] - Canceled [%s]", future.isDone() ? "yes" : "no", future.isCancelled() ? "yes" : "no" ));
                
                executor.shutdownNow();
            }
            catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
                    
        // add the results of the crawl to the cache
        SearchResultCache cache = new SearchResultCache();
        for ( SearchResult result : searchResults )
            if ( !cache.contains(result.getQuery().toString()) ) cache.add(result);
    }
    
//    private Map<Pattern,List<WebSite>> getWebSites(Map<Pattern,SearchResult> patternToSearchResult, Model model, Evidence evidence) {
//        
//        // get the text from the urls
//        ExecutorService executor = Executors.newFixedThreadPool(Constants.NUMBER_OF_SEARCH_THREADS);
//        this.logger.info(String.format("Creating thread pool for %s html crawlers!", Constants.NUMBER_OF_SEARCH_THREADS));
//        
//        // prepare the result variables
//        List<WebSiteScoreCallable> websiteCrawler = new ArrayList<WebSiteScoreCallable>();
//        
//        // prepare the crawlers for simultanous execution
//        for ( Map.Entry<Pattern,SearchResult> entry : patternToSearchResult.entrySet())
//            websiteCrawler.add(new WebSiteScoreCallable(evidence, entry.getKey(), entry.getValue(), model));
//
//        // execute the threads and wait for their termination for max 10 seconds
//        Map<Pattern,List<WebSite>> results = this.executeWebSiteCrawler(evidence, executor, websiteCrawler);
//                
//        // add the results of the crawl to the cache
//        SearchResultCache cache = new SearchResultCache();
//        for ( SearchResult result : patternToSearchResult.values() )
//            if ( !cache.contains(result.getQuery().toString()) ) cache.add(result);
//                
//        return results;        
//    }
    

    /**
     * 
     * @param executor
     * @param websiteCrawler
     * @return
     */
//    private Map<Pattern,List<WebSite>> executeWebSiteCrawler(Evidence evidence, ExecutorService executor, List<WebSiteScoreCallable> websiteCrawler) {
//
//        Map<Pattern,List<WebSite>> patternToWebSites = new LinkedHashMap<Pattern,List<WebSite>>();
//        int numberOfWebSites = 0;
//        
//        try {
//            
//            for ( Future<Map<Pattern,List<WebSite>>> future : executor.invokeAll(websiteCrawler)) 
//                for (Map.Entry<Pattern, List<WebSite>> entry : future.get().entrySet() ) {
//                    
//                    for ( WebSite site : entry.getValue() )
//                        site.setScore(this.scoreSite(evidence, site));
//                    
//                    patternToWebSites.put(entry.getKey(),entry.getValue());
//                    numberOfWebSites++;
//                }
//            logger.info(String.format("Found %s websites for model %s", numberOfWebSites, this.modelName));        
//        }
//        catch (InterruptedException e) {
//
//            logger.warn("Websites crawling was canceled because of: ", e);
//        }
//        catch (ExecutionException e) {
//
//            logger.warn("Websites crawling was canceled because of: ", e);
//        }
//        
//        executor.shutdown();
//        executor.shutdownNow();
//        
//        return patternToWebSites;
//    }
}
