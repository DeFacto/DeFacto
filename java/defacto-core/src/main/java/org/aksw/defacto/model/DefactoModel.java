/**
 * 
 */
package org.aksw.defacto.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.semanticweb.yars.nx.namespace.SKOS;
import org.semanticweb.yars.nx.namespace.XSD;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefactoModel {

    public Model model;
    public String name;
    public boolean correct;
    public DefactoResource subject;
    public Property predicate;
    public String predicateUri;
    public DefactoResource object;
    public DefactoTimePeriod timePeriod = new DefactoTimePeriod("", "");
    public List<String> languages = new ArrayList<String>();
    
    /**
     * Creates a new Defacto Model. This is a wrapper around a jena model. But with
     * additional information for example if we know if the fact is correct or the name
     * if the training file we got it from.
     * 
     * @param model - the jena model with 3 statements (1 link and 2 rdfs:label triples)
     * @param name - the name of the file we got the model from
     * @param correct is the fact contained in this model correct?
     */
    public DefactoModel(Model model, String name, boolean isCorrect, List<String> languages) {
        
        this.model      = model;
        this.name       = name;
        this.correct    = isCorrect;
        this.languages	= new ArrayList<String>(languages);
        
        init(model);
    }
    
    /**
     * 
     * @param language
     * @return
     */
    public Set<String> getObjectAltLabels(String language) {
		return this.object.getAltLabels(language);
	}

    /**
     * 
     * @return
     */
    public Set<String> getObjectAltLabels() {
		return this.object.getAltLabels();
	}

    /**
     * 
     * @param language
     * @return
     */
    public Set<String> getSubjectAltLabels(String language) {
		return this.subject.getAltLabels(language);
	}

    /**
     * 
     * @return
     */
    public Set<String> getSubjectAltLabels() {
		return this.subject.getAltLabels();
	}

    /**
     * @param language
     * @return the label of the subject resource in the given language,
     * if no such language is found english is used as fallback
     */
    public String getObjectLabel(String language) {
		return this.object.getLabel(language);
	}

    /**
     * @param language
     * @return the label of the subject resource in the given language,
     * if no such language is found english is used as fallback 
     */
    public String getSubjectLabel(String language) {

		return this.subject.getLabel(language);
	}

    /**
     * 
     * @return
     */
    public Set<String> getObjectLabels() {
		return this.getLabels(this.object);
	}

    /**
     * 
     * @return
     */
    public Set<String> getSubjectLabels() {
    	
		return this.getLabels(this.subject);
	}

    /**
     * 
     * @param resource
     * @return
     */
	private Set<String> getLabels(DefactoResource resource) {
		return resource.getLabels();
	}

	/**
	 * 
	 * @param model
	 */
	private void init(Model model) {
    	
		StmtIterator listIter = model.listStatements();
    	while ( listIter.hasNext() ) {
    		
    		Statement stmt = listIter.next();
    		// we have found the blank node
    		if ( stmt.getSubject().getURI().matches("^.*__[0-9]*$") ) {
    			
    			if ( stmt.getObject().isResource() ) {
    				
        			this.object = new DefactoResource(stmt.getObject().asResource(), model);
        			this.predicate = stmt.getPredicate();
        			this.predicateUri = this.predicate.getURI();
        			
        			String from = model.listObjectsOfProperty(stmt.getSubject(), Constants.DEFACTO_FROM).next().asLiteral().getLexicalForm();
        			String to = model.listObjectsOfProperty(stmt.getSubject(), Constants.DEFACTO_TO).next().asLiteral().getLexicalForm();
        			
        			this.timePeriod = new DefactoTimePeriod(from, to);
        			
        			StmtIterator listIter2 = model.listStatements();
                	while ( listIter2.hasNext() ) {

                		Statement stmt2 = listIter2.next();
                		if ( stmt2.getObject().isResource() && stmt2.getObject().asResource().getURI().equals(stmt.getSubject().getURI()) ) {

                			this.subject = new DefactoResource(stmt2.getSubject().asResource(), model);
                		}
                	};
    			}
    		}
    	}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((languages == null) ? 0 : languages.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((predicateUri == null) ? 0 : predicateUri.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result + ((timePeriod == null) ? 0 : timePeriod.hashCode());
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
		DefactoModel other = (DefactoModel) obj;
		if (languages == null) {
			if (other.languages != null)
				return false;
		} else if (!languages.equals(other.languages))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (predicateUri == null) {
			if (other.predicateUri != null)
				return false;
		} else if (!predicateUri.equals(other.predicateUri))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		if (timePeriod == null) {
			if (other.timePeriod != null)
				return false;
		} else if (!timePeriod.equals(other.timePeriod))
			return false;
		return true;
	}

	/**
     * @return the subject uri of the fact
     */
    public String getSubjectUri() {
        
        return this.subject.getUri();
    }
    
    /**
     * @return the uri of the property of the fact
     */
    public String getPropertyUri() {
        
        return this.predicate.getURI();
    }
    
    /**
     * @return the uri of the subject of the triple to be checked
     */
    public String getObjectUri() {
        
        return this.object.getUri();
    }
    
    /**
     * @return the correct
     */
    public boolean isCorrect() {

        return correct;
    }

    /**
     * @param correct the correct to set
     */
    public void setCorrect(boolean correct) {

        this.correct = correct;
    }

    
    /**
     * @return the name
     */
    public String getName() {
    
        return name;
    }

    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
    
        this.name = name;
    }

    /**
     * @return the size of a model which should always be 3
     */
    public long size() {

        return this.model.size();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return String.format("'%s' <%s> <%s> <%s> [%s-%s]", this.name, this.subject.getLabel("en"), this.predicate.getURI(), this.object.getLabel("en"), this.timePeriod.getFrom(), this.timePeriod.getTo()); 
    }

	public Statement getFact() {
		return this.model.createStatement(this.subject.getResource(), this.predicate, this.object.getResource());
	}

	public DefactoTimePeriod getTimePeriod() {
		return this.timePeriod;
	}

	public String getSubjectLabelNoFallBack(String language) {
		
		return this.subject.getLabelNoFallBack(language);
	}

	public String getObjectLabelNoFallBack(String language) {
		// TODO Auto-generated method stub
		return this.object.getLabelNoFallBack(language);
	}

	public List<String> getLanguages() {
		// TODO Auto-generated method stub
		return this.languages;
	}
	
	/**
	 * @return the subject
	 */
	public DefactoResource getSubject() {
		return subject;
	}

	/**
	 * @return the predicate
	 */
	public Property getPredicate() {
		return predicate;
	}

	/**
	 * @return the object
	 */
	public DefactoResource getObject() {
		return object;
	}

	public void write(String path, String name) throws IOException {
	
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("fbase", Constants.FREEBASE_RESOURCE_NAMESPACE);
		model.setNsPrefix("dbo", Constants.DBPEDIA_ONTOLOGY_NAMESPACE);
		model.setNsPrefix("owl", OWL.NS);
		model.setNsPrefix("rdfs", RDFS.getURI());
		model.setNsPrefix("xsd", XSD.NS);
		model.setNsPrefix("skos", SKOS.NS);
		
		// create all the necessary nodes
		Resource subject		= model.createResource(this.subject.getUri());
        Property bnodeProperty	= model.createProperty("http://defacto.aksw.org/bnode/property");
        Resource bnode			= model.createResource(this.subject.getUri() + "__1");
        Property property		= model.createProperty(this.predicate.getURI());
        Resource object			= model.createResource(this.object.getUri());
		
        // add them to the model
        model.add(subject, bnodeProperty, bnode);
        model.add(bnode, property, object);
        model.add(bnode, Constants.DEFACTO_FROM, this.timePeriod.getFrom() + "", XSDDatatype.XSDgYear);
        model.add(bnode, Constants.DEFACTO_TO, this.timePeriod.getTo() + "", XSDDatatype.XSDgYear);
        
        for ( Map.Entry<String, String> langToLabel : this.subject.labels.entrySet() ) {
        	
        	model.add(subject, RDFS.label, langToLabel.getValue(), langToLabel.getKey());
        }
        
        for ( Map.Entry<String, String> langToLabel : this.object.labels.entrySet() ) {
        	
        	model.add(object, RDFS.label, langToLabel.getValue(), langToLabel.getKey());
        }
        
        for ( Map.Entry<String, Set<String>> langToLabel : this.subject.altLabels.entrySet() )
        	for ( String label : langToLabel.getValue())
        		model.add(subject, Constants.SKOS_ALT_LABEL, label, langToLabel.getKey());
        
        for ( Map.Entry<String, Set<String>> langToLabel : this.object.altLabels.entrySet() )
        	for ( String label : langToLabel.getValue())
        		model.add(object, Constants.SKOS_ALT_LABEL, label, langToLabel.getKey());

        for ( Resource res : this.subject.owlSameAs) model.add(subject, OWL.sameAs, res);
        for ( Resource res : this.object.owlSameAs) model.add(object, OWL.sameAs, res);
        
        new File(path).mkdirs();
		// write them to the file
        model.write(new FileWriter(new File(path + name)), "TTL");
	}
	
	public void setObject(DefactoResource object) {
		
		this.object = object;
	}
	
	public void setSubject(DefactoResource subject) {
		
		this.subject = subject;
	}

	public void setProperty(Property property) {
		
		this.predicate = property;
	}

	public String getDBpediaSubjectUri() {
		
		for ( Resource res : this.subject.owlSameAs) 
			if ( res.getURI().startsWith("http://dbpedia.org/resource/") ) return res.getURI();
		
		return null;
	}

	public String getDBpediaObjectUri() {
		
		for ( Resource res : this.object.owlSameAs) 
			if ( res.getURI().startsWith("http://dbpedia.org/resource/") ) return res.getURI();
		
		return null;
	}
}
