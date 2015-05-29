package org.aksw.defacto.util;

import java.io.StringWriter;

import org.aksw.defacto.evidence.WebSite;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Generates the provenance information according to W3C draft described in http://www.w3.org/TR/prov-primer/
 */
public class ProvenanceInformationGenerator {

    private static final String PROV_CLASS_ENTITY = "http://www.w3.org/ns/prov#Entity";
    private static final String DUBLIN_CORE_PROPERTY_TITLE = "http://purl.org/dc/terms/title";
    private static final String PROV_PROPERTY_HAD_ORIGINAL_SOURCE = "http://www.w3.org/ns/prov#hadOriginalSource"; //Domain and range of type Entity
    private static final String PROV_PROPERTY_WAS_DERIVED_FROM = "http://www.w3.org/ns/prov#wasDerivedFrom";//Domain and range of type Entity
    private static final String RDF_PROPERTY_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    private static final String OWL_CLASS_AXIOM = "http://www.w3.org/2002/07/owl#Axiom";
    private static final String OWL_PROPERTY_ANNOTATED_SOURCE = "http://www.w3.org/2002/07/owl#annotatedSource";
    private static final String OWL_PROPERTY_ANNOTATED_PROPERTY = "http://www.w3.org/2002/07/owl#annotatedProperty";
    private static final String OWL_PROPERTY_ANNOTATED_TARGET = "http://www.w3.org/2002/07/owl#annotatedTarget";

    private static final String PROV_NAMESPACE = "http://www.w3.org/ns/prov#";
    private static final String DUBLIN_CORE_NAMESPACE = "http://purl.org/dc/terms/";
    private static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String OWL_NAMESPACE = "http://www.w3.org/2002/07/owl#";

    private static final String DBPEDIA_NAMESPACE = "http://dbpedia.org/resource/";
    private static final String DBPEDIA_ONTOLOGY_NAMESPACE = "http://dbpedia.org/ontology/";
    
    static HashFunction hf = Hashing.md5();

    /**
     * Generates the provenance data for the passed website
     * @param requiredWebsite   The website
     * @param subject   The subject
     * @param object    The object
     * @return  The provenance model
     */
    public static Model generateProvenanceInformation(WebSite requiredWebsite, String subject, String predicate, String object){
    	HashCode hc = hf.newHasher()
    		       .putString(subject, Charsets.UTF_8)
    		       .putString(predicate, Charsets.UTF_8)
    		       .putString(object, Charsets.UTF_8)
    		       .hash();
        String proposedTripleURI = "http://defacto.aksw.org/triple" + hc.toString();

        Model outputModel = ModelFactory.createDefaultModel();

        //Preparing the types of entities, define all of the as of type Entity
        outputModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(proposedTripleURI),
                ResourceFactory.createProperty(RDF_PROPERTY_TYPE),
                outputModel.createResource(PROV_CLASS_ENTITY)));

        outputModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(subject),
                ResourceFactory.createProperty(RDF_PROPERTY_TYPE),
                outputModel.createResource(PROV_CLASS_ENTITY)));

        outputModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(object),
                ResourceFactory.createProperty(RDF_PROPERTY_TYPE),
                outputModel.createResource(PROV_CLASS_ENTITY)));

        //defining the information about the article itself
        outputModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(requiredWebsite.getUrl().trim()),
                ResourceFactory.createProperty(RDF_PROPERTY_TYPE),
                outputModel.createResource(PROV_CLASS_ENTITY)));

        outputModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(requiredWebsite.getUrl().trim()),
                ResourceFactory.createProperty(DUBLIN_CORE_PROPERTY_TITLE),
                outputModel.createLiteral(requiredWebsite.getTitle())));

        //Defining annotations
        outputModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(proposedTripleURI),
                ResourceFactory.createProperty(RDF_PROPERTY_TYPE),
                outputModel.createResource(OWL_CLASS_AXIOM)));

        outputModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(proposedTripleURI),
                ResourceFactory.createProperty(OWL_PROPERTY_ANNOTATED_SOURCE),
                outputModel.createResource(subject)));


        outputModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(proposedTripleURI),
                ResourceFactory.createProperty(OWL_PROPERTY_ANNOTATED_PROPERTY),
                outputModel.createResource(predicate)));


        outputModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(proposedTripleURI),
                ResourceFactory.createProperty(OWL_PROPERTY_ANNOTATED_TARGET),
                outputModel.createResource(object)));



        //Defining the triple has original source referring to the website itself
        outputModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(proposedTripleURI),
                ResourceFactory.createProperty(PROV_PROPERTY_HAD_ORIGINAL_SOURCE),
                outputModel.createResource(requiredWebsite.getUrl().trim())));


        //Defining the triple as derived from the entities subject and object
        outputModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(proposedTripleURI),
                ResourceFactory.createProperty(PROV_PROPERTY_WAS_DERIVED_FROM),
                outputModel.createResource(subject)));

        outputModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(proposedTripleURI),
                ResourceFactory.createProperty(PROV_PROPERTY_WAS_DERIVED_FROM),
                outputModel.createResource(object)));


        return outputModel;
    }

    /**
     * Generates the provenance data for the passed website and returns it as string for display
     * @param requiredWebsite   The website
     * @param subject   The subject
     * @param predicate The predicate
     * @param object    The object
     * @param syntax    The required syntax format e.g. N-TRIPlE
     * @return  The provenance model
     */
    public static String getProvenanceInformationAsString(WebSite requiredWebsite, String subject, String predicate,
                                                          String object, String syntax){

        Model resultsModel = generateProvenanceInformation(requiredWebsite, subject, predicate, object);

        StringWriter out = new StringWriter();

        resultsModel.setNsPrefix("dbpedia", DBPEDIA_NAMESPACE);
        resultsModel.setNsPrefix("dbpediaowl", DBPEDIA_ONTOLOGY_NAMESPACE);
        resultsModel.setNsPrefix("dcterms", DUBLIN_CORE_NAMESPACE);
        resultsModel.setNsPrefix("rdf", RDF_NAMESPACE);
        resultsModel.setNsPrefix("prov", PROV_NAMESPACE);
        resultsModel.setNsPrefix("owl", OWL_NAMESPACE);

        resultsModel.write(out, syntax);
        return out.toString();
//        return outputProvenanceString;
    }

}
