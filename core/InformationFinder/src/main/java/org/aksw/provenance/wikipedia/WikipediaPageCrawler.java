/**
 * 
 */
package org.aksw.provenance.wikipedia;

import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.helper.WikipediaSearchResult;
import org.aksw.provenance.Constants;
import org.aksw.provenance.topic.frequency.Word;
import org.aksw.provenance.topic.frequency.WordFrequencyCounter;
import org.aksw.provenance.util.BasicJavaCrawlUtil;
import org.aksw.provenance.util.JsoupCrawlUtil;
import org.jsoup.Jsoup;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class WikipediaPageCrawler implements Callable<List<Word>> {

    private WikipediaSearchResult searchResult;
    private JsoupCrawlUtil crawlUtil;

    /**
     * 
     * @param result
     */
    public WikipediaPageCrawler(WikipediaSearchResult result) {
        
        this.searchResult = result;
        this.crawlUtil = new JsoupCrawlUtil();
    }
    
    @Override
    public List<Word> call() throws Exception {

        return getMostFrequentWordsInWikipediaArticle(this.searchResult.getPageURL(), Constants.TOPIC_TERMS_MAX_SIZE);
    }

    /**
     * Returns a list of the highly frequent words in a Wikipedia article
     * @param url   The URL for which the frequent word should be returned
     * @param topicTermsMaxSize 
     * @return  A list of the most frequent words
     */
    private List<Word> getMostFrequentWordsInWikipediaArticle(String url, int topicTermsMaxSize) {

        // crawl the text from wikipedia, remove the html markup and count the words
        List<Word> words = WordFrequencyCounter.getKeywordsSortedByFrequency(this.crawlUtil.readPage(url, Constants.WEB_SEARCH_TIMEOUT_MILLISECONDS));
        // and return only max values
        return words.size() >= Constants.TOPIC_TERMS_MAX_SIZE 
                ? words.subList(0, Constants.TOPIC_TERMS_MAX_SIZE) 
                : words;
    }
}
