package org.aksw.defacto.restful.webservice;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.restful.core.DummyData;
import org.aksw.defacto.restful.core.RestModel;
import org.aksw.defacto.restful.mongo.MongoDB;
import org.aksw.defacto.restful.utils.Cfg;
import org.aksw.defacto.util.FactBenchExample;
import org.aksw.defacto.util.FactBenchExamplesLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.ini4j.Ini;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ComparisonChain;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.mongodb.DBObject;

/**
 * 
 * @author rspeck
 *
 */
@RestController
@RequestMapping("/fusion")
public class Controller {
    static {
        PropertyConfigurator.configure(Cfg.LOG_FILE);
    }
    public static Logger LOG       = LogManager.getLogger(Controller.class);

    protected MongoDB    db        = null;
    protected JSONArray  examples  = null;
    protected RestModel  restModel = null;

    /**
     * 
     */
    @PostConstruct
    protected void init() {
        try {
            Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(Defacto.class.getClassLoader().getResourceAsStream("defacto.ini")));
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        db = new MongoDB();
        restModel = new RestModel();

        examples = new JSONArray();
        for (FactBenchExample example : FactBenchExamplesLoader.loadExamples()) {
            examples.put(
                    new JSONObject()
                            .put("fact", example.getFact())
                            .put("s", example.getTriple().getSubject().getURI())
                            .put("p", example.getTriple().getPredicate().getURI())
                            .put("o", example.getTriple().getObject().getURI())
                    );
        }
    }

    /**
     * service path: examples/<br>
     * 
     * Example facts and triples.
     * 
     * @return json array
     * 
     */
    @RequestMapping(
            value = "/examples",
            headers = "Accept=application/json",
            produces = "application/json",
            method = RequestMethod.GET
            )
            public @ResponseBody String examples() {
        return examples.toString();
    }

    /**
     * service path: exampleinput/<br>
     * 
     * @return json object
     */
    @RequestMapping(
            value = "/exampleinput",
            headers = "Accept=application/json",
            produces = "application/json",
            method = RequestMethod.GET)
    @ResponseBody
    String inputExample(HttpServletResponse response) {
        Triple t = DummyData.getDummyTriple();
        return input(new JSONObject()
                .put("s", t.getSubject().getURI().toString())
                .put("p", t.getPredicate().getURI().toString())
                .put("o", t.getObject().getURI().toString())
                .put("from", "1910")
                .put("to", "1930")
                .toString(), response);
    }

    /**
     * 
     * service path: input/<br>
     * 
     * @param s
     * @param p
     * @param o
     * @param from
     * @param to
     * @return json object
     */
    @RequestMapping(
            value = "/input",
            headers = "Accept=application/json",
            produces = "application/json",
            method = RequestMethod.POST)
    @ResponseBody
    String input(
            @RequestBody String json, HttpServletResponse response
            ) {

        JSONObject in = null;
        try {
            in = new JSONObject(json);
            // TODO
            // checkinput
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong input.");
                response.flushBuffer();
            } catch (IOException ee) {
                LOG.error(ee.getLocalizedMessage(), ee);
            }
            return new JSONObject().toString();
        }

        DefactoModel defactoModel = null;

        // fact is set if input is an example
        if (in.has("fact")) {
            for (FactBenchExample example : FactBenchExamplesLoader.loadExamples()) {
                if (example.getFact().equals(in.getString("fact"))) {
                    defactoModel = example.getModel();
                    break;
                }
            }
        }

        // user input
        if (defactoModel == null) {
            defactoModel = restModel
                    .getModel(
                            new Triple(
                                    NodeFactory.createURI(in.getString("s")),
                                    NodeFactory.createURI(in.getString("p")),
                                    NodeFactory.createURI(in.getString("o"))
                            ),
                            in.getString("from") == null ? "" : in.getString("from"),
                            in.getString("to") == null ? "" : in.getString("to")
                    );
        }
        // call of DeFacto
        //
        final Calendar startTime = Calendar.getInstance();
        LOG.info(startTime);
        final Evidence evidence = Defacto.checkFact(defactoModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);
        final Calendar endTime = Calendar.getInstance();
        LOG.info(endTime);

        // download:
        // TODO
        //

        // output:
        response.setStatus(HttpServletResponse.SC_OK);
        return out(evidence);
    }

    protected String out(Evidence evidence) {
        // sort websites bei defacto score
        List<WebSite> webSites = evidence.getAllWebSites();
        Collections.sort(webSites, new Comparator<WebSite>() {
            @Override
            public int compare(WebSite o1, WebSite o2) {
                return ComparisonChain.start()
                        .compare(o2.getScore(), o1.getScore())
                        .compare(o1.getTitle(), o2.getTitle())
                        .result();
            }
        });

        JSONArray jaWebSites = new JSONArray();
        for (final WebSite website : webSites) {
            JSONArray jaProofs = new JSONArray();
            int cnt = 1;
            List<Pattern> boaPatterns = evidence.getBoaPatterns();
            for (ComplexProof proof : evidence.getComplexProofs(website)) {

                for (Pattern pattern : boaPatterns) {
                    if (!pattern.getNormalized().trim().isEmpty() && proof.getProofPhrase().toLowerCase().contains(pattern.getNormalized().toLowerCase())) {
                        break;
                    }
                }
                if (!proof.getTinyContext().contains("http:") && !proof.getTinyContext().contains("ftp:")) {
                    JSONArray jaLabels = new JSONArray();
                    Set<String> labels = new HashSet<String>();
                    labels.add(website.getQuery().getSubjectLabel());
                    labels.add(website.getQuery().getObjectLabel());
                    for (String label : labels)
                        jaLabels.put(label);

                    jaProofs.put(new JSONObject()
                            .put("label", cnt++)
                            .put("tinyContext", proof.getTinyContext())
                            .put("keywords", jaLabels)
                            );
                } else {
                    LOG.warn("TINY:" + proof.getTinyContext());
                }
            }// all proofs

            jaWebSites.put(new JSONObject()
                    .put("url", website.getUrl())
                    .put("title", website.getTitle())
                    .put("proofs", jaProofs)
                    .put("coverage", website.getTopicCoverageScore())
                    .put("search", website.getTopicMajoritySearchFeature())
                    .put("web", website.getTopicMajorityWebFeature())
                    .put("score", website.getScore())
                    );

        } // all weSites
        return new JSONObject()
                .put("score", DecimalFormat.getPercentInstance(Locale.ENGLISH).format(evidence.getDeFactoScore()))
                .put("from", evidence.defactoTimePeriod == null ? "" : evidence.defactoTimePeriod.getFrom())
                .put("to", evidence.defactoTimePeriod == null ? "" : evidence.defactoTimePeriod.getTo())
                .put("websitesSize", evidence.getAllWebSites().size())
                .put("websites", jaWebSites)
                .toString();

    }

    // -- MONGO
    protected void insert(JSONObject jo) {
        db.insert(jo.toString());
    }

    protected Iterator<DBObject> search(JSONObject jo) {
        return db.search(jo.toString());
    }
}
