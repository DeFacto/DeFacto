/**
 * 
 */
package org.aksw.defacto.wikipedia;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.topic.frequency.Word;
import org.aksw.defacto.topic.frequency.WordFrequencyCounter;
import org.aksw.defacto.util.JsoupCrawlUtil;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class WikipediaPageCrawler implements Callable<List<Word>> {

    private WikipediaSearchResult searchResult;
    private JsoupCrawlUtil crawlUtil;
    
    private static Logger logger = Logger.getLogger(WordFrequencyCounter.class.getName());
    
    public  static List<String> stopwords = new ArrayList<String>();
    static {
        
        stopwords.addAll(Arrays.asList("disambiguation", "http", "retrieved", "com", 
                        "www", "html", "wikipedia", "link", "links", "isbn", "en", "jp",
                        "edit", "article", "world", "articles", "history", "free", "contact",
                        "changes", "pages", "news", "january", "february", "march", "april",
                        "may", "june", "july", "august", "september", "october", "november",
                        "december", "org", "categories", "sources", "st", "one", "page", "new", 
                        "create", "main", "encyclopedia", "navigation", "title", "references", "edu", "index"));
    }

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

        return getMostFrequentWordsInWikipediaArticle(this.searchResult.getPageURL(), 
        		Defacto.DEFACTO_CONFIG.getIntegerSetting("evidence", "TOPIC_TERMS_MAX_SIZE"));
    }

    /**
     * Returns a list of the highly frequent words in a Wikipedia article
     * @param url   The URL for which the frequent word should be returned
     * @param topicTermsMaxSize 
     * @return  A list of the most frequent words
     */
    private List<Word> getMostFrequentWordsInWikipediaArticle(String url, int topicTermsMaxSize) {

    	System.out.println(url);
    	
    	String text = this.crawlUtil.readPage(url, Defacto.DEFACTO_CONFIG.getIntegerSetting("crawl", "WEB_SEARCH_TIMEOUT_MILLISECONDS"));
    	
      System.out.println("Crwaling done: " +  text.length());
    	
        // crawl the text from wikipedia, remove the html markup and count the words
        List<Word> words = getKeywordsSortedByFrequency(text);
                
        // and return only max values
        return words.size() >= topicTermsMaxSize ? words.subList(0, topicTermsMaxSize) : words;
    }
    
    public ArrayList<Word> getKeywordsSortedByFrequency(String inputWords){

    	ArrayList<String> tokens = new ArrayList<String>(1000);
    	
        try{
        	
        	Analyzer analyzer	= new EnglishAnalyzer(Version.LUCENE_41);
        	Reader reader		= new StringReader(inputWords);
            TokenStream stream  = analyzer.tokenStream("", reader);
            stream.reset();
            while (stream.incrementToken()) {
                
                // we need to filter these stop words, mostly references in wikipedia
                String token = stream.getAttribute(CharTermAttribute.class).toString();
                
//                System.out.println(token);
                if ( token.length() > 2 && !stopwords.contains(token) ) tokens.add(token.trim());
            }
            System.out.println("finished!");
            reader.close();
            analyzer.close();
        }
        catch (IOException exp){
            
        	exp.printStackTrace();
            logger.error("Cannot get a token from the stream, due to " + exp.getMessage());
        }
        
        System.exit(0);

        Map<String,Word> map = new HashMap<String,Word>();
        for(String token : tokens){
            
            Word word = map.get(token);
            if ( word == null ) {
                
                word = new Word(token,1);
                map.put(token, word);
            }
            else word.incrementFrequency();
        }
        // sort the values by there frequency and return them
        ArrayList<Word> sortedKeywordList = new ArrayList<Word>(map.values());
        Collections.sort(sortedKeywordList);
        return sortedKeywordList;
    }
}
