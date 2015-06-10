package org.aksw.defacto.restful.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.defacto.Constants;
import org.aksw.defacto.model.DefactoModel;
import org.apache.jena.riot.Lang;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * 
 * @author rspeck
 *
 */
public class RestModel {

    public DefactoModel getTestModel() {
        // -----------------------------------------------------------
        // input
        String from = "1910";
        String to = "1930";

        String objectURI = "http://dbpedia.org/resource/Nobel_Prize_in_Physics";
        Map<String, String> objectLabels = new HashMap<String, String>();
        objectLabels.put("en", "Nobel Prize in Physics");
        objectLabels.put("de", "Nobelpreis für Physik");
        objectLabels.put("fr", "Prix Nobel de physique");

        String subjectURI = "http://dbpedia.org/resource/Albert_Einstein";
        String subjectProperty = "http://dbpedia.org/ontology/placeholder";
        Map<String, String> subjectLabels = new HashMap<String, String>();
        subjectLabels.put("en", "Albert Einstein");
        subjectLabels.put("de", "Albert Einstein");
        subjectLabels.put("fr", "Albert Einstein");

        String blankURI = subjectURI.concat("__123");
        String blankProperty = "http://dbpedia.org/ontology/award";
        // -----------------------------------------------------------

        return getDefactoModel(
                getModel(objectURI, subjectURI, blankURI, objectLabels, subjectLabels, subjectProperty, blankProperty, from, to),
                objectLabels.keySet());
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
            String objectURI, String subjectURI, String blankURI,
            Map<String, String> objectLabels, Map<String, String> subjectLabels,
            String subjectProperty, String blankProperty,
            String from, String to
            ) {

        // create model
        Model model = ModelFactory.createDefaultModel();

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

        model.write(System.out, Lang.TURTLE.getName());

        return model;
    }
}
