/**
 * 
 */
package org.aksw.defacto.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class Encoder {

    /**
     * 
     * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
     *
     */
    public enum Encoding {
        
        UTF_8("UTF-8"),
        ASCII("ASCII");
        
        private String name;
        
        Encoding(String name) {
            
            this.name = name;
        }
        
        @Override public String toString() {
            
            return this.name;
        }
    }
    
    /**
     * 
     * @param string
     * @param encoding
     * @return
     */
    public static String urlEncode(String string, Encoding encoding) {
        
        try {
            
            return URLEncoder.encode(string, encoding.toString());
        }
        catch (UnsupportedEncodingException e) {
            
            throw new RuntimeException("Encoding " + encoding + " not supported!");
        }
    }
}
