/**
 * 
 */
package org.aksw.defacto.util;

import java.io.IOException;
import java.io.StringWriter;

import org.aksw.commons.collections.Pair;
import org.aksw.defacto.DeFactoUI;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.nlp2rdf.core.Span;
import org.nlp2rdf.core.Text2RDF;
import org.nlp2rdf.core.urischemes.ContextHashBasedString;
import org.nlp2rdf.core.vocab.NIFObjectProperties;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class NIFExport {
	
	public static final String DEFACTO_NAMESPACE = "http://defacto.aksw.org/" ;
	 private static final String PROV_NAMESPACE = "http://www.w3.org/ns/prov#";
	    private static final String DUBLIN_CORE_NAMESPACE = "http://purl.org/dc/terms/";
	    
	private static final Text2RDF nifConverter = new Text2RDF();
	
	public static Model toNIF(ComplexProof proof, Evidence evidence, Triple triple){
		OntModel model = ModelFactory.createOntologyModel();
		model.setNsPrefix("nif", "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#");
		model.setNsPrefix("prov", PROV_NAMESPACE);
		
		WebSite webSite = proof.getWebSite();
		String contextString = proof.getProofPhrase();
		String text = webSite.getText();
		
		//nif
		int start = webSite.getText().indexOf(contextString);
		int end = start + contextString.length();
		Individual contextIndividual = nifConverter.createContextIndividual(DEFACTO_NAMESPACE, text, new ContextHashBasedString(), model);
		Individual cStringIndividual = nifConverter.createCStringIndividual(DEFACTO_NAMESPACE, contextIndividual, new Span(start, end), new ContextHashBasedString(), model);
		ObjectProperty sourceURL = NIFObjectProperties.sourceUrl.getObjectProperty(model);
		contextIndividual.addProperty(sourceURL, webSite.getUrl());
		
		//prov
		model.add(ProvenanceInformationGenerator.generateProvenanceInformation(webSite, 
				triple.getSubject().toString(), triple.getPredicate().toString(), triple.getObject().toString()));
		
		return model;
	}
	
	public static String toNIFAsString(ComplexProof proof, Evidence evidence, Triple triple){
		Model model = toNIF(proof, evidence, triple);
		
		StringWriter out = new StringWriter();
        model.write(out, "TURTLE");
        return out.toString();
	}
	
	public static void main(String[] args) throws Exception {
		 try {
				Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(DeFactoUI.class.getClassLoader().getResourceAsStream("defacto.ini")));
			} catch (InvalidFileFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		Pair<DefactoModel, Evidence> data = DummyData.createDummyData(1);
		DefactoModel model = data.first;
		Evidence evidence = data.second;
		Triple triple = DummyData.getDummyTriple();
		
		for (ComplexProof proof : evidence.getComplexProofs()) {
			try {
				System.out.println(toNIFAsString(proof, evidence, triple));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
