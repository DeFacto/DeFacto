/**
 * 
 */
package org.aksw.defacto.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

/**
 * @author Lorenz Buehmann
 *
 */
public class AGDISTIS {
	
	public static String AGDISTIS_SERVICE_URL = "http://139.18.2.164:8080/AGDISTIS";
	
	public static AGDISTISResult disambiguate(String subject, String object){
		try {
			String text = "<entity>" + subject + "</entity> <entity>" + object + "</entity>";
			text = URLEncoder.encode(text, "UTF-8").replace("+", "%20");
			String urlParameters = "text=" + text + "&type=agdistis";
			String request = AGDISTIS_SERVICE_URL;

			URL url = new URL(request);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			connection.disconnect();

			StringBuilder sb = new StringBuilder();
			InputStreamReader reader = new InputStreamReader(connection.getInputStream());
			char buffer[] = new char[1024];
			int length = reader.read(buffer);
			while (length > 0) {
				while (length > 0) {
					sb.append(buffer, 0, length);
					length = reader.read(buffer);
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				length = reader.read(buffer);
			}
			wr.close();
			reader.close();
			
			//parse the JSON
			JSONArray array = (JSONArray)JSONValue.parse(sb.toString());
			String subjectURI = (String)((JSONObject) array.get(0)).get("disambiguatedURL");
			String objectURI = (String)((JSONObject) array.get(1)).get("disambiguatedURL");
			return new AGDISTISResult(subjectURI, objectURI);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		AGDISTIS.disambiguate("Albert Einstein", "Nobel Prize");
	}

}
