package org.aksw.defacto.topic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.cache.solr.TopicTermSolr4Cache;
import org.aksw.defacto.topic.frequency.Word;
import org.aksw.defacto.wikipedia.WikipediaPageCrawler;
import org.aksw.defacto.wikipedia.WikipediaSearchResult;
import org.aksw.defacto.wikipedia.WikipediaSearcher;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Mohamed Morsey <morsey@informatik.uni-leipzig.de>
 */
public class TopicTermExtractor {

    private static Logger logger = Logger.getLogger(TopicTermExtractor.class);

    private static TopicTermSolr4Cache cache = new TopicTermSolr4Cache();
    
    public static void main(String[] args) throws InvalidFileFormatException, IOException {
    	
    	Defacto.init();
    	cache = new TopicTermSolr4Cache();
        for ( Word w : getPotentialTopicTerms("Amazon", "Jeff Bezos"))  System.out.println(w + " " + w.getFrequency());;
    }
    
    /**
     * 
     * @param subjectLabel
     * @param objectLabel
     * @return
     */
    private static List<Word> getPotentialTopicTerms(String language, String... labels) {
    	
    	List<Word> topics = new ArrayList<Word>();
    	for ( String label : labels ) 
    		topics.addAll(getPotentialTopicTermsFor(label, language));
    	
    	return new ArrayList<Word>(mergeTopicTerms(topics).values());
    }
    
    /**
     * 
     * @param potentialTopicTerms
     * @return
     */
    private static Map<String, Word> mergeTopicTerms(List<Word> potentialTopicTerms) {
		
    	Map<String, Word> topicTermsInPages = new LinkedHashMap<String, Word>();

        for ( Word word: potentialTopicTerms ) {
            
            // if the word is not in the HashMap, then we should add it and initialize its number of repetitions to 1
            if( !topicTermsInPages.containsKey(word.getWord()) )
                
                topicTermsInPages.put(word.getWord(), word);
            else {
                
                // it is already there, then we should just increase its repetitions
                Word newWord = topicTermsInPages.get(word.getWord());
                newWord.setFrequency(word.getFrequency() + newWord.getFrequency());
            }
        }
        
        return topicTermsInPages;
	}

    /**
     * 
     * @param label
     * @return
     */
	private static List<Word> getPotentialTopicTermsFor(String label, String language) {

		// go query for the subject in cache if it's no in there put it in
        if ( cache.contains(label) ) return cache.getEntry(label).relatedTopics;
		
    	List<Word> potentialTopicTerms = new ArrayList<Word>();
        potentialTopicTerms.addAll(queryWikipediaPageAndGetTopicTerms(WikipediaSearcher.queryWikipedia(label, language)));
        
        // the term that appears more than once is very likely to be a potential topic
        // so we should remove the other terms which are not repeated 
        Map<String, Word> topicTermsInPages = mergeTopicTerms(potentialTopicTerms);
        
        // get the top 20
        List<Word> topicTerms = new ArrayList<Word>(topicTermsInPages.values());
        Collections.sort(topicTerms, new WordFrequencyComparator());
        topicTerms = topicTerms.size() >= 20 ? topicTerms.subList(0, 20) : topicTerms;
        
        // add it to the cache and return only the related topics
        return cache.add(new TopicTerm(label, topicTerms)).relatedTopics;
	}

	private static List<Word> queryWikipediaPageAndGetTopicTerms(List<WikipediaSearchResult> wikiSearchResults) {
        
        long start = System.currentTimeMillis();
        List<Word> potentialTopicTerms = new ArrayList<Word>();
        if ( wikiSearchResults.isEmpty() ) return potentialTopicTerms;
        ExecutorService executorService = Executors.newFixedThreadPool(wikiSearchResults.size());
        List<WikipediaPageCrawler> wikipageCrawler = new ArrayList<WikipediaPageCrawler>();
        
        for ( WikipediaSearchResult result : wikiSearchResults )
        	wikipageCrawler.add(new WikipediaPageCrawler(result));
        
        try {
            
            for ( Future<List<Word>> future : executorService.invokeAll(wikipageCrawler))
                potentialTopicTerms.addAll(future.get());
        }
        catch (CancellationException ce) {
            
        	ce.printStackTrace();
            logger.warn("Single website crawling was canceled because of: ", ce);
        }
        catch (ExecutionException e) {
            
        	e.printStackTrace();
            logger.warn("Single website crawling was canceled because of: ", e);
        }
        catch (InterruptedException e) {
            
        	e.printStackTrace();
            logger.warn("Single website crawling was canceled because of: ", e);
        }
        logger.debug("It took " + (System.currentTimeMillis() - start) +  "ms to crawl wikipedia pages and extract " + potentialTopicTerms.size() + " topic terms!");
        
        executorService.shutdown();
        executorService.shutdownNow();
        
        return potentialTopicTerms;
    }

