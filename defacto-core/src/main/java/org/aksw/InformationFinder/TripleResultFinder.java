package org.aksw.InformationFinder;

import org.aksw.boa.BoaSearchResult;
import org.aksw.boa.BoaSearcher;
import org.aksw.helper.*;
import org.aksw.helper.Vector;
import org.aksw.provenance.search.engine.bing.BingSearchEngine;
import org.aksw.provenance.topic.frequency.Word;
import org.aksw.results.SearchResult;
import org.aksw.results.TopicTermsFinder;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 2/19/12
 * Time: 4:29 PM
 * Search the web for all pages supporting a triple
 */
public class TripleResultFinder {

    //Default values for Boa threshold, PageRank, and number of search results
    public static final double BOA_CONFIDENCE_THRESHOLD = 0.5;
    public static final int MINIMUM_PAGERANK = 4;
    public static final int MAXIMUM_NUMBER_RESULTS_PER_SEARCH = -1;

    private static Logger logger =  Logger.getLogger(TripleResultFinder.class);

    private static String subjectLabel, objectLabel;
    private static ArrayList<BoaSearchResult> BoaPredicate_Representations;

    //This matrix will hold the similarity values among search results
    private static double [][]similarityMatrix = null;

    //This HashMap will contain each URL along with its search result
    public static ConcurrentHashMap<String, SearchResult> hmSearchResults = new ConcurrentHashMap<String, SearchResult>();

    /**
     * Constructs a list of keywords that were used during the search process
     * @return  A full list of keywords used for search, which can also be used to highlight keywords.
     */
    public static ArrayList<String>getListOfKeywords(){

        ArrayList<String> arrKeywords = new ArrayList<String>();

        if(BoaPredicate_Representations == null)
            return arrKeywords;
        

        
        arrKeywords.add(subjectLabel);
        arrKeywords.add(objectLabel);
        
        for(BoaSearchResult keyword:BoaPredicate_Representations){
            arrKeywords.add(keyword.getNL_Represntation().replace(",", ""));
        }

        return arrKeywords;
    }

    /** Returns a complete list of websites for the given search triple.
     * It also uses the BOA index to get all NL representations of the predicate of the passed triple.
     *
     * @param searchTriple  The triple for which the list of search results should be fetched.
     * @return  A list of search results.
     */
    public static ArrayList<SearchResult> getCompleteSearchResults(String searchTriple){
        return getCompleteSearchResults(searchTriple, BOA_CONFIDENCE_THRESHOLD, MAXIMUM_NUMBER_RESULTS_PER_SEARCH,
                MINIMUM_PAGERANK);
    }


