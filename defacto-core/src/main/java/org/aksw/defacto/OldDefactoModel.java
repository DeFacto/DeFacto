/**
 * 
 */
package org.aksw.defacto;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.yars.nx.namespace.RDFS;
import org.semanticweb.yars.nx.namespace.SKOS;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class OldDefactoModel {

    private Model model;
    private String name;
    private boolean correct;
    private Statement fact;
    
    /**
     * Creates a new Defacto Model. This is a wrapper around a jena model. But with
     * additional information for example if we know if the fact is correct or the name
     * if the training file we got it from.
     * 
     * @param model - the jena model with 3 statements (1 link and 2 rdfs:label triples)
     * @param name - the name of the file we got the model from
     * @param correct is the fact contained in this model correct?
     */
    public OldDefactoModel(Model model, String name, boolean isCorrect) {
        
        this.model      = model;
        this.name       = name;
        this.correct    = isCorrect;
    }
    
    /**
     * @return the subject uri of the fact
     */
    public String getSubjectUri() {
        
        return this.getFact().getSubject().getURI();
    }
    
    /**
     * @param language 
     * @return the label of the subject of the fact
     */
    public String getSubjectLabel(String language) {
        
        return this.getLabel(this.getSubjectUri(), language);
    }
    
    /**
     * @return the uri of the property of the fact
     */
    public String getPropertyUri() {
        
        return this.getFact().getPredicate().getURI();
    }
    
    /**
     * @return the uri of the subject of the triple to be checked
     */
    public String getObjectUri() {
        
        return this.getFact().getObject().asResource().getURI();
    }
    
    /**
     * @param language 
     * @return label of the object of the fact
     */
    public String getObjectLabel(String language) {
        
        return this.getLabel(this.getObjectUri(), language);
    }
    
    /**
     * @param uri of the resource the labels want to be resolved for
     * @param language 
     * @return the label of the given uri, also if the label contains text between "(" and ")" this is stripped and trimed
     */
    private String getLabel(String uri, String language) {
        
        String label = "", backup = "";
        
        StmtIterator iter = this.model.listStatements();
        while (iter.hasNext()) {
            
            Statement stmt = iter.nextStatement();
            // adds the labels into the map
            if ( stmt.getPredicate().getURI().equals(Defacto.DEFACTO_CONFIG.getStringSetting("settings", "RESOURCE_LABEL")) && stmt.getSubject().getURI().equals(uri) ) {
            	
            	if ( stmt.getObject().asLiteral().getLanguage().equals(language) ) {
            		
            		label = stmt.getObject().asLiteral().getLexicalForm().replaceAll("\\(.+?\\)", "").trim();
            	}
            	if ( stmt.getObject().asLiteral().getLanguage().equals("en") ) {
            		
            		backup = stmt.getObject().asLiteral().getLexicalForm().replaceAll("\\(.+?\\)", "").trim();
            	}
            }
        }
        
        return label != null && !label.isEmpty() ? label : backup;
    }
    
    /**
     * 
     * @return
     */
    public Statement getFact() {
        
        if ( this.fact != null ) return this.fact;
        else {
            
            StmtIterator iter = this.model.listStatements();
            
            while (iter.hasNext()) {
                
                Statement stmt = iter.nextStatement();
                // adds the labels into the map
                if ( !stmt.getPredicate().getURI().equals(Defacto.DEFACTO_CONFIG.getStringSetting("settings", "RESOURCE_LABEL")) ) {
                    
                    this.fact = stmt;
                    return stmt;
                }
                    
            }
            throw new RuntimeException("No triple which is not rdfs:label in model to evaluate!");
        }
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

    /**
     * @return iterator over all statements
     */
    public StmtIterator listStatements() {

        return this.model.listStatements();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return this.getFact().toString();
    }

    /**
     * 
     * @param subjectUri
     * @param language
     * @return
     */
	public Set<String> getSubjectLabels(String subjectUri, String language) {
		return this.getLabels(subjectUri, language);
	}
	
	public Set<String> getSubjectLabels(String subjectUri){
		
		Set<String> labels =  new HashSet<String>();
		labels.addAll(this.getLabels(subjectUri, "en"));
		labels.addAll(this.getLabels(subjectUri, "de"));
		labels.addAll(this.getLabels(subjectUri, "fr"));
		
		return labels;
	}

	private Set<String> getLabels(String subjectUri, String language) {
		
		StmtIterator iter = this.model.listStatements();
		Set<String> labels = new HashSet<String>();
        
        while (iter.hasNext()) {
            
            Statement stmt = iter.nextStatement();
            // adds the labels into the map
            if ( stmt.getPredicate().getURI().equals(RDFS.LABEL) || stmt.getPredicate().getURI().equals(SKOS.NS + "altLabel")) {
                
                if ( stmt.getObject().asLiteral().getLanguage().equals(language) ) labels.add(stmt.getObject().asLiteral().getLexicalForm()); 
            }
                
        }
		return labels;
	}

	/**
	 * 
	 * @param objectUri
	 * @param language
	 * @return
	 */
	public Set<String> getObjectLabels(String objectUri) {
		Set<String> labels =  new HashSet<String>();
		labels.addAll(this.getLabels(objectUri, "en"));
		labels.addAll(this.getLabels(objectUri, "de"));
		labels.addAll(this.getLabels(objectUri, "fr"));
		
		return labels;
	}
}
