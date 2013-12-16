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
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.bytecode.opencsv.CSVWriter;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SimpleDefactoClient {

//	public static final WebResource webResource = Client.create().resource("http://139.18.2.164:1234/getdefactotimes");
	public static final WebResource webResource = Client.create().resource("http://localhost:1234/getdefactotimes");
	
	public static void main(String[] args) throws IOException, JSONException {

		System.out.println("Start");
		long start = System.currentTimeMillis();
		
		String enRiberyLabel = "Franck Ribery"; 
		Map<String, String> subjectLabels = new HashMap<>();
		subjectLabels.put("en", enRiberyLabel);
		subjectLabels.put("de", enRiberyLabel);
		subjectLabels.put("fr", enRiberyLabel);
		
		String enFCBLabel = "FC Bayern München";
		Map<String, String> objectLabels = new HashMap<>();
		objectLabels.put("en", enFCBLabel);
		objectLabels.put("de", enFCBLabel);
		objectLabels.put("fr", enFCBLabel);
		
		Map<String, Set<String>> altSubjectLabels = new HashMap<>();
		altSubjectLabels.put("en", new HashSet<>(Arrays.asList("Ribéry", "Ribery", "Frank R.")));
//		altSubjectLabels.put("de", new HashSet<>(Arrays.asList("Ribéry", "Ribery", "Frank R.")));
//		altSubjectLabels.put("fr", new HashSet<>(Arrays.asList("Ribéry", "Ribery", "Frank R.")));
		
		Map<String, Set<String>> altObjectLabels = new HashMap<>();
		altObjectLabels.put("en", new HashSet<>(Arrays.asList("FC Bayern", "Bayern", "FCB")));
//		altObjectLabels.put("de", new HashSet<>(Arrays.asList("FC Bayern", "Bayern", "FCB")));
//		altObjectLabels.put("fr", new HashSet<>(Arrays.asList("FC Bayern", "Bayern", "FCB")));
		
		// start the service
		JSONObject result = queryDefacto("http://dbpedia.org/resource/Frank_Ribéry", "http://dbpedia.org/ontology/team", "http://dbpedia.org/resource/FC_Bayern_München",
				Arrays.asList("en"/*, "de", "fr"*/), "2007", "2013", "tiny", subjectLabels, objectLabels, altSubjectLabels, altObjectLabels);
		
		System.out.println("End: " + (System.currentTimeMillis()-start));

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
		ClientResponse clientResponse = webResource.queryParams(queryParams).post(ClientResponse.class);
		String message = clientResponse.getEntity(String.class);
		
		if ( clientResponse.getClientResponseStatus().getStatusCode() == Response.Status.BAD_REQUEST.getStatusCode() ) 
			throw new RuntimeException("\nWrong parameters given: \n" + message);
		else {
			return new JSONObject(message);
		}
	}
}
