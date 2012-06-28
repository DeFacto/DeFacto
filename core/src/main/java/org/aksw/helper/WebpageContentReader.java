package org.aksw.helper;

import org.aksw.InformationFinder.TripleResultFinder;
import org.aksw.provenance.topic.frequency.Word;
import org.aksw.provenance.topic.frequency.WordFrequencyCounter;
import org.aksw.results.SearchResult;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 3/25/12
 * Time: 1:04 PM
 * Helps in fetching a webpage content faster, as using threads can make this in parallel
 */
public class WebpageContentReader extends Thread{

    private static Logger logger = Logger.getLogger(WebpageContentReader.class.getName());

    private static int ThreadNumber = 0;

    private String url;
    private String pageContent;
    private String pageTitle;
    private boolean isFinished = false;

    private SearchResult searchResult;

    public String getPageContent() {
        return pageContent;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public WebpageContentReader(String url) {
        ThreadNumber++;
        this.url = url;
        start();
    }

    public boolean isFinished() {
        return isFinished;
    }

    /*public void run() {
        while(true)
        {
        logger.info("Started running");
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        logger.info(Thread.currentThread().activeCount());
        logger.info("Stopped running");
        }
    }*/

//    @Override
//    public void start() {
//        super.start();
//
//        logger.info("Thread started for " + url);
//        //Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//        isFinished = false;
//        pageContent = readHTMLofURL(url);
//        pageTitle =  getPageTitleDirectlyFromContent();
//
//        //construct the final SearchResult object containing all required information
//        searchResult = new SearchResult(url, 6, pageTitle, pageContent);
//        isFinished = true;
//
//        //Add the result to the main HashMap
//        if(!TripleResultFinder.hmSearchResults.containsKey(url))
//            TripleResultFinder.hmSearchResults.put(url, new SearchResult(url, 6, pageTitle, pageContent));
//
//        //calculateMostFrequentWordInWikipediaArticle();
//
//        //Thread.currentThread().interrupt();
//        logger.info("Thread ended for " + url);
//    }

    public void run(){
        logger.info("Thread started for " + url);
        //Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        isFinished = false;
        pageContent = readHTMLofURL(url);
        pageTitle =  getPageTitleDirectlyFromContent();

        //construct the final SearchResult object containing all required information
        searchResult = new SearchResult(url, 6, pageTitle, pageContent);
        isFinished = true;

        //Add the result to the main HashMap
        if(!TripleResultFinder.hmSearchResults.containsKey(url))
            TripleResultFinder.hmSearchResults.put(url, new SearchResult(url, 6, pageTitle, pageContent));

        //calculateMostFrequentWordInWikipediaArticle();

        //Thread.currentThread().interrupt();
        logger.info("Thread ended for " + url);
    }

    @Override
    public String toString() {
        return "WebpageContentReader{" +
                "url='" + url + '\'' +
                ", pageTitle='" + pageTitle + '\'' +
                '}';
    }

    private String readHTMLofURL(String url){

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
            logger.error("Cannot read from the specified URL, due to " + exp.getMessage());
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
    private String getPageTitle(String url){
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
     * Gets the title of the webpage without contacting the page itself, i.e. through extracting it directly from pageContent
     * @return  The title of the webpage
     */
    private String getPageTitleDirectlyFromContent(){
        String title = this.url;
        Pattern p = Pattern.compile("(?i)<TITLE.*?>(.*?)</TITLE>");
        Matcher m = p.matcher(pageContent);
        if (m.find()) {
            title=m.group(1);
        }
        return title;
    }


    /**
     * Returns a list of the highly frequent words in a Wikipedia article
     * @return  A list of the most frequent words
     */
    private ArrayList<Word> calculateMostFrequentWordInWikipediaArticle() {

        String outputText = Jsoup.parse(pageContent).text();

        ArrayList<Word> arr = WordFrequencyCounter.getKeywordsSortedByFrequency(outputText);
        return arr;
    }



}
