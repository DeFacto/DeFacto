package org.aksw.provenance.search.query;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.aksw.provenance.Constants;
import org.aksw.provenance.boa.BoaPatternSearcher;
import org.aksw.provenance.boa.Pattern;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class QueryGenerator {

    private BoaPatternSearcher patternSearcher = new BoaPatternSearcher();
    private Logger logger = Logger.getLogger(QueryGenerator.class);
    private Model model;
    
    /**
     * 
     * @param model
     */
    public QueryGenerator(Model model) {
        
        this.model = model;
    }
    
    /**
     * 
     * @return
     */
    public Map<Pattern,MetaQuery> getSearchEngineQueries(){
        assert(this.model.size() == 3);
        
        Map<String,String> uriToLabels = new TreeMap<String,String>();
        
        // retrieve the labels and the link from the model 
        Statement fact = this.getResources(uriToLabels);
        // and generate the query strings 
        return this.generateSearchQueries(uriToLabels, fact);
    }
    
    /**
     * Retrieves the labels and the link from the supplied model 
     * 
     * @param uriToLabels
     * @param fact
     */
    private Statement getResources(Map<String, String> uriToLabels) {
        
        StmtIterator iter = model.listStatements();
        Statement fact = null;
        
        while (iter.hasNext()) {
            
            Statement stmt = iter.nextStatement();
            // adds the labels into the map
            if ( stmt.getPredicate().getURI().equals(Constants.RESOURCE_LABEL) )
                uriToLabels.put(stmt.getSubject().getURI(), stmt.getObject().toString());
            else 
                fact = stmt;
        }
        
        return fact;
    }
    
    /**
     * 
     * @param uriToLabels
     * @param fact
     * @return
     */
    private Map<Pattern,MetaQuery> generateSearchQueries(Map<String, String> uriToLabels, Statement fact){
     
        Map<Pattern,MetaQuery> queryStrings =  new HashMap<Pattern,MetaQuery>();
        String subjectLabel = uriToLabels.get(fact.getSubject().getURI()).replace("@en", "").replaceAll("\\(.+?\\)", "").trim(); 
        String propertyUri  = fact.getPredicate().getURI();
        String objectLabel  = uriToLabels.get(fact.getObject().asResource().getURI()).replace("@en", "").replaceAll("\\(.+?\\)", "").trim();
        
        for (Pattern pattern : this.patternSearcher.getNaturalLanguageRepresentations(propertyUri))
            queryStrings.put(pattern, new MetaQuery(subjectLabel, pattern.naturalLanguageRepresentation, objectLabel, null));
        
        queryStrings.put(new Pattern(), new MetaQuery(subjectLabel, "??? NONE ???", objectLabel, null));        
                
        logger.info(String.format("Generated %s queries for fact: %s", queryStrings.size(), fact.asTriple()));        
                
        return queryStrings;
    }
}
