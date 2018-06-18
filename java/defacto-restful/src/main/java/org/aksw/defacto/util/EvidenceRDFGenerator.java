/**
 * 
 */
package org.aksw.defacto.util;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.nlp2rdf.core.Span;
import org.nlp2rdf.core.Text2RDF;
import org.nlp2rdf.core.urischemes.ContextHashBasedString;
import org.nlp2rdf.core.vocab.NIFObjectProperties;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Lorenz Buehmann
 *
 */
public class EvidenceRDFGenerator {
	
	    private static final String PROV_NAMESPACE = "http://www.w3.org/ns/prov#";
	    private static final String PROV_CLASS_ENTITY = "http://www.w3.org/ns/prov#Entity";
	    private static final String PROV_CLASS_ACTIVITY = "http://www.w3.org/ns/prov#Activity";
	    private static final String PROV_CLASS_SOFTWARE_AGENT = "http://www.w3.org/ns/prov#SoftwareAgent";
	    private static final String PROV_PROPERTY_HAD_ORIGINAL_SOURCE = "http://www.w3.org/ns/prov#hadOriginalSource"; //Domain and range of type Entity
	    private static final String PROV_PROPERTY_WAS_DERIVED_FROM = "http://www.w3.org/ns/prov#wasDerivedFrom";//Domain and range of type Entity
	    private static final String PROV_PROPERTY_WAS_ASSOCIATED_WITH = "http://www.w3.org/ns/prov#wasAssociatedWith";
	    private static final String PROV_PROPERTY_WAS_GENERATED_BY = "http://www.w3.org/ns/prov#wasGeneratedBy";
	    private static final String PROV_PROPERTY_STARTED_AT_TIME = "http://www.w3.org/ns/prov#startedAtTime";
	    private static final String PROV_PROPERTY_ENDED_AT_TIME = "http://www.w3.org/ns/prov#endedAtTime";
	    
	    private static final String OWL_CLASS_AXIOM = "http://www.w3.org/2002/07/owl#Axiom";
	    private static final String OWL_PROPERTY_ANNOTATED_SOURCE = "http://www.w3.org/2002/07/owl#annotatedSource";
	    private static final String OWL_PROPERTY_ANNOTATED_PROPERTY = "http://www.w3.org/2002/07/owl#annotatedProperty";
	    private static final String OWL_PROPERTY_ANNOTATED_TARGET = "http://www.w3.org/2002/07/owl#annotatedTarget";

	    
	    private static final String DUBLIN_CORE_NAMESPACE = "http://purl.org/dc/elements/1.1/";
	    private static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	    private static final String OWL_NAMESPACE = "http://www.w3.org/2002/07/owl#";

	    private static final String DBPEDIA_NAMESPACE = "http://dbpedia.org/resource/";
	    private static final String DBPEDIA_ONTOLOGY_NAMESPACE = "http://dbpedia.org/ontology/";
	    
	    private static final String LANGUAGE_PROPERTY = "http://purl.org/dc/elements/1.1/language";
	    //time relations
	    private static final String TIME_PROPERTY_FROM = "http://dbpedia.org/ontology/startYear";
	    private static final String TIME_PROPERTY_TO = "http://dbpedia.org/ontology/endYear";
	    
	    private static final String DEFACTO_NAMESPACE = "http://defacto.aksw.org/";
	    private static final String DEFACTO_CLASS_EVIDENCE = DEFACTO_NAMESPACE + "Evidence";
	    private static final String DEFACTO_CLASS_PROOF = DEFACTO_NAMESPACE + "Proof";
	    
	    private static final String DEFACTO_PROPERTY_CONTEXT = DEFACTO_NAMESPACE + "context";
	    private static final String DEFACTO_PROPERTY_PROOF = DEFACTO_NAMESPACE + "proof";
	    private static final String DEFACTO_PROPERTY_EVIDENCE_SCORE = DEFACTO_NAMESPACE + "evidenceScore";
	    private static final String DEFACTO_PROPERTY_PROOF_SCORE = DEFACTO_NAMESPACE + "proofScore";
	    private static final String DEFACTO_PROPERTY_INPUT_FACT = DEFACTO_NAMESPACE + "generatedForFact";
	    
	    private static final String NIF_NAMESPACE = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";
	    private static final String NIF_CLASS_STRUCTURE = NIF_NAMESPACE + "Structure";

	    private static final HashFunction hf = Hashing.md5();
	    private static final Text2RDF nifConverter = new Text2RDF();

