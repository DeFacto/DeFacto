package org.aksw.defacto.restful.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.restful.utils.Cfg;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.Ini;

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

    public static Logger      LOG  = null;
    public static Set<String> lang = null;

    // sets langs
    static {
        PropertyConfigurator.configure(Cfg.LOG_FILE);
        LOG = LogManager.getLogger(RestModel.class);
        try {
            Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(Defacto.class.getClassLoader().getResourceAsStream("defacto.ini")));
            lang = new HashSet<>(Arrays.asList(Defacto.DEFACTO_CONFIG.getStringSetting("boa", "languages").split(",")));
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        if (lang == null) {
            lang = new HashSet<>(Arrays.asList(new String[] { "en" }));
        }
    }

    private Date              date = new Date();

    /**
     * 
     * @param triple
     * @param from
     * @param to
     * @return
     */
    public DefactoModel getModel(Triple triple, String from, String to) {

        String subjectURI = triple.getSubject().getURI().toString();
        String blankProperty = triple.getPredicate().getURI().toString();
        String objectURI = triple.getObject().getURI().toString();

        // create labels
        Set<String> set = new HashSet<>();
        set.add(subjectURI);
        set.add(blankProperty);
        set.add(objectURI);

        Model langModel = generateModel(set, lang);

        return getDefactoModel(
                getModel(langModel, objectURI, subjectURI, blankProperty, from, to),
                lang);
    }

    /**
     * 
     * @param model
     * @param lang
     * @return
     */
    public DefactoModel getDefactoModel(Model model, Set<String> lang) {
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
    public DefactoModel getDefactoModel(Model model, String modelName, boolean isCorrect, Set<String> lang) {
        DefactoModel defactoModel = new DefactoModel(model, modelName, isCorrect, new ArrayList<String>(lang));
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
    public Model getModel(Model model,
            String objectURI, String subjectURI,
            String blankProperty,
            String from, String to
            ) {
        Map<String, String> empty = new HashMap<>();
        return getModel(model, objectURI, subjectURI, empty, empty, blankProperty, from, to);
    }

    /**
     * 
     * 
     * @param objectURI
     * @param subjectURI
     * @param blankURI
     * @param objectLabels
     * @param subjectLabels
     * @param subjectProperty
     * @param blankProperty
     * @param from
     * @param to
     * @return
     */
    public Model getModel(
            String objectURI, String subjectURI, Map<String, String> objectLabels, Map<String, String> subjectLabels,
            String blankProperty, String from, String to
            ) {
        return getModel(
                null, objectURI, subjectURI, objectLabels, subjectLabels,
                blankProperty, from, to);
    }

    /**
     * 
     * @param uris
     * @param languages
     * @return
     */
    protected Model generateModel(Set<String> uris, Set<String> langs) {
        String rdfs_label = "http://www.w3.org/2000/01/rdf-schema#label";

        QueryExecutionFactory qef = new QueryExecutionFactoryHttp(
                SparqlEndpoint.getEndpointDBpedia().getURL().toString(),
                SparqlEndpoint.getEndpointDBpedia().getDefaultGraphURIs()
                );

        Model model = ModelFactory.createDefaultModel();
        for (String uri : uris) {
            String query = "CONSTRUCT {<" + uri + "> <" + rdfs_label + "> ?o} WHERE {<" + uri + "> <" + rdfs_label + "> ?o.";
            if (lang.size() > 0) {
                query += "FILTER(";

                int i = 0;
                for (String lang : langs) {
                    query += "LANGMATCHES(LANG(?o),'" + lang + "')";
                    if (i++ < langs.size() - 1)
                        query += " || ";
                }
                query += ")";
            }
            query += "}";

            QueryExecution qe = qef.createQueryExecution(query);
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
    protected Model getModel(Model model,
            String objectURI, String subjectURI,
            Map<String, String> objectLabels, Map<String, String> subjectLabels,
            String blankProperty,
            String from, String to
            ) {

        String time = Long.toString(date.getTime());
        String subjectProperty = "http://dbpedia.org/ontology/tmp".concat("_").concat(time);
        String blankURI = subjectURI.concat("__").concat(time);

        // create model
        if (model == null) {
            model = ModelFactory.createDefaultModel();
        }
        // object
        Resource objectRes = model.createResource(objectURI);
        for (Entry<String, String> entry : objectLabels.entrySet()) {
            objectRes.addProperty(Constants.RDFS_LABEL, model.createLiteral(entry.getValue(), entry.getKey()));
        }

        // blank node
        Resource blank = model.createResource(blankURI);
        blank.addProperty(Constants.DEFACTO_FROM, model.createTypedLiteral(from, NodeFactory.getType(from)));
        blank.addProperty(Constants.DEFACTO_TO, model.createTypedLiteral(to, NodeFactory.getType(to)));
        blank.addProperty(model.createProperty(blankProperty), objectRes.asResource());

        // subject
        Resource subjectRes = model.createResource(subjectURI);
        for (Entry<String, String> entry : subjectLabels.entrySet()) {
            subjectRes.addProperty(Constants.RDFS_LABEL, model.createLiteral(entry.getValue(), entry.getKey()));
        }
        subjectRes.addProperty(model.createProperty(subjectProperty), blank.asResource());

        return model;
    }
}
