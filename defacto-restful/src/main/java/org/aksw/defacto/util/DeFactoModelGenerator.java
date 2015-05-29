/**
 * 
 */
package org.aksw.defacto.util;

import java.util.Arrays;
import java.util.Set;

import org.aksw.defacto.model.DefactoModel;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class DeFactoModelGenerator {
	
	private String labelProperty = "http://www.w3.org/2000/01/rdf-schema#label";
	private String[] languages = new String[]{"en", "fr", "de"};

	private QueryExecutionFactory qef;
	
	public DeFactoModelGenerator(SparqlEndpoint endpoint) {
		this(endpoint, "http://www.w3.org/2000/01/rdf-schema#label");
	}
	
	public DeFactoModelGenerator(SparqlEndpoint endpoint, String labelProperty) {
		this.labelProperty = labelProperty;
		
		qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
	}
	
	/**
	 * Build the DeFacto model, i.e. get labels for the given resource."
	 * @param uri
	 * @param languages the language tags, e.g. en, fr, de,...
	 * @return
	 */
	public DefactoModel generateModel(String uri){
		return generateModel(uri, languages);
	}
	
	/**
	 * Build the DeFacto model, i.e. get labels for the given resource."
	 * @param uri
	 * @param languages the language tags, e.g. en, fr, de,...
	 * @return
	 */
	public DefactoModel generateModel(Set<String> uris){
		return generateModel(uris, languages);
	}
	
	
	/**
	 * Build the DeFacto model, i.e. get labels for the given resource."
	 * @param uri
	 * @param languages the language tags, e.g. en, fr, de,...
	 * @return
	 */
	public DefactoModel generateModel(String uri, String... languages){
		return generateModel(Sets.newHashSet(uri), languages);
	}
	
	/**
	 * Build the DeFacto model, i.e. get labels for the given resource."
	 * @param uri
	 * @param languages the language tags, e.g. en, fr, de,...
	 * @return
	 */
	public DefactoModel generateModel(Set<String> uris, String... languages){
		Model model = ModelFactory.createDefaultModel();
		
		for (String uri : uris) {
			String query = "CONSTRUCT {<" + uri + "> <" + labelProperty + "> ?o} WHERE {<" + uri + "> <" + labelProperty + "> ?o.";
			if (languages.length > 0) {
				query += "FILTER(";
				for (int i = 0; i < languages.length; i++) {
					String lang = languages[i];
					query += "LANGMATCHES(LANG(?o),'" + lang + "')";
					if(i < languages.length-1){
						query += " || ";
					}
				}
				query += ")";
			}
			query += "}";
			
			QueryExecution qe = qef.createQueryExecution(query);
			qe.execConstruct(model);
			qe.close();
		}
		
		return new DefactoModel(model, String.valueOf(uris.hashCode()), true, Arrays.asList(languages));
	}
	
	public static void main(String[] args) throws Exception {
		DefactoModel model = new DeFactoModelGenerator(SparqlEndpoint.getEndpointDBpedia()).generateModel("http://dbpedia.org/resource/Leipzig");
	}
}
