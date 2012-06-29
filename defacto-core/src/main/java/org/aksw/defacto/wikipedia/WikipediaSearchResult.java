package org.aksw.defacto.wikipedia;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 3/11/12
 * Time: 6:07 PM
 * Holds a search result obtained from Wikipedia
 */

public class WikipediaSearchResult {
    
    private String pageTitle;
    private String pageURL;
    private String searchSnippet;

    /**
     * 
     * @param pageTitle
     * @param pageURL
     * @param searchSnippet
     */
    public WikipediaSearchResult(String pageTitle, String pageURL, String searchSnippet) {
        this.pageTitle = pageTitle;
        this.pageURL = pageURL;
        this.searchSnippet = searchSnippet;
    }

    /**
     * 
     * @return
     */
    public String getPageTitle() {
        return pageTitle;
    }

    /**
     * 
     * @return
     */
    public String getPageURL() {
        return pageURL;
    }

    /**
     * 
     * @return
     */
    public String getSearchSnippet() {
        return searchSnippet;
    }
}
