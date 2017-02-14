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
import org.json.JSONException;
import org.json.JSONObject;

/*
        helper for JSON
 */
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

    /*
        this function will return the value of a JSON element (last in the path) from a JSON object obtained from a URL
        the "path" must have at least 2 strings, the first one for the object and the second one for representing the key
        that you want to get the value from
     */
    public static String getElementValueFromURL(String url, String path){
        String ret ="";
        try{

            if (!path.contains(";")){
                throw new Exception("Please use ';' to split the values from a nested json structure ");
            }else {

                JSONObject json = readJsonFromUrl(url);

                String[] objects = path.split(";");
                JSONObject temp;

                temp = json.getJSONObject(objects[0]);
                if (objects.length > 2) {
                    for (int i = 1; i < objects.length - 1; i++) {
                        JSONObject aux = temp;
                        temp = aux.getJSONObject(objects[i]);
                    }
                }
                ret = temp.get(objects[objects.length - 1]).toString();
            }
        }catch (Exception e){
            return "";
        }
        return ret;
    }


    public static void main(String[] args) throws IOException, JSONException {

        String ret = getElementValueFromURL("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=info&pageids=11727563&inprop=url",
                "query;pages;11727563;fullurl");
        System.out.println(ret);

    }
}
