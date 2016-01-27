package org.aksw.defacto.boa;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aksw.defacto.Defacto;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class BoaPatternSearcher {

    private static HttpSolrServer enIndex;
    private static HttpSolrServer deIndex;
    private static HttpSolrServer frIndex;
    private static Logger logger = Logger.getLogger(BoaPatternSearcher.class);
    private static Logger LOGDEV = Logger.getLogger("developer");
	private Map<String,QueryResponse> queryCache = new HashMap<>();

    /*
    enamex -> ORGANIZATION, PERSON, LOCATION
     */

    /*
    * http://dbpedia.org/ontology/award - ?D = dbo:Person / ?R = dbo:Award
    */
    private static String NER_AWARD = "PERSON;?R?";
    private static String NER_AWARD_INV = "?R?;PERSON";

    /*
    * http://dbpedia.org/ontology/nflTeam -> ?D = dbo:Athlete / ?R = dbo:SportsTeam
    */
    private static String NER_NFLTEAM = "PERSON;ORGANISATION";
    private static String NER_NFLTEAM_INV = "ORGANISATION;PERSON";

    /*
    * http://dbpedia.org/ontology/publicationDate -> dataProperty e nao objectProperty
    * Actually is "work/publication" and not date of publication...
    * http://dbpedia.org/ontology/publication -> dataProperty ?D = dbo:Person ?R = string
    */
    private static String NER_PUBLICATION_DATE = "PERSON;?R?";
    private static String NER_PUBLICATION_DATE_INV = "?R?;PERSON";

    /*
    * http://dbpedia.org/ontology/starring -> ?D = dbo:Work ?R = dbo:Actor
    */
    private static String NER_STARRING = "?D?;PERSON";
    private static String NER_STARRING_INV = "PERSON;?D?";

    /*
    * http://dbpedia.org/ontology/birthDate -> ?D = Person ?R = date
    * Actually is "birth place" and not birth date
    * Why not using http://dbpedia.org/ontology/birthPlace instead? -> ?D = dbo:Person ?R = dbo:Place
    */
    private static String NER_BIRTH_DATE = "PERSON;LOCATION";
    private static String NER_BIRTH_DATE_INV = "LOCATION;PERSON";

    /*
    * http://dbpedia.org/ontology/deathPlace -> ?D = dbo:Person ?R = dbo:Place
    */
    private static String NER_DEATH_PLACE = "PERSON;LOCATION";
    private static String NER_DEATH_PLACE_INV = "LOCATION;PERSON";

    /*
    * http://dbpedia.org/ontology/foundationPlace -> ?D = dbo:Organisation ?R = dbo:City
    */
    private static String NER_FOUNDATION_PLACE = "ORGANISATION;LOCATION";
    private static String NER_FOUNDATION_PLACE_INV = "LOCATION;ORGANISATION";

    /*
    * http://dbpedia.org/ontology/leaderName -> ?D = dbo:PopulatedPlace ?R = dbo:Person
    */
    private static String NER_LEARDER_NAME = "ORGANISATION;PERSON";
    private static String NER_LEARDER_NAME_INV = "PERSON;ORGANISATION";

    /*
    * http://dbpedia.org/ontology/spouse -> ?D = dbo:Person ?R = dbo:Person
    */
    private static String NER_SPOUSE = "PERSON;PERSON";
    private static String NER_SPOUSE_INV = "PERSON;PERSON";

    /*
    * http://dbpedia.org/ontology/subsidiary -> ?D = dbo:Company ?R dbo:Company
    */
    private static String NER_SUBSIDIARY = "ORGANISATION;ORGANISATION";
    private static String NER_SUBSIDIARY_INV = "ORGANISATION;ORGANISATION";


    public BoaPatternSearcher(){

        enIndex = new HttpSolrServer(Defacto.DEFACTO_CONFIG.getStringSetting("crawl", "solr_boa_en"));
        enIndex.setRequestWriter(new BinaryRequestWriter());
        deIndex = new HttpSolrServer(Defacto.DEFACTO_CONFIG.getStringSetting("crawl", "solr_boa_de"));
        deIndex.setRequestWriter(new BinaryRequestWriter());
        frIndex = new HttpSolrServer(Defacto.DEFACTO_CONFIG.getStringSetting("crawl", "solr_boa_fr"));
        frIndex.setRequestWriter(new BinaryRequestWriter());

    }
    
    /**
     * Queries the configured BOA index with the configured number of returned 
     * BOA Patterns (see Constants.NUMBER_OF_BOA_PATTERNS) and a pattern score
     * threshold of 0.5.
     * 
     * @param propertyUri
     * @param language 
     * @return
     */
    public List<Pattern> getNaturalLanguageRepresentations(String propertyUri, String language){

        int numberOfBoaPatterns = 50;
        return querySolrIndex(propertyUri,
                numberOfBoaPatterns,
                Defacto.DEFACTO_CONFIG.getDoubleSetting("boa", "PATTERN_SCORE_THRESHOLD"), language);
    }
    
    /**
     * Queries the configured BOA index with the configured number of returned 
     * BOA Patterns (see. Constants.NUMBER_OF_BOA_PATTERNS).
     * 
     * @param propertyUri 
     * @param numberOfBoaPatterns
     * @return
     */
    public List<Pattern> getNaturalLanguageRepresentations(String propertyUri, int numberOfBoaPatterns, String language){

        return querySolrIndex(propertyUri, numberOfBoaPatterns, 0.5D, language);
    }

    /**
     * It should be added to BOA Index, which supports just "positive" assessments.
     * In that case, the key should be altered in order to support POS and NEG cases for given URI.
     * However, since BOA is not maintained anymore, I will avoid extra work.
     * Moreover, many BOA features are not referenced in DeFacto's project, so let's try to keep it as simple as possible.
     * 
     * @param propertyUri
     * @param numberOfBoaPatterns
     * @param patternThreshold
     * @return
     */
    public List<Pattern> getInverseNaturalLanguageRepresentations(String propertyUri, String language){

        //We should think how we will (technically) merge this and current BOA architecture
        //return querySolrIndex(propertyUri, numberOfBoaPatterns, patternThreshold, language);
        Map<String,Pattern> patterns = new HashMap<>();

        if (propertyUri.equals("http://dbpedia.org/ontology/office"))
            propertyUri = "http://dbpedia.org/ontology/leaderName";

        try {

            if (propertyUri.equals("http://dbpedia.org/ontology/nflTeam")) {

            } else if (propertyUri.equals("http://dbpedia.org/ontology/publicationDate")) {

            } else if (propertyUri.equals("http://dbpedia.org/ontology/starring")) {

            } else if (propertyUri.equals("http://dbpedia.org/ontology/award")) {

                if (language.equals("en")){

                    this.logger.debug("Starting loading counter patterns for dbo:award: ");

                    Pattern pattern1 = new Pattern();
                    pattern1.generalized = "?D? _BE_ not awarded ?R?";
                    pattern1.naturalLanguageRepresentation = "?!D? was not awarded the ?R?";
                    pattern1.naturalLanguageRepresentationWithoutVariables = "was not awarded the";
                    pattern1.posTags = "VBD RB VBN DT";
                    pattern1.boaScore = 0d;
                    pattern1.language = language;
                    pattern1.NER = NER_AWARD;
                    patterns.put(pattern1.getNormalized(), pattern1);

                    Pattern pattern2 = new Pattern();
                    pattern2.generalized = "?R? not winner ?D?";
                    pattern2.naturalLanguageRepresentation = "?R? not winner ?D?";
                    pattern2.naturalLanguageRepresentationWithoutVariables = "not winner";
                    pattern2.posTags = "RB NN";
                    pattern2.boaScore = 0d;
                    pattern2.language = language;
                    pattern2.NER = NER_AWARD_INV;
                    patterns.put(pattern2.getNormalized(), pattern2);

                    Pattern pattern3 = new Pattern();
                    pattern3.generalized = "?R? loser ?D?";
                    pattern3.naturalLanguageRepresentation = "?R? loser ?D?";
                    pattern3.naturalLanguageRepresentationWithoutVariables = "loser";
                    pattern3.posTags = "NN";
                    pattern3.boaScore = 0d;
                    pattern3.language = language;
                    pattern3.NER = NER_AWARD_INV;
                    patterns.put(pattern3.getNormalized(), pattern3);

                    Pattern pattern4 = new Pattern();
                    pattern4.generalized = "?D? did not win ?R?";
                    pattern4.naturalLanguageRepresentation = "?D? did not win the ?R?";
                    pattern4.naturalLanguageRepresentationWithoutVariables = "did not win the";
                    pattern4.posTags = "VBD RB VB";
                    pattern4.boaScore = 0d;
                    pattern4.language = language;
                    pattern4.NER = NER_AWARD;
                    patterns.put(pattern4.getNormalized(), pattern4);

                    Pattern pattern5 = new Pattern();
                    pattern5.generalized = "?D? did not receive ?R?";
                    pattern5.naturalLanguageRepresentation = "?D? did not receive the ?R?";
                    pattern5.naturalLanguageRepresentationWithoutVariables = "did not receive the";
                    pattern5.posTags = "VBD RB VB";
                    pattern5.boaScore = 0d;
                    pattern5.language = language;
                    pattern5.NER = NER_AWARD;
                    patterns.put(pattern5.getNormalized(), pattern5);

                    Pattern pattern6 = new Pattern();
                    pattern6.generalized = "?D? renounce ?R?";
                    pattern6.naturalLanguageRepresentation = "?D? renounce the ?R?";
                    pattern6.naturalLanguageRepresentationWithoutVariables = "renounce the";
                    pattern6.posTags = "VB DT";
                    pattern6.boaScore = 0d;
                    pattern6.language = language;
                    pattern6.NER = NER_AWARD;
                    patterns.put(pattern6.getNormalized(), pattern6);

                    Pattern pattern7 = new Pattern();
                    pattern7.generalized = "?D? rejected ?R?";
                    pattern7.naturalLanguageRepresentation = "?D? rejected the ?R?";
                    pattern7.naturalLanguageRepresentationWithoutVariables = "rejected the";
                    pattern7.posTags = "VBD DT";
                    pattern7.boaScore = 0d;
                    pattern7.language = language;
                    pattern7.NER = NER_AWARD;
                    patterns.put(pattern7.getNormalized(), pattern7);

                    Pattern pattern8 = new Pattern();
                    pattern8.generalized = "?D? _BE_ not prizewinning ?R?";
                    pattern8.naturalLanguageRepresentation = "?D? was not prizewinning ?R?";
                    pattern8.naturalLanguageRepresentationWithoutVariables = "was not prizewinning";
                    pattern8.posTags = "VBD RB VBG";
                    pattern8.boaScore = 0d;
                    pattern8.language = language;
                    pattern8.NER = NER_AWARD;
                    patterns.put(pattern8.getNormalized(), pattern8);

                    Pattern pattern9 = new Pattern();
                    pattern9.generalized = "?R? _BE_ not the recipient of ?D?";
                    pattern9.naturalLanguageRepresentation = "?R? was not the recipient of ?D?";
                    pattern9.naturalLanguageRepresentationWithoutVariables = "was not the recipient of";
                    pattern9.posTags = "VBD RB DT JJ IN";
                    pattern9.boaScore = 0d;
                    pattern9.language = language;
                    pattern9.NER = NER_AWARD_INV;
                    patterns.put(pattern9.getNormalized(), pattern9);

                    Pattern pattern10 = new Pattern();
                    pattern10.generalized = "?R? losers ?D?";
                    pattern10.naturalLanguageRepresentation = "?R? losers ?D?";
                    pattern10.naturalLanguageRepresentationWithoutVariables = "losers";
                    pattern10.posTags = "NNS";
                    pattern10.boaScore = 0d;
                    pattern10.language = language;
                    pattern10.NER = NER_AWARD_INV;
                    patterns.put(pattern10.getNormalized(), pattern10);

                }


            } else if (propertyUri.equals("http://dbpedia.org/ontology/birthDate")) {

            } else if (propertyUri.equals("http://dbpedia.org/ontology/deathPlace")) {

            } else if (propertyUri.equals("http://dbpedia.org/ontology/foundationPlace")) {

            }  else if (propertyUri.equals("http://dbpedia.org/ontology/leaderName")) {

            } else if (propertyUri.equals("http://dbpedia.org/ontology/spouse")) {

            } else if (propertyUri.equals("http://dbpedia.org/ontology/subsidiary")) {

            }


        }
        catch (Exception e){
            logger.error("Could not execute query: " + e);
            e.printStackTrace();
        }

        List<Pattern> patternList = new ArrayList<>(patterns.values());

        return patternList;

    }
    
    /**
     * Returns all patterns from the index and their factFeatures for reverb and the
     * wordnet distance and the overall boa-boaScore.
     * 
     * @param propertyUri
     * @param language 
     * @return a list of patterns
     */
    public List<Pattern> querySolrIndex(String propertyUri, int numberOfBoaPatterns, double scoreThreshold, String language) {

        LOGDEV.debug(" -> Querying solr index for uri: " + propertyUri + " and language " + language + ".");

        Map<String,Pattern> patterns = new HashMap<String,Pattern>();

        try {
        	
        	if ( propertyUri.equals("http://dbpedia.org/ontology/office") ) propertyUri = "http://dbpedia.org/ontology/leaderName";
        	
            SolrQuery query = new SolrQuery("uri:\"" + propertyUri + "\"");
            query.addField("boa-score");
            query.addField("nlr-var");
            query.addField("nlr-gen");
            query.addField("nlr-no-var");
            query.addField("SUPPORT_NUMBER_OF_PAIRS_LEARNED_FROM");
            query.addSortField("SUPPORT_NUMBER_OF_PAIRS_LEARNED_FROM", ORDER.desc);
            //query.addSortField("boa-score", ORDER.desc);
            if ( numberOfBoaPatterns > 0 ) query.setRows(numberOfBoaPatterns);
            
            String key = propertyUri + numberOfBoaPatterns + language;
            
            if ( !this.queryCache.containsKey(key) ) {
            	
            	if ( language.equals("en") ) this.queryCache.put(key, enIndex.query(query));
                else if ( language.equals("de") ) this.queryCache.put(key, deIndex.query(query));
                else if ( language.equals("fr") ) this.queryCache.put(key, frIndex.query(query));
            }
            
            SolrDocumentList docList = this.queryCache.get(key).getResults();
            
            // return the first list of types
            int i=0;
            for (SolrDocument d : docList) {

            	Pattern pattern = new Pattern();
                pattern.naturalLanguageRepresentation = (String) d.get("nlr-var");
                pattern.generalized = (String) d.get("nlr-gen");
                pattern.naturalLanguageRepresentationWithoutVariables = (String) d.get("nlr-no-var");
                pattern.posTags = (String) d.get("pos");
                pattern.boaScore = (Double) d.get("SUPPORT_NUMBER_OF_PAIRS_LEARNED_FROM");
                pattern.language = language;
                
                LOGDEV.debug(" " + i + " Found pattern: " + pattern.naturalLanguageRepresentation);
                
//                System.out.println(pattern.getNormalized());
                
                // only add the first pattern, we don't want to override the better scored pattern
                if ( !pattern.getNormalized().trim().isEmpty() && !patterns.containsKey(pattern.getNormalized())  
                		&& patterns.size() < Defacto.DEFACTO_CONFIG.getIntegerSetting("boa", "NUMBER_OF_BOA_PATTERNS") ) 
                	patterns.put(pattern.getNormalized(), pattern);
            }

            LOGDEV.debug("");

        }
        catch (SolrServerException e) {

            LOGDEV.debug("Could not execute query: " + e);
            e.printStackTrace();
        }
        
        List<Pattern> patternList = new ArrayList<Pattern>(patterns.values());
        
        // we need to sort this list because we always want to have the same results in the eval
        Collections.sort(patternList, new Comparator<Pattern>() {

            @Override // -1 if first is smaller
            public int compare(Pattern pattern1, Pattern pattern2) {

                double difference = pattern1.boaScore - pattern2.boaScore;
                if (difference > 0) return -1;
                if (difference < 0) return 1;

                return pattern1.naturalLanguageRepresentation.compareTo(pattern2.naturalLanguageRepresentation);
            }
        });
        int i =1;
        for (Pattern ptemp: patternList) {
            LOGDEV.debug(" " + i + " BOA pattern: " + ptemp.naturalLanguageRepresentation.toString());
            i++;
        }

        return patternList;

    }
    
    public static void main(String[] args) {

    	Defacto.init();
        queryPatterns("http://dbpedia.org/ontology/award");
        queryPatterns("http://dbpedia.org/ontology/birthPlace");
        queryPatterns("http://dbpedia.org/ontology/deathPlace");
        queryPatterns("http://dbpedia.org/ontology/foundationPlace");
        queryPatterns("http://dbpedia.org/ontology/leaderName");
        queryPatterns("http://dbpedia.org/ontology/team");
        queryPatterns("http://dbpedia.org/ontology/author");
        queryPatterns("http://dbpedia.org/ontology/spouse");
        queryPatterns("http://dbpedia.org/ontology/starring");
        queryPatterns("http://dbpedia.org/ontology/subsidiary");
    }

	/**
	 * @param bps
	 */
	private static void queryPatterns(String uri) {

		int nr = 50;
		BoaPatternSearcher bps = new BoaPatternSearcher();
		List<Pattern> sub = new ArrayList<>();
        sub.addAll(bps.getNaturalLanguageRepresentations(uri, nr, "en"));
       // sub.addAll(bps.getNaturalLanguageRepresentations(uri, nr, "de"));
        //sub.addAll(bps.getNaturalLanguageRepresentations(uri, nr,  "fr"));
        
        logger.debug(uri);
        Iterator<Pattern> iterator = sub.iterator();
        while ( iterator.hasNext()) {
			Pattern pattern = iterator.next();
            logger.info(pattern.naturalLanguageRepresentation + " --> " + pattern.normalize());
        }
	}
}
