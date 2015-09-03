package org.aksw.defacto.wikipedia;

import java.util.ArrayList;

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
    private String pageID;
    private ArrayList<String> externalLinks;

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

    public WikipediaSearchResult(){

    }

    public ArrayList<String> getExternalLinksfromDBPedia(){
        return externalLinks;
    }

    public void setPageID(String id){
        this.pageID = id;
    }

    public void setPageTitle(String title){
        this.pageTitle = title;
    }

    public void addExternalLink(String link){
        this.externalLinks.add(link);
    }

    public String getPageID(){
        return pageID;
    }

    public String getPageTitle() {
        return pageTitle;
    }


    public String getPageURL() {
        return pageURL;
    }

    public String getSearchSnippet() {
        return searchSnippet;
    }


}
