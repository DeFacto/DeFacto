package org.aksw.defacto.util;

import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * Some convenience methods related to SparqlUtil.
 * 
 * @author Jens Lehmann
 *
 */
public class SparqlUtil {

	private String endpoint;
	private String graph;
	
	/**
	 * 
	 * @param endpoint
	 * @param graph
	 */
	public SparqlUtil(String endpoint, String graph) {
		this.endpoint = endpoint;
		this.graph = graph;
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	public ResultSet executeSelectQuery(String query) {
		return SparqlUtil.executeSelectQuery(endpoint, graph, query);
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	public Model executeConstructQuery(String query) {
		return SparqlUtil.executeConstructQuery(endpoint, graph, query);
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
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
	
	/**
	 * 
	 * @param resource
	 * @return
	 */
	public String getLabel(String resource) {
		return executeSelectQuery("SELECT ?label { <" + resource + "> rdfs:label ?label } LIMIT 1").next().getLiteral("label").toString();
	}
	
	/**
	 * 
	 * @param resource
	 * @return
	 */
	public String getEnLabel(String resource) {
		String query = "SELECT ?label { <" + resource + "> rdfs:label ?label . FILTER ( lang(?label) = \"en\" ) } LIMIT 1";
		System.out.println(query);
		return executeSelectQuery(query).next().getLiteral("label").getLexicalForm();
	}	
	
	
	public static void main(String[] args) {

        SparqlUtil util = new SparqlUtil("http://live.dbpedia.org/sparql", "http://dbpedia.org");
        System.out.println(util.getEnLabel("http://dbpedia.org/resource/Medal_of_Honor"));
    }
	
	/**
	 * 
	 * @param resource
	 * @return
	 */
	public String getEnLabelLiteral(String resource) {
		String label = getEnLabel(resource);
		return "\"" + label + "\"@en";
	}	
	
	/**
	 * 
	 * @param resource
	 * @return
	 */
	public String getEnLabelLiteralEscaped(String resource) {
		String label = getEnLabel(resource);
		return "\"" + label.replace("\"", "\\\"") + "\"@en";
	}
	
	/**
	 * 
	 * @param endpoint
	 * @param graph
	 * @param query
	 * @return
	 */
	public static ResultSet executeSelectQuery(String endpoint, String graph, String query) {
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint,query);
//		queryExecution.setTimeout(10); // works only in new Jena versions
		List<String> defaultGraphURIs = new LinkedList<String>();
		defaultGraphURIs.add(graph);
		queryExecution.setDefaultGraphURIs(defaultGraphURIs);
		return queryExecution.execSelect();
	}
	
	/**
	 * 
	 * @param endpoint
	 * @param graph
	 * @param query
	 * @return
	 */
	public static Model executeConstructQuery(String endpoint, String graph, String query) {
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint,query);
//		queryExecution.setTimeout(10); // works only in new Jena versions
		List<String> defaultGraphURIs = new LinkedList<String>();
		defaultGraphURIs.add(graph);
		queryExecution.setDefaultGraphURIs(defaultGraphURIs);
		return queryExecution.execConstruct();
	}
	
}
