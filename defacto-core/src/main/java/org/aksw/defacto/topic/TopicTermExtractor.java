package org.aksw.defacto.topic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.topic.frequency.Word;
import org.aksw.defacto.wikipedia.WikipediaPageCrawler;
import org.aksw.defacto.wikipedia.WikipediaSearchResult;
import org.aksw.defacto.wikipedia.WikipediaSearcher;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Mohamed Morsey <morsey@informatik.uni-leipzig.de>
 */
public class TopicTermExtractor {

    private static Logger logger = Logger.getLogger(TopicTermExtractor.class);
    
    private static Map<String,List<Word>> topicTermsCache = new HashMap<String,List<Word>>();
    
    static {

        try {
            
            // if it's not there create it and read an empty file
            if ( !new File("resources/cache/topics/cache.txt").exists() ) new File("resources/cache/topics/cache.txt").createNewFile();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("resources/cache/topics/cache.txt"))));
            String line;
            while ((line = reader.readLine()) != null) {
                
                if ( !line.trim().isEmpty() ) {
                    
                    String[] parts = line.split("---");
                    String wikiPage = parts[0];
                    List<Word> topicTerms = new ArrayList<Word>();
                    
                    for (int i = 1 ; i < parts.length ; i++ ) {
                        
                        String topic = parts[i].split(";")[0];
                        String occurrence = parts[i].split(";")[1];
                        topicTerms.add(new Word(topic, Integer.valueOf(occurrence)));
                    }
                    
                    topicTermsCache.put(wikiPage, topicTerms);
                }
            }
            reader.close();
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {

        System.out.println(getPotentialTopicTerms("Mehdi Hashemi Rafsanjani", "Islamic Azad University"));
        System.out.println(getPotentialTopicTerms("Mingus at the Bohemia", "Charles Mingus"));
    }
    
    /**
     * Write the url and the pagerank to the file and updates the cache object
     * 
     * @param domain
     * @param result
     */
    private static void udpateCache(String wikipediaPage, List<Word> topicTerms) {

        try {
            
            topicTermsCache.put(wikipediaPage, topicTerms);
            
            BufferedWriter bufferWritter = new BufferedWriter( new FileWriter( new File("resources/cache/topics/cache.txt"), true));

            List<String> words = new ArrayList<String>();
            for ( Word word : topicTerms)
                words.add(word.getWord()+";"+word.getFrequency());
            
            bufferWritter.write(wikipediaPage + "---" + StringUtils.join(words, "---") + "\n");
            bufferWritter.close();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @param subjectLabel
     * @param objectLabel
     * @return
     */
    private static List<Word> getPotentialTopicTerms(String subjectLabel, String objectLabel) {

        List<Word> potentialTopicTerms = new ArrayList<Word>();
        
        if ( topicTermsCache.containsKey(subjectLabel) ) potentialTopicTerms.addAll(topicTermsCache.get(subjectLabel));
        else {
            
            List<Word> topics = queryWikipediaPageAndGetTopicTerms(WikipediaSearcher.queryWikipedia(subjectLabel));
            potentialTopicTerms.addAll(topics);
            udpateCache(subjectLabel,topics);
        }
        
        if ( topicTermsCache.containsKey(objectLabel) ) potentialTopicTerms.addAll(topicTermsCache.get(objectLabel));
        else {
            
            List<Word> topics = queryWikipediaPageAndGetTopicTerms(WikipediaSearcher.queryWikipedia(objectLabel));
            potentialTopicTerms.addAll(topics);
            udpateCache(objectLabel,topics);
        }
        
        // now potentialTopicTerms contains all terms with high frequency in Wikipedia
        // we need sort them with the term, so that the repeated words are placed next to each other
        Collections.sort(potentialTopicTerms, new WordComparator());
                
        // the term that appears more than once is very likely to be a potential topic
        // so we should remove the other terms which are not repeated 
        Map<String, Word> topicTermsInPages = new LinkedHashMap<String, Word>();

        for ( Word word: potentialTopicTerms ) {
            
            // if the word is not in the HashMap, then we should add it and initialize its number of repetitions to 1
            if( !topicTermsInPages.containsKey(word.getWord()) )
                
                topicTermsInPages.put(word.getWord(), new Word(word.getWord(), word.getFrequency()));
            else {
                
                // it is already there, then we should just increase its repetitions
                Word newWord = topicTermsInPages.get(word.getWord());
                newWord.setFrequency(word.getFrequency() + newWord.getFrequency());
            }
        }

        List<Word> topicTerms = new ArrayList<Word>();
        
        // if the word is repeated only n-times, then we should remove it from the final list of terms
        for(Map.Entry<String, Word> entry : topicTermsInPages.entrySet()){
            
            if ( entry.getValue().getFrequency() > Defacto.DEFACTO_CONFIG.getIntegerSetting("evidence", "TOPIC_TERM_PAGE_THRESHOLD") )
                topicTerms.add(entry.getValue());
        }
        // this is probably not necessary but it does not cost too much
        Collections.sort(topicTerms, new WordFrequencyComparator());
        
        // debugging
        // logger.info("Found potential topic terms: " + topicTerms);
        return topicTerms.size() >= 20 ? topicTerms.subList(0, 20) : topicTerms;
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
            
            logger.warn("Single website crawling was canceled because of: ", ce);
        }
        catch (ExecutionException e) {
            
            logger.warn("Single website crawling was canceled because of: ", e);
        }
        catch (InterruptedException e) {
            
            logger.warn("Single website crawling was canceled because of: ", e);
        }
        logger.info("It took " + (System.currentTimeMillis() - start) +  "ms to crawl wikipedia pages and extract " + potentialTopicTerms.size() + " topic terms!");
        
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
            if ( webSite.getText().toLowerCase().contains(potentialTopicTerm.getWord().toLowerCase()) ) {
                
                numberOfSearchResultsWithTopicTerm++;
                topicTermFoundInBody = true;
            }

            // if the subject or the object label appears in the title of the page
            // then we should increment numberOfSearchResultsWithQueryTermsInTitle
            if ( webSite.getTitle().toLowerCase().contains(subjectLabel.toLowerCase()) 
                    || webSite.getTitle().toLowerCase().contains(objectLabel.toLowerCase()) ){

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
     * 
     * @param evidence
     * @return
     */
    public static List<Word> getTopicTerms(Evidence evidence) {
        
        List<Word> potentialTopicTerms = getPotentialTopicTerms(evidence.getSubjectLabel(), evidence.getObjectLabel());
        
        Set<WebSite> websites = new HashSet<WebSite>();
        for ( List<WebSite> sites : evidence.getWebSites().values() ) websites.addAll(sites);
            
        Iterator<Word> topicTermsIterator = potentialTopicTerms.iterator();
        while ( topicTermsIterator.hasNext() ) {
            
            Word topicTerm = topicTermsIterator.next();
            boolean isTopicTerm = TopicTermExtractor.isTopicTerm(websites, evidence.getSubjectLabel(), evidence.getObjectLabel(), topicTerm);
            
            // TODO if we leave it like this a topic term would then be only a topic term if it's a topic term for ALL website results
            if ( !isTopicTerm ) {
                
                logger.debug("Removing topic term: " + topicTerm.getWord());
                topicTermsIterator.remove();
            }
        }
        logger.info(evidence.getSubjectLabel() +" | "+ evidence.getObjectLabel() + ": " + StringUtils.join(potentialTopicTerms, ", "));
        
        return potentialTopicTerms;
    }
}
