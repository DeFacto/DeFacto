package org.aksw.defacto.util;

import java.util.HashMap;
import java.util.Map;

import org.aksw.defacto.Defacto;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * 
 * A triple and labels of S-P-O. Convenience class. 
 * 
 * @author Jens Lehmann
 *
 */
public class LabeledTriple implements Comparable<LabeledTriple> {

	private String subjectURI;
	
	private String subjectLabel; 
	
	private String predicateURI;
	
	private String predicateLabel;
	
	private String objectURI;
	
	private String objectLabel;

	/**
	 * Convenience method, which can load a model having only a single fact as
	 * well as labels for subject and object in this fact.
	 * @param model
	 */
	public LabeledTriple(Model model) {
        
        StmtIterator iter = model.listStatements();
        
        Map<String,String> labels = new HashMap<String,String>();
        while (iter.hasNext()) {         
            Statement stmt = iter.nextStatement();
            if ( stmt.getPredicate().getURI().equals(Defacto.DEFACTO_CONFIG.getStringSetting("settings", "RESOURCE_LABEL")) ) {
            	labels.put(stmt.getSubject().asLiteral().getString(), stmt.getObject().asLiteral().getString());
            } else {
            	subjectURI = stmt.getSubject().toString();
            	predicateURI = stmt.getPredicate().toString();
            	objectURI = stmt.getObject().toString();
            }
        }
        subjectLabel = labels.get(subjectURI);
        objectLabel = labels.get(objectURI);
        
        throw new Error("No fact in model!");
	}
	
	/**
	 * Convenience method to read parts of a Jena model into a labeled triple.
	 * @param model Jena model.
	 * @param triple The considered triple.
	 */
	public LabeledTriple(Model model, Triple triple) {
		subjectURI = model.getResource(triple.getSubject().toString()).getURI();
		predicateURI = model.getResource(triple.getPredicate().toString()).getURI();
		objectURI = model.getResource(triple.getObject().toString()).getURI();
		
		subjectLabel = model.getResource(subjectURI).getProperty(RDFS.label).getObject().asLiteral().toString();
//		model.getResource(predicateURI).getProperty(RDFS.label);
		objectLabel = model.getResource(objectURI).getProperty(RDFS.label).getObject().asLiteral().toString();
		
//		System.out.println(model);
//		
//		System.out.println(subjectURI);
//		System.out.println(model.getResource(subjectURI).listProperties().toSet());
	}
	
	public LabeledTriple(String subjectURI, String subjectLabel,
			String predicateURI, String predicateLabel, String objectURI,
			String objectLabel) {
		super();
		if(subjectURI == null) {
			throw new RuntimeException("subject must not be null");
		}
		if(predicateURI == null) {
			throw new RuntimeException("predicate must not be null");
		}
		if(objectURI == null) {
			throw new RuntimeException("object must not be null");
		}
		this.subjectURI = subjectURI;
		this.subjectLabel = subjectLabel;
		this.predicateURI = predicateURI;
		this.predicateLabel = predicateLabel;
		this.objectURI = objectURI;
		this.objectLabel = objectLabel;
	}

	public String toString() {
		return "(" + subjectURI + " with label " + subjectLabel + "," + predicateURI + " with label " + predicateLabel + "," + objectURI + " with label " + objectLabel + ")";
	}	
	
	public String toLabelString() {
		return "(" + subjectLabel + "," + predicateLabel + "," + objectLabel + ")";
	}
	
	public String toURIString() {
		return "(" + subjectURI + "," + predicateURI + "," + objectURI + ")";
	}	
	
	public String getSubjectURI() {
		return subjectURI;
	}

	public String getSubjectLabel() {
		return subjectLabel;
	}

	public String getPredicateURI() {
		return predicateURI;
	}

	public String getPredicateLabel() {
		return predicateLabel;
	}

	public String getObjectURI() {
		return objectURI;
	}

	public String getObjectLabel() {
		return objectLabel;
	}

	@Override
	public int compareTo(LabeledTriple lt) {
		int s = subjectURI.compareTo(lt.subjectURI);
		if(s==0) {
			int p = predicateURI.compareTo(lt.predicateURI);
			if(p == 0) {
				return objectURI.compareTo(lt.objectURI);
			} else {
				return p;
			}
		} else {
			return s;
		}
	}
	
}
