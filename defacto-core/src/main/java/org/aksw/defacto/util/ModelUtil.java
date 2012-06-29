package org.aksw.defacto.util;

import org.aksw.defacto.Defacto;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class ModelUtil {

    /**
     * just returns the link between two resources
     * 
     * @param model
     * @return
     */
    public static Statement getFact(Model model) {
        
        StmtIterator iter = model.listStatements();
        Statement fact = null;
        
        while (iter.hasNext()) {
            
            Statement stmt = iter.nextStatement();
            // adds the labels into the map
            if ( !stmt.getPredicate().getURI().equals(Defacto.DEFACTO_CONFIG.getStringSetting("settings", "RESOURCE_LABEL")) )
                fact = stmt;
        }
        
        return fact;
    }
    
    /**
     * 
     * @param uri
     * @param model
     * @return
     */
    public static String getLabel(String uri, Model model) {
        
        String label = "";
        
        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            
            Statement stmt = iter.nextStatement();
            // adds the labels into the map
            if ( stmt.getPredicate().getURI().equals(Defacto.DEFACTO_CONFIG.getStringSetting("settings", "RESOURCE_LABEL")) && stmt.getSubject().getURI().equals(uri) )
                label = stmt.getObject().asLiteral().getLexicalForm().replaceAll("\\(.+?\\)", "").trim();
        }
        
        return label;
    }

    /**
     * Returns the property of a given defacto model or ""
     * (empty string) if it does not exist!
     * 
     * @param model
     * @return
     */
    public static String getPropertyUri(Model model) {

        StmtIterator iter = model.listStatements();
        String propertyUri = "";
        
        while (iter.hasNext()) {
            
            Statement stmt = iter.nextStatement();
            // adds the labels into the map
            if ( !stmt.getPredicate().getURI().equals(Defacto.DEFACTO_CONFIG.getStringSetting("settings", "RESOURCE_LABEL")) )
                propertyUri = stmt.getPredicate().getURI();
        }
        
        return propertyUri;
    }

    /**
     * 
     * @param model
     * @return
     */
    public static String getSubjectUri(Model model) {

        return ModelUtil.getFact(model).getSubject().getURI();
    }

    /**
     * 
     * @param model
     * @return
     */
    public static String getObjectUri(Model model) {

        return ModelUtil.getFact(model).getObject().asResource().getURI();
    }
}
