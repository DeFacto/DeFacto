package org.aksw.defacto.restful.webservice;

import javax.annotation.PostConstruct;

import org.aksw.defacto.restful.core.DummyData;
import org.aksw.defacto.restful.utils.Cfg;
import org.aksw.defacto.restful.utils.SupportedRelations;
import org.aksw.defacto.util.FactBenchExample;
import org.aksw.defacto.util.FactBenchExamplesLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.graph.Triple;

/**
 * 
 * @author rspeck
 *
 */
@RestController
@RequestMapping("/examples")
public class Examples {
    static {
        PropertyConfigurator.configure(Cfg.LOG_FILE);
    }
    public static Logger LOG      = LogManager.getLogger(Examples.class);

    protected JSONArray  examples = new JSONArray();

    /**
     * Defacto config and example data loading.
     */
    @PostConstruct
    protected void init() {
        for (FactBenchExample example : FactBenchExamplesLoader.loadExamples())
            examples.put(
                    new JSONObject()
                            .put("fact", example.getFact())
                            .put("s", example.getTriple().getSubject().getURI())
                            .put("p", example.getTriple().getPredicate().getURI())
                            .put("o", example.getTriple().getObject().getURI()));
    }

    @RequestMapping(
            value = "/supportedrelations",
            headers = "Accept=application/json",
            produces = "application/json",
            method = RequestMethod.GET)
    @ResponseBody
    public String supportedRelation() {
        return SupportedRelations.getSupportedRelation();
    }

    /**
     * method: GET<br>
     * path: examples/<br>
     * 
     * Example facts and triples.
     * 
     * @return json array
     * 
     */
    @RequestMapping(
            value = "/all",
            headers = "Accept=application/json",
            produces = "application/json",
            method = RequestMethod.GET)
    @ResponseBody
    public String examples() {
        return examples.toString();
    }

    /**
     * method: GET<br>
     * path: exampleinput/<br>
     * 
     * @return json object
     */
    @RequestMapping(
            value = "/triple",
            headers = "Accept=application/json",
            produces = "application/json",
            method = RequestMethod.GET)
    @ResponseBody
    public String exampleinput() {
        Triple t = DummyData.getDummyTriple();
        return new JSONObject()
                .put("s", t.getSubject().getURI().toString())
                .put("p", t.getPredicate().getURI().toString())
                .put("o", t.getObject().getURI().toString())
                .put("from", "1910")
                .put("to", "1930")
                .toString();
    }

    /**
     * method: GET<br>
     * path: exampleinputs/<br>
     * 
     * @return json object
     */
    @RequestMapping(
            value = "/triples",
            headers = "Accept=application/json",
            produces = "application/json",
            method = RequestMethod.GET)
    @ResponseBody
    public String exampleinputs() {
        return new JSONArray()
                .put(new JSONObject()
                        .put("s", "http://dbpedia.org/resource/Albert_Einstein")
                        .put("p", "http://dbpedia.org/ontology/award")
                        .put("o", "http://dbpedia.org/resource/Nobel_Prize_in_Physics"))
                .put(new JSONObject()
                        .put("s", "http://dbpedia.org/resource/Albert_Einstein")
                        .put("p", "http://dbpedia.org/ontology/award")
                        .put("o", "http://dbpedia.org/resource/AFL_Rising_Star"))
                .put(new JSONObject()
                        .put("s", "http://dbpedia.org/resource/Albert_Einstein")
                        .put("p", "http://dbpedia.org/ontology/award")
                        .put("o", "http://dbpedia.org/resource/Academy_Award_for_Best_Original_Song"))
                .put(new JSONObject()
                        .put("s", "http://dbpedia.org/resource/Albert_Einstein")
                        .put("p", "http://dbpedia.org/ontology/award")
                        .put("o", "http://dbpedia.org/resource/World_Food_Prize"))

                .toString();
    }
}
