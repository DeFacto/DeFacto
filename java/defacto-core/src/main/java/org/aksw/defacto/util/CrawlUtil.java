/**
 * 
 */
package org.aksw.defacto.util;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface CrawlUtil {
    
    /**
     * 
     * @param url
     * @return
     */
    public String readPage(String url, int timeout);
}