    /** Returns a complete list of websites for the given search triple.
     * It also uses the BOA index to get all NL representations of the predicate of the passed triple.
     *
     * @param searchTriple  The triple for which the list of search results should be fetched.
     * @param maximumNumberOfResultsPerSearchQuery  Single triple can result in several search queries with varying number
     *                                              number of results, for each result the PageRank should be fetched.
     *                                              This number limits the number of results for which the PageRank should
     *                                              be checked.
     * @return  A list of search results.
     */
    private static ArrayList<SearchResult> getCompleteSearchResults(String searchTriple, int maximumNumberOfResultsPerSearchQuery){

        return getCompleteSearchResults(searchTriple, BOA_CONFIDENCE_THRESHOLD, maximumNumberOfResultsPerSearchQuery, MINIMUM_PAGERANK);

        /*ArrayList<SearchResult> completeSearchResults = null;

        HashMap<String, SearchResult> hmSearchResults = new HashMap<String, SearchResult>();


        String subject, predicate;

        //Split the required triple into its parts
//        String [] tripleParts = KeywordExtractor.getTripleParts(searchTriple);

        try{
            //Extract the subject and the predicate out of the search predicate
            subject = KeywordExtractor.getTripleSubject(searchTriple);
            predicate = KeywordExtractor.getTriplePredicate(searchTriple);

            //Get the object of the triple
            TripleObject object = KeywordExtractor.getTripleObject(searchTriple);

            //Get the labels of the parts from the endpoint
            subjectLabel = DBpediaKnowledgeBaseQueryExecutor.getLabelForURI(subject);
//        predicateLabel = DBpediaKnowledgeBaseQueryExecutor.getLabelForURI(tripleParts[1]);

            BoaPredicate_Representations = BoaSearcher.searchBOA(predicate, 0.5D, 1100000);

            if(object.isURI())
                objectLabel = DBpediaKnowledgeBaseQueryExecutor.getLabelForURI(object.getObject());
            else
                objectLabel = object.getObject();

//        String[] searchResultURLsList = BingSearchEngine.getSearchResultsForQuery(new String[]{subjectLabel, predicateLabel, objectLabel});

//        String[] searchResultURLsList = BingSearchEngine.getSearchResultsForQuery(new String[]{subjectLabel, predicateLabel, objectLabel});

//            completeSearchResults = new ArrayList<SearchResult>();

            //Iterate through the returned list of representations and search Bing with each on in combination with
            // the subject and the object
            for(BoaSearchResult Boa_Predicate:BoaPredicate_Representations){
                ArrayList<String> results = BingSearchEngine.getSearchResultsForQuery(new String[]{subjectLabel,
                        Boa_Predicate.getNL_Represntation(), objectLabel});

                //Iterate over the resulting list, construct a SearchResult object and add it to the final output list
//                for(String resultURL:results){
//                    completeSearchResults.add(new SearchResult(resultURL, PageRank.GetPageRank(resultURL), ""));
//                }
                
                int maxResults = results.size();
                
                if((maximumNumberOfResultsPerSearchQuery != -1) && (maximumNumberOfResultsPerSearchQuery < maxResults))
                    maxResults = maximumNumberOfResultsPerSearchQuery;
                
                for(int i = 0; i < maxResults; i++){
                    String resultURL = results.get(i);
//                    completeSearchResults.add(new SearchResult(resultURL, PageRank.GetPageRank(resultURL), ""));

                    //Check if the URL already exists, in order to minimize the number of requests of PageRank
                    if(!hmSearchResults.containsKey(resultURL))
                        hmSearchResults.put(resultURL, new SearchResult(resultURL, PageRank.GetPageRank(resultURL)));
                }

//                return completeSearchResults;

            }

        }
        catch (Exception exp){
            logger.error("Unable to contact Bing search engine to get the results due to " + exp.getMessage());
        }


//        ArrayList<SearchResult> test = new ArrayList<SearchResult>(completeSearchResults);
//        test.addAll(completeSearchResults);
//
//        removeDuplicates(test);
//        removeDuplicates(completeSearchResults);
        completeSearchResults = new ArrayList<SearchResult>(hmSearchResults.values());

        Collections.sort(completeSearchResults);

        //Remove all search results whose Rank are below 4
        Iterator<SearchResult> iter = completeSearchResults.iterator();
        while (iter.hasNext()){
            if(iter.next().getRank() < 4)
                iter.remove();
        }

        return completeSearchResults; */

    }

