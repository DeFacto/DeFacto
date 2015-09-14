package org.aksw.defacto.restful.webservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.restful.core.RestModel;
import org.aksw.defacto.restful.mongo.MongoManager;
import org.aksw.defacto.restful.utils.Cfg;
import org.aksw.defacto.util.EvidenceRDFGenerator;
import org.aksw.defacto.util.FactBenchExample;
import org.aksw.defacto.util.FactBenchExamplesLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bson.BsonArray;
import org.bson.BsonObjectId;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * 
 * @author rspeck
 *
 */
@RestController
@RequestMapping("/fusion")
public class Fusion {
    static {
        PropertyConfigurator.configure(Cfg.LOG_FILE);
    }
    public static Logger       LOG               = LogManager.getLogger(Fusion.class);

    public static final String collectionResults = "results";
    public static final String collectionTriples = "triples";
    public static final String collectionFacts   = "facts";

    protected MongoManager     mongoTriples      = null;
    protected MongoManager     mongoResults      = null;
    protected MongoManager     mongoFacts        = null;

    protected RestModel        model             = null;

    private final String       factsId           = "factId";
    private final String       idKey             = "_id";

    /**
     * Defacto config and example data loading.
     */
    @PostConstruct
    protected void init() {
        model = new RestModel();
        mongoTriples = MongoManager.getMongoManager().setCollection(collectionTriples);
        mongoResults = MongoManager.getMongoManager().setCollection(collectionResults);
        mongoFacts = MongoManager.getMongoManager().setCollection(collectionFacts);
    }

