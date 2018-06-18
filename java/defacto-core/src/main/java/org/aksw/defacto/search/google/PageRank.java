package org.aksw.defacto.search.google;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.log4j.Logger;


/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 2/6/12
 * Time: 11:15 AM
 * Class PageRank handles the issues related to getting Google's PageRank associated with a specific URL
 */
public class PageRank {
    
    private static Logger logger = Logger.getLogger(PageRank.class.getName());
    /*static {
        logger = Logger.getLogger(PageRank.class.getName());
    }
    */

    private static String API_Key = "NTMxZjM2NjNiNzBmMzlmMTc3M2Y1ZTQ2";
//    private static String API_Key = "QEKO9GXaCLg6LSDuHUUajVuiJ3eY76e9BKtep09fhTk06mVQUYlsToL45k7jWVAP";
//    private static String serviceAddress = "http://api.exslim.net/pagerank";
    private static String serviceAddress = "http://pr.webinfodb.net/pr.php";


    public static int GetPageRank(String websiteURL){

        int pageRank = -1;

        try{

            //Extract domain name only from the URL, as the URL may contain some unnecessary parameters
//            URL requiredWebsiteURL = new URL(websiteURL);
//            String domainName = requiredWebsiteURL.getHost();


            String fullURL = serviceAddress + "?key=" + API_Key + "&url=" + websiteURL;
            URL serviceURL = new URL(fullURL);

            BufferedReader in = new BufferedReader(new InputStreamReader(serviceURL.openStream()));
            String inputLine;


            if ((inputLine = in.readLine()) != null){
                logger.info(inputLine);
                pageRank = Integer.parseInt(inputLine);
            }

        }
        catch (Exception exp){
            logger.error("Cannot retrieve PageRank due to " + exp.getMessage());
        }
        return pageRank;
    }
    
    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        System.out.println(GetPageRank("http://informatik.uni-leipzig.de"));
        System.out.println("Took: " + (System.currentTimeMillis() - start) + "ms");
    }
}
