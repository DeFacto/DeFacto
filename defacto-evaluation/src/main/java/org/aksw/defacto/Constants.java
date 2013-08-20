/**
 * 
 */
package org.aksw.defacto;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author gerb
 *
 */
public class Constants {

	public static final String LUCENE_SEARCH_RESULT_ID_FIELD		= "id";
	public static final String LUCENE_SEARCH_RESULT_QUERY_FIELD		= "query";
	public static final String LUCENE_SEARCH_RESULT_HIT_COUNT_FIELD = "hits";
	public static final String LUCENE_SEARCH_RESULT_URL_FIELD		= "url";
	public static final String LUCENE_SEARCH_RESULT_RANK_FIELD		= "rank";
	public static final String LUCENE_SEARCH_RESULT_PAGE_RANK_FIELD = "pagerank";
	public static final String LUCENE_SEARCH_RESULT_CONTENT_FIELD	= "content";
	public static final String LUCENE_SEARCH_RESULT_TITLE_FIELD		= "title";
	public static final String LUCENE_SEARCH_RESULT_CREATED_FIELD	= "created";
	public static final String LUCENE_SEARCH_RESULT_TAGGED_FIELD	= "tagged";
	public static final String LUCENE_SEARCH_RESULT_LANGUAGE		= "language";
	
	public static final String LUCENE_TOPIC_TERM_LABEL = "label";
	public static final String LUCENE_TOPIC_TERM_RELATED_TERM = "related";
	public static final String TOPIC_TERM_SEPARATOR = "\t";
	
	public static final String DBPEDIA_RESOURCE_NAMESPACE = "http://dbpedia.org/resource/";
	public static final String DE_DBPEDIA_RESOURCE_NAMESPACE = "http://de.dbpedia.org/resource/";
	public static final String FR_DBPEDIA_RESOURCE_NAMESPACE = "http://fr.dbpedia.org/resource/";
	public static final String FREEBASE_RESOURCE_NAMESPACE = "http://rdf.freebase.com/ns";
	public static final String DBPEDIA_ONTOLOGY_NAMESPACE = "http://dbpedia.org/ontology/";
	
	
	public static final Property DEFACTO_FROM = ResourceFactory.createProperty("http://dbpedia.org/ontology/from");
	public static final Property DEFACTO_TO = ResourceFactory.createProperty("http://dbpedia.org/ontology/to");
	public static final Property DEFACTO_ON = ResourceFactory.createProperty("http://dbpedia.org/ontology/on");
	public static final Property RDFS_LABEL = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
	public static final Property SKOS_ALT_LABEL = ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#altLabel");
	public static final Property OWL_SAME_AS = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#sameAs");
	
	public static final String NO_LABEL = "no-label";
	public static final String NAMED_ENTITY_TAG_DELIMITER = "_";
	
	
	public enum LANGUAGE {
		
		en,
		de
	}
}
