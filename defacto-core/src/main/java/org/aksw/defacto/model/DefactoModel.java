/**
 * 
 */
package org.aksw.defacto.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.webservices.client.DefactoClient;
import org.apache.commons.io.FileUtils;
import org.semanticweb.yars.nx.namespace.RDFS;
import org.semanticweb.yars.nx.namespace.SKOS;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefactoModel {

    private Model model;
    private String name;
    private boolean correct;
    
    private DefactoResource subject;
    private Property predicate;
	private DefactoResource object;
	private DefactoTimePeriod timePeriod = new DefactoTimePeriod("", "");
	private List<String> languages = new ArrayList<String>();
    
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
    
    public static void main(String[] args) throws FileNotFoundException {
		
    	Model model = ModelFactory.createDefaultModel();
        model.read(new FileReader(new File(DefactoClient.class.getResource("/eval/spouse_0.ttl").getFile())), "", "TTL");
    	
        DefactoModel defactoModel = new DefactoModel(model, "test", true, Arrays.asList("en"));
        
    	System.out.println(defactoModel);
    	System.out.println("S    : " + defactoModel.getSubjectLabels());
    	System.out.println("S_en : " + defactoModel.getSubjectLabel("en"));
    	System.out.println("SA   : " + defactoModel.getSubjectAltLabels());
    	System.out.println("SA_en: " + defactoModel.getSubjectAltLabels("en"));
    	System.out.println("O    : " + defactoModel.getObjectLabels());
    	System.out.println("O_en : " + defactoModel.getObjectLabel("en"));
    	System.out.println("OA   : " + defactoModel.getObjectAltLabels());
    	System.out.println("OA_en: " + defactoModel.getObjectAltLabels("en"));
    	
    	model = ModelFactory.createDefaultModel();
        model.read(new FileReader(new File(DefactoClient.class.getResource("/eval/award_0.ttl").getFile())), "", "TTL");
    	
        defactoModel = new DefactoModel(model, "test", true, Arrays.asList("en"));
        
    	System.out.println(defactoModel);
    	System.out.println("S    : " + defactoModel.getSubjectLabels());
    	System.out.println("S_en : " + defactoModel.getSubjectLabel("en"));
    	System.out.println("SA   : " + defactoModel.getSubjectAltLabels());
    	System.out.println("SA_en: " + defactoModel.getSubjectAltLabels("en"));
    	System.out.println("O    : " + defactoModel.getObjectLabels());
    	System.out.println("O_en : " + defactoModel.getObjectLabel("en"));
    	System.out.println("OA   : " + defactoModel.getObjectAltLabels());
    	System.out.println("OA_en: " + defactoModel.getObjectAltLabels("en"));
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

        return String.format("<%s> <%s> <%s> [%s-%s]", this.subject.getUri(), this.predicate.getURI(), this.object.getUri(), this.timePeriod.getFrom(), this.timePeriod.getTo()); 
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
}