    /** Returns a complete list of websites for the given search triple.
     * It also uses the BOA index to get all NL representations of the predicate of the passed triple.
     *
     * @param searchTriple  The triple for which the list of search results should be fetched.
     * @param boaConfidenceThreshold    The threshold required for BOA index searching.
     * @param maximumNumberOfResultsPerSearchQuery  Single triple can result in several search queries with varying number
     *                                              number of results, for each result the PageRank should be fetched.
     *                                              This number limits the number of results for which the PageRank should
     *                                              be checked.
     * @param minimumPageRank   The minimum PageRank, so the pages whose PageRank are higher than that rank are the only
     *                          ones returned.
     * @return  A list of search results.
     */
    public static ArrayList<SearchResult> getCompleteSearchResults(String searchTriple, double boaConfidenceThreshold,
                           int maximumNumberOfResultsPerSearchQuery, int minimumPageRank){

        //Validate input values, and in case any invalid value is passed the defaults are used
        if((boaConfidenceThreshold < 0) || (boaConfidenceThreshold > 1.0))
            boaConfidenceThreshold = BOA_CONFIDENCE_THRESHOLD;

        if((maximumNumberOfResultsPerSearchQuery < -1) || (maximumNumberOfResultsPerSearchQuery > 500))
            maximumNumberOfResultsPerSearchQuery = MAXIMUM_NUMBER_RESULTS_PER_SEARCH;

        if((minimumPageRank < 0) || (minimumPageRank > 10))
            minimumPageRank = MINIMUM_PAGERANK;

        ArrayList<SearchResult> completeSearchResults = null;
        ArrayList<SearchResult> finalSearchResults = null;

        //Initialize the HashMap containing the results
        hmSearchResults = new ConcurrentHashMap<String, SearchResult>();

        ArrayList<Word> potentialTopicTerms = new ArrayList<Word>();
        String subject, predicate;
        SearchQueryTerms queryTerms = null;
        //Split the required triple into its parts
//        String [] tripleParts = KeywordExtractor.getTripleParts(searchTriple);

        try{
            /*//Extract the subject and the predicate out of the search predicate
            subject = KeywordExtractor.getTripleSubject(searchTriple);
            predicate = KeywordExtractor.getTriplePredicate(searchTriple);

            //Get the object of the triple
            TripleObject object = KeywordExtractor.getTripleObject(searchTriple);

            //Get the labels of the parts from the endpoint
            subjectLabel = DBpediaKnowledgeBaseQueryExecutor.getLabelForURI(subject);

            BoaPredicate_Representations = BoaSearcher.searchBOA(predicate, boaConfidenceThreshold, 1100000);

            if(object.isURI())
                objectLabel = DBpediaKnowledgeBaseQueryExecutor.getLabelForURI(object.getObject());
            else
                objectLabel = object.getObject();

//        String[] searchResultURLsList = BingSearchEngine.getSearchResultsForQuery(new String[]{subjectLabel, predicateLabel, objectLabel});

//        String[] searchResultURLsList = BingSearchEngine.getSearchResultsForQuery(new String[]{subjectLabel, predicateLabel, objectLabel});

//            completeSearchResults = new ArrayList<SearchResult>();
              */

            queryTerms = getVariousSearchQueryTerms(searchTriple, boaConfidenceThreshold);

            potentialTopicTerms = Util.getPotentialTopicTermsFromWikipedia(queryTerms);

            //Iterate through the returned list of representations and search Bing with each on in combination with
            // the subject and the object
            for(BoaSearchResult Boa_Predicate:queryTerms.getBoaPredicate_Representations()){
                ArrayList<String> results = new ArrayList<String>(); 
//                        BingSearchEngine.getSearchResultsForQuery(new String[]{queryTerms.getSubjectLabel(),
//                        Boa_Predicate.getNL_Represntation(), queryTerms.getObjectLabel()});

                //Iterate over the resulting list, construct a SearchResult object and add it to the final output list
//                for(String resultURL:results){
//                    completeSearchResults.add(new SearchResult(resultURL, PageRank.GetPageRank(resultURL), ""));
//                }

                int maxResults = results.size();

                if((maximumNumberOfResultsPerSearchQuery != -1) && (maximumNumberOfResultsPerSearchQuery < maxResults))
                    maxResults = maximumNumberOfResultsPerSearchQuery;
                /*

                for(int i = 0; i < maxResults; i++){
                    String resultURL = results.get(i);
//                    completeSearchResults.add(new SearchResult(resultURL, PageRank.GetPageRank(resultURL), ""));

                    //Check if the URL already exists, in order to minimize the number of requests of PageRank
                    if(!hmSearchResults.containsKey(resultURL))
                        //Get PageRank on the fly and attach it to the page
//                        hmSearchResults.put(resultURL, new SearchResult(resultURL, PageRank.GetPageRank(resultURL)));
                        hmSearchResults.put(resultURL, new SearchResult(resultURL, 6, Util.getPageTitle(resultURL),
                                Util.readHTMLofURL(resultURL)));
                }
                */
                getPageContents(results, maxResults);


                for (Map.Entry<String, SearchResult> hmSearchResultEntry : hmSearchResults.entrySet()) {
                    logger.info("KEY = " + hmSearchResultEntry.getKey());
                    logger.info("VALUE = " + hmSearchResultEntry.getValue());
                    // ...
                }


//                return completeSearchResults;

            }

        }
        catch (Exception exp){
            logger.error("Unable to contact Bing search engine to get the results due to " + exp.getMessage());
        }


        completeSearchResults = new ArrayList<SearchResult>(hmSearchResults.values());

        Collections.sort(completeSearchResults);

        //Remove all search results whose Rank are below 4
        Iterator<SearchResult> iter = completeSearchResults.iterator();
        while (iter.hasNext()){
            if(iter.next().getRank() < minimumPageRank)
                iter.remove();
        }

        ArrayList<Word> finalTopicTermList = new ArrayList<Word>();

        for(Word potentialTopicTerm : potentialTopicTerms){

            boolean isTopicTerm = TopicTermsIdentifier.isTopicTerm(completeSearchResults, queryTerms, potentialTopicTerm);
            if(isTopicTerm)
                finalTopicTermList.add(potentialTopicTerm);
        }

        buildFeatureVectorForSearchResults(completeSearchResults, finalTopicTermList);

        constructSimilarityMatrix(completeSearchResults);

        ArrayList<SearchResultSimilarity> sortedSimilarities = new ArrayList<SearchResultSimilarity>(completeSearchResults.size());

        double theta = 0.8;
        for(int i = 0; i < similarityMatrix.length; i++){

            int countOfValuesAboveTheta = 0;
            for(int j = 0; j < similarityMatrix[i].length; j++){

                if(similarityMatrix[i][j] > theta)
                    countOfValuesAboveTheta ++ ;

            }
            System.out.print("Count of Values for result # " + i + " = " + countOfValuesAboveTheta + "\n");
            sortedSimilarities.add( new SearchResultSimilarity(i, countOfValuesAboveTheta));
        }

        Collections.sort(sortedSimilarities);

        //finalSearchResults will contain the search results after discarding the ones with low similarities
        finalSearchResults = new ArrayList<SearchResult>(completeSearchResults.size()/2);
        for(int i = 0; i <  completeSearchResults.size()/2; i++){
            finalSearchResults.add(completeSearchResults.get(sortedSimilarities.get(i).searchResultIndex));
            logger.info(calculateTopicCoverage(finalSearchResults.get(i)));
        }

        return finalSearchResults;

    }

