package org.aksw.defacto.ml.training;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.aksw.defacto.util.SparqlUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Creates a training set for confidence estimation using DBpedia. Positive examples
 * are facts contained in DBpedia (we assume they are true) and negative examples
 * are modifications of those facts derived via different methods.
 * 
 * @author Jens Lehmann
 *
 */
public class TrainingSetCreatorDBpedia implements TrainingSetCreator {

	Logger logger = LoggerFactory.getLogger(TrainingSetCreatorDBpedia.class);
	
	private String endpoint = "http://live.dbpedia.org/sparql";
	private String graph = "http://dbpedia.org";
	private SparqlUtil sparql;
	private Random rand;
	
	private String propertyList = "resources/properties/properties.txt";
	private String posDir = "resources/training/data/true/";
	private String negDir = "resources/training/data/false/";
	
	// statistical values
	private int nrOfProperties;
	List<String> properties;
	
	// configuration options
	private int posExamplesPerProperty = 10;
	private int maxOffset = 50000; // 100000; // for birthPlace we need huge offsets
	
	public TrainingSetCreatorDBpedia() {
		sparql = new SparqlUtil(endpoint,graph);
		rand = new Random(201206);
		try {
			run();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() throws IOException {
		nrOfProperties = sparql.count("SELECT COUNT(?p) AS ?count {?p a owl:ObjectProperty}");
		
		// test with a single property for now
//		run("http://dbpedia.org/ontology/residence");
//		System.exit(0);
		
		// get all properties and create training data for each of them
		properties = FileUtils.readLines(new File(propertyList));
		for(String property : properties) {
			
			// occupation does not work at the moment - instances do not have labels
			if(!property.equals("http://dbpedia.org/ontology/occupation")) {
				run(property);
			}		
			
			
		}
	}	
	
	public void run(String property) throws FileNotFoundException {	
		// http://docs.openlinksw.com/virtuoso/virtuosotipsandtricks.html => 1.5.32 => bif:rnd
		
		String domain = getDomain(property);
		String range = getRange(property);
		String label = sparql.getEnLabel(property);
		String fileLabel = label.replace(" ", "_");
		
		// check whether the last created file for this property already exists => if yes, skip it (allows to restart the script)
		String lastCreatedFile = negDir + fileLabel + "_" + (posExamplesPerProperty-1) + "_random.ttl";
		if(new File(lastCreatedFile).exists()) {
			logger.info("Skipping " + property + " as it was already created (delete files to re-run training data creation).");
			return;
		}
		
		int facts = sparql.count("SELECT COUNT(?s) AS ?count { ?s <"+property+"> ?o . ?s rdfs:label ?slabel . ?o rdfs:label ?olabel .  } ");
		
		// TODO: check whether we need sub-selects or can just query the number of instances directly
		int domainCount = sparql.count("SELECT COUNT(?s) AS ?count { { SELECT ?s { ?s a <"+domain+"> } LIMIT "+maxOffset+" }} ");
		if(domainCount == 0) {
			System.out.println("Ignoring property " + property + " because its domain " + domain + " has no instances.");
			return;
		}
		int rangeCount = sparql.count("SELECT COUNT(?s) AS ?count { { SELECT ?s { ?s a <"+range+"> } LIMIT "+maxOffset+" }} ");
		if(rangeCount == 0) {
			System.out.println("Ignoring property " + property + " because its range " + range + " has no instances.");
			return;
		}
		 
		logger.info("Generating training data for property " + property + " (domain: " + domain + ", range: " + range + ", facts: " + facts + ", label: " + label + ").");
		
		// construction of positive examples
		Model posExamples = ModelFactory.createDefaultModel();
		// we use a random offset to get random facts, which unfortunately means we have to run one query per example
		for(int i=0; i<posExamplesPerProperty; i++) {
			// we limit the offset to max. 10000
			int offset = rand.nextInt(Math.min(maxOffset, facts));
			// TODO: it might be more efficient to get the labels later because the endpoint does not need to do a join
			String query = "CONSTRUCT {?s <"+property+"> ?o . ?s rdfs:label ?slabel . ?o rdfs:label ?olabel .} { ?s <"+property+"> ?o . ?s rdfs:label ?slabel . ?o rdfs:label ?olabel } OFFSET " + offset + " LIMIT 1";
//			System.out.println(query);
			Model tmp = sparql.executeConstructQuery(query);
			writeModel(tmp, posDir + fileLabel + "_" + i + ".ttl");
			posExamples = posExamples.union(tmp);
		}
		
		// get some facts from DBpedia (including labels)
		// TODO: we need a more randomized method here
//		String query = "CONSTRUCT {?s ?p ?o . ?s rdfs:label ?slabel . ?p rdfs:label ?plabel . ?o rdfs:label ?olabel .} { ?s ?p ?o . ?s rdfs:label ?slabel . ?p rdfs:label ?plabel . ?o rdfs:label ?olabel . FILTER ( 1>  <SHORT_OR_LONG::bif:rnd>  (100, ?s, ?p, ?o)) . ?p a owl:ObjectProperty . FILTER(?p != \"http://dbpedia.org/ontology/wikiPageExternalLink\") . FILTER(regex(str(?p),\"http://dbpedia.org/ontology/.*\")) } OFFSET 1000 LIMIT 10";
//		String query = "CONSTRUCT {?s <"+property+"> ?o . ?s rdfs:label ?slabel . ?o rdfs:label ?olabel .} { ?s <"+property+"> ?o . ?s rdfs:label ?slabel . ?o rdfs:label ?olabel } LIMIT 10";
//		Model model = SparqlUtil.executeConstructQuery(endpoint, graph, query);
		
		// construction of negative examples
		StmtIterator it = posExamples.listStatements();
//		Model modelRange = null;
//		Model modelDomain = null;
		
		int i=0;
		while(it.hasNext()) {
			Statement st = it.next();

			// the "non-rdfs:label" statements are those from which we can construct negative examples
			if(!st.getPredicate().hasURI("http://www.w3.org/2000/01/rdf-schema#label")) {
				logger.debug("  positive example: " + st);				
				String subject = st.getSubject().toString();
				String subjectLabel2 = sparql.getEnLabel(subject);
				String subjectLabel = sparql.getEnLabelLiteral(subject); // we actually queried the label before, so query load could be optimised here
				String subjectLabelEscaped = sparql.getEnLabelLiteralEscaped(subject);
				String object = st.getObject().toString();
				String objectLabel = sparql.getEnLabelLiteral(object);
				String objectLabel2 = sparql.getEnLabel(object);
				String objectLabelEscaped = sparql.getEnLabelLiteralEscaped(object);
				Model tmp;
				String query;
				
				// 1: get a statement with equal object, but the subject is replaced by one with the correct
				// domain, but no such fact exists within DBpedia
//				int count = sparql.count("SELECT COUNT(?s) AS ?count{ ?s a <"+domain+"> . FILTER NOT EXISTS { ?s <"+property+"> <"+object+"> . }  }");
//				System.out.println("count: " + count);
				// for the sake of efficiency, we could make assumptions here: domain and range restrictions are correct in DBpedia and discarding the object 
				// (via FILTER NOT EXISTS) does not have a major influence - this way we can reuse the overall property count, but it could happen that we get empty results,
				// which is why we have to check for empty models here
				do {
				int offsetD = rand.nextInt(Math.min(maxOffset, domainCount));
				// idea for count: use subqueries with LIMIT 10000 and then perform a count, e.g.
				// SELECT COUNT(?s) { { SELECT ?s { ?s a $class } LIMIT 10000 } 
				// http://stackoverflow.com/questions/5677340/how-to-select-random-dbpedia-nodes-from-sparql
				// SELECT ?s
//				WHERE {
//					  ?s ?p ?o
//					}
//					ORDER BY RAND()
//					LIMIT 10
				
				// count trick: we use a subselect to retrieve those objects, which we want, but limit them to some number
//				int n = sparql.count("SELECT COUNT(?s) AS ?count { { SELECT ?s { ?s a <"+domain+"> . FILTER NOT EXISTS { ?s <"+property+"> <"+object+"> . }  . ?s rdfs:label ?slabel . } LIMIT 10000 } }");
				query = "CONSTRUCT { ?s <"+property+"> <"+object+">  . ?s rdfs:label ?slabel . <"+object+"> rdfs:label "+objectLabelEscaped+" . } { ?s a <"+domain+"> . FILTER NOT EXISTS { ?s <"+property+"> <"+object+"> . }  . ?s rdfs:label ?slabel . } OFFSET " + offsetD + " LIMIT 1";
				logger.debug("  query: " + query);
				tmp = SparqlUtil.executeConstructQuery(endpoint, graph, query);
				} while(tmp.isEmpty()); // repeat with different offset in (rare) cases when the model is empty
//				writeModel(tmp, negDir + fileLabel + "_" + i + "_dom.ttl");
				writeModel(tmp, negDir + "domain/" + fileLabel + "_" + i + ".ttl");
				
				// 2: same but for range
				do {
				int offsetR = rand.nextInt(Math.min(maxOffset, rangeCount));
				query = "CONSTRUCT { <"+subject+"> <"+property+"> ?o . <"+subject+"> rdfs:label "+subjectLabelEscaped+"  . ?o rdfs:label ?olabel .} { ?o a <"+range+"> . FILTER NOT EXISTS { <"+subject+"> <"+property+"> ?o . } . ?o rdfs:label ?olabel . } OFFSET " + offsetR + " LIMIT 1";
				logger.debug("  query: " + query);
				tmp = SparqlUtil.executeConstructQuery(endpoint, graph, query);
				} while(tmp.isEmpty());
//				writeModel(tmp, negDir + fileLabel + "_" + i + "_ran.ttl");
				writeModel(tmp, negDir + "range/" + fileLabel + "_" + i + ".ttl");
				
				// 3: replace both taking domain/range into account
//				try {
				do {
				// Virtuoso places a very high time estimate on queries, in which you have two very broad restrictions (domain and range) without a join between them,
				// so we first get random resources and then verify that they are not connected via the given property
				// TODO: this won't work if all elements of domain and range are always connected via the current property
				int offsetSub = rand.nextInt(domainCount);
				int offsetObj = rand.nextInt(rangeCount);
				query = "SELECT ?s { ?s a <"+domain+"> } OFFSET " + offsetSub + " LIMIT 1";
				logger.debug("  query: " + query);
				String s = sparql.executeSelectQuery(query).next().get("s").toString();
				
				query = "SELECT ?o { ?o a <"+range+"> } OFFSET " + offsetObj + " LIMIT 1";
				logger.debug("  query: " + query);		
				String o = sparql.executeSelectQuery(query).next().get("o").toString();
				
//				boolean exists = sparql.executeAskQuery("ASK { <"+s+"> <"+property+"> <"+o+"> }");
				// check whether we have not accidentally found a pair which is connected via the property - the construct query is a awkward way to check this,
				// but it saves the effort of having to create a model manually and it retrieves the labels of subject and object
				query = "CONSTRUCT { <"+s+"> <"+property+"> <"+o+"> . <"+s+"> rdfs:label ?slabel . <"+o+"> rdfs:label ?olabel .} {  FILTER NOT EXISTS { <"+s+"> <"+property+"> <"+o+"> . } . <"+s+"> rdfs:label ?slabel . <"+o+"> rdfs:label ?olabel . } LIMIT 1";
				tmp = SparqlUtil.executeConstructQuery(endpoint, graph, query);
				} while(tmp.isEmpty());
//				writeModel(tmp, negDir + fileLabel + "_" + i + "_dr.ttl");
				writeModel(tmp, negDir + "domain_range/" + fileLabel + "_" + i + ".ttl");
				
//				int offsetDR = rand.nextInt(Math.min(maxOffset, domainCount * rangeCount));
//				query = "CONSTRUCT { ?s <"+property+"> ?o . ?s rdfs:label ?slabel . ?o rdfs:label ?olabel .} {  ?s a <"+domain+"> . ?o a <"+range+"> . FILTER NOT EXISTS { ?s <"+property+"> ?o . } . ?s rdfs:label ?slabel . ?o rdfs:label ?olabel . } OFFSET " + offsetDR + " LIMIT 1";
//				logger.debug("  query: " + query);
//				tmp = SparqlUtil.executeConstructQuery(endpoint, graph, query);
//				} while(tmp.isEmpty());
//				writeModel(tmp, negDir + fileLabel + "_" + i + "_dr.ttl");				
//				} catch(Exception e)  {
//					logger.warn("  skipping because of query problems.");
//				}
				
				// 4a: replace the property by a random different property - skip because we cannot compute factFeatures for all properties at the moment
//				int offsetP = rand.nextInt(nrOfProperties);
//				query = "CONSTRUCT { <"+subject+"> ?p <"+object+"> . <"+subject+"> rdfs:label "+subjectLabel+" .  <"+object+"> rdfs:label "+objectLabel+" . } { ?p a owl:ObjectProperty . FILTER NOT EXISTS { <"+subject+"> ?p <"+object+"> . }  } OFFSET " + offsetP + " LIMIT 1";
//				logger.debug("  query: " + query);
//				tmp = SparqlUtil.executeConstructQuery(endpoint, graph, query);
//				writeModel(tmp, negDir + fileLabel + "_" + i + "_prop.ttl");
				
				// 4b: replace the property by a different property in our list
				String otherProp;
				do {
				otherProp = properties.get(rand.nextInt(properties.size()));
				} while(otherProp.equals(property));
				tmp = ModelFactory.createDefaultModel();
				tmp.add(ResourceFactory.createStatement(ResourceFactory.createResource(subject), ResourceFactory.createProperty(otherProp), ResourceFactory.createResource(object)));
				tmp.add(ResourceFactory.createStatement(ResourceFactory.createResource(subject), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"), tmp.createLiteral(subjectLabel2, "en")));
				tmp.add(ResourceFactory.createStatement(ResourceFactory.createResource(object), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"), tmp.createLiteral(objectLabel2, "en")));
//				writeModel(tmp, negDir + fileLabel + "_" + i + "_prop.ttl");
				writeModel(tmp, negDir + "property/" + fileLabel + "_" + i + ".ttl");
				
				// 5: create a random triple
				int offsetSub = rand.nextInt(maxOffset);
				int offsetProp = rand.nextInt(nrOfProperties);
				int offsetObj = rand.nextInt(maxOffset);
				query = "SELECT ?s ?label { ?s ?p ?o . ?p a owl:ObjectProperty . ?s a owl:Thing . ?s rdfs:label ?label} OFFSET " + offsetSub + "LIMIT 1";
//				query = "SELECT ?s ?label { ?s ?p ?o . ?p a owl:ObjectProperty . ?s rdfs:label ?label . FILTER ( lang(?label) = \"en\" )} OFFSET " + offsetSub + "LIMIT 1";
				logger.debug("  query: " + query);
				QuerySolution qs = sparql.executeSelectQuery(query).next();
				String sub = qs.get("s").toString();
				String subLabel = qs.getLiteral("label").getLexicalForm();
				// any other property
//				query = "SELECT ?p { ?p a owl:ObjectProperty . } OFFSET " + offsetProp + "LIMIT 1";
//				logger.debug("  query: " + query);
//				String prop = sparql.executeSelectQuery(query).next().get("p").toString();
				// another property in the list
				String prop;
				do {
				prop = properties.get(rand.nextInt(properties.size()));
				} while(prop.equals(property));
				query = "SELECT ?o ?label { ?s ?p ?o . ?p a owl:ObjectProperty .  ?o a owl:Thing . ?o rdfs:label ?label} OFFSET " + offsetObj + "LIMIT 1";
//				query = "SELECT ?o ?label { ?s ?p ?o . ?p a owl:ObjectProperty .  ?o rdfs:label ?label . FILTER ( lang(?label) = \"en\" )} OFFSET " + offsetObj + "LIMIT 1";				
				logger.debug("  query: " + query);
				qs = sparql.executeSelectQuery(query).next();
				String obj = qs.get("o").toString();
				String objLabel = qs.getLiteral("label").getLexicalForm() ;
				tmp = ModelFactory.createDefaultModel();
				tmp.add(ResourceFactory.createStatement(ResourceFactory.createResource(sub), ResourceFactory.createProperty(prop), ResourceFactory.createResource(obj)));
				tmp.add(ResourceFactory.createStatement(ResourceFactory.createResource(sub), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"), tmp.createLiteral(subLabel, "en")));
				tmp.add(ResourceFactory.createStatement(ResourceFactory.createResource(obj), ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"), tmp.createLiteral(objLabel, "en")));
//				writeModel(tmp, negDir + fileLabel + "_" + i + "_random.ttl");
				writeModel(tmp, negDir + "random/" + fileLabel + "_" + i + ".ttl");
				
				i++;
			}
		}
//		writeModel(modelDomain,negDir + "birthPlaceDomain.ttl");
//		writeModel(modelRange,negDir + "birthPlaceRange.ttl");
	}
	
	private String getDomain(String property) {
		String query = "SELECT ?domain WHERE { <"+property+"> rdfs:domain ?domain } LIMIT 1";
		ResultSet rs = sparql.executeSelectQuery(query);
		if(rs.hasNext()) {
			return rs.next().get("domain").toString();	
		} else {
			// TODO: if domain and/or range do not exist, take a sample triple and use this for
			// domain/range			
			return "http://www.w3.org/2002/07/owl#Thing";
		}
	}
	
	private String getRange(String property) {
		String query = "SELECT ?domain WHERE { <"+property+"> rdfs:range ?domain } LIMIT 1";
		ResultSet rs = sparql.executeSelectQuery(query);
		if(rs.hasNext()) {
			return rs.next().get("domain").toString();	
		} else {
			return "http://www.w3.org/2002/07/owl#Thing";
		}		
	}	
	
	private void writeModel(Model model, String file) throws FileNotFoundException {
		FileOutputStream fout=new FileOutputStream(file);
		model.write(fout, "TURTLE");
	}
	
	@Override
	public Triple generatePositives() {
		return null;
	}

	@Override
	public Triple generateNegatives() {

		return null;
	}
	
	public static void main(String[] args) {
		Layout layout = new PatternLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);
		
		TrainingSetCreatorDBpedia ts = new TrainingSetCreatorDBpedia();
		
	}
	
}
