package org.aksw.defacto.util;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 6/14/12
 * Time: 1:18 PM
 * Reads the list of properties supported by the system from the file, and returns them for display
 */
public class PropertiesReader {
    private static Logger logger  = Logger.getLogger(PropertiesReader.class);

    private static final String PROPERTIES_FINLENAME = "defacto-core/resources/properties/properties.txt";

    public static HashMap<String, String> getPropertiesList(){
        try{

            SparqlUtil sparqlEndpointDBpediaLive = new SparqlUtil("http://live.dbpedia.org/sparql", "http://dbpedia.org");

            List<String> propertyList = FileUtils.readLines(new File(PROPERTIES_FINLENAME));

            //Occupation is supported at the moment
            propertyList.remove("http://dbpedia.org/ontology/occupation");

            HashMap<String, String> hmPropertyWithLabelList = new HashMap<String, String>();
            for(String property : propertyList)
                hmPropertyWithLabelList.put(property, sparqlEndpointDBpediaLive.getEnLabel(property));
            return hmPropertyWithLabelList;
        }
        catch (IOException exp){
            logger.warn("Properties cannot be read from the file due to " + exp.getMessage());
            return null;
        }

    }
}