    /**
     * Determines whether potentialTopicTerm is a TopicTerm according to the results in list searchResults, which are
     * fetched from a search engine
     * The calculation is mainly based on the paper titled "Trustworthiness Analysis of Web Search Results".
     * @param searchResults A list of search results fetched from the search engine
     * @param queryTerms    An object containing the terms used for searching using the search engine
     * @param potentialTopicTerm    A term that can probably be a TopicTerm (mostly fetched from Wikipedia)
     * @return  Whether it is a topic term or not
     */
    public static boolean isTopicTerm(Set<WebSite> webSites, String subjectLabel, String objectLabel, Word potentialTopicTerm){
        
        // number of results returned by the search engine for a query
        int numberOfSearchResults                                        = webSites.size(); 
        // number of results returned by the search engine in which the potential Topic Term also appears
        int numberOfSearchResultsWithTopicTerm                           = 0;
        // the number of pages that contain query terms in title
        int numberOfSearchResultsWithQueryTermsInTitle                   = 0;
        // the number of pages that contain query terms in title, and also contain the potential topic term in body
        int numberOfSearchResultsWithQueryTermsInTitleAndTopicTermInBody = 0;

        boolean topicTermFoundInBody;
        
        for ( WebSite webSite : webSites ) {

            topicTermFoundInBody = false;

            // if the term appears in the webpage body, then numberOfSearchResultsWithTopicTerm should be incremented
            if ( webSite.getLowerCaseText().contains(potentialTopicTerm.getWord().toLowerCase()) ) {
                
                numberOfSearchResultsWithTopicTerm++;
                topicTermFoundInBody = true;
            }

            // if the subject or the object label appears in the title of the page
            // then we should increment numberOfSearchResultsWithQueryTermsInTitle
            if ( webSite.getLowerCaseTitle().contains(subjectLabel.toLowerCase()) 
                    || webSite.getLowerCaseTitle().contains(objectLabel.toLowerCase()) ){

                numberOfSearchResultsWithQueryTermsInTitle++;
                
                // if the potential topic term was also found in body, then we should also 
                // increment numberOfSearchResultsWithQueryTermsInTitleAndTopicTermInBody
                if ( topicTermFoundInBody ) numberOfSearchResultsWithQueryTermsInTitleAndTopicTermInBody++;
            }
        }

        float numberOfSearchResultsWithTopicTermProbability = 0;
        float numberOfSearchResultsWithQueryTermsInTitleAndTopicTermInBodyProbability = 0;
        
        if ( numberOfSearchResults > 0 ) // make sure that denominator is above 0
            numberOfSearchResultsWithTopicTermProbability = (float) numberOfSearchResultsWithTopicTerm / (float) numberOfSearchResults;

        if ( numberOfSearchResultsWithQueryTermsInTitle > 0 ) //make sure that denominator is above 0
            numberOfSearchResultsWithQueryTermsInTitleAndTopicTermInBodyProbability = (float) numberOfSearchResultsWithQueryTermsInTitleAndTopicTermInBody / (float) numberOfSearchResultsWithQueryTermsInTitle;

        return numberOfSearchResultsWithQueryTermsInTitleAndTopicTermInBodyProbability >= numberOfSearchResultsWithTopicTermProbability;
    }
    
    /**
     * 
     * @author Mohamed Morsey <morsey@informatik.uni-leipzig.de>
     */
    public static class WordComparator implements Comparator<Word> {
        
        public int compare(Word firstWord, Word secondWord){
            int comparisonResult = 0;

            //Compare the words as strings
            comparisonResult = firstWord.getWord().compareTo(secondWord.getWord());

            //if the words are the same, then sort them with the frequencies
            if ( comparisonResult == 0 ) {
                comparisonResult = (firstWord.getFrequency() > secondWord.getFrequency() ? 1 :
                        (firstWord.getFrequency() == secondWord.getFrequency() ? 0 : -1));
            }

            return comparisonResult;
        }
    }
    
    /**
     * 
     * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
     */
    private static class WordFrequencyComparator implements Comparator<Word> {
        
        public int compare(Word firstWord, Word secondWord){

            return firstWord.getFrequency() > secondWord.getFrequency() ? -1 :
                firstWord.getFrequency() == secondWord.getFrequency() ? 0 : 1;
        }
    }

    /**
     * TODO uncomment
     * 
     * 
     * @param evidence
     * @return
     */
    public static List<Word> getTopicTerms(String subjectLabel, String objectLabel, String language, Evidence evidence) {
        
        List<Word> potentialTopicTerms = getPotentialTopicTerms(language, subjectLabel, objectLabel);
        
        Set<WebSite> websites = new HashSet<WebSite>();
        for ( List<WebSite> sites : evidence.getWebSites().values() ) websites.addAll(sites);
            
        Iterator<Word> topicTermsIterator = potentialTopicTerms.iterator();
        while ( topicTermsIterator.hasNext() ) {
            
            Word topicTerm = topicTermsIterator.next();
            boolean isTopicTerm = TopicTermExtractor.isTopicTerm(websites, subjectLabel, objectLabel, topicTerm);
            
            // TODO if we leave it like this a topic term would then be only a topic term if it's a topic term for ALL website results
            if ( !isTopicTerm ) {
                
                logger.debug("Removing topic term: " + topicTerm.getWord());
                topicTermsIterator.remove();
            }
        }
        logger.debug(language + " | " + subjectLabel + " | "+ objectLabel + ": " + StringUtils.join(potentialTopicTerms, ", "));
        
        return potentialTopicTerms;
    }
}
