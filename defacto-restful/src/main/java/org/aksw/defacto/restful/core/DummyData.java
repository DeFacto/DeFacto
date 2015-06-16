package org.aksw.defacto.restful.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.aksw.defacto.model.DefactoModel;
import org.apache.jena.riot.Lang;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class DummyData {

    static RestModel rm = new RestModel();

    public static void main(String[] a) {
        DefactoModel dm;

        dm = DummyData.getTestModel();
        dm.model.write(System.out, Lang.TURTLE.getName());

        dm = DummyData.getTestModel2();
        dm.model.write(System.out, Lang.TURTLE.getName());

        dm = DummyData.getEinsteinModel();
        dm.model.write(System.out, Lang.TURTLE.getName());
    }

    public static Triple getDummyTriple() {
        return new Triple(
                NodeFactory.createURI("http://dbpedia.org/resource/Albert_Einstein"),
                NodeFactory.createURI("http://dbpedia.org/ontology/award"),
                NodeFactory.createURI("http://dbpedia.org/resource/Nobel_Prize_in_Physics"));
    }

    public static DefactoModel getEinsteinModel() {
        Model model = ModelFactory.createDefaultModel();
        model.read(DefactoModel.class.getClassLoader().getResourceAsStream("Einstein.ttl"), null, "TURTLE");
        return new DefactoModel(model, "Einstein Model", true, Arrays.asList("en", "fr", "de"));
    }

    public static DefactoModel getTestModel() {
        return rm.getModel(getDummyTriple(), "1910", "1930");
    }

    public static DefactoModel getTestModel2() {
        // -----------------------------------------------------------
        // input
        String from = "1910";
        String to = "1930";

        String subjectURI = "http://dbpedia.org/resource/Albert_Einstein";
        String blankProperty = "http://dbpedia.org/ontology/award";
        String objectURI = "http://dbpedia.org/resource/Nobel_Prize_in_Physics";

        Map<String, String> objectLabels = new HashMap<String, String>();
        objectLabels.put("en", "Nobel Prize in Physics");
        objectLabels.put("de", "Nobelpreis für Physik");
        objectLabels.put("fr", "Prix Nobel de physique");

        Map<String, String> subjectLabels = new HashMap<String, String>();
        subjectLabels.put("en", "Albert Einstein");
        subjectLabels.put("de", "Albert Einstein");
        subjectLabels.put("fr", "Albert Einstein");

        // -----------------------------------------------------------
        return rm.getDefactoModel(
                rm.getModel(objectURI, subjectURI, objectLabels, subjectLabels, blankProperty, from, to),
                objectLabels.keySet());
    }

    public static JSONObject getInput() {

        String s = "http://wikidata.dbpedia.org/resource/Q1000008";
        String p = "http://dbpedia.org/ontology/timeZone";
        String o = "http://wikidata.dbpedia.org/resource/Q25989";

        return new JSONObject()
                .put("s", s)
                .put("p", p)
                .put("o", o)
                .put(s, new JSONObject()
                        .put("accept", 1)
                        .put("accepted", new JSONArray()
                                .put(new JSONObject()
                                        .put("language", "de")
                                        .put("value", "http://wikidata.dbpedia.org/resource/Q985")
                                        .put("datatype", "")
                                        .put("context", "http://de.wikipedia.org/wiki/Florian_Eichinger?oldid=131471908#section=Weblinks&relative-line=30&absolute-line=50")
                                        .put("dataset", "mappingbased-properties"))
                                .put(new JSONObject()
                                        .put("language", "en")
                                        .put("value", "http://wikidata.dbpedia.org/resource/Q25989")
                                        .put("datatype", "")
                                        .put("context", "http://de.wikipedia.org/wiki/Florian_Eichinger?oldid=131471908#section=Weblinks&relative-line=30&absolute-line=50")
                                        .put("dataset", "mappingbased-properties"))
                        )
                        .put("others", new JSONArray())
                );
    }
}
