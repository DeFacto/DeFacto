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
import org.apache.lucene.analysis.core.SimpleAnalyzer;
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
    
    public static List<String> stopwords = new ArrayList<String>();
    static {
        
        stopwords.addAll(Arrays.asList("disambiguation", "http", "retrieved", "com", 
                        "xml" , "www", "html", "wikipedia", "link", "links", "isbn", "en", "jp",
                        "edit", "article", "world", "articles", "history", "free", "contact", "bearbeiten",
                        "changes", "pages", "news", "january", "february", "march", "april", "wurden",
                        "may", "june", "july", "august", "september", "october", "november", "original",
                        "december", "org", "categories", "sources", "st", "one", "page", "new", "archived",
                        "create", "main", "encyclopedia", "navigation", "title", "references", "edu", "index"));
    	stopwords.addAll(Arrays.asList(":", " ", "``", "''", ",", "'", "'s", "-LRB-", "-RRB-", ".", "-", "--", "i", "a", "about", "an", "and", "are", "as", "at", "be", "but", "by", "com", "for", "from", 
		        "how", "in", "is", "it", "of", "on", "or", "that", "the", "The", "was", "were", "him", "his", "her", "she", "into", "they",
				"this", "to", "what", "when", "where", "who", "will", "with", "the", "www", "before", ",", "after", ";", "like", "and", "such", "-LRB-", "-RRB-", "-lrb-", "-rrb-", "aber", "als",
				"am", "an", "auch", "auf", "aus", "bei", "bin", "bis", "bist", "da", "dadurch", "daher", "darum", "das", "daß", "dass", "dein", "deine", "dem", "den", "der", "des", "dessen",
				"deshalb", "die", "dies", "dieser", "dieses", "doch", "dort", "du", "durch", "ein", "eine", "einem", "einen", "einer", "eines", "er", "es", "euer", "eure", "für", "hatte", "hatten",
				"hattest", "hattet", "hier", "hinter", "ich", "ihr", "ihre", "im", "in", "ist", "ja", "jede", "jedem", "jeden", "jeder", "jedes", "jener", "jenes", "jetzt", "kann", "kannst",
				"können", "könnt", "machen", "mein", "meine", "mit", "muß", "mußt", "musst", "müssen", "müßt", "nach", "nachdem", "nein", "nicht", "nun", "oder", "seid", "sein", "seine", "sich",
				"sie", "sind", "soll", "sollen", "sollst", "sollt", "sonst", "soweit", "sowie", "und", "unser unsere", "unter", "vom", "von", "vor", "wann", "warum", "weiter", "weitere", "wenn",
				"wer", "werde", "werden", "werdet", "weshalb", "wie", "wieder", "wieso", "wir", "wird", "wirst", "wo", "woher", "wohin", "zu", "zum", "zur", "über", "wurde", "est", "svg"));
    	stopwords.addAll(Arrays.asList("alors", "au", "aucuns", "aussi", "autre", "avant", "avec", "avoir", "bon", "car", "ce", "cela", "ces", "ceux", "chaque", "ci", "comme", "comment", "dans", 
										"des", "du", "dedans", "dehors", "depuis", "deux", "devrait", "doit", "donc", "dos", "droite", "début", "elle", "elles", "en", "encore", "essai", "est", 
										"et", "eu", "fait", "faites", "fois", "font", "force", "haut", "hors", "ici", "il", "ils", "je  juste", "la", "le", "les", "leur", "là", "ma", "maintenant", 
										"mais", "mes", "mine", "moins", "mon", "mot", "même", "ni", "nommés", "notre", "nous", "nouveaux", "ou", "où", "par", "parce", "parole", "pas", "personnes", 
										"peut", "peu", "pièce", "plupart", "pour", "pourquoi", "quand", "que", "quel", "quelle", "quelles", "quels", "qui", "sa", "sans", "ses", "seulement", "si", 
										"sien", "son", "sont", "sous", "soyez   sujet", "sur", "ta", "tandis", "tellement", "tels", "tes", "ton", "tous", "tout", "trop", "très", "tu", "valeur", 
										"voie", "voient", "vont", "votre", "vous", "vu", "ça", "étaient", "état", "étions", "été", "être"));
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

        // crawl the text from wikipedia, remove the html markup and count the words
        List<Word> words = getKeywordsSortedByFrequency(
        		this.crawlUtil.readPage(url, Defacto.DEFACTO_CONFIG.getIntegerSetting("crawl", "WEB_SEARCH_TIMEOUT_MILLISECONDS")));
                
        // and return only max values
        return words.size() >= topicTermsMaxSize ? words.subList(0, topicTermsMaxSize) : words;
    }
    
    public ArrayList<Word> getKeywordsSortedByFrequency(String inputWords){

    	ArrayList<String> tokens = new ArrayList<String>(1000);
    	
        try{
        	
        	Analyzer analyzer	= new SimpleAnalyzer(Version.LUCENE_41);
        	Reader reader		= new StringReader(inputWords);
            TokenStream stream  = analyzer.tokenStream("", reader);
            stream.reset();
            
            while (stream.incrementToken()) {
                
                // we need to filter these stop words, mostly references in wikipedia
                String token = stream.getAttribute(CharTermAttribute.class).toString();
                if ( token.length() > 2 && !stopwords.contains(token) ) tokens.add(token.trim());
            }
            
            reader.close();
            analyzer.close();
        }
        catch (IOException exp){
            
        	exp.printStackTrace();
            logger.error("Cannot get a token from the stream, due to " + exp.getMessage());
        }
        
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
