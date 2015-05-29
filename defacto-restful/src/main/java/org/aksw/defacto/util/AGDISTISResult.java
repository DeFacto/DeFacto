package org.aksw.defacto.util;

public class AGDISTISResult {
	String subjectURI;
	String objectURI;

	public AGDISTISResult(String subjectURI, String objectURI) {
		this.subjectURI = subjectURI;
		this.objectURI = objectURI;
	}

	/**
	 * @return the subjectURI
	 */
	public String getSubjectURI() {
		return subjectURI;
	}

	/**
	 * @return the objectURI
	 */
	public String getObjectURI() {
		return objectURI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Subject URI:\t" + subjectURI + "\nObject URI:\t" + objectURI;
	}
}
