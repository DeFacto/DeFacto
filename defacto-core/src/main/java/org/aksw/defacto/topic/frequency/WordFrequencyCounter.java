package org.aksw.defacto.topic.frequency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.PatternAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 3/9/12
 * Time: 11:59 AM
 * Helps in counting the frequency of words in a string and sort them descendingly with the frequency
 * copied from http://javabycode.blogspot.com/2010/12/word-frequency-counter.html
 */
public class WordFrequencyCounter {

    private static Logger logger = Logger.getLogger(WordFrequencyCounter.class.getName());
    
    private static List<String> stopwords = new ArrayList<String>();
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
     * @param inputWords
     * @return
     */
    public static ArrayList<Word> getKeywordsSortedByFrequency(String inputWords){

        PatternAnalyzer keywordAnalyzer     = PatternAnalyzer.EXTENDED_ANALYZER;
        TokenStream pageTokens              = keywordAnalyzer.tokenStream("", inputWords);
        CharTermAttribute charTermAttribute = pageTokens.getAttribute(CharTermAttribute.class);
        ArrayList<String> tokens            = new ArrayList<String>(1000);

        try{
            
            while (pageTokens.incrementToken()) {
                
                // we need to filter these stop words, mostly references in wikipedia
                String token = charTermAttribute.toString();
                if ( token.length() > 2 && !stopwords.contains(token) ) tokens.add(token.trim());
            }
        }
        catch (IOException exp){
            
            logger.error("Cannot get a token from the stream, due to " + exp.getMessage());
        }

        HashMap<String,Word> map = new HashMap<String,Word>();
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