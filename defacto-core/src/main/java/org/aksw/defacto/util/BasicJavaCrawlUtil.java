package org.aksw.defacto.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;


public class BasicJavaCrawlUtil implements CrawlUtil {

    private static Logger logger = Logger.getLogger(BasicJavaCrawlUtil.class);
    
    /**
     * 
     * @param url
     * @return
     * @throws Exception
     */
    public String readPage(String url, int timeout) {

        DefaultHttpClient client    = null;
        HttpGet request             = null;
        HttpResponse response       = null;
        Reader reader               = null;
        StringBuffer sb             = new StringBuffer();
        
        try {
            
            client   = new DefaultHttpClient();
            request  = new HttpGet(new URL(url).toURI());
            response = client.execute(request);
            
            reader = new InputStreamReader(response.getEntity().getContent());

            int read;
            char[] cbuf = new char[1024];
            while ((read = reader.read(cbuf)) != -1)
                sb.append(cbuf, 0, read);
        }
        catch (MalformedURLException e) {
            
            logger.warn("Could not download page, malformed url: " + url, e);
        }
        catch (URISyntaxException e) {
            
            logger.warn("Could not download page, malformed url: " + url, e);
        }
        catch (IllegalStateException e) {
            
            logger.warn("IllegalStateException, url: " + url, e);
        }
        catch (IOException e) {
        
            logger.warn("IOException, url: " + url, e);
        } 
        finally {
            
            if (reader != null) {
                
                try {
                    
                    reader.close();
                } 
                catch (IOException e) {
                    
                    logger.error("Could not close writer for url: " + url, e);
                }
            }
        }
        return sb.toString();
    }
}
