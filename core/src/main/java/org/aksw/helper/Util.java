package org.aksw.helper;

import com.boilerpipe.BoilerPipeCaller;

import org.aksw.boa.BoaSearchResult;
import org.aksw.provenance.topic.frequency.Word;
import org.aksw.provenance.topic.frequency.WordFrequencyCounter;
import org.aksw.provenance.wikipedia.WikipediaSearcher;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 3/12/12
 * Time: 3:00 PM
 * Holds some beneficial utilities used through the entire application
 */

public class Util {

    private static Logger logger = Logger.getLogger(Util.class.getName());

    /**
     * Returns the HTML code associated with the specified URL
     * @param url   URL of the webpage for which the HTML should be returned
     * @return  The HTML code of the webpage
     */
    public static String readHTMLofURL(String url){

        StringBuilder outputHTML = new StringBuilder();
        try{

            URL webpageURL = new URL(url);
            URLConnection con = webpageURL.openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null){
                outputHTML.append(inputLine);
                outputHTML.append("\n");
            }
            in.close();
        }
        catch (MalformedURLException exp){
            logger.error("Invalid URL is passed");
        }
        catch (IOException exp){
            logger.error("Cannot read from the specified URL: "+url+", due to " + exp.getMessage());
        }
        finally {
            return outputHTML.toString();
        }

    }

    /**
     * Gets the title of the webpage with the passed URL
     * @param url   The URL of the page
     * @return  The title of the webpage
     */
    public static String getPageTitle(String url){
        try{

            URL pageURL = new URL(url);
            URLConnection con = pageURL.openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);


            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            Pattern pHead = Pattern.compile("(?i)</HEAD>");
            Matcher mHead;
            Pattern pTitle = Pattern.compile("(?i)</TITLE>");
            Matcher mTitle;

            String inputLine;
            boolean found=false;
            boolean notFound=false;
            String html = "";
            String title = "";
            while (!(((inputLine = in.readLine()) == null) || found || notFound)){
                html=html+inputLine;
                mHead=pHead.matcher(inputLine);
                if(mHead.find()){
                    notFound=true;
                }
                else{
                    mTitle=pTitle.matcher(inputLine);
                    if(mTitle.find()){
                        found=true;
                    }
                }
            }
            in.close();

            html = html.replaceAll("\\s+", " ");
            if(found){
                Pattern p = Pattern.compile("(?i)<TITLE.*?>(.*?)</TITLE>");
                Matcher m = p.matcher(html);
                while (m.find()) {
                    title=m.group(1);
                }
            }
            return title;
        }
        catch (Exception exp){
            return url;
        }

    }

    /**
     * Returns a list of potential topic terms from Wikipedia
     * @param queryTerms    Query terms extracted from the triple
     * @return A list of potential topic terms extracted from Wikipedia, an empty list is returned if none exists
     */
    public static ArrayList<Word> getPotentialTopicTermsFromWikipedia(SearchQueryTerms queryTerms) {

        ArrayList<Word> potentialTopicTerms = getPotentialTopicTerms(queryTerms);

        //Now we have a list of potential topic terms, sorted by word, and each one with its frequency in the article
        //We should calculate the running sum of frequencies and then select the top 10 out of them
        HashMap<String, Integer> hmTopicTerms  =  new HashMap<String, Integer>();

        for(Word word: potentialTopicTerms){
            //If the word is not in the HashMap, then we should add it
            if(!hmTopicTerms.containsKey(word.getWord()))
                hmTopicTerms.put(word.getWord(), word.getFrequency());
            else{
                //It is already there, then we should just add the frequencies up
                int oldFrequency = hmTopicTerms.get(word.getWord());
                hmTopicTerms.put(word.getWord(), oldFrequency + word.getFrequency());
            }

        }

        //ArrayList topicTerms will contain the final topic terms that should be returned
        ArrayList<Word> topicTerms = new ArrayList<Word>(hmTopicTerms.size());

        //If the word is repeated only once, then we should remove it from the final list of terms
        for(Map.Entry<String, Integer> wordWithRepetitions : hmTopicTerms.entrySet()){
            topicTerms.add(new Word(wordWithRepetitions.getKey(), wordWithRepetitions.getValue(), true));
        }

        //Now we sort the topic terms according to their running total frequencies, in order to get the top 10 out of it
        Collections.sort(topicTerms);

        for(Word word: topicTerms){
            logger.info(word);
        }
        return topicTerms;
    }
    
    public static void main(String[] args) {

//        for (WikipediaSearchResult res : WikipediaSearcher.queryWikipedia("Barack Obama Michelle Obama")) {
//            
//            System.out.println(res.getPageTitle());
//            System.out.println(res.getPageURL());
//            System.out.println(res.getSearchSnippet());
//        }
        SearchQueryTerms s = new SearchQueryTerms("barack obama", "michelle obama", new ArrayList<BoaSearchResult>());
        System.out.println(getPotentialTopicTerms(s));
    }

    /**
     * Returns a list of potential topic terms, i.e. the important terms that appear in the pages
     * @param queryTerms    The terms of the query
     * @return  A list of terms that can probably be topic terms
     */
    private static ArrayList<Word> getPotentialTopicTerms(SearchQueryTerms queryTerms) {
        ArrayList<Word> potentialTopicTerms = new ArrayList<Word>();

        //Here we can use the subject and object only to search Wikipedia, this will help in 2 ways:
        //1- It's faster as we should not iterate through all NL representations of the predicate
        //2- some search queries return no result when the NL of the predicate is used

//        for(BoaSearchResult rep : queryTerms.getBoaPredicate_Representations())
        {
//            String str = queryTerms.getSubjectLabel() + " " +
//                    rep.getNL_Represntation() + " " + queryTerms.getObjectLabel();

            String strQueryForWikipedia = queryTerms.getSubjectLabel() + " " + queryTerms.getObjectLabel();

            ArrayList<WikipediaSearchResult> wikipediaResults = WikipediaSearcher.queryWikipedia(strQueryForWikipedia);

            //Make sure that search query returns results from Wikipedia
            if(wikipediaResults.size() <=0 )
                return potentialTopicTerms;


            for(WikipediaSearchResult result: wikipediaResults){
                ArrayList<Word> termsSortedByFrequency = getMostFrequentWordInWikipediaArticle(result.getPageURL());

                for(int i = 0; i < 10 && i < termsSortedByFrequency.size(); i++){
                    potentialTopicTerms.add(termsSortedByFrequency.get(i));
                }
            }


        }

        //Now ArrayList potentialTopicTerms contains all terms with high frequency in Wikipedia

        //Now we need sort them with the term, so that the repeated words are placed next to each other
        Collections.sort(potentialTopicTerms, new WordComparator());


        //The term that appears more than once is very likely to be a potential topic, so we should remove the other terms
        //which are not repeated
        //That HashMap will contain each word along with its repetitions
        HashMap<String, Integer> hmWordsWithRepetitionsInWikipediaArticles = new HashMap<String, Integer>();

        for(Word word: potentialTopicTerms){

            //If the word is not in the HashMap, then we should add it and initialize its number of repetitions to 1
            if(!hmWordsWithRepetitionsInWikipediaArticles.containsKey(word.getWord()))
                hmWordsWithRepetitionsInWikipediaArticles.put(word.getWord(), 1);
            else{
                //It is already there, then we should just increase its repetitions
                int repetitions = hmWordsWithRepetitionsInWikipediaArticles.get(word.getWord());
                hmWordsWithRepetitionsInWikipediaArticles.put(word.getWord(), repetitions+1);
            }

        }

        //If the word is repeated only once, then we should remove it from the final list of terms
        for(Map.Entry<String, Integer> wordWithRepetitions : hmWordsWithRepetitionsInWikipediaArticles.entrySet()){
            if(wordWithRepetitions.getValue() <= 1 ){

                //We can use BinarySearch to look for the word in the list as it's already sorted
                //potentialTopicTerms.remove(new Word(wordWithRepetitions.getKey(),1));
                int position = Collections.binarySearch(potentialTopicTerms, new Word(wordWithRepetitions.getKey(),wordWithRepetitions.getValue()),
                        new WordComparatorForBinarySearch());
                potentialTopicTerms.remove(position);
            }
        }
        return potentialTopicTerms;
    }
    

    /**
     * Returns a list of the highly frequent words in a Wikipedia article
     * @param requiredURL   The URL for which the frequent word should be returned
     * @return  A list of the most frequent words
     */
    private static ArrayList<Word> getMostFrequentWordInWikipediaArticle(String requiredURL) {
//        String outputHTML = BoilerPipeCaller.getCleanHTMLPage(wikipediaResults.get(0).getPageURL());
//        String outputHTML = BoilerPipeCaller.getCleanHTMLPage(requiredURL);
        //In case that the HTML page is so long, BoilerPipe, will return an error and an empty HTML text,
        //and so we should get the HTML as is as a fallback
//        if(outputHTML.compareTo("") == 0)
            String outputHTML = Util.readHTMLofURL(requiredURL);

        String outputText = Jsoup.parse(outputHTML).text();

        return WordFrequencyCounter.getKeywordsSortedByFrequency(outputText);
    }


    private static class WordComparator implements Comparator<Word> {
        public int compare(Word firstWord, Word secondWord){
            int comparisonResult = 0;

            //Compare the words as strings
            comparisonResult = firstWord.getWord().compareTo(secondWord.getWord());

            //if the words are the same, then sort them with the frequencies
            if(comparisonResult == 0){
                comparisonResult = (firstWord.getFrequency() > secondWord.getFrequency() ? 1 :
                        (firstWord.getFrequency() == secondWord.getFrequency() ? 0 : -1));
            }

            return comparisonResult;
        }
    }


    private static class WordComparatorForBinarySearch implements Comparator<Word> {
        public int compare(Word firstWord, Word secondWord){

            //Compare the words as strings
            return firstWord.getWord().compareTo(secondWord.getWord());
        }
    }

    
}
