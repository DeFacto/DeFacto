package org.aksw.defacto.util;

/**
 * Created by esteves on 03.09.15.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonReader {


    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }



    public static void main(String[] args) throws IOException, JSONException {

        try{
            String url = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=info&pageids=11727563&inprop=url";
            JSONObject json = readJsonFromUrl(url);

            System.out.println(json.getJSONObject("query").getJSONObject("pages").getJSONObject("11727563").get("fullurl").toString());

        }catch (Exception e){
            System.out.println(e.toString());
        }


    }
}
