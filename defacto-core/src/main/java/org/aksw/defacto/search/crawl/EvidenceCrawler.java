package org.aksw.defacto.search.crawl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.DefactoModel;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.cache.Cache;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.cache.solr.Solr4SearchResultCache;
import org.aksw.defacto.search.concurrent.HtmlCrawlerCallable;
import org.aksw.defacto.search.concurrent.ParseCallable;
import org.aksw.defacto.search.concurrent.WebSiteScoreCallable;
import org.aksw.defacto.search.engine.SearchEngine;
import org.aksw.defacto.search.engine.bing.AzureBingSearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.SearchResult;
import org.aksw.defacto.topic.TopicTermExtractor;
import org.aksw.defacto.util.ListUtil;
import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

import com.github.gerbsen.math.Frequency;

import edu.stanford.nlp.util.StringUtils;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class EvidenceCrawler {

    private Logger logger = Logger.getLogger(EvidenceCrawler.class);
    private Map<Pattern,MetaQuery> patternToQueries;
    private DefactoModel model;
    
    /**
     * 
     * @param model
     * @param patternToQueries
     */
    public EvidenceCrawler(DefactoModel model, Map<Pattern, MetaQuery> queries) {

        this.patternToQueries = queries;
        this.model            = model;
    }

    /**
     * 
     * @return
     */
    public Evidence crawlEvidence(String subjectLabel, String objectLabel) {

        Set<SearchResult> searchResults = this.generateSearchResultsInParallel();
        
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
    
    private Set<SearchResult> generateSearchResultsInParallel() {

        Set<SearchResult> results = new HashSet<SearchResult>();
        Set<SearchResultCallable> searchResultCallables = new HashSet<SearchResultCallable>();
        
        // collect the urls for a particular pattern
        // could be done in parallel 
        for ( Map.Entry<Pattern, MetaQuery> entry : this.patternToQueries.entrySet())
            searchResultCallables.add(new SearchResultCallable(entry.getValue(), entry.getKey()));
        
        // wait als long as the scoring needs, and score every website in parallel        
        ExecutorService executor = Executors.newFixedThreadPool(this.patternToQueries.size());
        try {
            
            for ( Future<SearchResult> result : executor.invokeAll(searchResultCallables)) {

                try {
                    
                    results.add(result.get());
                }
                catch (ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            executor.shutdownNow();
        }
        catch (Exception e) {

            e.printStackTrace();
        }
        
        return results;
    }

    private void scoreSearchResults(Set<SearchResult> searchResults, DefactoModel model, Evidence evidence) {

        evidence.setBoaPatterns(new BoaPatternSearcher().getNaturalLanguageRepresentations(model.getPropertyUri(), 200, 0.5));
        
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

    private void crawlAndCacheSearchResults(Set<SearchResult> searchResults, DefactoModel model, Evidence evidence) {
        
        // prepare the result variables
        List<HtmlCrawlerCallable> htmlCrawlers = new ArrayList<HtmlCrawlerCallable>();
        List<ParseCallable> parsers = new ArrayList<ParseCallable>();
        List<WebSite> websites = new ArrayList<WebSite>();
        
        // prepare the crawlers for simultanous execution
        for ( SearchResult searchResult : searchResults)
            for ( WebSite site : searchResult.getWebSites() ) {
            	
            	websites.add(site);
            	htmlCrawlers.add(new HtmlCrawlerCallable(site));
            }
                
        // nothing found. nothing to crawl
        if ( !htmlCrawlers.isEmpty() ) {

            // get the text from the urls
        	this.logger.info(String.format("Creating thread pool for %s html crawlers!", htmlCrawlers.size()));
            executeAndWaitAndShutdownCallables(Executors.newFixedThreadPool(htmlCrawlers.size()), htmlCrawlers);
        }

        // create |CPU|/2 parsers for n websites and split them to the parsers
        for ( List<WebSite> websiteSublist : ListUtil.split(websites, (websites.size() / (Runtime.getRuntime().availableProcessors() / 2)) + 1)) 
        	parsers.add(new ParseCallable(websiteSublist));
        
        this.logger.info(String.format("Creating thread pool for %s html crawlers!", htmlCrawlers.size()));
        executeAndWaitAndShutdownCallables(Executors.newFixedThreadPool(parsers.size()), parsers);
        this.extractDates(websites, evidence);
        
        List<SearchResult> results = new ArrayList<SearchResult>();
        // add the results of the crawl to the cache
        Cache<SearchResult> cache = new Solr4SearchResultCache();
        // this filters out links which are in the result of multiple search engine quries
        for ( SearchResult result : searchResults ) 
        	if ( !cache.contains(result.getQuery().toString()) ) 
        		results.add(result);
        
        for ( Map.Entry<String, Long> entry : evidence.yearOccurrences.entrySet()) {
        	
        	System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        
        cache.addAll(results);
    }
    
    /**
     * 
     * @param websites
     * @param evidence
     */
    private void extractDates(List<WebSite> websites, Evidence evidence) {
    	
    	Frequency frequency = new Frequency();
    	
    	for ( WebSite site : websites ) {
    		
    		if ( site.getTaggedText().isEmpty() ) continue;
    		for (String date : getEntities(StringUtils.split(site.getTaggedText(), "-=-"))) {
    			frequency.addValue(date);
    		}
    	}
    	
    	for ( Map.Entry<Comparable<?>, Long> entry : frequency.sortByValue()) 
    		evidence.yearOccurrences.put((String) entry.getKey(), entry.getValue());
    }

    /**
     * 
     * @param mergedTaggedSentence
     * @return
     */
	private List<String> getEntities(List<String> mergedTaggedSentence) {

		List<String> entities = new ArrayList<String>();
		for (String entity : mergedTaggedSentence) {

			if (entity.endsWith("_DATE"))
				entities.add(entity.replace("_DATE", ""));
		}
		
		return entities;
	}

	/**
     * 
     * @param executor
     * @param callables
     * @return
     */
    private <T> List<Future<T>> executeAndWaitAndShutdownCallables(ExecutorService executor, List<? extends Callable<T>> callables) {
    	
    	List<Future<T>> results = null;
    	
    	try {
            
            results = executor.invokeAll(callables);
            executor.shutdownNow();
        }
        catch (InterruptedException e) {

            e.printStackTrace();
        }
    	
    	return results;
    }
}
