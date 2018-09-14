/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.defacto.webservices.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.webservices.client.SimpleDefactoClient;
import org.aksw.defacto.webservices.server.DefactoServer;
import org.ini4j.Ini;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

import edu.stanford.nlp.util.StringUtils;

/**
 *
 * @author ngonga
 */
@Path("/getdefactotimes")
public class DefactoTimePeriodService {

	// http://localhost:1234/getdefactotimes?s=http%3A%2F%2Fdbpedia.org%2Fresource%2FBallack&p=http%3A%2F%2Fdbpedia.org%2Fontology%2Fteam&o=http%3A%2F%2Fdbpedia.org%2Fresource%2FChelsea_F.C.&slabel=Michael%20Ballack&olabel=Chelsea
	// http://localhost:1234/getdefactotimes?s=http%3A%2F%2Fdbpedia.org%2Fresource%2FBallack&p=http%3A%2F%2Fdbpedia.org%2Fontology%2Fteam&o=http%3A%2F%2Fdbpedia.org%2Fresource%2FChelsea_F.C.&slabel=Michael%20Ballack&olabel=Chelsea
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefactoTimePeriodService.class);
	
    @POST
    @Produces("application/json")
    public Response getJson(
    		@QueryParam("s") String subject, @QueryParam("sLabel") List<String> slabels, @QueryParam("sAltLabel") List<String> sAltLabels,
    		@QueryParam("o") String object, @QueryParam("oLabel") List<String> olabels, @QueryParam("oAltLabel") List<String> oAltLabels,
            @QueryParam("p") String property,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("language") List<String> languages,
            @QueryParam("contextSize") String contextSize
            ) {
    	
    	String message = validateInput(subject, slabels, sAltLabels, object, olabels, oAltLabels, property, from, to, languages, contextSize);
    	
    	if ( !message.isEmpty() )
    		return Response.status(Response.Status.BAD_REQUEST).entity(message).type(MediaType.TEXT_PLAIN).build();
    	
    	LOGGER.info("Processing <" + subject + ">, <" + property + ">, <" + object + ">");
        if ( olabels != null && slabels != null) 
        	LOGGER.info("Processing <" + slabels + ">, <" + property + ">, <" + olabels + ">");
        
        try {
        	
            org.apache.log4j.PropertyConfigurator.configure("log/log4j.properties");
            Defacto.DEFACTO_CONFIG.setStringSetting("settings", "context-size", contextSize);
            Model model = ModelFactory.createDefaultModel();
            
            // subj
            Resource subj = model.createResource(subject);
        	for ( String labelToLang : slabels) {
        		
        		String label	= labelToLang.substring(0, labelToLang.length() - 3);
        		String lang		= labelToLang.substring(labelToLang.length() - 2);
        		
        		subj.addProperty(RDFS.label, label, lang);
        	}
        	for ( String altLabel : sAltLabels) {
        		
        		String label	= altLabel.substring(0, altLabel.length() - 3);
        		String lang		= altLabel.substring(altLabel.length() - 2);
        		
        		subj.addProperty(Constants.SKOS_ALT_LABEL, label, lang);
        	}
        	
        	// obj
            Resource obj = model.createResource(object);
            for (String labelToLang : olabels) {
        		
        		String label	= labelToLang.substring(0, labelToLang.length() - 3);
        		String lang		= labelToLang.substring(labelToLang.length() - 2);
        		
        		obj.addProperty(RDFS.label, label, lang);
        	}
            for ( String altLabel : oAltLabels) {
        		
        		String label	= altLabel.substring(0, altLabel.length() - 3);
        		String lang		= altLabel.substring(altLabel.length() - 2);
        		
        		obj.addProperty(Constants.SKOS_ALT_LABEL, label, lang);
        	}
            
            Resource bnode = model.createResource("http://defacto.aksw.org/resource/Temp__1");
            subj.addProperty(ResourceFactory.createProperty("http://defacto.aksw.org/ontology/temp"), bnode);
            bnode.addProperty(model.createProperty(property), obj);
            bnode.addProperty(Constants.DEFACTO_FROM, from == null ? "" : from);
            bnode.addProperty(Constants.DEFACTO_TO, to == null ? "" : to);
            
            Evidence ev = Defacto.checkFact(new DefactoModel(model, 
            		subject + " " + property + " " + object, true, languages), Defacto.TIME_DISTRIBUTION_ONLY.YES);
            
            JSONObject result = new JSONObject();
            result.put("subject", subject);
            result.put("sLabel", StringUtils.join(slabels, ", "));
            result.put("predicate", property);
            result.put("object", object);
            result.put("oLabel", StringUtils.join(olabels, ", "));
            result.put("from", from == null ? "" : from);
            result.put("to", to == null ? "" : to);
            buildYearOccurrences(result, ev);
        	
            return Response.ok(result.toString()).header("Access-Control-Allow-Origin", "*").build();
        }
        catch (Exception e) {
        
        	LOGGER.error("Error while processing <" + subject + "," + property + "," + object + ">");
        	LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        return Response.serverError().build();
    }


	private String validateInput(String subject, List<String> slabels, List<String> sAltLabels, String object, List<String> olabels, List<String> oAltLabels, 
			String property, String from, String to,
			List<String> languages, String contextSize) {

		String message = "";
		
		if ( subject == null || subject.isEmpty() ) message += "\tNo subject uri given!\n";
		if ( object == null  || object.isEmpty() ) message += "\tNo object uri given!\n";
		if ( slabels == null || slabels.isEmpty() ) message += "\tNo subject labels given!\n";
		if ( olabels == null || olabels.isEmpty() ) message += "\tNo object labels given!\n";
		if ( sAltLabels == null || sAltLabels.isEmpty() ) message += "\tNo subject alternative labels given!\n";
		if ( oAltLabels == null || oAltLabels.isEmpty() ) message += "\tNo object alternative labels given!\n";
		if ( property == null || property.isEmpty() ) message += "\tNo property uri given!\n";
		
		if ( languages == null || languages.isEmpty() ) message += "\tNo languages given!\n";
		if ( !languages.isEmpty() ) {
			
			List<String> configuredLanguages = Arrays.asList(Defacto.DEFACTO_CONFIG.getStringSetting("settings", "languages").split(","));
			for ( String suppliedLanguage : languages ) {
				
				if ( !configuredLanguages.contains(suppliedLanguage) ) message += "\tLanguage '"+suppliedLanguage+"' not supported!";
			}
		}
		
		return message;
	}
	
	public static void main(String[] args) throws UniformInterfaceException, ClientHandlerException, JSONException {
		
//		String subjectUri, String propertyUri, String objectUri, List<String> languages,
//		String from, String to, String contextSize, 
//		Map<String,String> subjectLabels, Map<String,String> objectLabels, 
//		Map<String,Set<String>> altSubjectLabels, Map<String,Set<String>> altObjectLabels) 
		
		Map<String, Set<String>> altSubjectLabels = new HashMap<>();
		altSubjectLabels.put("en", new HashSet<>(Arrays.asList("Ribéry", "Ribery", "Frank R.")));
		
		Map<String,String> empty = new HashMap<String,String>();
		Map<String,Set<String>> empty2 = new HashMap<String,Set<String>>();
		JSONObject result = SimpleDefactoClient.queryDefacto("", "", "", Arrays.asList("en"), "", "", "", empty, empty, altSubjectLabels, altSubjectLabels);
		System.out.println(result);
	}


	private void buildYearOccurrences(JSONObject result, Evidence ev) throws JSONException {
		
		JSONObject tiny = new JSONObject();
        for ( Map.Entry<String,Long> times : ev.tinyContextYearOccurrences.entrySet()) 
        	tiny.put(times.getKey(), times.getValue());
		
        JSONObject small = new JSONObject();
        for ( Map.Entry<String,Long> times : ev.smallContextYearOccurrences.entrySet()) 
        	small.put(times.getKey(), times.getValue());
        
        JSONObject medium = new JSONObject();
        for ( Map.Entry<String,Long> times : ev.mediumContextYearOccurrences.entrySet()) 
        	medium.put(times.getKey(), times.getValue());
        
        JSONObject large = new JSONObject();
        for ( Map.Entry<String,Long> times : ev.largeContextYearOccurrences.entrySet()) 
        	large.put(times.getKey(), times.getValue());

        result.put("tiny", tiny);
        result.put("small", small);
        result.put("medium", medium);
        result.put("large", large);
	}
}
