/**
 * 
 */
package org.aksw.defacto.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.defacto.Constants;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefactoResource {

	private Resource resource;
	private Model model;
	public Map<String,String> labels = new HashMap<String,String>();
	public Map<String,Set<String>> altLabels = new HashMap<String,Set<String>>();
	public List<Resource> owlSameAs = new ArrayList<Resource>();
	private String uri;

	public DefactoResource(Resource resource, Model model) {
		
		this.resource = resource;
		this.uri = this.resource.getURI();
		this.model = model;
		this.altLabels.put("en", new HashSet<String>());
		this.altLabels.put("de", new HashSet<String>());
		this.altLabels.put("fr", new HashSet<String>());
		
		init();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefactoResource other = (DefactoResource) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	private void init() {

		NodeIterator rdfsIterator = model.listObjectsOfProperty(resource, Constants.RDFS_LABEL);
		while (rdfsIterator.hasNext()) {
			RDFNode rdfNode = (RDFNode) rdfsIterator.next();
			
			String language = rdfNode.asLiteral().getLanguage();
			String label = rdfNode.asLiteral().getLexicalForm();
			
			this.labels.put(language, label);
		}
		
		NodeIterator skosAltIterator = model.listObjectsOfProperty(resource, Constants.SKOS_ALT_LABEL);
		while (skosAltIterator.hasNext()) {
			RDFNode rdfNode = (RDFNode) skosAltIterator.next();
			
			String language = rdfNode.asLiteral().getLanguage();
			String label = rdfNode.asLiteral().getLexicalForm();
			this.altLabels.get(language).add(label);
		}
		
		NodeIterator sameAsIterator = model.listObjectsOfProperty(resource, Constants.OWL_SAME_AS);
		while (sameAsIterator.hasNext()) {
			RDFNode rdfNode = (RDFNode) sameAsIterator.next();
			
			this.owlSameAs.add(rdfNode.asResource());
			
			NodeIterator listObjectsOfProperty = model.listObjectsOfProperty(rdfNode.asResource(), Constants.SKOS_ALT_LABEL);
			while ( listObjectsOfProperty.hasNext() ) {
				
				RDFNode node = listObjectsOfProperty.next();
				this.altLabels.get(node.asLiteral().getLanguage()).add(node.asLiteral().getLexicalForm());
			}
		}
	}

	public String getUri() {
		return this.resource.getURI();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		String output = "Uri: " + this.resource.getURI();
		return output;
	}

	/**
	 * 
	 * @return
	 */
	public Set<String> getLabels() {
		
		return new HashSet<String>(this.labels.values());
	}

	/**
	 * Returns the language of the reosurce in the given language.
	 * if no such language was found, english is used as fallback.
	 * 
	 * @param language
	 * @return
	 */
	public String getLabel(String language) {
		
		String label = this.labels.get(language);
		return label != null && !label.isEmpty() ? label : this.labels.get("en");
	}
	
	public String getLabelNoFallBack(String language) {
		
		String label = this.labels.get(language);
		return label != null && !label.isEmpty() ? label : Constants.NO_LABEL;
	}

	/**
	 * 
	 * @param language
	 * @return
	 */
	public Set<String> getAltLabels(String language) {
		return this.altLabels.get(language);
	}

	/**
	 * 
	 * @return
	 */
	public Set<String> getAltLabels() {
		
		Set<String> allAltLables = new HashSet<String>();
		for ( Set<String> altLabels : this.altLabels.values()) allAltLables .addAll(altLabels);
		
		return allAltLables;
	}

	public Resource getResource() {
		return this.resource;
	}

	public Map<String,String> getAllLabels() {
		return this.labels;
	}

	
	
}
