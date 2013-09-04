package org.aksw.defacto.webservices.client;

import java.io.IOException;

import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DefactoYearsClient {

	public static void main(String[] args) throws IOException, JSONException {

		Client client = Client.create(); // should be reused since it expensive to create
//		WebResource webResource = client.resource("http://localhost:1234/getdefactotimes");
		WebResource webResource = client.resource("http://titan.scms.eu:1234/getdefactotimes");
		
		String subjectUri	= "http://dbpedia.org/resource/Barack_Obama";
		String subjectLabel = "Barack Obama@en";
		String objectUri	= "http://dbpedia.org/resource/Michelle_Obama";
		String objectLabel	= "Michelle Obama@en";
		String propertyUri	= "http://dbpedia.org/ontology/spouse";

		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add("s", subjectUri);
		queryParams.add("sLabel", subjectLabel);
		queryParams.add("p", propertyUri);
		queryParams.add("o", objectUri);
		queryParams.add("oLabel", objectLabel);
		queryParams.add("language", "en");
		
		String result = webResource.queryParams(queryParams).post(String.class);
		JSONObject s = new JSONObject(result);
		
		JSONObject tiny = s.getJSONObject("tiny"); // or small, medium, large
     	
		JSONArray years = tiny.names();
		if ( years != null ) {

			for (int i = 0; i < years.length(); i++) {
				
				Integer occ		= (Integer)(tiny.get((String) years.get(i)));
				Integer year	= Integer.valueOf((String) years.get(i));
				
				System.out.println(String.format("%s;%s;%s;%s;%s;%s;%s", 
						subjectUri, subjectLabel, propertyUri, objectUri, objectLabel, year, occ));
			}
		}
	}
}
