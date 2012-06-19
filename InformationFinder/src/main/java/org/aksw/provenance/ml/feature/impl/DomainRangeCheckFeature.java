package org.aksw.provenance.ml.feature.impl;

import org.aksw.helper.SPARQL;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.ml.feature.AbstractFeature;
import org.aksw.provenance.util.ModelUtil;

import com.hp.hpl.jena.query.ResultSet;

/**
 * 
 * @author Jens Lehmann
 *
 */
public class DomainRangeCheckFeature extends AbstractFeature {

	@Override
	public void extractFeature(Evidence evidence) {
		String subjectURI = ModelUtil.getSubjectUri(evidence.getModel());
		String propertyURI = ModelUtil.getPropertyUri(evidence.getModel());
		String objectURI = ModelUtil.getObjectUri(evidence.getModel());
		
		String endpoint = "http://live.dbpedia.org/sparql";
		String graph = "http://dbpedia.org";
		SPARQL sparql = new SPARQL(endpoint, graph);
		
		boolean domainViolation = false;
		String queryDom = "SELECT * WHERE { <"+propertyURI+"> rdfs:domain ?dom }";
		ResultSet rs = sparql.executeSelectQuery(queryDom);
		// without a domain, there can be no violation, so we just need to check the case in which a domain exists
		if(rs.hasNext()) {
			String domain = rs.next().get("dom").toString();
			ResultSet rs2 = sparql.executeSelectQuery("SELECT * { <"+subjectURI+"> a ?type . FILTER (?type LIKE 'http://dbpedia.org/ontology/%') }");
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
//			domainViolation = !sparql.executeAskQuery("ASK { <"+subjectURI+"> a <"+domain+"> }");
		}
		
		boolean rangeViolation = false;
		String queryRan = "SELECT * WHERE { <"+propertyURI+"> rdfs:range ?ran }";
		rs = sparql.executeSelectQuery(queryRan);
		// without a range, there can be no violation, so we just need to check the case in which a range exists
		if(rs.hasNext()) {
			String range = rs.next().get("ran").toString();
			ResultSet rs2 = sparql.executeSelectQuery("SELECT * { <"+objectURI+"> a ?type . FILTER (?type LIKE 'http://dbpedia.org/ontology/%') }");
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
//			domainViolation = !sparql.executeAskQuery("ASK { <"+objectURI+"> a <"+range+"> }");
		}		
		
		double score = 1.0;
		if(domainViolation) {
			score -= 0.5;
		}
		if(rangeViolation) {
			score -= 0.5;
		}
		
		evidence.getFeatures().setValue(AbstractFeature.DOMAIN_RANGE_CHECK, score);
	}

}
