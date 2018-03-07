package org.aksw.defacto.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.rest.utils.Cfg;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.Ini;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.ComparisonChain;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 *
 * @author rspeck
 *
 */
public class RestModel {

    public static Logger LOG = null;
    public static Set<String> lang = null;

    // sets langs
    static {
        //PropertyConfigurator.configure(Cfg.LOG_FILE);
        LOG = LogManager.getLogger(RestModel.class);
        try {
            Defacto.DEFACTO_CONFIG = new DefactoConfig(
                    new Ini(Defacto.class.getClassLoader().getResourceAsStream("defacto.ini")));
            lang = new HashSet<>(
                    Arrays.asList(Defacto.DEFACTO_CONFIG.getStringSetting("boa", "languages").split(",")));
        } catch (final IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        if (lang == null) {
            lang = new HashSet<>(Arrays.asList(new String[] {"en", "de", "fr"}));
        }
    }

    private final Date date = new Date();

    /**
     *
     * @param triple
     * @param from
     * @param to
     * @return
     */
    public DefactoModel getModel(final Triple triple, final String from, final String to) {

        final String subjectURI = triple.getSubject().getURI().toString();
        final String blankProperty = triple.getPredicate().getURI().toString();
        final String objectURI = triple.getObject().getURI().toString();

        // create labels
        final Set<String> set = new HashSet<>();
        set.add(subjectURI);
        set.add(blankProperty);
        set.add(objectURI);

        final Model langModel = generateModel(set, lang);

        return getDefactoModel(getModel(langModel, objectURI, subjectURI, blankProperty, from, to),
                lang);
    }

    /**
     *
     * @param model
     * @param lang
     * @return
     */
    public DefactoModel getDefactoModel(final Model model, final Set<String> lang) {
        return getDefactoModel(model, "DefactoModel", false, lang);
    }

    /**
     *
     * @param model
     * @param modelName
     * @param isCorrect
     * @param lang
     * @return
     */
    public DefactoModel getDefactoModel(final Model model, final String modelName,
                                        final boolean isCorrect, final Set<String> lang) {
        final DefactoModel defactoModel =
                new DefactoModel(model, modelName, isCorrect, new ArrayList<String>(lang));
        return defactoModel;
    }

    /**
     *
     * @param model
     * @param objectURI
     * @param subjectURI
     * @param blankProperty
     * @param from
     * @param to
     * @return
     */
    public Model getModel(final Model model, final String objectURI, final String subjectURI,
                          final String blankProperty, final String from, final String to) {
        final Map<String, String> empty = new HashMap<>();
        return getModel(model, objectURI, subjectURI, empty, empty, blankProperty, from, to);
    }

    /**
     *
     *
     * @param objectURI
     * @param subjectURI
     * @param objectLabels
     * @param subjectLabels
     * @param blankProperty
     * @param from
     * @param to
     * @return
     */
    public Model getModel(final String objectURI, final String subjectURI,
                          final Map<String, String> objectLabels, final Map<String, String> subjectLabels,
                          final String blankProperty, final String from, final String to) {
        return getModel(null, objectURI, subjectURI, objectLabels, subjectLabels, blankProperty, from,
                to);
    }

    /**
     *
     * @param uris
     * @return
     */
    protected Model generateModel(final Set<String> uris, final Set<String> langs) {
        final String rdfs_label = "http://www.w3.org/2000/01/rdf-schema#label";

        final QueryExecutionFactory qef =
                new QueryExecutionFactoryHttp(SparqlEndpoint.getEndpointDBpedia().getURL().toString(),
                        SparqlEndpoint.getEndpointDBpedia().getDefaultGraphURIs());

        final Model model = ModelFactory.createDefaultModel();
        for (final String uri : uris) {
            String query = "CONSTRUCT {<" + uri + "> <" + rdfs_label + "> ?o} WHERE {<" + uri + "> <"
                    + rdfs_label + "> ?o.";
            if (lang.size() > 0) {
                query += "FILTER(";

                int i = 0;
                for (final String lang : langs) {
                    query += "LANGMATCHES(LANG(?o),'" + lang + "')";
                    if (i++ < (langs.size() - 1)) {
                        query += " || ";
                    }
                }
                query += ")";
            }
            query += "}";

            final QueryExecution qe = qef.createQueryExecution(query);
            qe.execConstruct(model);
            qe.close();
        }
        return model;
    }

    /**
     *
     * @param model
     * @param objectURI
     * @param subjectURI
     * @param objectLabels
     * @param subjectLabels
     * @param blankProperty
     * @param from
     * @param to
     * @return
     */
    protected Model getModel(Model model, final String objectURI, final String subjectURI,
                             final Map<String, String> objectLabels, final Map<String, String> subjectLabels,
                             final String blankProperty, final String from, final String to) {

        final String time = Long.toString(date.getTime());
        final String subjectProperty = "http://dbpedia.org/ontology/tmp".concat("_").concat(time);
        final String blankURI = subjectURI.concat("__").concat(time);

        // create model
        if (model == null) {
            model = ModelFactory.createDefaultModel();
        }
        // object
        final Resource objectRes = model.createResource(objectURI);
        for (final Entry<String, String> entry : objectLabels.entrySet()) {
            objectRes.addProperty(Constants.RDFS_LABEL,
                    model.createLiteral(entry.getValue(), entry.getKey()));
        }

        // blank node
        final Resource blank = model.createResource(blankURI);
        blank.addProperty(Constants.DEFACTO_FROM,
                model.createTypedLiteral(from, NodeFactory.getType(from)));
        blank.addProperty(Constants.DEFACTO_TO, model.createTypedLiteral(to, NodeFactory.getType(to)));
        blank.addProperty(model.createProperty(blankProperty), objectRes.asResource());

        // subject
        final Resource subjectRes = model.createResource(subjectURI);
        for (final Entry<String, String> entry : subjectLabels.entrySet()) {
            subjectRes.addProperty(Constants.RDFS_LABEL,
                    model.createLiteral(entry.getValue(), entry.getKey()));
        }
        subjectRes.addProperty(model.createProperty(subjectProperty), blank.asResource());

        return model;
    }

    public JSONObject out(final Evidence evidence) throws Exception{

        // sort websites bei defacto score
        final List<WebSite> webSites = evidence.getAllWebSites();
        Collections.sort(webSites, (o1, o2) -> ComparisonChain.start()
                .compare(o2.getScore(), o1.getScore()).compare(o1.getTitle(), o2.getTitle()).result());

        final JSONArray jaWebSites = new JSONArray();
        for (final WebSite website : webSites) {
            final JSONArray jaProofs = new JSONArray();
            int cnt = 1;
            final List<Pattern> boaPatterns = evidence.getBoaPatterns();
            for (final ComplexProof proof : evidence.getComplexProofs(website)) {

                for (final Pattern pattern : boaPatterns) {
                    if (!pattern.getNormalized().trim().isEmpty() && proof.getProofPhrase().toLowerCase()
                            .contains(pattern.getNormalized().toLowerCase())) {
                        break;
                    }
                }
                if (!proof.getTinyContext().contains("http:") && !proof.getTinyContext().contains("ftp:")) {
                    final JSONArray jaLabels = new JSONArray();
                    final Set<String> labels = new HashSet<String>();
                    labels.add(website.getQuery().getSubjectLabel());
                    labels.add(website.getQuery().getObjectLabel());
                    for (final String label : labels) {
                        jaLabels.put(label);
                    }

                    jaProofs.put(new JSONObject().put("label", cnt++)
                            .put("tinyContext", proof.getTinyContext()).put("keywords", jaLabels));
                } else {
                    LOG.warn("TINY:" + proof.getTinyContext());
                }
            } // all proofs

            jaWebSites.put(new JSONObject().put("url", website.getUrl()).put("title", website.getTitle())
                    .put("proofs", jaProofs).put("coverage", website.getTopicCoverageScore())
                    .put("search", website.getTopicMajoritySearchFeature())
                    .put("web", website.getTopicMajorityWebFeature()).put("score", website.getScore()));

        } // all webSites

        return new JSONObject().put("maxScore", 1)

                .put("sumCoverage",
                        evidence.getFeatures().value(AbstractEvidenceFeature.TOPIC_COVERAGE_SUM))
                .put("maxCoverage",
                        evidence.getFeatures().value(AbstractEvidenceFeature.TOPIC_COVERAGE_MAX))

                .put("sumSearch",
                        evidence.getFeatures().value(AbstractEvidenceFeature.TOPIC_MAJORITY_SEARCH_RESULT_SUM))
                .put("maxSearch",
                        evidence.getFeatures().value(AbstractEvidenceFeature.TOPIC_MAJORITY_SEARCH_RESULT_MAX))

                .put("sumWeb", evidence.getFeatures().value(AbstractEvidenceFeature.TOPIC_MAJORITY_WEB_SUM))
                .put("maxWeb", evidence.getFeatures().value(AbstractEvidenceFeature.TOPIC_MAJORITY_WEB_MAX))

                .put("sl", evidence.getModel().getSubject().getLabels().iterator().next())
                .put("pl", evidence.getModel().getPredicate().getLocalName())
                .put("ol", evidence.getModel().getObject().getLabels().iterator().next())

                .put("s", evidence.getModel().getSubject().getUri())
                .put("p", evidence.getModel().getPredicate().getURI())
                .put("o", evidence.getModel().getObject().getUri())

                .put("score", evidence.getDeFactoScore())
                .put("from", evidence.defactoTimePeriod == null ? "" : evidence.defactoTimePeriod.getFrom())
                .put("to", evidence.defactoTimePeriod == null ? "" : evidence.defactoTimePeriod.getTo())
                .put("websitesSize", evidence.getAllWebSites().size()).put("websites", jaWebSites);
    }
}
