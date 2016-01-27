package org.aksw.defacto.search.crawl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.Defacto.TIME_DISTRIBUTION_ONLY;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.cache.Cache;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.cache.solr.Solr4SearchResultCache;
import org.aksw.defacto.search.concurrent.HtmlCrawlerCallable;
import org.aksw.defacto.search.concurrent.RegexParseCallable;
import org.aksw.defacto.search.concurrent.WebSiteScoreCallable;
import org.aksw.defacto.search.engine.SearchEngine;
import org.aksw.defacto.search.engine.bing.AzureBingSearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.SearchResult;
import org.aksw.defacto.topic.TopicTermExtractor;
import org.aksw.defacto.topic.frequency.Word;
import org.aksw.defacto.util.Frequency;
import org.aksw.defacto.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class EvidenceCrawler {

	private static final Logger LOGGER = LoggerFactory.getLogger(EvidenceCrawler.class);
    public static org.apache.log4j.Logger LOGDEV    = org.apache.log4j.Logger.getLogger("developer");
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[0-9]{4}");
    private Map<Pattern,MetaQuery> patternToQueries;
    private DefactoModel model;
    
    public Map<DefactoModel,Evidence> evidenceCache = new HashMap<DefactoModel,Evidence>();
    public Map<DefactoModel,Evidence> counterEvidenceCache = new HashMap<DefactoModel,Evidence>();

    private Constants.EvidenceType evidenceType;
    
    /**
     * 
     * @param model
     * @param patternToQueries
     */
    public EvidenceCrawler(DefactoModel model, Map<Pattern, MetaQuery> queries) {
        this.patternToQueries = queries;
        this.model            = model;
    }


    private Evidence crawl(SearchEngine engine, Constants.EvidenceType evidenceType) {

        Map<DefactoModel, Evidence> cache;
        Evidence evidence = null;

        this.evidenceType = evidenceType;

        LOGDEV.debug(" -> starting crawling for " + evidenceType.toString() + " evidences" + "[SearchEngineClass: " + engine.getClass().toString() + "]");

        if (evidenceType.equals(Constants.EvidenceType.POS)) {
            cache = evidenceCache;
        }else if (evidenceType.equals(Constants.EvidenceType.NEG)) {
            cache = counterEvidenceCache;
        }else {
            LOGDEV.fatal(" -> evidence type value is not valid: " + evidenceType.toString());
            return null;
        }

        if ( !cache.containsKey(this.model) ) {

            LOGDEV.debug(" -> evidences not cached, starting caching...");

            long start = System.currentTimeMillis();
            LOGDEV.debug(" -> start getting search results");
            Set<SearchResult> searchResults = this.generateSearchResultsInParallel(engine);
            LOGDEV.debug(" -> finished getting search results in " + (System.currentTimeMillis() - start));

            // multiple pattern bring the same results but we dont want that
            this.filterSearchResults(searchResults);

            Long totalHitCount = 0L; // sum over the n*m query results
            for ( SearchResult result : searchResults ) {
                totalHitCount += result.getTotalHitCount();
            }

            LOGDEV.debug(" -> total hint count = " + totalHitCount);

            evidence = new Evidence(model, totalHitCount, patternToQueries.keySet());

            // basically downloads all websites in parallel
            LOGDEV.debug(" -> [1.1] starting crawling the search results...");
            crawlSearchResults(searchResults, model, evidence);

            // tries to find proofs and possible proofs and scores those
            LOGDEV.debug(" -> [1.2] scoring the search results...");
            scoreSearchResults(searchResults, model, evidence);

            // put it in solr cache
            LOGDEV.debug(" -> [1.3] caching search results...");
            cacheSearchResults(searchResults);

            // start multiple threads to download the text of the websites simultaneously
            for ( SearchResult result : searchResults )
                evidence.addWebSites(result.getPattern(), result.getWebSites());

            cache.put(model, evidence);

            if (evidenceType.equals(Constants.EvidenceType.POS)) {
                evidenceCache = cache;
                evidence.setEvidenceType(Constants.EvidenceType.POS);
            }else if (evidenceType.equals(Constants.EvidenceType.NEG)) {
                counterEvidenceCache = cache;
                evidence.setEvidenceType(Constants.EvidenceType.NEG);
            }

        }

        LOGDEV.debug(" -> getting cached evidence...");

        evidence = cache.get(model);

        // get the time frame or point
        evidence.calculateDefactoTimePeriod();

        long start = System.currentTimeMillis();
        // save all the time we can get
        if ( Defacto.onlyTimes.equals(TIME_DISTRIBUTION_ONLY.NO) ) {

            for ( String language : model.getLanguages() ) {

                String subjectLabel = evidence.getModel().getSubjectLabel(language);
                String objectLabel = evidence.getModel().getObjectLabel(language);

                if ( !subjectLabel.equals(Constants.NO_LABEL) && !objectLabel.equals(Constants.NO_LABEL) ) {

                    List<Word> topicTerms = TopicTermExtractor.getTopicTerms(subjectLabel, objectLabel, language, evidence);
                    evidence.setTopicTerms(language, topicTerms);
                    evidence.setTopicTermVectorForWebsites(language);
                }
            }
            evidence.calculateSimilarityMatrix();
        }
        LOGGER.info(String.format("  -> Extraction of topic terms took %s", TimeUtil.formatTime(System.currentTimeMillis() - start)));

        return evidence;


    }

    public Evidence crawlCounterEvidence(SearchEngine engine) {

        LOGDEV.info(" -> starting counter evidence (NEG) crawl process");
        Evidence ret =  crawl(engine, Constants.EvidenceType.NEG);
        return ret;
    }

    /**
     * 
     * @return
     */
    public Evidence crawlEvidence(SearchEngine engine) {

        LOGDEV.info(" -> starting evidence (POS) crawl process");
        Evidence ret =  crawl(engine, Constants.EvidenceType.POS);
        return ret;

    }
    
    /**
     * 
     * @param searchResults
     */
    private void cacheSearchResults(Set<SearchResult> searchResults) {
    	
    	long start = System.currentTimeMillis();
    	List<SearchResult> results = new ArrayList<SearchResult>();
        // add the results of the crawl to the cache
        Cache<SearchResult> cache = new Solr4SearchResultCache();
        // this filters out links which are in the result of multiple search engine quries
        for ( SearchResult result : searchResults ) 
        	if ( !cache.contains(result.getQuery().toString()) ) 
        		results.add(result);
        
        cache.addAll(results);
        LOGGER.debug(String.format("  -> Caching took %sms", System.currentTimeMillis()-start));
	}

    /**
     * 
     * @return
     */
	private Set<SearchResult> generateSearchResultsInParallel(SearchEngine engine) {

        Set<SearchResult> results = new HashSet<SearchResult>();
        Set<SearchResultCallable> searchResultCallables = new HashSet<SearchResultCallable>();

        
        // collect the urls for a particular pattern
        // could be done in parallel 
        for ( Map.Entry<Pattern, MetaQuery> entry : this.patternToQueries.entrySet()) {
            //set the evidence type for metaquery
            entry.getValue().setEvidenceTypeRelation(this.evidenceType);
            searchResultCallables.add(new SearchResultCallable(entry.getValue(), entry.getKey(), engine));
        }

        LOGDEV.debug(" -> Starting to crawl/get from cache " + searchResultCallables.size() + " search results with " +
                Defacto.DEFACTO_CONFIG.getIntegerSetting("crawl", "NUMBER_OF_SEARCH_RESULTS_THREADS") + " threads.");
        
        try {
        	
        	ExecutorService executor = Executors.newFixedThreadPool(Defacto.DEFACTO_CONFIG.getIntegerSetting("crawl", "NUMBER_OF_SEARCH_RESULTS_THREADS"));
            int i=1;
            for ( Future<SearchResult> result : executor.invokeAll(searchResultCallables)) {

                results.add(result.get());
                LOGDEV.debug(" query " + i + ": " + result.get().getQuery().toString() + ": nr. websites = " + result.get().getWebSites().size());
                i++;
            }
            executor.shutdownNow();
        }
        catch (Exception e) {

            e.printStackTrace();
        }
        
        return results;
    }

    private void scoreSearchResults(Set<SearchResult> searchResults, DefactoModel model, Evidence evidence) {

        // ########################################
    	// 1. Score the websites 
        List<WebSiteScoreCallable> scoreCallables =  new ArrayList<WebSiteScoreCallable>();
        for ( SearchResult result : searchResults ) 
            for (WebSite site : result.getWebSites() )
                scoreCallables.add(new WebSiteScoreCallable(site, evidence, model, result.getPattern()));
        
        // nothing found, nothing to score
        if ( scoreCallables.isEmpty() ) return;
                    
        long start = System.currentTimeMillis();
        // wait als long as the scoring needs, and score every website in parallel
        this.executeAndWaitAndShutdownCallables(Executors.newFixedThreadPool(50), scoreCallables);
        
        // ########################################
    	// 2. parse the pages to look for dates
        List<RegexParseCallable> parsers = new ArrayList<RegexParseCallable>();
        List<ComplexProof> proofs = new ArrayList<ComplexProof>(evidence.getComplexProofs());
        
        // create |CPU| parsers for n websites and split them to the parsers
        for ( ComplexProof proofsSublist : proofs)
        	parsers.add(new RegexParseCallable(proofsSublist));
        
        start = System.currentTimeMillis();
        LOGGER.debug(String.format("Proof parsing %s websites per parser, %s at a time!", parsers.size(), Defacto.DEFACTO_CONFIG.getIntegerSetting("extract", "NUMBER_NLP_STANFORD_MODELS")));
        executeAndWaitAndShutdownCallables(Executors.newFixedThreadPool(
        		Defacto.DEFACTO_CONFIG.getIntegerSetting("extract", "NUMBER_NLP_STANFORD_MODELS")), parsers);
        LOGGER.debug(String.format("Proof parsing finished in %sms!", (System.currentTimeMillis() - start)));
        
        this.extractDates(evidence);
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

    private void crawlSearchResults(Set<SearchResult> searchResults, DefactoModel model, Evidence evidence) {
        
        // prepare the result variables
        List<HtmlCrawlerCallable> htmlCrawlers = new ArrayList<HtmlCrawlerCallable>();
        
        // prepare the crawlers for simultanous execution
        for ( SearchResult searchResult : searchResults)
            for ( WebSite site : searchResult.getWebSites() ) {
                htmlCrawlers.add(new HtmlCrawlerCallable(site));
                LOGDEV.debug(" -> adding returned website: " + site.getUrl() + " - query: " + site.getQuery().toString());
            }

        // nothing found. nothing to crawl
        if ( !htmlCrawlers.isEmpty() ) {
        	
        	int threads = 10;
        	
        	long start = System.currentTimeMillis();
            // get the text from the urls
        	LOGGER.debug(String.format("Creating thread pool for %s html crawlers, with %s threads!", htmlCrawlers.size(), threads));
            executeAndWaitAndShutdownCallables(Executors.newFixedThreadPool(threads), htmlCrawlers);
            LOGGER.debug(String.format("Html crawling took %sms", (System.currentTimeMillis() - start)));
        }
    }
    
    /**
     * 
     * @param websites
     * @param evidence
     */
    private void extractDates(Evidence evidence) {
    	
    	Frequency tinyContextFrequency = new Frequency();
    	Frequency smallContextFrequency = new Frequency();
    	Frequency mediumContextFrequency = new Frequency();
    	Frequency largeContextFrequency = new Frequency();
    	
    	for ( ComplexProof proof : evidence.getComplexProofs() ) {

    		addFrequency(proof.getTinyContext(), proof.getTaggedTinyContext(), proof, tinyContextFrequency, evidence);
    		addFrequency(proof.getSmallContext(), proof.getTaggedSmallContext(), proof, smallContextFrequency, evidence);
    		addFrequency(proof.getMediumContext(), proof.getTaggedMediumContext(), proof, mediumContextFrequency, evidence);
    		addFrequency(proof.getLargeContext(), proof.getTaggedLargeContext(), proof, largeContextFrequency, evidence);
    	}
    	
    	for ( Map.Entry<Comparable<?>, Long> entry : tinyContextFrequency.sortByValue()) 
    		evidence.tinyContextYearOccurrences.put((String) entry.getKey(), entry.getValue());
    	for ( Map.Entry<Comparable<?>, Long> entry : smallContextFrequency.sortByValue()) 
    		evidence.smallContextYearOccurrences.put((String) entry.getKey(), entry.getValue());
    	for ( Map.Entry<Comparable<?>, Long> entry : mediumContextFrequency.sortByValue()) 
    		evidence.mediumContextYearOccurrences.put((String) entry.getKey(), entry.getValue());
    	for ( Map.Entry<Comparable<?>, Long> entry : largeContextFrequency.sortByValue()) 
    		evidence.largeContextYearOccurrences.put((String) entry.getKey(), entry.getValue());
    }
    
    private void addFrequency(String context, String taggedContext, ComplexProof proof, Frequency frequency, Evidence evidence) {
    	
    	if ( taggedContext == null ) return;
    	
		String fact = proof.getSubject().trim() + " " + proof.getProofPhrase().trim() + " " + proof.getObject().trim();
		int firstIndex	= context.indexOf(fact);
		int mediumIndex	= firstIndex + (fact.length() / 2);
		
		for (String entity : StringUtils.split(taggedContext, "-=-")) {

			if (entity.endsWith("_DATE")) {
				
				String date = entity.replace("_DATE", "");
				Matcher matcher = pattern.matcher(date);
			    while (matcher.find()) {
			    	
			    	String match = matcher.group();
			    	
			    	int indexOfDate = context.indexOf(match);
			    	int distance = Integer.MAX_VALUE;
			    	
					if ( indexOfDate > mediumIndex ) distance  = indexOfDate - mediumIndex;
			    	else distance = mediumIndex - indexOfDate;
					
					evidence.addDate(match, distance);
			    	frequency.addValue(match);
			    }
			}
		}
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
