package org.aksw.defacto.webservices.client;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.bytecode.opencsv.CSVWriter;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SimpleDefactoClient {

	public static final WebResource webResource = Client.create().resource("http://139.18.2.164:1234/getdefactotimes");
	
	public static void main(String[] args) throws IOException, JSONException {

		Map<String, String> subjectLabels = new HashMap<>();
		subjectLabels.put("en", "Albert Einstein");
		subjectLabels.put("de", "Albert Einstein");
		subjectLabels.put("fr", "Albert Einstein");
		
		Map<String, String> objectLabels = new HashMap<>();
		objectLabels.put("en", "Nobel Prize in Physics");
		objectLabels.put("de", "Nobelpreis für Physik");
		objectLabels.put("fr", "Prix Nobel de physique");
		
		Map<String, Set<String>> altSubjectLabels = new HashMap<>();
		Set<String> enSAltLabels = new HashSet<>(Arrays.asList("A. Einstein")); // add more for each language
		Set<String> deSAltLabels = new HashSet<>(Arrays.asList("Einstein, Albert")); // from the surface form files
		Set<String> frSAltLabels = new HashSet<>(Arrays.asList("Albert Einstin"));
		altSubjectLabels.put("en", enSAltLabels);
		altSubjectLabels.put("de", deSAltLabels);
		altSubjectLabels.put("fr", frSAltLabels);
		
		Map<String, Set<String>> altObjectLabels = new HashMap<>();
		Set<String> enOAltLabels = new HashSet<>(Arrays.asList("Nobel Physics Prize")); // add more for each language
		Set<String> deOAltLabels = new HashSet<>(Arrays.asList("Physik-Nobelpreis")); // from the surface form files
		Set<String> frOAltLabels = new HashSet<>(Arrays.asList("Nobel laureates in physics"));
		altObjectLabels.put("en", enOAltLabels);
		altObjectLabels.put("de", deOAltLabels);
		altObjectLabels.put("fr", frOAltLabels);
		
		// start the service
		JSONObject result = queryDefacto("http://dbpedia.org/resource/Albert_Einstein", "http://dbpedia.org/ontology/award", "http://dbpedia.org/resource/Nobel_Prize_in_Physics",
				Arrays.asList("en", "de", "fr"), "1921", "1921", "tiny", subjectLabels, objectLabels, altSubjectLabels, altObjectLabels);

		writeData(result);
	}
	
	private static void writeData(JSONObject result) throws IOException, JSONException {
		
		for ( String contextLength : Arrays.asList("tiny", "small", "medium", "large") ) {
			
			CSVWriter writer = new CSVWriter(new FileWriter("/Users/gerb/TMP/"+contextLength+".csv"), '\t');
			JSONObject years = result.getJSONObject(contextLength);
			
			for (int i = 0; i < years.names().length(); i++) {
				
				List<String> output = new ArrayList<>();
				output.add(result.getString("subject"));
				output.add(result.getString("predicate"));
				output.add(result.getString("object"));
				output.add(result.getString("from"));
				output.add(result.getString("to"));
				output.add(years.names().getString(i));
				output.add("" + years.get(years.names().getString(i)));
				writer.writeNext(output.toArray(new String[]{}));
			}
			writer.close();
		}
	}

	public static JSONObject queryDefacto(String subjectUri, String propertyUri, String objectUri, List<String> languages,
									String from, String to, String contextSize, 
									Map<String,String> subjectLabels, Map<String,String> objectLabels, 
									Map<String,Set<String>> altSubjectLabels, Map<String,Set<String>> altObjectLabels) 
											throws UniformInterfaceException, ClientHandlerException, JSONException{
		
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

		// subject
		queryParams.add("s", subjectUri);
		for (String lang : languages) {

			queryParams.add("sLabel", subjectLabels.get(lang) + "@" + lang);
			for (String altLabel : altSubjectLabels.get(lang)) {

				// if (j++ == 50) break;
				queryParams.add("sAltLabel", altLabel + "@" + lang);
			}
		}

		// predicate
		queryParams.add("p", propertyUri);

		// object
		queryParams.add("o", objectUri);
		for (String lang : languages) {

			queryParams.add("oLabel", objectLabels.get(lang) + "@" + lang);
			for (String altLabel : altObjectLabels.get(lang)) {

				// if (j++ == 50) break;
				queryParams.add("oAltLabel", altLabel + "@" + lang);
			}
		}

		// languages
		for (String lang : languages)
			queryParams.add("language", lang);
		
		// context size: tiny, small, medium, large
		queryParams.add("contextSize", "tiny");
		
		// time period
			queryParams.add("from", from);
			queryParams.add("to", to);

		// start the service
		return new JSONObject(webResource.queryParams(queryParams).post(String.class));
	}
}
