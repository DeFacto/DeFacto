package org.aksw.defacto.util;

import org.aksw.defacto.model.DefactoModel;

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
	 * Convenience method to read parts of a Jena model into a labeled triple.
	 * @param model Jena model.
	 * @param triple The considered triple.
	 */
	public LabeledTriple(DefactoModel model) {
		subjectURI = model.getSubjectUri();
		predicateURI = model.getPropertyUri();
		objectURI = model.getObjectUri();
		
		subjectLabel = model.getSubjectLabel("en");
//		model.getResource(predicateURI).getProperty(RDFS.label);
		objectLabel = model.getObjectLabel("en");
		
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
