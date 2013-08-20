/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.defacto.webservices.server;

/**
 *
 * @author ngonga
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.ws.rs.core.UriBuilder;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 *
 * @author ngonga
 */
public class DefactoServer {

    public static Logger log = Logger.getLogger(DefactoServer.class.toString());
    
    private static URI getBaseURI() {
    	
    	try {
    		Defacto.init();
			Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(new File("defacto.ini")));
		} catch (InvalidFileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return UriBuilder.fromUri(Defacto.DEFACTO_CONFIG.getStringSetting("server", "ip")).
        		port(Defacto.DEFACTO_CONFIG.getIntegerSetting("server", "port")).build();
    }
    public static final URI BASE_URI = getBaseURI();

    protected static HttpServer startServer() throws IOException {

    	ResourceConfig rc = new PackagesResourceConfig("org.aksw.defacto.webservices");
    	HttpServer httpServer = GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
        httpServer.getListener("grizzly").setMaxHttpHeaderSize(Integer.MAX_VALUE);

        // don't forget to start the server explicitly
        httpServer.start();
        return httpServer;
    }

    public static void main(String[] args) throws IOException {
        try {
        	
            FileHandler fh = new FileHandler("DeFacto.log");
            log.addHandler(fh);
            Logger l = Logger.getLogger("org.apache.solr.client.solrj.impl.HttpClientUtil");
            l.setLevel(Level.WARNING);
            Logger g = Logger.getLogger("edu.stanford.nlp.process.PTBLexer");
            g.setLevel(Level.SEVERE);
            
            //logger.setLevel(Level.ALL);  
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // the following statement is used to log any messages  
            log.info("Service started");
            HttpServer httpServer = startServer();
            System.out.println(String.format("Jersey app started with WADL available at "
                    + "%sapplication.wadl\nTry out %sgetdefactoscore"
                    + "\nHit enter to stop it...",
                    BASE_URI, BASE_URI));
            System.in.read();
            httpServer.stop();
        } catch (Exception e) {
            log.warning("Messed up handlers");
            e.printStackTrace();
        }
    }
}
