package org.aksw.results;

import org.aksw.provenance.topic.frequency.Word;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 3/25/12
 * Time: 7:27 PM
 * Sets the values of the feature vector of a SearchResult object
 * It finds the occurrences of topic terms in the body of the search result
 * it's implemented as a thread in order to calculate the occurrences of topic terms in several search results in parallel
 */
public class TopicTermsFinder extends Thread{

    private static Logger logger = Logger.getLogger(TopicTermsFinder.class.getName());

    private SearchResult searchResult = null;
    ArrayList<Word> topicTerms = null;

    public TopicTermsFinder(SearchResult result, ArrayList<Word> topicTermList){

        searchResult = result;
        topicTerms = topicTermList;
        start();
    }

    @Override
    public void start() {
        super.start();
        
        int [] featureVector = new int[topicTerms.size()];

        //Find the number of occurrences of each topic term in the body of the page
        int i = 0;
        for(Word topicTerm : topicTerms ){
            featureVector[i++] = StringUtils.countMatches(searchResult.getPageContent(), topicTerm.getWord());
        }

        searchResult.setFeatureVector(featureVector);
    }
}
