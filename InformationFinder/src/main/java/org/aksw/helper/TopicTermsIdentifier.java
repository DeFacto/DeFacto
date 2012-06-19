package org.aksw.helper;

import org.aksw.provenance.topic.frequency.Word;
import org.aksw.results.SearchResult;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 3/21/12
 * Time: 4:15 PM
 * This class determines whether the given terms are topic terms or not
 */
public class TopicTermsIdentifier {

    /**
     * Determines whether potentialTopicTerm is a TopicTerm according to the results in list searchResults, which are
     * fetched from a search engine
     * The calculation is mainly based on the paper titled "Trustworthiness Analysis of Web Search Results".
     * @param searchResults A list of search results fetched from the search engine
     * @param queryTerms    An object containing the terms used for searching using the search engine
     * @param potentialTopicTerm    A term that can probably be a TopicTerm (mostly fetched from Wikipedia)
     * @return  Whether it is a topic term or not
     */
    public static boolean isTopicTerm(ArrayList<SearchResult> searchResults, SearchQueryTerms queryTerms,
                                      Word potentialTopicTerm){
        int DF_q = searchResults.size(); //Number of results returned by the search engine for a query (it is formulated
        // by concatenating Subject and object labels)
        int DF_q_t = 0;//Number of results returned by the search engine in which the potential Topic Term also appears
        int DF_intitle_q = 0;//the number of pages that contain query terms in title
        int DF_intitle_q_t = 0;//the number of pages that contain query terms in title, and also contain the potential topic term in body

        boolean foundInBody;//Indicates whether the potential topic term is found in the body or not
        for(SearchResult res: searchResults){


//            //If the potential topic term is already fetched from Wikipedia, and the search result is also from Wikipedia
//            //then we should count that occurrence of that term, as it affects the overall probability, because it will
//            //definitely appear in the article
//            if((res.getTitle().contains("wikipedia")) && (potentialTopicTerm.isFromWikipedia()))
//                continue;


            foundInBody = false;

            //If the term appears in the webpage body, then DF_q_t should be incremented
            if(res.getPageContent().contains(potentialTopicTerm.getWord().toLowerCase())){
                DF_q_t ++;
                foundInBody = true;
            }

            //If the subject label appears in the title of the page, the we should increment DF_intitle_q
            if(res.getTitle().contains(queryTerms.getSubjectLabel().toLowerCase())){

                DF_intitle_q ++;
                if(foundInBody)//If the potential topic term was also found in body, then we should also increment DF_intitle_q_t
                    DF_intitle_q_t++;
            }
        }

        float probability_t_q = 0, probability_t_intitle_q = 0;
        if(DF_q > 0) //make sure that denominator is above 0
            probability_t_q = (float) DF_q_t / DF_q;

        if(DF_intitle_q > 0) //make sure that denominator is above 0
            probability_t_intitle_q = (float) DF_intitle_q_t / DF_intitle_q;


        return probability_t_intitle_q >= probability_t_q;
    }

}
