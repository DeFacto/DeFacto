package org.aksw.defacto.nlp.ner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Collection;

/**
 * Created by dnes on 08/12/15.
 */
public class FOXNamedEntityRecognition {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(StanfordNLPNamedEntityRecognition.class);

    private static final String FOX_LIGHT_SPOTLIGHT_EN = "org.aksw.fox.tools.ner.en.SpotlightEN";
    private static final String FOX_LIGHT_TAGME_EN = "org.aksw.fox.tools.ner.en.TagMeEN";
    private static final String FOX_LIGHT_STANFORD_EN = "org.aksw.fox.tools.ner.en.StanfordEN";
    private static final String FOX_LIGHT_OPENNLP_EN = "org.aksw.fox.tools.ner.en.OpenNLPEN";
    private static final String FOX_LIGHT_NERDML_EN = "org.aksw.fox.tools.ner.en.NerdMLEN";
    private static final String FOX_LIGHT_ILLINOISEXTENDED_EN = "org.aksw.fox.tools.ner.en.IllinoisExtendedEN";
    private static final String FOX_LIGHT_BALIE_EN = "org.aksw.fox.tools.ner.en.BalieEN";

    private static final String FOX_PERSON = "[\"scmsann:PERSON\",\"ann:Annotation\"]";
    private static final String FOX_LOCATION = "[\"scmsann:LOCATION\",\"ann:Annotation\"]";

    private static Multimap<String, String> multimap;

    public static void main(String[] args) {

        Collection<String> locations;
        Collection<String> persons;

        String test = "The philosopher and mathematician Leibniz was born in Leipzig";
        Multimap<String, String> map;

        FOXNamedEntityRecognition t  = new FOXNamedEntityRecognition();
        map = t.getAnnotatedArray(test);

        locations = map.get("LOCATION");
        persons = map.get("PERSON");


    }

    public FOXNamedEntityRecognition(){
        multimap = ArrayListMultimap.create();
    }

    public Multimap<String,String> getAnnotatedArray(String sentence){

        try{
            JSONObject myObject = new JSONObject(getAnnotatedSenteces(sentence));

            JSONArray foxArray = myObject.getJSONArray("@graph");
            for (int i = 0, size = foxArray.length(); i < size; i++) {
                JSONObject oFOX = foxArray.getJSONObject(i);
                String[] elementNames = JSONObject.getNames(oFOX);
                for (String elementName : elementNames)
                {
                    String value = oFOX.getString(elementName);
                    System.out.printf("name=%s, value=%s\n", elementName, value);

                    if (elementName.equals("@type") == true && value.equals(FOX_LOCATION) == true){
                        multimap.put("LOCATION",oFOX.getString("ann:body"));
                    }else if (elementName.equals("@type") ==true && value.equals(FOX_PERSON) ==true){
                        multimap.put("PERSON",oFOX.getString("ann:body"));
                    }
                }
            }

        }catch (JSONException e) {
            LOG.error("Error on converting the FOX Object: " + e.toString());
        }catch (Exception e){
            LOG.error("Error: " + e.toString());
        }

        return multimap;

    }

    private String getAnnotatedSenteces(String s){

        try{

            /*
            input : text or an url (e.g.: `G. W. Leibniz was born in Leipzig`, `http://en.wikipedia.org/wiki/Leipzig_University`)
            type : { text | url }
            task : { NER }
            output : { JSON-LD | N-Triples | RDF/{ JSON | XML } | Turtle | TriG | N-Quads}
            returnHtml: true | false
            foxlight:
             */

            //String url = "http://" + server.host + ":" + server.port;

            String url = "http://fox-demo.aksw.org";
            final Charset UTF_8 = Charset.forName("UTF-8");
            JSONObject jo = new JSONObject()
                    .put("type", "text")
                    .put("task", "NER")
                    .put("output", "JSON-LD")
                    .put("disamb","off")
                    .put("foxlight", FOX_LIGHT_STANFORD_EN)
                    //.put("returnHtml", "false")
                    .put("input", s);

            LOG.debug("Parameter: " + jo);

            Response response = Request
                    .Post(url.concat("/call/ner/entities"))
                    .addHeader("Content-type", "application/json;charset=".concat(UTF_8.toString()))
                    .addHeader("Accept-Charset", UTF_8.toString())
                    .body(
                            new StringEntity(
                                    jo.toString(), ContentType.APPLICATION_JSON
                            )
                    )
                    .execute();

            HttpResponse httpResponse = response.returnResponse();
            LOG.debug(httpResponse.getStatusLine().toString());

            HttpEntity entry = httpResponse.getEntity();
            String r = IOUtils.toString(entry.getContent(), UTF_8);
            EntityUtils.consume(entry);

            LOG.debug(r);

            return r;

        }catch (Exception e){
            return "";
        }

    }

}
