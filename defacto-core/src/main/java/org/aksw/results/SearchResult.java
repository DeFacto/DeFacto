package org.aksw.results;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 2/21/12
 * Time: 6:06 PM
 * This class represents single search result 
 */
public class SearchResult implements Comparable<SearchResult>{
    
    private int rank;
    private String url;
    private String title;
    private String pageContent;
    
    private int[] featureVector;

    public SearchResult(String url, int rank, String title, String pageContent) {
        this.rank = rank;
        this.url = url;

        //We convert both title and content to lower case, in order to simplify the search in strings, i.e. using String.contains
        this.title = title.toLowerCase();
        this.pageContent = pageContent.toLowerCase();
    }

    //Contains the
    public static ArrayList<String> keywords = new ArrayList<String>();

    public SearchResult(String url, int rank, String title) {
        //We convert both title and content to lower case, in order to simplify the search in strings, i.e. using String.contains
        this(url, rank, title.toLowerCase(), "");
    }

    public SearchResult(String url, int rank) {
        this(url, rank, "");
    }

    public int getRank() {
        return rank;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getPageContent() {
        return pageContent;
    }

    public int[] getFeatureVector() {
        return featureVector;
    }

    public void setFeatureVector(int[] featureVector) {
        this.featureVector = featureVector;
    }

    public int compareTo(SearchResult searchResult) {

        //Note that we reverse the sign of the output, as we want the results to be sorted descendingly

        if(this.rank == searchResult.getRank())
            return this.url.compareTo(searchResult.getUrl());
        else
            return (this.rank > searchResult.getRank() ? -1 : 1);
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "rank=" + rank +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
//                ", pageContent='" + pageContent + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchResult that = (SearchResult) o;

        if (url != null ? !url.equals(that.url) : that.url != null) return false;

        return true;
    }

}
