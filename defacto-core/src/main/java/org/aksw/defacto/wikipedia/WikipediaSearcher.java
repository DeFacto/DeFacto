package org.aksw.defacto.wikipedia;

import info.bliki.api.Connector;
import info.bliki.api.SearchResult;
import info.bliki.api.User;
import info.bliki.api.XMLSearchParser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.aksw.defacto.Defacto;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;


/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 2/27/12
 * Time: 8:50 PM
 * Searches Wikipedia for certain search query
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Mohamed Morsey <morsey@informatik.uni-leipzig.de>
 */
public class WikipediaSearcher {

    private static Logger logger =  Logger.getLogger(WikipediaSearcher.class);

    /**
     * Searches Wikipedia for the passed
     * @param searchQuery   The query to search Wikipedia with
     * @return A list of results obtained from Wikipedia
     */
    public static ArrayList<WikipediaSearchResult> queryWikipedia(String searchQuery, String language) {

        ArrayList<WikipediaSearchResult> searchResults = new ArrayList<WikipediaSearchResult>();
        User user = new User("", "", "http://"+language+".wikipedia.org/w/api.php");
        user.login();

        String[] queryParams = { "list", "search", "srsearch", searchQuery, "sroffset", "0", 
        		"srlimit", Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "MAX_WIKIPEDIA_RESULTS") };

        Connector connector = new Connector();
        XMLSearchParser parser;
        
        try {

            logger.debug("Querying wikipedia for topic terms: \"" + searchQuery + "\" ("+language+")");
            
            String responseBody = connector.queryXML(user, queryParams);
            while (responseBody != null) {
                
                parser = new XMLSearchParser(responseBody);
                parser.parse();

                for (SearchResult searchResult : parser.getSearchResultList())
                    searchResults.add(new WikipediaSearchResult(
                                           searchResult.getTitle(), 
                                           getWikipediaPageFullURL(searchResult.getTitle(), language),
                                           searchResult.getSnippet()));
                
                // there are more results available change the offset 
                if (parser.getSrOffset().length() > 0) {
                    
                    queryParams[5] = parser.getSrOffset();
                    responseBody = connector.queryXML(user, queryParams);
                    
                    if ( Integer.valueOf(parser.getSrOffset()) >= Defacto.DEFACTO_CONFIG.getIntegerSetting("evidence", "MAX_WIKIPEDIA_RESULTS")) 
                        break;
                }
                else break;
            }
        }
        catch (SAXException e) {
            
            e.printStackTrace();
        }
        catch (IOException e) {
            
            e.printStackTrace();
        }
        
        logger.debug("Results: " + searchResults.size() + " for:  " + searchQuery + " ("+language+")");
        
        return searchResults;
    }
    
    /**
     * 
     * @param pageTitle
     * @return
     */
    private static String getWikipediaPageFullURL(String pageTitle, String language){

        try{
            
            return "http://"+language+".wikipedia.org/wiki/" + URLEncoder.encode(pageTitle.replace(" ", "_"), "UTF-8");
        }
        catch (UnsupportedEncodingException exp){
            
            throw new RuntimeException("Wikipedia page: " + pageTitle +" cannot be encoded due to " + exp.getMessage());
        }
    }
}
