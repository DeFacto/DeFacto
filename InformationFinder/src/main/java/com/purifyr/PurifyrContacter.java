package com.purifyr;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 2/14/12
 * Time: 10:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class PurifyrContacter {

    private static Logger logger = Logger.getLogger(PurifyrContacter.class.getName());
//    static {
//        logger = Logger.getLogger(PurifyrContacter.class.getName());
//    }

    private static String serviceAddress = "http://purifyr.com";


    public static int GetCleanHTMLPage(String websiteURL){

        int pageRank = -1;

        try{

            serviceAddress += "?url=" + websiteURL;
            URL serviceURL = new URL(serviceAddress);

            BufferedReader in = new BufferedReader(new InputStreamReader(serviceURL.openStream()));
            String inputLine;


            while ((inputLine = in.readLine()) != null){
                logger.info(inputLine);
            }

        }
        catch (Exception exp){
            logger.error("Cannot retrieve the clean HTML page from Purifyr due to " + exp.getMessage());
        }
        return pageRank;
    }
}
