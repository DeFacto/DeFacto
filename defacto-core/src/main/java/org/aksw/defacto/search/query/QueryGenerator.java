package org.aksw.defacto.search.query;

import java.util.HashMap;
import java.util.Map;

import org.aksw.defacto.DefactoModel;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class QueryGenerator {

    private BoaPatternSearcher patternSearcher = new BoaPatternSearcher();
    private Logger logger = Logger.getLogger(QueryGenerator.class);
    private DefactoModel model;
    
    /**
     * 
     * @param model
     */
    public QueryGenerator(DefactoModel model) {
        
        this.model = model;
    }
    
    /**
     * 
     * @return
     */
    public Map<Pattern,MetaQuery> getSearchEngineQueries(){
        assert(this.model.size() == 3);
        
        // and generate the query strings 
        return this.generateSearchQueries(model.getFact());
    }
    
    /**
     * 
     * @param uriToLabels
     * @param fact
     * @return
     */
    private Map<Pattern,MetaQuery> generateSearchQueries(Statement fact){
     
        Map<Pattern,MetaQuery> queryStrings =  new HashMap<Pattern,MetaQuery>();
        String subjectLabel = model.getSubjectLabel(); 
        String objectLabel  = model.getObjectLabel();
        
        // query boa index and generate the meta queries
        for (Pattern pattern : this.patternSearcher.getNaturalLanguageRepresentations(fact.getPredicate().getURI()))
            queryStrings.put(pattern, new MetaQuery(subjectLabel, pattern.naturalLanguageRepresentation, objectLabel, null));
        
        // add one query without any predicate
        queryStrings.put(new Pattern(), new MetaQuery(subjectLabel, "??? NONE ???", objectLabel, null));        
                
        logger.info(String.format("Generated %s queries for fact: %s", queryStrings.size(), fact.asTriple()));        
                
        return queryStrings;
    }
}