	    /**
	     * Generates the provenance data for the passed website
	     * @param requiredWebsite   The website
	     * @param subject   The subject
	     * @param object    The object
	     * @return  The provenance model
	     */
	    public static Model generateProvenanceInformation(Triple triple, Evidence evidence, Calendar startTime, Calendar endTime){
	    	//generate a hash based URI for the triple
	    	HashCode hc = hf.newHasher()
	    		       .putString(triple.getSubject().getURI(), Charsets.UTF_8)
	    		       .putString(triple.getPredicate().getURI(), Charsets.UTF_8)
	    		       .putString(triple.getObject().getURI(), Charsets.UTF_8)
	    		       .hash();
	        String proposedTripleURI = DEFACTO_NAMESPACE + "triple" + hc.toString();

	        OntModel outputModel = ModelFactory.createOntologyModel();
	        
	        //create the defacto:Evidence resource 
	        String evidenceURI = DEFACTO_NAMESPACE + "evidence" + hc.toString();
	        Resource evidenceIndividual = ResourceFactory.createResource(evidenceURI);
	        //add type defacto:Evidence
	        outputModel.add(ResourceFactory.createStatement(
	        		evidenceIndividual,
	                RDF.type,
	                ResourceFactory.createResource(DEFACTO_CLASS_EVIDENCE)));
	        //add type prov:Entity
	        outputModel.add(ResourceFactory.createStatement(
	        		evidenceIndividual,
	                RDF.type,
	                ResourceFactory.createResource(PROV_CLASS_ENTITY)));
	        //add the evidence score
	        outputModel.add(ResourceFactory.createStatement(
	        		evidenceIndividual,
        			ResourceFactory.createProperty(DEFACTO_PROPERTY_EVIDENCE_SCORE),
	                ResourceFactory.createTypedLiteral(evidence.getDeFactoScore())));
	        //add the from year
	        outputModel.add(ResourceFactory.createStatement(
	        		evidenceIndividual,
        			ResourceFactory.createProperty(TIME_PROPERTY_FROM),
	                ResourceFactory.createTypedLiteral(evidence.defactoTimePeriod.getFrom().toString(), new XSDDatatype("gYear"))));
	        //add the to year
	        outputModel.add(ResourceFactory.createStatement(
	        		evidenceIndividual,
        			ResourceFactory.createProperty(TIME_PROPERTY_TO),
        			ResourceFactory.createTypedLiteral(evidence.defactoTimePeriod.getTo().toString(), new XSDDatatype("gYear"))));
	        //add the input fact
	        Resource tripleResource = ResourceFactory.createResource(proposedTripleURI);
	        outputModel.add(ResourceFactory.createStatement(
	        		evidenceIndividual,
        			ResourceFactory.createProperty(DEFACTO_PROPERTY_INPUT_FACT),
        			tripleResource));
	        outputModel.add(tripleResource,
	                ResourceFactory.createProperty(RDF.type.getURI()),
	                outputModel.createResource(OWL_CLASS_AXIOM));
	        outputModel.add(tripleResource,
	                ResourceFactory.createProperty(OWL_PROPERTY_ANNOTATED_SOURCE),
	                outputModel.createResource(triple.getSubject().getURI()));
	        outputModel.add(tripleResource,
	                ResourceFactory.createProperty(OWL_PROPERTY_ANNOTATED_PROPERTY),
	                outputModel.createResource(triple.getPredicate().getURI()));
	        outputModel.add(tripleResource,
	                ResourceFactory.createProperty(OWL_PROPERTY_ANNOTATED_TARGET),
	                outputModel.createResource(triple.getObject().getURI()));
	        //create DeFacto as prov:SoftwareAgent
	        Resource defacto = ResourceFactory.createResource(DEFACTO_NAMESPACE + "DeFacto");
	        outputModel.add(ResourceFactory.createStatement(
	        		defacto,
	        		RDF.type,
	        		ResourceFactory.createResource(PROV_CLASS_SOFTWARE_AGENT)));
	        //create the DeFacto run as prov:Activity
	        Resource defactoRun = ResourceFactory.createResource(DEFACTO_NAMESPACE + "DeFactoRun");
	        outputModel.add(ResourceFactory.createStatement(
	        		defactoRun,
	        		RDF.type,
	        		ResourceFactory.createResource(PROV_CLASS_ACTIVITY)));
	        //add startTime and endTime to run
	        outputModel.add(ResourceFactory.createStatement(
	        		defactoRun,
	        		ResourceFactory.createProperty(PROV_PROPERTY_STARTED_AT_TIME),
	        		ResourceFactory.createTypedLiteral(startTime)));
	        outputModel.add(ResourceFactory.createStatement(
	        		defactoRun,
	        		ResourceFactory.createProperty(PROV_PROPERTY_ENDED_AT_TIME),
	        		ResourceFactory.createTypedLiteral(endTime)));
	        //run prov:associatedWith defacto
	        outputModel.add(ResourceFactory.createStatement(
	        		defactoRun,
	        		ResourceFactory.createProperty(PROV_PROPERTY_WAS_ASSOCIATED_WITH),
	                defacto));
	        //evidence prov:wasGeneratedBy run
	        outputModel.add(ResourceFactory.createStatement(
	        		evidenceIndividual,
	        		ResourceFactory.createProperty(PROV_PROPERTY_WAS_GENERATED_BY),
	                defactoRun));
	        
	        //add proofs
	        int i = 1;
	        for (ComplexProof proof : evidence.getComplexProofs()) {
				//create individual for the proof by using NIF
	        	WebSite webSite = proof.getWebSite();
	    		String contextString = proof.getTinyContext();
	    		String text = webSite.getText();
	    		System.out.println(text);
	    		System.out.println(proof.getTinyContext());
	    		int start = webSite.getText().indexOf(contextString);
	    		int end = start + contextString.length();
	    		Individual contextIndividual = nifConverter.createContextIndividual(DEFACTO_NAMESPACE, text, new ContextHashBasedString(), outputModel);
	    		Individual proofIndividual = nifConverter.createCStringIndividual(DEFACTO_NAMESPACE, contextIndividual, new Span(start, end), new ContextHashBasedString(), outputModel);
	    		ObjectProperty sourceURL = NIFObjectProperties.sourceUrl.getObjectProperty(outputModel);
	    		contextIndividual.addProperty(sourceURL, webSite.getUrl());
	        	
	        	
//	        	Resource proofIndividual = ResourceFactory.createResource(DEFACTO_NAMESPACE + "proof" + i++);
	        	//add type defacto:Proof 
	        	outputModel.add(ResourceFactory.createStatement(
	        			proofIndividual,
		                RDF.type,
		                ResourceFactory.createResource(DEFACTO_CLASS_PROOF)));
	        	//add type prov:Entity
	        	outputModel.add(ResourceFactory.createStatement(
	        			proofIndividual,
		                RDF.type,
		                ResourceFactory.createResource(PROV_CLASS_ENTITY)));
	        	//add type nif:Structure
	        	outputModel.add(ResourceFactory.createStatement(
	        			proofIndividual,
		                RDF.type,
		                ResourceFactory.createResource(NIF_CLASS_STRUCTURE)));
	        	//add tiny context
	        	outputModel.add(ResourceFactory.createStatement(
	        			proofIndividual,
	        			ResourceFactory.createProperty(DEFACTO_PROPERTY_CONTEXT),
		                ResourceFactory.createPlainLiteral(proof.getTinyContext())));
	        	//add proof score
	        	outputModel.add(ResourceFactory.createStatement(
	        			proofIndividual,
	        			ResourceFactory.createProperty(DEFACTO_PROPERTY_PROOF_SCORE),
		                ResourceFactory.createTypedLiteral(Double.valueOf(proof.getScore()))));
	        	//add language
	        	outputModel.add(ResourceFactory.createStatement(
	        			proofIndividual,
	        			ResourceFactory.createProperty(LANGUAGE_PROPERTY),
		                ResourceFactory.createPlainLiteral(proof.getLanguage())));
	        	
	        	
	        	//add website
	        	
	        	
	        	//add the proof to the Evidence
	        	outputModel.add(ResourceFactory.createStatement(
	        			evidenceIndividual,
	        			ResourceFactory.createProperty(DEFACTO_PROPERTY_PROOF),
		                proofIndividual));
	        	
	        	break;
			}

	        return outputModel;
	    }
	    
//	    private static Resource generateEvidenceResource(Evidence evidence){
//	    	
//	    }
//	    
//	    private static Resource generateProofResource(ComplexProof proof){
//	    	
//	    }
	    
