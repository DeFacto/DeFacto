/**
 * 
 */
package org.aksw.defacto;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author gerb
 *
 */
public class Constants {

	public enum EvidenceType {POS, NEG}; //pos -> positive claims / neg -> possible claims that could refute the pos ones

	/* esteves: adding indexed wikipedia */
	public static final String LUCENE_WIKI_PAGEID_FIELD				= "id";
	public static final String LUCENE_WIKI_TITLE_FIELD 				= "titleText";
	public static final String LUCENE_WIKI_PAGETEXT_FIELD 			= "text";

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
    //pattern suport
    public static final String LUCENE_SEARCH_RESULT_PATTERN_FIELD		= "query";
	
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
	
	public static final Set<String> STOP_WORDS = new HashSet<String>();
	static {
		Collections.addAll(STOP_WORDS, ":", " ", "``", "`", "_NE_", "''", ",", "'", "'s", "-LRB-", "-RRB-", ".", "-", "--", "i", "a", "about", "an", "and", "are", "as", "at", "be", "but", "by", "com", "for", "from", 
		        "how", "in", "is", "it", "of", "on", "or", "that", "the", "The", "was", "were", "à",
				"under", "this", "to", "what", "when", "where", "who", "will", "with", "the", "www", "before", ",", "after", ";", "like", "and", "such", "-LRB-", "-RRB-", "-lrb-", "-rrb-", "aber", "als",
				"am", "an", "auch", "auf", "aus", "bei", "bin", "bis", "bist", "da", "dadurch", "daher", "darum", "das", "daß", "dass", "dein", "deine", "dem", "den", "der", "des", "dessen",
				"deshalb", "die", "dies", "dieser", "dieses", "doch", "dort", "du", "durch", "ein", "eine", "einem", "einen", "einer", "eines", "er", "es", "euer", "eure", "für", "hatte", "hatten",
				"hattest", "hattet", "hier", "hinter", "ich", "ihr", "ihre", "im", "in", "ist", "ja", "jede", "jedem", "jeden", "jeder", "jedes", "jener", "jenes", "jetzt", "kann", "kannst",
				"können", "könnt", "machen", "mein", "meine", "mit", "muß", "mußt", "musst", "müssen", "müßt", "nach", "nachdem", "nein", "nicht", "nun", "oder", "seid", "sein", "seine", "sich",
				"sie", "sind", "soll", "sollen", "sollst", "sollt", "sonst", "soweit", "sowie", "und", "unser", "unsere", "unter", "vom", "von", "vor", "wann", "warum", "weiter", "weitere", "wenn",
				"wer", "werde", "war", "wurde", "um", "werden", "werdet", "weshalb", "wie", "wieder", "wieso", "wir", "wird", "wirst", "wo", "woher", "wohin", "zu", "zum", "zur", "über",
				"alors", "au", "aucuns", "aussi", "autre", "avant", "avec", "avoir", "bon", "car", "ce", "cela", "ces", "ceux", "chaque", "ci", "comme", "comment",
				"dans", "de", "des", "du", "dedans", "dehors", "depuis", "deux", "devrait", "doit", "donc", "dos", "droite", "début", "elle", "elles", "en", "encore", 
				"essai", "est", "et", "eu", "fait", "faites", "fois", "font", "force", "haut", "hors", "ici", "il", "ils", "je", "juste", "la", "le", "les", "leur",
				"là", "ma", "maintenant", "mais", "mes", "mine", "moins", "mon", "mot", "même", "ni", "nommés", "notre", "nous", "nouveaux", "ou", "où", "par", "parce",
				"parole", "pas", "personnes", "peut", "peu", "pièce", "plupart", "pour", "pourquoi", "quand", "que", "quel", "quelle", "quelles", "quels", "qui", "sa", 
				"sans", "ses", "seulement", "si", "sien", "son", "sont", "sous", "soyez", "sujet", "sur", "ta", "tandis", "tellement", "tels", "tes", "ton", "tous", 
				"tout", "trop", "très", "tu", "valeur", "voie", "voient", "vont", "votre", "vous", "vu", "ça", "étaient", "état", "étions", "été", "être");
	}
	