    private static void getPageContents(ArrayList<String> results, int maxResults) throws InterruptedException {
        //This HashMap will contain each URL and an object of type WebpageContentReader, which is a thread that enables
        //getting the contents of Webpages in parallel
        /*HashMap<String, WebpageContentReader> hmSearchResultPageContents = new HashMap<String, WebpageContentReader>();

        for(int i = 0; i < maxResults; i++){
            String resultURL = results.get(i);


            //Check if the URL already exists, in order to minimize the number of requests of PageRank
            if(!hmSearchResultPageContents.containsKey(resultURL))
                //Get PageRank on the fly and attach it to the page
                hmSearchResultPageContents.put(resultURL, new WebpageContentReader(resultURL));
        }

        Thread.sleep(1000);*/
        int tpSize = 5;

        ThreadPoolExecutor tpe = new ThreadPoolExecutor(tpSize, tpSize, 200, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        //The maximum time for ThreadPoolExecutor to wait
        long maxTimeToWait = (200 * maxResults) / tpSize;

        long time1= System.nanoTime();


        for(int i = 0; i < maxResults; i++){
            String resultURL = results.get(i);
            tpe.execute(new WebpageContentReader(resultURL));
        }

        tpe.shutdown();
        //tpe.awaitTermination( maxTimeToWait, TimeUnit.SECONDS);
        while(!tpe.isTerminated())
            //tpe.awaitTermination( maxTimeToWait, TimeUnit.MILLISECONDS);
            Thread.sleep(50);

        long diff= System.nanoTime() - time1;

        logger.info("IT TOOK " + diff);
    }

    private static double calculateTopicCoverage(SearchResult result){
        double modulus_T = result.getFeatureVector().length;//This |T|: the number of topic terms
        double modulus_T_Intersection_P = 0;//This |T Π P|: the number of topic terms

        for(double featureValue : result.getFeatureVector())
            if(featureValue > 0)
                modulus_T_Intersection_P ++;

        return modulus_T > 0 ? (modulus_T_Intersection_P / modulus_T) : modulus_T;
    }

    /**
     * Builds the feature vector associated with each article with respect to the topic terms
     * @param searchResults The results returned from the search engine
     * @param topicTermList The list of topic terms on which we should base our feature vector
     */
    private static void buildFeatureVectorForSearchResults(ArrayList<SearchResult> searchResults,
                                                    ArrayList<Word> topicTermList ){

        //Spawn at most 5 threads in parallel in order not to exhaust all system resources
        TopicTermsFinder[]topicTermsFinderThreads = new TopicTermsFinder[5];

        for(int i=0; i < searchResults.size(); i++){
            topicTermsFinderThreads[i % 5] = new TopicTermsFinder(searchResults.get(i), topicTermList);

            try{
                //For the last thread, i.e. 5th, we should wait until it finishes, before spawning more threads for other search results
                if((i%5 == 0) && (i > 0))
                    topicTermsFinderThreads[i % 5].join();
            }
            catch (InterruptedException exp){
                logger.error("Thread for finding the topic terms is interrupted, due to " + exp.getMessage());
            }
        }


    }


    /**
     * Builds the similarity matrix among all search results
     * @param searchResults The results returned from the search engine, and after calculating the feature vector associated with each on
     */
    private static void constructSimilarityMatrix(ArrayList<SearchResult> searchResults){

        similarityMatrix = new double[searchResults.size()][searchResults.size()];

        //Iterate through the matrix, and initialize matrix[i][i], with 1, as each feature vector is identical to itself
        for(int i = 0; i < searchResults.size(); i++){
            similarityMatrix[i][i] = 1;

            for(int j = i+1; j < searchResults.size(); j++){
                double similarity = Vector.calculateSimilarity(searchResults.get(i).getFeatureVector(),
                        searchResults.get(j).getFeatureVector());

//                double similarity2 = Vector.calculateSimilarity(searchResults.get(j).getFeatureVector(),
//                        searchResults.get(i).getFeatureVector());

                similarityMatrix[i][j] = similarityMatrix[j][i] = similarity;

            }
        }

    }


    /**
     * Gets the title of the webpage with the passed URL
     * @param url   The URL of the page
     * @return  The title of the webpage
     */
    private static String getWebpageTitle(String url){

        try{
            /*HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.connect();

            InputStream inputStream = con.getInputStream();

            HtmlCleaner cleaner = new HtmlCleaner();
            CleanerProperties props = cleaner.getProperties();

            TagNode node = cleaner.clean(inputStream);
            TagNode titleNode = node.findElementByName("title", true);

            String title = titleNode.getText().toString();
            title = StringEscapeUtils.unescapeHtml(title).trim();
            title = title.replace("\n", "");
            return title;*/

            InputStream in=new URL(url).openStream();
            Document doc= new Tidy().parseDOM(in, null);

            String titleText=doc.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
            if (titleText == null)
                titleText=doc.getElementsByTagName("TITLE").item(0).getFirstChild().getNodeValue();

            return titleText;

        }
        catch (Exception exp){
            logger.warn("The title of the webpage cannot be fetched, " + exp.getMessage());
            return url;
        }

    }


    /**
     * Gets the title of the webpage with the passed URL
     * @param url   The URL of the page
     * @return  The title of the webpage
     */
    private static String getPageTitle(String url){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));