	    /**
	     * Generates the provenance data for the passed website and returns it as string for display
	     * @param requiredWebsite   The website
	     * @param subject   The subject
	     * @param predicate The predicate
	     * @param object    The object
	     * @param syntax    The required syntax format e.g. N-TRIPlE
	     * @return  The provenance model
	     */
	    public static String getProvenanceInformationAsString(Triple triple, Evidence evidence, Calendar startTime, Calendar endTime, String syntax){

	        Model resultsModel = generateProvenanceInformation(triple, evidence, startTime, endTime);

	        StringWriter out = new StringWriter();

	        resultsModel.setNsPrefix("dbpedia", DBPEDIA_NAMESPACE);
	        resultsModel.setNsPrefix("dbpedia-owl", DBPEDIA_ONTOLOGY_NAMESPACE);
	        resultsModel.setNsPrefix("dc", DUBLIN_CORE_NAMESPACE);
	        resultsModel.setNsPrefix("rdf", RDF_NAMESPACE);
	        resultsModel.setNsPrefix("prov", PROV_NAMESPACE);
	        resultsModel.setNsPrefix("owl", OWL_NAMESPACE);
	        resultsModel.setNsPrefix("defacto", DEFACTO_NAMESPACE);
	        resultsModel.setNsPrefix("nif", NIF_NAMESPACE);

	        resultsModel.write(out, syntax);
	        return out.toString();
	    }
}
