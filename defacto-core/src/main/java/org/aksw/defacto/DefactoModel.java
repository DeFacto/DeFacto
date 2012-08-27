/**
 * 
 */
package org.aksw.defacto;

import com.hp.hpl.jena.rdf.model.Model;
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
    public DefactoModel(Model model, String name, boolean isCorrect) {
        
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
     * @return the label of the subject of the fact
     */
    public String getSubjectLabel() {
        
        return this.getLabel(this.getSubjectUri());
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
     * @return label of the object of the fact
     */
    public String getObjectLabel() {
        
        return this.getLabel(this.getObjectUri());
    }
    
    /**
     * @param uri of the resource the labels want to be resolved for
     * @return the label of the given uri, also if the label contains text between "(" and ")" this is stripped and trimed
     */
    private String getLabel(String uri) {
        
        String label = "";
        
        StmtIterator iter = this.model.listStatements();
        while (iter.hasNext()) {
            
            Statement stmt = iter.nextStatement();
            // adds the labels into the map
            if ( stmt.getPredicate().getURI().equals(Defacto.DEFACTO_CONFIG.getStringSetting("settings", "RESOURCE_LABEL")) && stmt.getSubject().getURI().equals(uri) )
                label = stmt.getObject().asLiteral().getLexicalForm().replaceAll("\\(.+?\\)", "").trim();
        }
        
        return label;
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
}
