package com.boilerpipe;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 3/7/12
 * Time: 4:35 PM
 * Calls the service provided by BoilerPipe, which can strip out the images that may exist in web pages, and return only
 * the body text.
 */
public class BoilerPipeCaller {

    private static Logger logger = null;
    static {
        logger = Logger.getLogger(BoilerPipeCaller.class.getName());
    }

    private static String serviceAddress = "http://boilerpipe-web.appspot.com/extract";

    /**
     * Returns the body text of the Webpage after stripping all images out.
     * @param websiteURL    The URL of the webpage for which the text only should be returned.
     * @return  The HTML text of the webpage without the images and all unnecessary stuff.
     */
    public static String getCleanHTMLPage(String websiteURL){

        StringBuilder stripedText = new StringBuilder();

        try{

            //Extract domain name only from the URL, as the URL may contain some unnecessary parameters
//            URL requiredWebsiteURL = new URL(websiteURL);
//            String domainName = requiredWebsiteURL.getHost();


            //serviceAddress += "?url=" + websiteURL;
            URL serviceURL = new URL(serviceAddress + "?url=" + websiteURL + "&extractor=KeepEverythingExtractor&output=htmlFragment");

            BufferedReader in = new BufferedReader(new InputStreamReader(serviceURL.openStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null){
                //System.out.println(inputLine);
                //pageRank = Integer.parseInt(inputLine);
                stripedText.append(inputLine);
                stripedText.append("\n");
            }

        }
        catch (Exception exp){
            logger.error("Cannot retrieve the body text of the page due to " + exp.getMessage());
        }
        return stripedText.toString();
    }

}
