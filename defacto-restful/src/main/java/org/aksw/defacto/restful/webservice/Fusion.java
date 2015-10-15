package org.aksw.defacto.restful.webservice;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.restful.core.RestModel;
import org.aksw.defacto.restful.mongo.MongoManager;
import org.aksw.defacto.restful.utils.Cfg;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.mongodb.DBObject;

@RestController
@RequestMapping("/fusion")
public class Fusion {
    static {
        PropertyConfigurator.configure(Cfg.LOG_FILE);
    }
    public static Logger       LOG               = LogManager.getLogger(Fusion.class);

    public static String       redirecturl       = "http://localhost:4441/#/facts/";

    public static final String collectionData    = "data-mappingbased-properties";
    public static final String collectionResults = "results";

    protected MongoManager     mongoData         = null;
    protected MongoManager     mongoResults      = null;

    protected RestModel        model             = null;

    Vote                       vote              = new Vote();

    /**
     * Defacto config and example data loading.
     */
    @PostConstruct
    protected void init() {
        model = new RestModel();
        mongoData = MongoManager.getMongoManager().setCollection(collectionData);
        mongoResults = MongoManager.getMongoManager().setCollection(collectionResults);
    }

    // ----

    /**
     * method: GET<br>
     * path: id/<id>/input<br>
     * 
     * @param id
     *            [0-9a-fA-F]{24}
     * @param response
     * @return
     */
    @RequestMapping(
            value = "/id/{id:^[0-9a-fA-F]{24}$}",
            produces = "application/json",
            method = RequestMethod.GET)
    @ResponseBody
    public String id(@PathVariable("id") String id, HttpServletResponse response) {
        JSONArray votes = vote.getVotes(id);

        JSONArray ja = new JSONArray();
        // find triple in db
        DBObject ob = mongoData.findDocumentById(id);
        JSONObject jo = new JSONObject(ob.toString());
        String s = jo.getString("s_dbpedia");
        String p = jo.getString("p");
        JSONArray context = jo.getJSONArray("context");
        // for each context get stored results
        for (int i = 0; i < context.length(); i++) {
            if (!context.getJSONObject(i).getString("dataset").equals("mappingbased-properties"))
                continue;

            String o = context.getJSONObject(i).getString("value_dbpedia");
            // find result
            Iterator<DBObject> iter = mongoResults.find(new JSONObject().put("s", s).put("p", p).put("o", o).toString());
            while (iter.hasNext()) {
                JSONObject joo = new JSONObject(iter.next().toString());
                if (votes.length() > 0) {
                    for (int ii = 0; ii < votes.length(); ii++) {
                        JSONObject vote = votes.getJSONObject(ii);
                        if (vote.getString("s").equals(s) && vote.getString("p").equals(p) && vote.getString("o").equals(o)) {
                            joo.put("votes", vote.getInt("votes"));
                            break;
                        }
                    }
                }
                ja.put(joo);
            }
        }
        return ja.toString(2);
    }

    // ----

    /**
     * <code>
     http://localhost:4441/fusion/insert?s=http://data.dbpedia.org/resource/Q1000051&p=http://dbpedia.org/ontology/birthPlace&o=http://data.dbpedia.org/resource/Q54156
    </code>
     * 
     * method: POST<br>
     * path: insert/<br>
     *
     * @return json object
     */
    @RequestMapping(
            value = "/insert",
            method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> input(@RequestParam String s, @RequestParam String p, @RequestParam String o, HttpServletRequest request, HttpServletResponse response) {
        String id = "";
        try {
            new URI(s);
            new URI(p);
            new URI(o);

            String q = new JSONObject()
                    .put("s", s).put("p", p).put("o", o).toString();
            Iterator<DBObject> iter = mongoData.find(q);

            JSONArray ja = new JSONArray();
            while (iter.hasNext())
                ja.put(new JSONObject(iter.next().toString()));

            if (ja.length() > 1)
                LOG.info("Results found in DB for (" + q + "): " + ja.length());

            // we expect to have just one result for a given s,p,o
            if (ja.length() > 0) {
                handleDBResults(ja.getJSONObject(0));
                id = ja.getJSONObject(0).getJSONObject("_id").getString("$oid").toString();
            }

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong input.");
                response.flushBuffer();
            } catch (IOException ee) {
                LOG.error(ee.getLocalizedMessage(), ee);
            }
        }

        // redirect
        HttpHeaders httpHeaders = new HttpHeaders();
        URI uri;
        try {
            uri = new URI(redirecturl + id);
            httpHeaders.setLocation(uri);
        } catch (URISyntaxException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
    }

    private void handleDBResults(JSONObject jo) {

        String s = jo.getString("s_dbpedia");
        String p = jo.getString("p");

        JSONArray ja = jo.getJSONArray("context");
        for (int i = 0; i < ja.length(); i++) {
            JSONObject joo = ja.getJSONObject(i);
            String v = joo.getString("value_dbpedia");
            callDefacto(s, p, v);
        }
    }

    protected JSONObject callDefacto(String s, String p, String o) {
        // find results in db
        Iterator<DBObject> iter = mongoResults
                .find(new JSONObject()
                        .put("s", s)
                        .put("p", p)
                        .put("o", o)
                        .toString());

        while (iter.hasNext())
            return new JSONObject(iter.next().toString());

        // call defacto
        Triple triple = new Triple(
                NodeFactory.createURI(s),
                NodeFactory.createURI(p),
                NodeFactory.createURI(o)
                );

        DefactoModel defactoModel = model.getModel(triple, "", "");
        final Evidence evidence = Defacto.checkFact(defactoModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);
        JSONObject jo = model.out(evidence)
                .put("s", s)
                .put("p", p)
                .put("o", o);

        // store results in db
        mongoResults.insert(jo.toString());
        return jo;
    }
}
