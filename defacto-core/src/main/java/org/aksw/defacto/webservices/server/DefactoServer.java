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

import javax.ws.rs.core.UriBuilder;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 *
 * @author ngonga
 */
public class DefactoServer {

    private static URI getBaseURI() {
    	
    	Defacto.init();
    	
        return UriBuilder.fromUri(Defacto.DEFACTO_CONFIG.getStringSetting("server", "ip")).
        		port(Defacto.DEFACTO_CONFIG.getIntegerSetting("server", "port")).build();
    }
    public static final URI BASE_URI = getBaseURI();
    private static final Logger LOGGER = LoggerFactory.getLogger(DefactoServer.class);

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
        	
            // the following statement is used to log any messages  
            LOGGER.info("Service started");
            HttpServer httpServer = startServer();
            System.out.println(String.format("Jersey app started with WADL available at "
                    + "%sapplication.wadl\nTry out %sgetdefactoscore"
                    + "\nHit enter to stop it...",
                    BASE_URI, BASE_URI));
            System.in.read();
            httpServer.stop();
        }
        catch (Exception e) {
        	
        	LOGGER.error("Messed up handlers");
            e.printStackTrace();
        }
    }
}
