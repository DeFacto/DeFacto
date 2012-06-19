package org.aksw.helper;

import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * Some convenience methods related to SPARQL.
 * 
 * @author Jens Lehmann
 *
 */
public class SPARQL {

	private String endpoint;
	private String graph;
	
	public SPARQL(String endpoint, String graph) {
		this.endpoint = endpoint;
		this.graph = graph;
	}
	
	public ResultSet executeSelectQuery(String query) {
		return SPARQL.executeSelectQuery(endpoint, graph, query);
	}
	
	public Model executeConstructQuery(String query) {
		return SPARQL.executeConstructQuery(endpoint, graph, query);
	}
	
	public boolean executeAskQuery(String query) {
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint,query);
//		queryExecution.setTimeout(10); // works only in new Jena versions
		List<String> defaultGraphURIs = new LinkedList<String>();
		defaultGraphURIs.add(graph);
		queryExecution.setDefaultGraphURIs(defaultGraphURIs);
		return queryExecution.execAsk();
	}
	
	/**
	 * 
	 * @param query Query returning a single value using ?count as variable.
	 * @return Value of ?count.
	 */
	public int count(String query) {
		return executeSelectQuery(query).next().getLiteral("count").getInt();
	}
	
	public String getLabel(String resource) {
		return executeSelectQuery("SELECT ?label { <" + resource + "> rdfs:label ?label } LIMIT 1").next().getLiteral("label").toString();
	}
	
	public String getEnLabel(String resource) {
		String query = "SELECT ?label { <" + resource + "> rdfs:label ?label . FILTER ( lang(?label) = \"en\" ) } LIMIT 1";
//		System.out.println(query);
		return executeSelectQuery(query).next().getLiteral("label").getLexicalForm();
	}	
	
	public String getEnLabelLiteral(String resource) {
		String label = getEnLabel(resource);
		return "\"" + label + "\"@en";
	}	
	
	public String getEnLabelLiteralEscaped(String resource) {
		String label = getEnLabel(resource);
		return "\"" + label.replace("\"", "\\\"") + "\"@en";
	}
	
	public static ResultSet executeSelectQuery(String endpoint, String graph, String query) {
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint,query);
//		queryExecution.setTimeout(10); // works only in new Jena versions
		List<String> defaultGraphURIs = new LinkedList<String>();
		defaultGraphURIs.add(graph);
		queryExecution.setDefaultGraphURIs(defaultGraphURIs);
		return queryExecution.execSelect();
	}
	
	public static Model executeConstructQuery(String endpoint, String graph, String query) {
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint,query);
//		queryExecution.setTimeout(10); // works only in new Jena versions
		List<String> defaultGraphURIs = new LinkedList<String>();
		defaultGraphURIs.add(graph);
		queryExecution.setDefaultGraphURIs(defaultGraphURIs);
		return queryExecution.execConstruct();
	}
	
}
