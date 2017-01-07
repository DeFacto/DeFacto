/**
 * 
 */
package org.aksw.defacto.evaluation.generation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.util.ListUtil;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class BenchmarkDataGeneration {

	static String year;
    static String dataset_store_path = "/home/esteves/github/FactBench/all";

    //CSV files
    static String csv_split = ",";
    static String csv_rel001_leader = "";
    static String csv_rel002_birth = "";
    static String csv_rel003_actor = "";
    static String csv_rel004_death = "";
    static String csv_rel005_NBA = "/home/esteves/github/FactBench/files/dbpedia/complete/out_dbpedia_rel_005.csv";

    //SPARQL queries
	static String qs_rel005_NBA =
            "PREFIX dbo: <http://dbpedia.org/ontology/> " +
                    "PREFIX dbr: <http://dbpedia.org/resource/> " +
                    "PREFIX yago: <http://dbpedia.org/class/yago/> " +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +

//				"SELECT ?player ?playerLabel ?timePeriod ?team ?teamLabel ?from ?to WHERE {  " +
                    "SELECT ?player ?timePeriod ?team ?from ?to FROM <http://dbpedia.org> WHERE {  " +
                    "?player dbo:league	dbr:NBA  .  " +
//					"?player rdfs:label ?playerLabel .  " +
                    "?player dbo:termPeriod ?timePeriod .  " +
                    "?timePeriod dbo:team ?team .  " +
//					"?team rdfs:label ?teamLabel .  " +
                    "?team rdf:type yago:WikicatNationalBasketballAssociationTeams . " +
                    "?timePeriod dbo:team ?team .  " +
                    "?timePeriod dbo:activeYearsStartYear ?from . " +
                    "?timePeriod dbo:activeYearsEndYear ?to . " +
                    "FILTER( xsd:gYear(?from) > \"2000\"^^xsd:gYear ) " +
                    "} ";

    static String qs_rel001_politicians =
            "PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
                    "PREFIX dbr: <http://dbpedia.org/resource/> \n" +
                    "PREFIX yago: <http://dbpedia.org/class/yago/> \n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n\n" +

                    "SELECT * \n" +
                    "FROM <http://dbpedia.org> \n" +
                    "WHERE { \n" +
                    "\t?person dbo:termPeriod ?timePeriod . \n" +
                    "\t?timePeriod dbo:office ?office . \n" +
                    "\t?timePeriod dbo:activeYearsStartDate ?from . \n" +
                    "\t?timePeriod dbo:activeYearsEndDate ?to . \n" +
                    "\tFILTER (regex(?office, \"^Prime Minister of.*\")) \n" +
                    "}";

    static String qs_rel002_birth =
            "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> \n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +

                    "SELECT ?person ?place ?date  \n" +
                    "WHERE {   \n" +
                    "   ?person dbpedia-owl:birthPlace ?place .  \n" +
                    "   ?place rdf:type dbpedia-owl:City  .   \n" +
                    "   ?person dbpedia-owl:birthDate ?date .  \n" +
                    "   ?person dbpedia-owl:numberOfInboundLinks ?personInbound .  \n" +
                    "   ?place dbpedia-owl:numberOfInboundLinks ?placeInbound .  \n" +
                    "}  \n" +
                    "ORDER BY DESC(?personInbound) DESC(?placeInbound) \n ";

    static String qs_rel003_starring =
            "PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +

                    "SELECT DISTINCT ?film ?actor ?date \n" +
                    "WHERE {   \n" +
                    "   ?film dbo:starring ?actor .  \n" +
                    "   ?film rdf:type dbo:Film . \n" +
                    "   ?film dbo:releaseDate ?date . \n " +
                    "   ?film dbo:numberOfInboundLinks ?filmInbound .  \n" +
                    "}  \n" +
                    "ORDER BY DESC(?filmInbound) ASC(?date) \n ";

    static String qs_rel004_death =
            "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> \n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +

                    "SELECT ?person ?place ?date \n" +
                    "FROM <http://dbpedia.org> \n" +
                    "WHERE {  \n" +
                    "   ?person dbpedia-owl:deathPlace ?place . \n" +
                    "   ?place rdf:type dbpedia-owl:City  .  \n" +
                    "   ?person dbpedia-owl:deathDate ?date . \n" +
                    "   ?person dbpedia-owl:numberOfInboundLinks ?personInbound . \n" +
                    "   ?place dbpedia-owl:numberOfInboundLinks ?placeInbound . \n" +
                    "} \n" +
                    "ORDER BY DESC(?personInbound) DESC(?placeInbound)\n";

    public static void dropEvalDirectory() throws IOException{
		
		FileUtils.deleteDirectory(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/birth/"));
		FileUtils.deleteDirectory(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/death/"));
		FileUtils.deleteDirectory(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/spouse/"));
		FileUtils.deleteDirectory(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/foundationPlace/"));
		FileUtils.deleteDirectory(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/award/"));
		FileUtils.deleteDirectory(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/publicationDate/"));
		FileUtils.deleteDirectory(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/nbateam/"));
		FileUtils.deleteDirectory(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/leader/"));
		FileUtils.deleteDirectory(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/subsidiary/"));
		FileUtils.deleteDirectory(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/starring/"));
		
		FileUtils.forceMkdir(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/birth/"));
		FileUtils.forceMkdir(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/death/"));
		FileUtils.forceMkdir(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/spouse/"));
		FileUtils.forceMkdir(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/foundationPlace/"));
		FileUtils.forceMkdir(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/award/"));
		FileUtils.forceMkdir(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/publicationDate/"));
		FileUtils.forceMkdir(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/nbateam/"));
		FileUtils.forceMkdir(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/leader/"));
		FileUtils.forceMkdir(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/subsidiary/"));
		FileUtils.forceMkdir(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/starring/"));
	}
	
	/**
	 * 
	 * @param args
	 * @throws JSONException
	 * @throws IOException
	 */
	public static void main(String[] args) throws JSONException, IOException {


	    try{
            Defacto.init();
            BenchmarkPrerequisiteGeneration pre = new BenchmarkPrerequisiteGeneration();

            System.out.print("Start generating temporal facts ... ");
            BenchmarkDataGeneration.dropEvalDirectory();

            /* FreeBase */
            //BenchmarkDataGeneration.loadSpouse();
            //BenchmarkDataGeneration.loadFoundationPlace();
            //BenchmarkDataGeneration.loadPublishDates();
            //BenchmarkDataGeneration.loadAwards();
            //BenchmarkDataGeneration.loadSubsidiary();

            /* DBPedia */

            //BenchmarkDataGeneration.loadPoliticians();
            //BenchmarkDataGeneration.loadBirth();
            //BenchmarkDataGeneration.loadStarring();
            //BenchmarkDataGeneration.loadDeath();
            BenchmarkDataGeneration.loadNBAPlayers(1);

            System.out.println("DONE!");
        }catch (Exception e){

        }

	}

	public static void loadSubsidiary() throws JSONException, IOException {
		
		JSONArray jsonMainArr = new JSONArray( FileUtils.readFileToString(
				new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "freebase/data/acquisition.json"), "UTF-8"));
		
		for (int i = 0; i < jsonMainArr.length(); i++) {  // **line 2**
			
			JSONObject element = jsonMainArr.getJSONObject(i).getJSONArray("/organization/organization/acquired_by").getJSONObject(0);
			
			JSONArray parent = element.getJSONArray("/business/acquisition/acquiring_company");
			JSONArray daughter = element.getJSONArray("/business/acquisition/company_acquired");
			String date = (String) element.getJSONObject("/business/acquisition/date").get("value");
			
			if ( date.equals("-0337") ) continue;
			
			// set namespaces
			Model model = ModelFactory.createDefaultModel();
			BenchmarkPrerequisiteGeneration.setPrefixes(model);
			
			// create all the necessary nodes
			Resource first		= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + parent.getJSONObject(0).getString("mid").replace("/m/", "/m."));
            Property acq 		= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "acquisition");
            Property subsidiary	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "subsidiary");
            Resource second		= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + daughter.getJSONObject(0).getString("mid").replace("/m/", "/m."));
            Resource bnode		= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + parent.getJSONObject(0).getString("mid").replace("/m/", "/m.") + "__" + i);
            
            // add them to the model
            model.add(first, acq, bnode);
            model.add(bnode, subsidiary, second);
            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_FROM, date);
            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_TO, date);
            BenchmarkPrerequisiteGeneration.addNames(model, first, parent.getJSONObject(0).getJSONArray("name"));
            BenchmarkPrerequisiteGeneration.addNames(model, second, daughter.getJSONObject(0).getJSONArray("name"));
            BenchmarkPrerequisiteGeneration.addAliases(model, first, parent.getJSONObject(0).getJSONArray("/common/topic/alias"));
            BenchmarkPrerequisiteGeneration.addAliases(model, second, daughter.getJSONObject(0).getJSONArray("/common/topic/alias"));
            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, first);
            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, second);
            
			// write them to the file
            model.write(new FileWriter(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") 
            		+ "eval/correct/subsidiary/subsidiary_" + (String.format("%05d", i)) + ".ttl")), "TURTLE");
		}
	}
	
	/**
	 * 
	 * @throws JSONException
	 * @throws IOException
	 */
	public static void loadSpouse() throws JSONException, IOException {
		
		JSONArray jsonMainArr = new JSONArray( FileUtils.readFileToString(
				new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "freebase/data/spouse.json"), "UTF-8"));
		
		for (int i = 0; i < jsonMainArr.length(); i++) {  // **line 2**
		     
			JSONObject childJSONObject = jsonMainArr.getJSONObject(i);
			JSONObject spouseRelation = childJSONObject.getJSONArray("/people/person/spouse_s").getJSONObject(0);
			
			// only one case where there is a second person missing
			if ( spouseRelation.getJSONArray("spouse").length() < 2 ) continue;
		    
			// read data
			String fromDate	= spouseRelation.getString("from");
			String toDate	= spouseRelation.getJSONObject("to").getString("value");
			String firstId	= spouseRelation.getJSONArray("spouse").getJSONObject(0).getString("mid").replace("/m/", "/m.");
			String secondId = spouseRelation.getJSONArray("spouse").getJSONObject(1).getString("mid").replace("/m/", "/m.");
			
			// set namespaces
			Model model = ModelFactory.createDefaultModel();
			BenchmarkPrerequisiteGeneration.setPrefixes(model);
			
			// create all the necessary nodes
			Resource first		= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + firstId);
            Property marriage	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "marriage");
            Property spouse		= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "spouse");
            Resource second		= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + secondId);
            Resource bnode		= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + firstId + "__" + i);
            
            // add them to the model
            model.add(first, marriage, bnode);
            model.add(bnode, spouse, second);
            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_FROM, fromDate);
            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_TO, toDate);
            BenchmarkPrerequisiteGeneration.addNames(model, first,  spouseRelation.getJSONArray("spouse").getJSONObject(0).getJSONArray("name"));
            BenchmarkPrerequisiteGeneration.addNames(model, second, spouseRelation.getJSONArray("spouse").getJSONObject(1).getJSONArray("name"));
            BenchmarkPrerequisiteGeneration.addAliases(model, first,  spouseRelation.getJSONArray("spouse").getJSONObject(0).getJSONArray("/common/topic/alias"));
            BenchmarkPrerequisiteGeneration.addAliases(model, second, spouseRelation.getJSONArray("spouse").getJSONObject(1).getJSONArray("/common/topic/alias"));
            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, first);
            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, second);
            
			// write them to the file
            model.write(new FileWriter(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/spouse/spouse_" + (String.format("%05d", i)) + ".ttl")), "TURTLE");
		}
	}
	
	/**
	 * 
	 * @throws JSONException
	 * @throws IOException
	 */
	public static void loadFoundationPlace() throws JSONException, IOException {
		
		JSONArray jsonMainArr = new JSONArray(FileUtils.readFileToString(
				new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "freebase/data/foundationPlace.json"), "UTF-8")).getJSONObject(0).getJSONArray("/business/industry/companies");
		
		for (int i = 0; i < jsonMainArr.length(); i++) {  // **line 2**
		     
			JSONObject company = jsonMainArr.getJSONObject(i);
			
			// read data for places and company
			String foundDate = company.getJSONObject("/organization/organization/date_founded").getString("value");
			String companyId = company.getString("mid").replace("/m/", "/m.");
			JSONArray companyNames = company.getJSONArray("name");
			JSONArray companyAliases = company.getJSONArray("/common/topic/alias");
			
			JSONObject place		= company.getJSONArray("/organization/organization/place_founded").getJSONObject(0);
			String placeId			= place.getString("mid").replace("/m/", "/m.");;
			JSONArray placeNames	= place.getJSONArray("name");
			JSONArray placeAliases	= place.getJSONArray("/common/topic/alias");

			// set namespaces
			Model model = ModelFactory.createDefaultModel();
			BenchmarkPrerequisiteGeneration.setPrefixes(model);
			
			// create all the necessary nodes
			Resource first		= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + companyId);
            Property marriage	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "foundation");
            Property spouse		= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "foundationPlace");
            Resource second		= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + placeId);
            Resource bnode		= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + companyId + "__" + i);
			
            // add them to the model
            model.add(first, marriage, bnode);
            model.add(bnode, spouse, second);
            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_FROM, foundDate);
            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_TO, foundDate);
            BenchmarkPrerequisiteGeneration.addNames(model, first, companyNames);
            BenchmarkPrerequisiteGeneration.addNames(model, second, placeNames);
            BenchmarkPrerequisiteGeneration.addAliases(model, first,  companyAliases);
            BenchmarkPrerequisiteGeneration.addAliases(model, second, placeAliases);
            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, first);
            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, second);
            
			// write them to the file
            model.write(new FileWriter(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
            		"eval/correct/foundationPlace/foundationPlace_" + (String.format("%05d", i)) + ".ttl")), "TURTLE");
		}
	}
	
	/**
	 * 
	 * @throws JSONException
	 * @throws IOException
	 */
	public static void loadPublishDates() throws JSONException, IOException {
		
		JSONArray jsonMainArr = new JSONArray(FileUtils.readFileToString(
				new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "freebase/data/publicationDate.json"), "UTF-8"));
		
		for (int i = 0, j = 0; i < jsonMainArr.length(); i++) {  // **line 2**
			
			String authorId = jsonMainArr.getJSONObject(i).getString("mid").replace("/m/", "/m.");
			JSONArray authorNames = jsonMainArr.getJSONObject(i).getJSONArray("name");
			JSONArray authorAlias = jsonMainArr.getJSONObject(i).getJSONArray("/common/topic/alias");
			
			JSONObject bookArray = jsonMainArr.getJSONObject(i).getJSONArray("/book/author/works_written").getJSONObject(0);
			
			// read data for places and company
			String pubdate 			= bookArray.getJSONObject("date_of_first_publication").getString("value").substring(0, 4);
			String bookID			= bookArray.getString("mid").replace("/m/", "/m.");
			JSONArray bookNames		= bookArray.getJSONArray("name");
			JSONArray bookAliases	= bookArray.getJSONArray("/common/topic/alias");
			
			// set namespaces
			Model model = ModelFactory.createDefaultModel();
			BenchmarkPrerequisiteGeneration.setPrefixes(model);
			
			// create all the necessary nodes
			Resource book		= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + bookID);
			Resource author		= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + authorId);
			Property publication= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "publication");
			Property authorProp	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "author");
            
            Resource bnode1		= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + bookID + "__" + j++);
			
            // add them to the model
            model.add(author, publication, bnode1);
            model.add(bnode1, authorProp, book);
            BenchmarkPrerequisiteGeneration.addDates(model, bnode1, Constants.DEFACTO_FROM, pubdate);
            BenchmarkPrerequisiteGeneration.addDates(model, bnode1, Constants.DEFACTO_TO, pubdate);
            BenchmarkPrerequisiteGeneration.addNames(model, book, bookNames);
            BenchmarkPrerequisiteGeneration.addNames(model, author, authorNames);
            BenchmarkPrerequisiteGeneration.addAliases(model, author,  authorAlias);
            BenchmarkPrerequisiteGeneration.addAliases(model, book,  bookAliases);
            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, book);
            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, author);
            
			// write them to the file
            model.write(new FileWriter(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
            		"eval/correct/publicationDate/publicationDate_" + (String.format("%05d", i)) + ".ttl")), "TURTLE");
		}
	}
	
	/**
	 * 
	 * @throws JSONException
	 * @throws IOException
	 */
	public static void loadAwards() throws JSONException, IOException {
		
		JSONArray jsonMainArr = new JSONArray(FileUtils.readFileToString(
				new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "freebase/data/award.json"), "UTF-8"));
		
		for (int i = 0; i < jsonMainArr.length(); i++) {  // **line 2**
		     
			JSONObject award = jsonMainArr.getJSONObject(i).getJSONObject("award");
			
			year					= jsonMainArr.getJSONObject(i).getString("year");
			String awardID			= award.getString("mid").replace("/m/", "/m.");;
			JSONArray awardNames	= award.getJSONArray("name");
			JSONArray awardAliases	= award.getJSONArray("/common/topic/alias");
			
			JSONArray winners = jsonMainArr.getJSONObject(i).getJSONArray("award_winner");
			for (int j = 0; j < winners.length(); j++) { 
				
				JSONObject winner = winners.getJSONObject(j);
				
				String winnerID			= winner.getString("mid").replace("/m/", "/m.");;
				JSONArray winnerNames	= winner.getJSONArray("name");
				JSONArray winnerAliases	= winner.getJSONArray("/common/topic/alias");

				// set namespaces
				Model model = ModelFactory.createDefaultModel();
				BenchmarkPrerequisiteGeneration.setPrefixes(model);
				
				// create all the necessary nodes
				Resource first			= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + winnerID);
	            Property bnodeProperty	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "recievedAward");
	            Resource bnode			= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + winnerID + "__" + i);
	            Property awardProperty	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "award");
	            Resource second			= model.createResource(Constants.FREEBASE_RESOURCE_NAMESPACE + awardID);
				
	            // add them to the model
	            model.add(first, bnodeProperty, bnode);
	            model.add(bnode, awardProperty, second);
	            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_FROM, year);
	            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_TO, year);
	            BenchmarkPrerequisiteGeneration.addNames(model, first, winnerNames);
	            BenchmarkPrerequisiteGeneration.addNames(model, second, awardNames);
	            BenchmarkPrerequisiteGeneration.addAliases(model, first,  winnerAliases);
	            BenchmarkPrerequisiteGeneration.addAliases(model, second, awardAliases);
	            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, first);
	            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, second);
	            
				// write them to the file
	            model.write(new FileWriter(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory")
	            		+ "eval/correct/award/award_" + (String.format("%05d", i)) + ".ttl")), "TURTLE");
			}
		}
	}


    private static void saveNBAPlayers(String playerUri, String teamUri, String bnodeUri, String from, String to,
                                       Integer i) throws Exception{
        Map<String, Map<String, String>> languageLabels = getLanguageLabels(playerUri, teamUri);
        Map<String,String> playerLabels = languageLabels.get(playerUri);
        Map<String,String> teamLabels = languageLabels.get(teamUri);

        // set namespaces
        Model model = ModelFactory.createDefaultModel();
        BenchmarkPrerequisiteGeneration.setPrefixes(model);

        // create all the necessary nodes
        Resource player			= model.createResource(playerUri);
        Property bnodeProperty	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "playedTeam");
        Resource bnode			= model.createResource(bnodeUri);
        Property teamProperty	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "team");
        Resource team			= model.createResource(teamUri);

        // add them to the model
        model.add(player, bnodeProperty, bnode);
        model.add(bnode, teamProperty, team);
        BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_FROM, from);
        BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_TO, to);
        BenchmarkPrerequisiteGeneration.addNames(model, player, playerLabels);
        BenchmarkPrerequisiteGeneration.addNames(model, team, teamLabels);
        BenchmarkPrerequisiteGeneration.addOwlSameAs(model, player);
        BenchmarkPrerequisiteGeneration.addOwlSameAs(model, team);

        // write them to the file
        model.write(new FileWriter(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory")
                + "eval/correct/nbateam/nbateam_" + (String.format("%05d", i)) + ".ttl")), "TURTLE");
    }
    private static void loadNBAPlayersFromCSV() throws Exception{

        BufferedReader br = null;
        String line = "";  // "player","team","timePeriod","from","to","dbin","dbout"
        br = new BufferedReader(new FileReader(csv_rel005_NBA));
        int i = 0;
        while ((line = br.readLine()) != null) {

            String[] values = line.split(csv_split);

            saveNBAPlayers(values[0], values[1], values[2], values[3], values[4], i);
            i++;

        }

    }
	private static void loadNBAPlayersFromSPARQL() throws Exception{

        Dataset dataset = TDBFactory.createDataset(dataset_store_path);
        Model dbpedia = dataset.getNamedModel("http://dbpedia.org");
        Query query = QueryFactory.create(qs_rel005_NBA, Syntax.syntaxARQ);
        int i = 0;

        ResultSet result = QueryExecutionFactory.create(query, dbpedia).execSelect();
        while ( result.hasNext() ) {

            QuerySolution solution = result.next();

            String playerUri	= solution.getResource("player").getURI();
            String teamUri		= solution.getResource("team").getURI();
            String bnodeUri		= solution.getResource("timePeriod").getURI();
//			String playerName	= solution.getLiteral("playerLabel").getLexicalForm();
//			String teamName		= solution.getLiteral("teamLabel").getLexicalForm();
            String from			= solution.getLiteral("from").getLexicalForm();
            String to			= solution.getLiteral("to").getLexicalForm();

           saveNBAPlayers(playerUri, teamUri, bnodeUri, from, to, i);
           i++;
        }

    }

    /**
     *
     * @param type 0 = from SPARQL query, 1 = from csv file
     * @throws IOException
     */
	public static void loadNBAPlayers(Integer type) throws Exception {

	    if (type.equals(0)){
            loadNBAPlayersFromSPARQL();
        }else if (type.equals(1)){
            loadNBAPlayersFromCSV();
        }else{
            throw new Exception("parameter value must be 1 or 2");
        }

	}
	
	public static void loadDeath() throws IOException {
		
		Dataset dataset = TDBFactory.createDataset(dataset_store_path);
		Model dbpedia = dataset.getNamedModel("http://dbpedia.org");
		

		
		List<QuerySolution> results = getResults(qs_rel004_death, dbpedia);
		
		// we create 5 parts so that we take some very popular, some  
		// popular, some not so popular... and not popular persons
		List<List<QuerySolution>> split = ListUtil.split(results, results.size() / 5);
		List<List<QuerySolution>> parts = new ArrayList<List<QuerySolution>>();
		for ( int i = 0 ; i < 5 ; i++ ) parts.add(split.get(i).subList(0, 200));
		
		int i = 0;
		for ( List<QuerySolution> part : parts ) {
			for ( QuerySolution solution : part ) {
				
				String personUri 	= solution.getResource("person").getURI();
				String placeUri		= solution.getResource("place").getURI();
				String date			= solution.getLiteral("date").getLexicalForm();
				
				Map<String, Map<String, String>> languageLabels = getLanguageLabels(personUri, placeUri);
				Map<String,String> personLabels = languageLabels.get(personUri);
				Map<String,String> placeLabels = languageLabels.get(placeUri);
				
				// set namespaces
				Model model = ModelFactory.createDefaultModel();
				BenchmarkPrerequisiteGeneration.setPrefixes(model);
				
				// create all the necessary nodes
				Resource person				= model.createResource(personUri);
	            Property bnodeProperty		= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "death");
	            Resource bnode				= model.createResource(personUri + "__1");
	            Property deathPlaceProperty	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "deathPlace");
	            Resource place				= model.createResource(placeUri);
				
	            // add them to the model
	            model.add(person, bnodeProperty, bnode);
	            model.add(bnode, deathPlaceProperty, place);
	            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_FROM, date);
	            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_TO, date);
	            BenchmarkPrerequisiteGeneration.addNames(model, person, personLabels);
	            BenchmarkPrerequisiteGeneration.addNames(model, place, placeLabels);
	            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, person);
	            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, place);
	            
				// write them to the file
	            model.write(new FileWriter(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory")
	            		+ "eval/correct/death/death_" + (String.format("%05d", i++)) + ".ttl")), "TURTLE");
			}
		}
	}
	
	public static void loadStarring() throws IOException {
		
		Dataset dataset = TDBFactory.createDataset(dataset_store_path);
		Model dbpedia = dataset.getNamedModel("http://dbpedia.org");
		

		
		QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(qs_rel003_starring, Syntax.syntaxARQ), dbpedia);
		ResultSet rs = qexec.execSelect();
        
		List<QuerySolution> results = new ArrayList<QuerySolution>();
        
        String previousUri = "";
        String previousDate = "";
        
        while (rs.hasNext()) {
        	
        	QuerySolution qs = rs.next();
        	
        	String uri	= qs.getResource("film").getURI();
        	String date	= qs.getLiteral("date").getLexicalForm();
        	
        	// bad date
        	if ( date.contains("-11-31") ) continue;
        	
        	// same movie
        	if ( previousUri.equals(uri) ) {
        		
        		// same release
        		if ( date.equals(previousDate) ) {

        			results.add(qs);
            		previousUri = uri;
            		previousDate = date;
        		}
        		// new release
        		else continue;
        	}
        	// else new movie
        	else {
        		
        		results.add(qs);
        		previousUri = uri;
        		previousDate = date;
        	}
        }
        
        // we create 5 parts so that we take some very popular, some  
 		// popular, some not so popular... and not popular films
 		List<List<QuerySolution>> split = ListUtil.split(results, results.size() / 5);
 		List<List<QuerySolution>> parts = new ArrayList<List<QuerySolution>>();
 		for ( int i = 0 ; i < 5 ; i++ ) parts.add(split.get(i).subList(0, 200));
        
 		int i = 0;
		for ( List<QuerySolution> part : parts ) {
			for ( QuerySolution solution : part ) {
				
				String actorUri 	= solution.getResource("actor").getURI();
				String filmUri		= solution.getResource("film").getURI();
				String date			= solution.getLiteral("date").getLexicalForm();
				
				Map<String, Map<String, String>> languageLabels = getLanguageLabels(actorUri, filmUri);
				Map<String,String> filmLabels = languageLabels.get(filmUri);
				Map<String,String> actorLabels = languageLabels.get(actorUri);
				
				// set namespaces
				Model model = ModelFactory.createDefaultModel();
				BenchmarkPrerequisiteGeneration.setPrefixes(model);
				
				// create all the necessary nodes
				Resource film				= model.createResource(filmUri);
	            Property bnodeProperty		= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "isStarring");
	            Resource bnode				= model.createResource(filmUri + "__1");
	            Property starringProperty	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "starring");
	            Resource actor				= model.createResource(actorUri);
				
	            // add them to the model
	            model.add(film, bnodeProperty, bnode);
	            model.add(bnode, starringProperty, actor);
	            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_FROM, date);
	            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_TO, date);
	            BenchmarkPrerequisiteGeneration.addNames(model, film, filmLabels);
	            BenchmarkPrerequisiteGeneration.addNames(model, actor, actorLabels);
	            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, film);
	            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, actor);
	            
				// write them to the file
	            model.write(new FileWriter(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory")
	            		+ "eval/correct/starring/starring_" + (String.format("%05d", i++)) + ".ttl")), "TURTLE");
			}
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public static void loadBirth() throws IOException {
		
		Dataset dataset = TDBFactory.createDataset(dataset_store_path);
		Model dbpedia = dataset.getNamedModel("http://dbpedia.org");

		List<QuerySolution> results = getResults(qs_rel002_birth, dbpedia);
		
		// we create 5 parts so that we take some very popular, some  
		// popular, some not so popular... and not popular persons
		List<List<QuerySolution>> split = ListUtil.split(results, results.size() / 5);
		List<List<QuerySolution>> parts = new ArrayList<List<QuerySolution>>();
		for ( int i = 0 ; i < 5 ; i++ ) parts.add(split.get(i).subList(0, 200));
		
		int i = 0;
		for ( List<QuerySolution> part : parts ) {
			for ( QuerySolution solution : part ) {
				
				String personUri 	= solution.getResource("person").getURI();
				String placeUri		= solution.getResource("place").getURI();
				String date			= solution.getLiteral("date").getLexicalForm();
				
				Map<String, Map<String, String>> languageLabels = getLanguageLabels(personUri, placeUri);
				Map<String,String> personLabels = languageLabels.get(personUri);
				Map<String,String> placeLabels = languageLabels.get(placeUri);
				
				// set namespaces
				Model model = ModelFactory.createDefaultModel();
				BenchmarkPrerequisiteGeneration.setPrefixes(model);
				
				// create all the necessary nodes
				Resource person				= model.createResource(personUri);
	            Property bnodeProperty		= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "birth");
	            Resource bnode				= model.createResource(personUri + "__1");
	            Property birthPlaceProperty	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "birthPlace");
	            Resource place			= model.createResource(placeUri);
				
	            // add them to the model
	            model.add(person, bnodeProperty, bnode);
	            model.add(bnode, birthPlaceProperty, place);
	            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_FROM, date);
	            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_TO, date);
	            BenchmarkPrerequisiteGeneration.addNames(model, person, personLabels);
	            BenchmarkPrerequisiteGeneration.addNames(model, place, placeLabels);
	            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, person);
	            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, place);
	            
				// write them to the file
	            model.write(new FileWriter(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory")
	            		+ "eval/correct/birth/birth_" + (String.format("%05d", i++)) + ".ttl")), "TURTLE");
			}
		}
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public static void loadPoliticians() throws IOException {
		
		Dataset dataset = TDBFactory.createDataset(dataset_store_path);
		Model dbpedia = dataset.getNamedModel("http://dbpedia.org");

		Query query = QueryFactory.create(qs_rel001_politicians, Syntax.syntaxARQ);
		
		int i = 0;
		
		ResultSet result = QueryExecutionFactory.create(query, dbpedia).execSelect();
		while ( result.hasNext() ) {
			
			QuerySolution solution = result.next();
			
			String playerUri	= solution.getResource("person").getURI();
			String countryName	= solution.getLiteral("office").getLexicalForm().replaceAll("Prime Minister of (the )?", "");
			
			List<String> wrongCountries = Arrays.asList("French Mandate of Lebanon", "");
			if ( wrongCountries .contains(countryName) ) continue;
			
			String countryUri	= getCountryUri(countryName);
			
			if ( countryUri == null ) continue;
			
			String bnodeUri		= solution.getResource("timePeriod").getURI();
			String from			= solution.getLiteral("from").getLexicalForm();
			String to			= solution.getLiteral("to").getLexicalForm();
			
			Map<String, Map<String, String>> languageLabels = getLanguageLabels(playerUri, countryUri);
			Map<String,String> politicianLabels = languageLabels.get(playerUri);
			Map<String,String> countryLabels = languageLabels.get(countryUri);
			
			// set namespaces
			Model model = ModelFactory.createDefaultModel();
			BenchmarkPrerequisiteGeneration.setPrefixes(model);
			
			// create all the necessary nodes
			Resource politician		= model.createResource(playerUri);
            Property bnodeProperty	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "inOffice");
            Resource bnode			= model.createResource(bnodeUri);
            Property officeProperty	= model.createProperty(Constants.DBPEDIA_ONTOLOGY_NAMESPACE + "office");
            Resource country		= model.createResource(countryUri);
			
            // add them to the model
            model.add(politician, bnodeProperty, bnode);
            model.add(bnode, officeProperty, country);
            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_FROM, from);
            BenchmarkPrerequisiteGeneration.addDates(model, bnode, Constants.DEFACTO_TO, to);
            BenchmarkPrerequisiteGeneration.addNames(model, politician, politicianLabels);
            BenchmarkPrerequisiteGeneration.addNames(model, country, countryLabels);
            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, politician);
            BenchmarkPrerequisiteGeneration.addOwlSameAs(model, country);
            
			// write them to the file
            model.write(new FileWriter(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory")
            		+ "eval/correct/leader/leader_" + (String.format("%05d", i++)) + ".ttl")), "TURTLE");
		}
	}

	/**
	 * 
	 * @param countryName
	 * @return
	 */
	private static String getCountryUri(String countryName) {
		
		Dataset dataset = TDBFactory.createDataset(dataset_store_path);
		Model dbpedia = dataset.getNamedModel("http://dbpedia.org");
		
		String queryString = 
				
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
				"PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
				"SELECT ?country { " + 
					"?country rdfs:label \""+ countryName+"\"@en . " +
//					"?country rdf:type dbo:Country " +
				"}";
		
		Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		
		ResultSet result = QueryExecutionFactory.create(query, dbpedia).execSelect();
		while ( result.hasNext() ) {
			
			QuerySolution solution = result.next();
			
			String countryUri	 = solution.contains("country") ? solution.getResource("country").getURI() : "";
			return countryUri;
		}
		
		return null;
	}

	/**
	 * 
	 * @param playerUri
	 * @param teamUri
	 * @return
	 */
	private static Map<String, Map<String, String>> getLanguageLabels(String playerUri, String teamUri) {

		Dataset dataset = TDBFactory.createDataset(dataset_store_path);
		Model dbpedia = dataset.getNamedModel("http://dbpedia.org");
		
		String queryString = 
				
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"SELECT * { " + 
				"OPTIONAL { " + 
				"	?s rdfs:label ?playerEnLabel . FILTER(lang(?playerEnLabel) = \"en\") " +
				"} " +
				"OPTIONAL { " + 
				"	?s rdfs:label ?playerDeLabel . FILTER(lang(?playerDeLabel) = \"de\") " +
				"} " +
				"OPTIONAL { " + 
				"	?s rdfs:label ?playerFrLabel . FILTER(lang(?playerFrLabel) = \"fr\") " +
				"} " +
				"OPTIONAL { " + 
				"	?o rdfs:label ?teamEnLabel . FILTER(lang(?teamEnLabel) = \"en\") " +
				"} " +
				"OPTIONAL { " + 
				"	?o rdfs:label ?teamDeLabel . FILTER(lang(?teamDeLabel) = \"de\") " +
				"} " +
				"OPTIONAL { " + 
				"	?o rdfs:label ?teamFrLabel . FILTER(lang(?teamFrLabel) = \"fr\") " +
				"} " +
			"}";
		
		queryString = queryString.replace("?s", "<"+ playerUri + ">");
		queryString = queryString.replace("?o", "<"+ teamUri + ">");
		
		Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		
//		ResultSet result = SparqlUtil.executeSelectQuery("http://dbpedia.org/sparql", "http://dbpedia.org", query);
		ResultSet result = QueryExecutionFactory.create(query, dbpedia).execSelect();
		while ( result.hasNext() ) {
			
			QuerySolution solution = result.next();
			
			String enPlayerLabel = solution.contains("playerEnLabel") ? solution.getLiteral("playerEnLabel").getLexicalForm() : "";
			String dePlayerLabel = solution.contains("playerDeLabel") ? solution.getLiteral("playerDeLabel").getLexicalForm() : "";
			String frPlayerLabel = solution.contains("playerFrLabel") ? solution.getLiteral("playerFrLabel").getLexicalForm() : "";
			
			String enTeamLabel = solution.contains("teamEnLabel") ? solution.getLiteral("teamEnLabel").getLexicalForm() : "";
			String deTeamLabel = solution.contains("teamDeLabel") ? solution.getLiteral("teamDeLabel").getLexicalForm() : "";
			String frTeamLabel = solution.contains("teamFrLabel") ? solution.getLiteral("teamFrLabel").getLexicalForm() : "";
			
			Map<String,Map<String,String>> labels = new HashMap<String,Map<String,String>>();
			Map<String,String> playerLabels = new HashMap<String,String>();
			playerLabels.put("en", enPlayerLabel);
			playerLabels.put("de", dePlayerLabel);
			playerLabels.put("fr", frPlayerLabel);
			Map<String,String> teamLabels = new HashMap<String,String>();
			teamLabels.put("en", enTeamLabel);
			teamLabels.put("de", deTeamLabel);
			teamLabels.put("fr", frTeamLabel);
			
			labels.put(playerUri, playerLabels);
			labels.put(teamUri, teamLabels);
			
			return labels;
		}
		
		throw new RuntimeException("Could not find labels for URI: " + playerUri + " or " + teamUri);
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 */
    protected static List<QuerySolution> getResults(String query) {

    	return getResults(query, null);
    }
    
    /**
     * DANGER! This method removes duplicate rows. It only uses the first row
     * where a person uri occurrs first.
     * 
     * @param query
     * @param model
     * @return
     */
	private static List<QuerySolution> getResults(String query, Model model) {
		
		QueryExecution qexec = null;
		
		// use dbpedia as default
		if ( model == null ) {
		
			qexec = new QueryEngineHTTP("http://dbpedia.org/sparql", query);
			((QueryEngineHTTP) qexec).addDefaultGraph("http://dbpedia.org");
		}
		// take the provided model
		else qexec = QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), model);
		
        List<QuerySolution> resultSetList = new ArrayList<QuerySolution>();

        ResultSet rs = qexec.execSelect();
        
//        ResultSetFormatter.out(System.out, rs);
        
        String previousUri = "";
        
        while (rs.hasNext()) {
        	
        	QuerySolution qs = rs.next();
        	
        	String uri = qs.getResource("person").getURI();
        	
        	// bad date
        	if ( qs.contains("date") && qs.get("date").asLiteral().getLexicalForm().contains("-11-31") ) continue;
        	
        	if ( !previousUri.equals(uri) ) {
        		
        		resultSetList.add(qs);
        		previousUri = uri;
        	}
        }
        
        return resultSetList;
	}
}