            Pattern pHead = Pattern.compile("(?i)</HEAD>");
            Matcher mHead;
            Pattern pTitle = Pattern.compile("(?i)</TITLE>");
            Matcher mTitle;

            String inputLine;
            boolean found=false;
            boolean notFound=false;
            String html = "";
            String title=new String();
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
     * Removes all duplicate entries from an ArrayList of search results
     * @param list  The list from which duplicates should be removed
     */
    private static void removeDuplicates(ArrayList<SearchResult> list) {
        HashSet<SearchResult> set = new HashSet<SearchResult>(list);
        list.clear();
        list.addAll(set);
    }

    /**
     *
     * @param searchTriple  Triple to return its components, i.e. the subject label, the object label, and the
     *                      various NL representations of the predicate.
     * @param boaConfidenceThreshold    The confidence threshold required for BOA index.
     * @return  A SearchQueryTerms object containing the various parts of the triple.
     */
    public static SearchQueryTerms getVariousSearchQueryTerms(String searchTriple, double boaConfidenceThreshold){

        String subject, predicate;

        //Extract the subject and the predicate out of the search predicate
        subject = KeywordExtractor.getTripleSubject(searchTriple);
        predicate = KeywordExtractor.getTriplePredicate(searchTriple);

        //Get the object of the triple
        TripleObject object = KeywordExtractor.getTripleObject(searchTriple);

        //Get the labels of the parts from the endpoint
        subjectLabel = DBpediaKnowledgeBaseQueryExecutor.getLabelForURI(subject);

        ArrayList<BoaSearchResult> boaPredicate_Representations = BoaSearcher.searchBOA(predicate, boaConfidenceThreshold, 1100000);

        if(object.isURI())
            objectLabel = DBpediaKnowledgeBaseQueryExecutor.getLabelForURI(object.getObject());
        else
            objectLabel = object.getObject();

        //As the NL representations returned from BOA may contain commas or spaces we should remove them
//        for(BoaSearchResult rep: boaPredicate_Representations){
//            rep.getNL_Represntation().replace(",", "");
//        }


        return new SearchQueryTerms(subjectLabel, objectLabel, boaPredicate_Representations);
        
    }


    /**
     * This class helps in sorting the search results got from the search engine according to the similarity measure 
     * calculated and stored in similarityMatrix
     */
    private static class SearchResultSimilarity implements Comparable<SearchResultSimilarity>{
        int searchResultIndex = 0;
        int countOfSimilaritiesAboveTheta = 0;

        private SearchResultSimilarity(int searchResultIndex, int countOfSimilaritiesAboveTheta) {
            this.searchResultIndex = searchResultIndex;
            this.countOfSimilaritiesAboveTheta = countOfSimilaritiesAboveTheta;
        }

        public int getSearchResultIndex() {
            return searchResultIndex;
        }

        public int getCountOfSimilaritiesAboveTheta() {
            return countOfSimilaritiesAboveTheta;
        }

        public int compareTo(SearchResultSimilarity searchResultSimilarity) {

            //Note that we want to sort them descendingly
            if(countOfSimilaritiesAboveTheta == searchResultSimilarity.getCountOfSimilaritiesAboveTheta())
                return 0;
            else
                return countOfSimilaritiesAboveTheta < searchResultSimilarity.getCountOfSimilaritiesAboveTheta() ? 1 : -1;
        }
    }
}
