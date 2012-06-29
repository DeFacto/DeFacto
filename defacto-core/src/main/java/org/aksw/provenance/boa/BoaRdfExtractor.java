package org.aksw.provenance.boa;

import java.io.StringReader;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.aksw.provenance.Constants;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class BoaRdfExtractor {

    public static boolean checkFact(String text, Model model) {

        // uri1 property uri2
        Statement linkStatement = getLinkStatement(model);
        
        // creates a model with all triples BOA has extracted
        Model boaModel = queryBoa(text, linkStatement.getPredicate().getURI());
        
        // check if the fact we want to evaluate is also in the fact BOA has generated
        return boaModel.contains(linkStatement);
    }

    /**
     * 
     * @param text
     * @param patternMappingUri
     * @return
     */
    private static Model queryBoa(String text, String patternMappingUri) {

        MultivaluedMap<String,String> formData = new MultivaluedMapImpl();
        formData.add("text", text);
        formData.add("patternMappingUri", patternMappingUri);
        formData.add("patternScoreThreshold", Constants.PATTERN_SCORE_THRESHOLD);
        formData.add("contextLookAheadThreshold", Constants.CONTEXT_LOOK_AHEAD_THRESHOLD);
        formData.add("dbpediaLinksOnly", "true");
        
        WebResource resource = Client.create().resource(UriBuilder.fromUri("http://localhost:8080/boa").build()).path("text2rdf");
        String rdf = resource.type("application/x-www-form-urlencoded").post(String.class, formData);
        
        Model model = ModelFactory.createDefaultModel();
        if (!rdf.trim().isEmpty()) model.read(new StringReader(rdf), "", "N3");
        
        return model;
    }

    /**
     * 
     * @param model
     * @return
     */
    private static Statement getLinkStatement(Model model) {

        StmtIterator stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()) {
            
            Statement st = stmtIterator.next();
            if ( !st.getPredicate().getURI().equals(Constants.RESOURCE_LABEL) ) return st; 
        }
        
        return null;
    }

}
