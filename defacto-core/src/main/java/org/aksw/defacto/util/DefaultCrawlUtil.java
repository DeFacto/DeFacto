package org.aksw.defacto.util;

import org.apache.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.*;
import java.nio.charset.IllegalCharsetNameException;

/**
 * Created by esteves on 10/03/2017.
 */
public class DefaultCrawlUtil implements CrawlUtil {

    private static Logger logger = Logger.getLogger(DefaultCrawlUtil.class);


    public static void main(String[] args) {

        DefaultCrawlUtil crawl = new DefaultCrawlUtil();
        String s = crawl.readPage("http://www.globo.com", 1000);
        System.out.print(s);
    }

    @Override
    public String readPage(String url, int timeout) {

        URL u;
        InputStream is = null;
        String s;
        String content = "";

        try{
            u = new URL(url);
            is = u.openStream();
            BufferedReader d = new BufferedReader(new InputStreamReader(is));
            while ((s = d.readLine()) != null) {
                content += s;
            }
            try {
                is.close();
            } catch (IOException ioe) {
                // just going to ignore this one
            }

        }catch (Throwable e) {
            // we need to do this because the log file is flooded with useless error messages
            if ( e.getMessage().contains("Unhandled content type") ||
                    e.getMessage().contains("Premature EOF") ||
                    e.getMessage().contains("Read timed out") ||
                    e.getMessage().contains("Connection refused") ||
                    e.getMessage().contains("-1 error loading URL") ||
                    e.getMessage().contains("401 error loading URL") ||
                    e.getMessage().contains("403 error loading URL") ||
                    e.getMessage().contains("404 error loading URL") ||
                    e.getMessage().contains("405 error loading URL") ||
                    e.getMessage().contains("408 error loading URL") ||
                    e.getMessage().contains("410 error loading URL") ||
                    e.getMessage().contains("500 error loading URL") ||
                    e.getMessage().contains("502 error loading URL") ||
                    e.getMessage().contains("503 error loading URL") ||
                    e instanceof UnknownHostException  ||
                    e instanceof SSLHandshakeException ||
                    e instanceof SocketException ||
                    e instanceof SocketTimeoutException ||
                    e instanceof IllegalCharsetNameException) {

                logger.debug(String.format("Error crawling website: %s", url));
            }
            else logger.error(String.format("Error crawling website: %s", url), e);
        }

        return content;
    }
}