	public static final Set<String> NEW_STOP_WORDS = new HashSet<String>();
	static {
		Collections.addAll(NEW_STOP_WORDS, "","''", ":", " ", "``", "`", "_NE_", "''", ",", "'", "'s", /*"-LRB-", "-RRB-",*/ ".", "-", "--", "i", "a", "about", "an", "and", "are", "as", "at", "be", "but", "by", "com", "for", "from", 
		        "how", "in", "is", "it", "of", "on", "or", "that", "the", "The", "was", "were", "à", "den",
				"under", "this", "to", "what", "when", "where", "who", "will", "with", "the", "www", "before", ",", "after", ";", "like", "and", "such", "-LRB-", "-RRB-", "-lrb-", "-rrb-", "aber", "als",
				"am", "an", "auch", "auf", "aus", "bei", "bin", "bis", "bist", "da", "dadurch", "daher", "darum", "das", "daß", "dass", "dein", "deine", "dem", "den", "der", "des", "dessen",
				"deshalb", "die", "dies", "dieser", "dieses", "doch", "dort", "du", "durch", "ein", "eine", "einem", "einen", "einer", "eines", "er", "es", "euer", "eure", "für", "hatte", "hatten",
				"hattest", "hattet", "hier", "hinter", "ich", "ihr", "ihre", "im", "in", "ist", "ja", "jede", "jedem", "jeden", "jeder", "jedes", "jener", "jenes", "jetzt", "kann", "kannst",
				"können", "könnt", "machen", "mein", "meine", "mit", "muß", "mußt", "musst", "müssen", "müßt", "nach", "nachdem", "nein", "nicht", "nun", "oder", "seid", "sein", "seine", "sich",
				"sie", "sind", "soll", "sollen", "sollst", "sollt", "sonst", "soweit", "sowie", "und", "unser", "unsere", "unter", "vom", "von", "vor", "wann", "warum", "weiter", "weitere", "wenn",
				"wer", "werde", "war", "wurde", "um", "werden", "werdet", "weshalb", "wie", "wieder", "wieso", "wir", "wird", "wirst", "wo", "woher", "wohin", "zu", "zum", "zur", "über",
				"alors", "au", "aucuns", "aussi", "autre", "avant", "avec", "avoir", "bon", "car", "ce", "cela", "ces", "ceux", "chaque", "ci", "comme", "comment",
				"dans", "des", "du", "dedans", "dehors", "depuis", "deux", "devrait", "doit", "donc", "dos", "droite", "début", "elle", "elles", "en", "encore", 
				"essai", "est", "et", "eu", "fait", "faites", "fois", "font", "force", "haut", "hors", "ici", "il", "ils", "je", "juste", "la", "le", "les", "leur",
				"là", "ma", "maintenant", "mais", "mes", "mine", "moins", "mon", "mot", "même", "ni", "nommés", "notre", "nous", "nouveaux", "ou", "où", "par", "parce",
				"parole", "pas", "personnes", "peut", "peu", "pièce", "plupart", "pour", "pourquoi", "quand", "que", "quel", "quelle", "quelles", "quels", "qui", "sa", 
				"sans", "ses", "seulement", "si", "sien", "son", "sont", "sous", "soyez", "sujet", "sur", "ta", "tandis", "tellement", "tels", "tes", "ton", "tous", 
				"tout", "trop", "très", "tu", "valeur", "voie", "voient", "vont", "votre", "vous", "vu", "ça", "étaient", "état", "étions", "été", "être",
				"dean", "priscilla", "bates", "alan", "prinzessin", "bayern", "österreich", "daughter");
	}
	
	/**
	 * Use this property to write new lines in files or stdouts
	 */
	public static final String NEW_LINE_SEPARATOR	= System.getProperty("line.separator");
	
	public enum LANGUAGE {
		
		en,
		de
	}
}
