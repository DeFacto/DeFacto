package org.aksw.defacto.webservices.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.model.DefactoModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.aksw.defacto.util.BufferedFileWriter;
import org.aksw.defacto.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.defacto.util.Encoder.Encoding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import edu.stanford.nlp.util.StringUtils;

public class DefactoClient {

	public static void main(String[] args) throws IOException, JSONException {

		Defacto.init();
		
		String type = "award";
		
		Client client = Client.create();
//		WebResource webResource = client.resource("http://localhost:1234/getdefactotimes");
		WebResource webResource = client.resource("http://139.18.2.164:1234/getdefactotimes");

		List<File> modelFiles = new ArrayList<File>();
        modelFiles.addAll(Arrays.asList(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
        		Defacto.DEFACTO_CONFIG.getStringSetting("eval", "train-directory") + "correct/"+type+"/").listFiles()));
        Collections.sort(modelFiles);
        
        List<List<String>> languages = Arrays.asList(Arrays.asList("en", "fr", "de"), Arrays.asList("en", "fr"), Arrays.asList("en", "de"), Arrays.asList("de", "fr"), Arrays.asList("en"), Arrays.asList("de"), Arrays.asList("fr"));
        
        for ( List<String> language : languages) {
        	
        	 JSONArray results = new JSONArray();
             BufferedFileWriter csv = new BufferedFileWriter(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/" + type + "_" + StringUtils.join(language, "") + ".csv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
             
             csv.write(String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s", 
     				"name", "subject", "predicate", "object", 
     				"from", "to", 
     				"small", "medium", "large"));
             
             int i = 0;
     		for ( File modelFile : modelFiles ) {
     			
     			System.out.println(modelFile.getName());
     			if ( !modelFile.getName().contains("award_00004") ) continue;
     			
     			Model model = ModelFactory.createDefaultModel();
				model.read(new FileReader(modelFile), "", "TTL");
				String name = modelFile.getName();
                 
				System.out.println("Running DeFacto Webservice for: " + name + "_" + StringUtils.join(language, ""));
				DefactoModel dm = new DefactoModel(model, name, true, language);
                 
     			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
     			
     			// subject
     			queryParams.add("s", dm.getSubjectUri());
     			for ( String lang : language ) {
     				
     				int j=0;
     				queryParams.add("sLabel", dm.getSubjectLabel(lang) + "@" + lang);
     				for ( String altLabel : dm.getSubjectAltLabels(lang)) {
     					
//     					if (j++ == 50) break; 
     					queryParams.add("sAltLabel", altLabel + "@" + lang);
     				}
     			}
     			
     			// predicate
     			queryParams.add("p", dm.getPropertyUri());
     			
     			// object
     			queryParams.add("o", dm.getObjectUri());
     			for ( String lang : language ) {
     				
     				queryParams.add("oLabel", dm.getObjectLabel(lang) + "@" + lang);
     				int j=0;
     				for ( String altLabel : dm.getObjectAltLabels(lang)) {
     					
//     					if (j++ == 50) break; 
     					queryParams.add("oAltLabel", altLabel + "@" + lang);
     				}
     			}
     				
     			// time period
     			queryParams.add("from", dm.getTimePeriod().getFrom().toString());
     			queryParams.add("to", dm.getTimePeriod().getTo().toString());
     			
     			// languages
     			for ( String lang : dm.getLanguages() ) 
     				queryParams.add("language", lang);
     			
     			// start the service
     			JSONObject s = new JSONObject(webResource.queryParams(queryParams).post(String.class));
     			
     			JSONObject tiny = s.getJSONObject("tiny");
     			JSONObject small = s.getJSONObject("small");
     			JSONObject medium = s.getJSONObject("medium");
     			JSONObject large = s.getJSONObject("large");
     			
     			String output = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s", 
     					dm.getName(), dm.getSubjectUri(), dm.getPropertyUri(), dm.getObjectUri(), 
     					dm.getTimePeriod().getFrom(), dm.getTimePeriod().getTo(), 
     					getMostFrequentYear(tiny), getMostFrequentYear(small), getMostFrequentYear(medium), getMostFrequentYear(large));
     			csv.write(output);
     			csv.flush();
     			
     			results.put(s);
//     			if ( i++ == 3) break;
     		}
     		
     		Gson gson = new GsonBuilder().setPrettyPrinting().create();
     		String json = gson.toJson(new JsonParser().parse(results.toString()));
     		BufferedFileWriter jsonWriter = new BufferedFileWriter(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/" + type + "_" + StringUtils.join(language, "") + ".json", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
     		jsonWriter.write(json);
     		jsonWriter.close();
     		csv.close();
        }
	}

	private static Integer getMostFrequentYear(JSONObject yearDistribution) throws JSONException {
		
		Integer maxOcc = 0, maxYear = 0;
		
		JSONArray years = yearDistribution.names();
		if ( years == null ) return -1;
		
		for (int i = 0; i < years.length(); i++) {
			
			Integer occ		= (Integer)(yearDistribution.get((String) years.get(i)));
			Integer year	= Integer.valueOf((String) years.get(i));
			
			if ( occ >= maxOcc ) {
				
				maxOcc = occ;
				maxYear = year;
			}
		}
		
		return maxYear;
	}
}
