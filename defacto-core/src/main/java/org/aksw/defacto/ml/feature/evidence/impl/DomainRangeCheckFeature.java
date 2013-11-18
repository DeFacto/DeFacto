package org.aksw.defacto.ml.feature.evidence.impl;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.util.SparqlUtil;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ResultSet;

/**
 * 
 * @author Jens Lehmann
 *
 */
public class DomainRangeCheckFeature extends AbstractEvidenceFeature {

	private static final Logger LOGGER = LoggerFactory.getLogger(DomainRangeCheckFeature.class);
	
	@Override
	public void extractFeature(Evidence evidence) {
	    
	    try {
	        
	        String subjectURI = evidence.getModel().getDBpediaSubjectUri();
	        String propertyURI = evidence.getModel().getPropertyUri();
	        String objectURI = evidence.getModel().getDBpediaObjectUri();
	        
//	        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://lod.openlinksw.com/sparql", "http://dbpedia.org");
//	        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://live.dbpedia.org/sparql", "http://dbpedia.org");
	        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");
	        // Some boilerplace code which may get simpler soon
	        long timeToLive = 150l * 60l * 60l * 1000l; 
	        CacheCoreEx cacheBackend = CacheCoreH2.create("mldefacto", timeToLive, false);
	        CacheEx cacheFrontend = new CacheExImpl(cacheBackend);

	        qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
	        
	        boolean domainViolation = false;
	        String queryDom = "SELECT * WHERE { <"+propertyURI+"> rdfs:domain ?dom }";
	        LOGGER.debug("DR-Feature DOMAIN: " + queryDom);
	        ResultSet rs = qef.createQueryExecution(queryDom).execSelect();
	        // without a domain, there can be no violation, so we just need to check the case in which a domain exists
	        if(rs.hasNext()) {
	            String domain = rs.next().get("dom").toString();
	            ResultSet rs2 = qef.createQueryExecution("SELECT * { <"+subjectURI+"> a ?type . FILTER (?type LIKE 'http://dbpedia.org/ontology/%') }").execSelect();
	            // if there is no type, there is no violations
	            if(rs2.hasNext()) {
	                boolean test = false;
	                while(rs2.hasNext()) {
	                    String type = rs2.next().get("type").asNode().getURI();
	                    if(type.equals(domain)) {
	                        test = true;
	                    }
	                }
	                domainViolation = !test;
	            }
//	          domainViolation = !sparql.executeAskQuery("ASK { <"+subjectURI+"> a <"+domain+"> }");
	        }
	        
	        boolean rangeViolation = false;
	        String queryRan = "SELECT * WHERE { <"+propertyURI+"> rdfs:range ?ran }";
	        LOGGER.debug("DR-Feature DOMAIN: " + queryRan);
	        rs = qef.createQueryExecution(queryRan).execSelect();
	        // without a range, there can be no violation, so we just need to check the case in which a range exists
	        if(rs.hasNext()) {
	            String range = rs.next().get("ran").toString();
	            ResultSet rs2 = qef.createQueryExecution("SELECT * { <"+objectURI+"> a ?type . FILTER (?type LIKE 'http://dbpedia.org/ontology/%') }").execSelect();
	            // if there is no type, there is no violations
	            if(rs2.hasNext()) {
	                boolean test = false;
	                while(rs2.hasNext()) {
	                    String type = rs2.next().get("type").asNode().getURI();
	                    if(type.equals(range)) {
	                        test = true;
	                    }
	                }
	                rangeViolation = !test;
	            }           
//	          domainViolation = !sparql.executeAskQuery("ASK { <"+objectURI+"> a <"+range+"> }");
	        }       
	        
	        double score = 1.0;
	        if(domainViolation) {
	            score -= 0.5;
	        }
	        if(rangeViolation) {
	            score -= 0.5;
	        }
	        
	        evidence.getFeatures().setValue(AbstractEvidenceFeature.DOMAIN_RANGE_CHECK, score);	        
	    }
	    catch ( Exception e ) {
	        
	        evidence.getFeatures().setValue(AbstractEvidenceFeature.DOMAIN_RANGE_CHECK, 0D);
	        e.printStackTrace();
	    }
	}
}