    /**
     * method: POST<br>
     * path: input/<br>
     *
     * @return json object
     */
    @RequestMapping(
            value = "/input",
            headers = "Accept=application/json",
            produces = "application/json",
            method = RequestMethod.POST)
    @ResponseBody
    public String input(@RequestBody String jsonObject, HttpServletResponse response) {
        LOG.info("input: " + jsonObject);
        // TODO: check input, clean
        JSONObject in = null;
        try {
            in = new JSONObject(jsonObject);

            DefactoModel defactoModel = null;
            Triple triple = null;

            JSONObject jo = null;
            String id = null;

            // fact is set if input is an example fact
            if (in.has("fact")) {
                for (FactBenchExample example : FactBenchExamplesLoader.loadExamples()) {
                    if (example.getFact().equals(in.getString("fact"))) {
                        LOG.info("Found example fact.");
                        defactoModel = example.getModel();
                        triple = example.getTriple();
                        break;
                    }
                }
            } else {
                // check db for result
                if (in.has("id")) {
                    id = in.getString("id");
                    jo = findResult(id);
                } else
                    LOG.info("Has not an id.");
            }

            // db results
            if (jo != null) {
                LOG.info("Found in db.");
                response.setStatus(HttpServletResponse.SC_OK);
                return jo.toString();

            } else if (defactoModel == null || triple == null) {
                LOG.info("A new fact.");
                // no example fact, so new input
                triple = new Triple(
                        NodeFactory.createURI(in.getString("s")),
                        NodeFactory.createURI(in.getString("p")),
                        NodeFactory.createURI(in.getString("o"))
                        );
                String from = in.has("from") && in.getString("from") != null ? in.getString("from") : "";
                String to = in.has("to") && in.getString("to") != null ? in.getString("to") : "";
                defactoModel = model.getModel(triple, from, to);
            }

            LOG.info("Calls defacto ...");
            final Calendar startTime = Calendar.getInstance();
            final Evidence evidence = Defacto.checkFact(defactoModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);
            final Calendar endTime = Calendar.getInstance();

            // TODO: download rdf option
            String rdf = EvidenceRDFGenerator.getProvenanceInformationAsString(triple, evidence, startTime, endTime, "TURTLE");
            InputStream is = new ByteArrayInputStream(rdf.getBytes());

            // output
            jo = model.out(evidence);
            if (id != null) {
                jo.put("id", id);
            }
            if (!in.has("fact"))
                insertResults(jo);

            response.setStatus(HttpServletResponse.SC_OK);
            return jo.toString();

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong input.");
                response.flushBuffer();
            } catch (IOException ee) {
                LOG.error(ee.getLocalizedMessage(), ee);
            }
        }
        return new JSONObject().toString();
    }

    /**
     * method: POST<br>
     * path: inputs/<br>
     * 
     * Adds an id to each object in the array and stores the objects in the DB.
     * 
     * @param triples
     *            with json objects
     * @param response
     * @return DB id
     */
    @RequestMapping(
            value = "/insert",
            headers = "Accept=application/json",
            produces = "application/json",
            method = RequestMethod.POST)
    @ResponseBody
    public String insert(@RequestBody String triples, HttpServletResponse response) {
        try {
            String id = insertTriples(new JSONArray(triples));
            response.setStatus(HttpServletResponse.SC_OK);
            response.flushBuffer();
            return new JSONObject().put(factsId, id).toString(2);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong input.\n" + e.getLocalizedMessage());
                response.flushBuffer();
            } catch (IOException ioe) {
                LOG.error(ioe.getLocalizedMessage(), ioe);
            }
            return new JSONObject().put("error", e.getLocalizedMessage()).toString(2);
        }
    }

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
            value = "/id/{id:^[0-9a-fA-F]{24}$}/input",
            produces = "application/json",
            method = RequestMethod.GET)
    @ResponseBody
    public String idinput(@PathVariable("id") String id, HttpServletResponse response) {
        LOG.info(factsId + " " + id);
        String res = "";
        try {
            res = inputs(getTriples(id).toString(), response);
            response.setStatus(HttpServletResponse.SC_OK);
            response.flushBuffer();
            return res;
        } catch (Exception e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong input.\n" + e.getLocalizedMessage());
                response.flushBuffer();
            } catch (IOException ioe) {
                LOG.error(ioe.getLocalizedMessage(), ioe);
            }
            LOG.error(e.getLocalizedMessage(), e);
        }
        return res;
    }

    /**
     * method: GET<br>
     * path: id/<br>
     * 
     * Reads database.
     * 
     * @param groupId
     *            ObjectId
     * @param response
     * 
     * @return JSON array with JONS objects with triples.
     */
    @RequestMapping(
            value = "/id/{id:^[0-9a-fA-F]{24}$}",
            produces = "application/json",
            method = RequestMethod.GET)
    @ResponseBody
    public String id(@PathVariable("id") String id, HttpServletResponse response) {
        LOG.info(factsId + " " + id);
        JSONArray returnArray = new JSONArray();
        try {
            returnArray = getTriples(id);
            response.setStatus(HttpServletResponse.SC_OK);
            response.flushBuffer();
        } catch (Exception e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong input.\n" + e.getLocalizedMessage());
                response.flushBuffer();
            } catch (IOException ioe) {
                LOG.error(ioe.getLocalizedMessage(), ioe);
            }
            LOG.error(e.getLocalizedMessage(), e);
        }
        return returnArray.toString();
    }

    /**
     * method: POST<br>
     * path: input/<br>
     * 
     * @param json
     *            array
     * @return json array of json objects
     */
    @RequestMapping(
            value = "/inputs",
            headers = "Accept=application/json",
            produces = "application/json",
            method = RequestMethod.POST)
    @ResponseBody
    public String inputs(@RequestBody String array, HttpServletResponse response) {
        // TODO: try catch
        JSONArray ja = new JSONArray(array);
        JSONArray returnArray = new JSONArray();

        // TODO: parallel
        for (int i = 0; i < ja.length(); i++) {
            returnArray.put(
                    new JSONObject(
                            input(ja.getJSONObject(i).toString(), response)
                    ));
        }
        return returnArray.toString();
    }

    /**
     * Finds for a facts id all triples in DB {@link #mongoFacts} and the
     * triples in DB {@link #mongoTriples}. Returns a JSON array with all
     * triples <code>{"s":"url","p":"url","o":"url"}</code>.
     * 
     * @param factsIdd
     * @return JSON array with all triples
     * @throws Exception
     */
    protected JSONArray getTriples(String factsIdd) throws Exception {
        JSONArray facts = new JSONArray();
        DBObject e = mongoFacts.findDocumentById(factsIdd);
        if (e != null) {

            JSONObject fact = new JSONObject(JSON.serialize(e));
            if (fact.has("triples")) {
                JSONArray tripleIds = fact.getJSONArray("triples");

                BsonArray ba = new BsonArray();
                for (int i = 0; i < tripleIds.length(); i++) {
                    ba.add(new BsonObjectId(new ObjectId(tripleIds.getString(i))));
                }

                // all triples for the given facts id
                Iterator<DBObject> iter = mongoTriples.findOperation(idKey, "$in", ba);
                while (iter.hasNext()) {
                    DBObject dbo = iter.next();

                    ObjectId _id = (ObjectId) dbo.get(idKey);
                    String subject = (String) dbo.get("s");
                    String predicate = (String) dbo.get("p");
                    String object = (String) dbo.get("o");

                    facts.put(new JSONObject()
                            // TODO: we need this(facts.html)?
                            .put(factsId, factsIdd)
                            .put("id", _id.toString())
                            .put("s", subject)
                            .put("p", predicate)
                            .put("o", object)
                            );
                }
            }
        }
        return facts;
    }

    /**
     * Stores triples in {@link #mongoTriples} if not exist. <br>
     * Stores the triples ids in {@link #mongoFacts}.
     * 
     * @param ja
     *            JSON array with triple objects
     *            <code>{"s":"url","p":"url","o":"url"}</code>.
     * @return _id of the facts entry
     */
    protected String insertTriples(JSONArray ja) {

        // String id = ObjectId.get().toString();
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);

            // find triples in db
            Iterator<DBObject> iter = mongoTriples.find(
                    new JSONObject()
                            .put("s", jo.getString("s"))
                            .put("p", jo.getString("p"))
                            .put("o", jo.getString("o"))
                            .toString()
                    );

            int c = 0;
            while (iter.hasNext()) {
                if (c == 0) {
                    DBObject o = iter.next();
                    ObjectId id = (ObjectId) o.get(idKey);
                    LOG.info("id: " + id);
                    ids.add(id.toString());
                    c++;
                } else
                    c++;
            }

            if (c > 1)
                LOG.warn("Multiple triples found!");

            // not found in db
            if (c == 0) {
                // insert triples
                LOG.info("insert triple: " + jo.toString(2));
                String id = mongoTriples
                        .insert(
                        jo.toString()
                        );

                ids.add(id);
            }
        }

        LOG.info("insert fact");
        JSONArray jaa = new JSONArray();
        ids.forEach(jaa::put);
        String id = mongoFacts.insert(new JSONObject()
                .put("triples", jaa)
                .toString());
        return id;
    }

    /**
     * Finds in DB {@link #mongoResults} objects with 'id' and the given id.
     * 
     * @param id
     * @return
     */
    protected JSONObject findResult(String id) {
        String key = "id";

        LOG.info("findResult: (" + key + ":" + id + ")");
        Iterator<DBObject> iter = mongoResults.find(new JSONObject().put(key, id).toString());
        int c = 0;
        String json = null;
        while (iter.hasNext()) {
            c++;
            if (json == null)
                json = JSON.serialize(iter.next());
        }
        if (c > 0) {
            if (c > 1)
                LOG.warn("More than one object in db with: (" + key + ":" + id + ")");
            return new JSONObject(json);
        }
        LOG.info("Not found in DB.");
        return null;
    }

    /**
     * Stores a JSON object in {@link #mongoResults}.
     * 
     * @param jo
     *            JSON object
     * @return _id key in db
     */
    protected String insertResults(JSONObject jo) {
        LOG.info("insertResults: " + jo.toString(2));
        return mongoResults.insert(jo.toString());
    }
}
