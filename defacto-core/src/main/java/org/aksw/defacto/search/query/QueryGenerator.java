package org.aksw.defacto.search.query;

import java.util.HashMap;
import java.util.Map;

import org.aksw.defacto.Constants;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.model.DefactoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class QueryGenerator {

    public static final BoaPatternSearcher patternSearcher = new BoaPatternSearcher();
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryGenerator.class);
    public static org.apache.log4j.Logger LOGDEV    = org.apache.log4j.Logger.getLogger("developer");
    private DefactoModel model;
    
    /**
     * 
     * @param model
     */
    public QueryGenerator(DefactoModel model) {
        
        this.model = model;
    }

    public Map<Pattern,MetaQuery> getCounterExampleSearchEngineQueries(String language){
        return this.generateCounterExampleSearchQueries(model.getFact(), language);
    }

    private Map<Pattern,MetaQuery> generateCounterExampleSearchQueries(Statement fact, String language){

        Map<Pattern,MetaQuery> counterqueryStrings =  new HashMap<>();
        String subjectLabel = model.getSubjectLabelNoFallBack(language);
        String objectLabel  = model.getObjectLabelNoFallBack(language);

        //just for english
        if (!language.equals("en")) return null;

        // we dont have labels in the given language so we generate a foreign query with english labels
        if ( subjectLabel.equals(Constants.NO_LABEL) || objectLabel.equals(Constants.NO_LABEL) ) {
            subjectLabel = model.getSubjectLabel("en");
            objectLabel = model.getObjectLabel("en");
        }

        for (Pattern pattern : patternSearcher.getInverseNaturalLanguageRepresentations(fact.getPredicate().getURI(), language)) {

            if ( !pattern.getNormalized().trim().isEmpty() ) {

                //defining rules for each predicate

                MetaQuery metaQuery;
                try{

                    //penalizes dif Subject on D-R, Subject on R-D, Object on D-R, Object on R-D

                    switch (model.getPropertyUri()) {
                        case "http://dbpedia.org/ontology/award":
                            //try to extract NER = person whom could had received the award
                            metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null, 1, 1, 0.25, 0.25, pattern);
                            break;
                        case "http://dbpedia.org/ontology/birthDate":
                            //look for date pattern. A different objectLabel (birth date) should be penalized
                            metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null, 0, 1, 0.0, 1.0, pattern);
                            break;
                        case "http://dbpedia.org/ontology/deathPlace":
                            //look for NER = place
                            metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null, 0, 1, 0.0, 1.0, pattern);
                            break;
                        case "http://dbpedia.org/ontology/foundationPlace":
                            //look for NER = place
                            metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null, 0, 1, 0.0, 1.0, pattern);
                            break;
                        case "http://dbpedia.org/ontology/leaderName":
                            //look for NER = place
                            metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null, 1, 1, 0.25, 0.75, pattern);
                            break;
                        case "http://dbpedia.org/ontology/nflTeam":
                            //look for NER = team
                            metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null, 0, 1, 0.0, 0.95, pattern);
                            break;
                        //case "http://dbpedia.org/ontology/publicationDate":
                        case "http://dbpedia.org/ontology/author":
                            //look for NER = person
                            metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null, 1, 1, 0.20, 0.20, pattern);
                            break;
                        case "http://dbpedia.org/ontology/spouse":
                            //look for NER = person
                            metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null, 1, 1, 0.99, 0.99, pattern);
                            break;
                        case "http://dbpedia.org/ontology/starring":
                            //look for NER = person
                            metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null, 0, 1, 0.00, 0.05, pattern);
                            break;
                        case "http://dbpedia.org/ontology/subsidiary":
                            //look for NER = company O was acquired by S / S acquired O
                            metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null, 1, 1, 0.80, 0.80, pattern);
                            break;

                        default:
                            throw new Exception("The property " + model.getPropertyUri() + " has not been implemented");
                    }

                    LOGDEV.debug(metaQuery);
                    counterqueryStrings.put(pattern, metaQuery);

                }catch (Exception e){
                   LOGDEV.error(e.toString());
                }


            }
        }

        // add one query without any predicate
        //counterqueryStrings.put(new Pattern("??? NONE ???", language), new MetaQuery(subjectLabel, "??? NONE ???", objectLabel, language, null));
        LOGGER.debug(String.format("Generated %s negated queries for fact ('%s'): %s", counterqueryStrings.size(), language, fact.asTriple()));

        return counterqueryStrings;

    }
    
    /**
     * 
     * @return
     */
    public Map<Pattern,MetaQuery> getSearchEngineQueries(String language){
        return this.generateSearchQueries(model.getFact(), language);
    }


    /**
     * 
     * @param uriToLabels
     * @param fact
     * @return
     */
    private Map<Pattern,MetaQuery> generateSearchQueries(Statement fact, String language){

        Map<Pattern,MetaQuery> queryStrings =  new HashMap<>();
        String subjectLabel = model.getSubjectLabelNoFallBack(language);//.replaceAll("\\(.+?\\)", "").trim(); 
        String objectLabel  = model.getObjectLabelNoFallBack(language);//.replaceAll("\\(.+?\\)", "").trim();
        
        // we dont have labels in the given language so we generate a foreign query with english labels
        if ( subjectLabel.equals(Constants.NO_LABEL) || objectLabel.equals(Constants.NO_LABEL) ) {
        	subjectLabel = model.getSubjectLabel("en");
        	objectLabel = model.getObjectLabel("en");
        }
        
        // TODO
        // query boa index and generate the meta queries
        int i =0;
        for (Pattern pattern : patternSearcher.getNaturalLanguageRepresentations(fact.getPredicate().getURI(), language)) {
        	
        	if ( !pattern.getNormalized().trim().isEmpty() ) {
        		
        		MetaQuery metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null, pattern);
                LOGGER.info(metaQuery.toString());
                queryStrings.put(pattern, metaQuery);

                LOGDEV.debug(" " + i + " pattern norm = [" + pattern.getNormalized() + "] metaquery: " +
                        " s = [" + metaQuery.getSubjectLabel() +
                        "] p = [" + metaQuery.getPropertyLabel() +
                        "] o = [" + metaQuery.getObjectLabel() + "]");

        	}
            i++;
        }
        
        // add one query without any predicate
        Pattern p = new Pattern("??? NONE ???", language);

        queryStrings.put(p, new MetaQuery(subjectLabel, "??? NONE ???", objectLabel, language, null, p));
        LOGDEV.debug(String.format(" -> Generated %s queries for fact ('%s'): %s", queryStrings.size(), language, fact.asTriple()));
        
        return queryStrings;
    }

}
