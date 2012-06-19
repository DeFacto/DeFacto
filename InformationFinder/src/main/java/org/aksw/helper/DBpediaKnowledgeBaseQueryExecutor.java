package org.aksw.helper;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 2/10/12
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class DBpediaKnowledgeBaseQueryExecutor {

    private static Logger logger = Logger.getLogger(DBpediaKnowledgeBaseQueryExecutor.class.getName());

    private static String DBpedia_Live_ServerAddress = "http://live.dbpedia.org/sparql";
//    private static String DBpedia_Live_ServerAddress = "http://lgd.aksw.org:8999/sparql";
    private static QueryEngineHTTP virtuosoQueryEngine = null;
    
    public static String getLabelForURI(String itemURI){

        String requiredLabel = "";
        
        String sparqlQuery = String.format("SELECT * WHERE { ?s ?p ?label. filter(?s = <%s> && ?p = <http://www.w3.org/2000/01/rdf-schema#label>" +
                " && (lang(?label) = \"\" || lang(?label) = \"en\")) }", itemURI);

        virtuosoQueryEngine = (QueryEngineHTTP)com.hp.hpl.jena.query.QueryExecutionFactory.sparqlService(
                DBpedia_Live_ServerAddress, sparqlQuery);

        ResultSet rsItemLabel = virtuosoQueryEngine.execSelect();
        if(rsItemLabel.hasNext()){
            requiredLabel = rsItemLabel.next().get("?label").asLiteral().getString();

            logger.info("LABEL = " + requiredLabel);
        }


//        String sparqlQuery = "SELECT * WHERE { ?s ?p ?o.filter(?s = <http://dbpedia.org/resource/Lionel_Messi> && ?p = <http://www.w3.org/2000/01/rdf-schema#label>) }";
        return requiredLabel;
    }

    public static String getLabelForTripleParts(String itemURI){

        String requiredLabel = "";

        String sparqlQuery = String.format("SELECT * WHERE { ?s ?p ?label. filter(?s = <%s> && ?p = <http://www.w3.org/2000/01/rdf-schema#label>" +
                " && (lang(?label) = \"\" || lang(?label) = \"en\")) }", itemURI);

        virtuosoQueryEngine = (QueryEngineHTTP)com.hp.hpl.jena.query.QueryExecutionFactory.sparqlService(
                DBpedia_Live_ServerAddress, sparqlQuery);

        ResultSet rsItemLabel = virtuosoQueryEngine.execSelect();
        if(rsItemLabel.hasNext()){
            requiredLabel = rsItemLabel.next().get("?label").asLiteral().getString();

            logger.info("LABEL = " + requiredLabel);
        }


//        String sparqlQuery = "SELECT * WHERE { ?s ?p ?o.filter(?s = <http://dbpedia.org/resource/Lionel_Messi> && ?p = <http://www.w3.org/2000/01/rdf-schema#label>) }";
        return requiredLabel;
    }

    public static void getCountOfInstancesForEachClass(){
        String stringDBpediaClasses = "http://dbpedia.org/ontology/AcademicJournal\n" +
                "http://dbpedia.org/ontology/Activity\n" +
                "http://dbpedia.org/ontology/Actor\n" +
                "http://dbpedia.org/ontology/AdministrativeRegion\n" +
                "http://dbpedia.org/ontology/AdultActor\n" +
                "http://dbpedia.org/ontology/Agent\n" +
                "http://dbpedia.org/ontology/Aircraft\n" +
                "http://dbpedia.org/ontology/Airline\n" +
                "http://dbpedia.org/ontology/Airport\n" +
                "http://dbpedia.org/ontology/Album\n" +
                "http://dbpedia.org/ontology/Ambassador\n" +
                "http://dbpedia.org/ontology/AmericanFootballLeague\n" +
                "http://dbpedia.org/ontology/AmericanFootballPlayer\n" +
                "http://dbpedia.org/ontology/AmericanFootballTeam\n" +
                "http://dbpedia.org/ontology/Amphibian\n" +
                "http://dbpedia.org/ontology/AnatomicalStructure\n" +
                "http://dbpedia.org/ontology/Animal\n" +
                "http://dbpedia.org/ontology/Arachnid\n" +
                "http://dbpedia.org/ontology/Archaea\n" +
                "http://dbpedia.org/ontology/Architect\n" +
                "http://dbpedia.org/ontology/ArchitecturalStructure\n" +
                "http://dbpedia.org/ontology/Arena\n" +
                "http://dbpedia.org/ontology/Artery\n" +
                "http://dbpedia.org/ontology/Artist\n" +
                "http://dbpedia.org/ontology/Asteroid\n" +
                "http://dbpedia.org/ontology/Astronaut\n" +
                "http://dbpedia.org/ontology/Athlete\n" +
                "http://dbpedia.org/ontology/Atoll\n" +
                "http://dbpedia.org/ontology/AustralianFootballLeague\n" +
                "http://dbpedia.org/ontology/AustralianRulesFootballPlayer\n" +
                "http://dbpedia.org/ontology/AutoRacingLeague\n" +
                "http://dbpedia.org/ontology/Automobile\n" +
                "http://dbpedia.org/ontology/AutomobileEngine\n" +
                "http://dbpedia.org/ontology/Award\n" +
                "http://dbpedia.org/ontology/Bacteria\n" +
                "http://dbpedia.org/ontology/BadmintonPlayer\n" +
                "http://dbpedia.org/ontology/Band\n" +
                "http://dbpedia.org/ontology/BaseballLeague\n" +
                "http://dbpedia.org/ontology/BaseballPlayer\n" +
                "http://dbpedia.org/ontology/BasketballLeague\n" +
                "http://dbpedia.org/ontology/BasketballPlayer\n" +
                "http://dbpedia.org/ontology/BasketballTeam\n" +
                "http://dbpedia.org/ontology/Beverage\n" +
                "http://dbpedia.org/ontology/Bibo:Book\n" +
                "http://dbpedia.org/ontology/BiologicalDatabase\n" +
                "http://dbpedia.org/ontology/Biomolecule\n" +
                "http://dbpedia.org/ontology/Bird\n" +
                "http://dbpedia.org/ontology/BodyOfWater\n" +
                "http://dbpedia.org/ontology/Bone\n" +
                "http://dbpedia.org/ontology/Book\n" +
                "http://dbpedia.org/ontology/BowlingLeague\n" +
                "http://dbpedia.org/ontology/Boxer\n" +
                "http://dbpedia.org/ontology/BoxingLeague\n" +
                "http://dbpedia.org/ontology/Brain\n" +
                "http://dbpedia.org/ontology/Bridge\n" +
                "http://dbpedia.org/ontology/BritishRoyalty\n" +
                "http://dbpedia.org/ontology/BroadcastNetwork\n" +
                "http://dbpedia.org/ontology/Broadcaster\n" +
                "http://dbpedia.org/ontology/Building\n" +
                "http://dbpedia.org/ontology/BullFighter\n" +
                "http://dbpedia.org/ontology/CanadianFootballLeague\n" +
                "http://dbpedia.org/ontology/CanadianFootballPlayer\n" +
                "http://dbpedia.org/ontology/CanadianFootballTeam\n" +
                "http://dbpedia.org/ontology/Canal\n" +
                "http://dbpedia.org/ontology/Cardinal\n" +
                "http://dbpedia.org/ontology/Cave\n" +
                "http://dbpedia.org/ontology/Celebrity\n" +
                "http://dbpedia.org/ontology/CelestialBody\n" +
                "http://dbpedia.org/ontology/Chancellor\n" +
                "http://dbpedia.org/ontology/ChemicalCompound\n" +
                "http://dbpedia.org/ontology/ChemicalElement\n" +
                "http://dbpedia.org/ontology/ChemicalSubstance\n" +
                "http://dbpedia.org/ontology/ChessPlayer\n" +
                "http://dbpedia.org/ontology/ChristianBishop\n" +
                "http://dbpedia.org/ontology/Church\n" +
                "http://dbpedia.org/ontology/City\n" +
                "http://dbpedia.org/ontology/Cleric\n" +
                "http://dbpedia.org/ontology/ClubMoss\n" +
                "http://dbpedia.org/ontology/College\n" +
                "http://dbpedia.org/ontology/CollegeCoach\n" +
                "http://dbpedia.org/ontology/Colour\n" +
                "http://dbpedia.org/ontology/Comedian\n" +
                "http://dbpedia.org/ontology/ComicBook\n" +
                "http://dbpedia.org/ontology/ComicsCharacter\n" +
                "http://dbpedia.org/ontology/ComicsCreator\n" +
                "http://dbpedia.org/ontology/Company\n" +
                "http://dbpedia.org/ontology/Congressman\n" +
                "http://dbpedia.org/ontology/Conifer\n" +
                "http://dbpedia.org/ontology/Constellation\n" +
                "http://dbpedia.org/ontology/Continent\n" +
                "http://dbpedia.org/ontology/Convention\n" +
                "http://dbpedia.org/ontology/Country\n" +
                "http://dbpedia.org/ontology/CricketLeague\n" +
                "http://dbpedia.org/ontology/Cricketer\n" +
                "http://dbpedia.org/ontology/Criminal\n" +
                "http://dbpedia.org/ontology/Crustacean\n" +
                "http://dbpedia.org/ontology/CurlingLeague\n" +
                "http://dbpedia.org/ontology/Currency\n" +
                "http://dbpedia.org/ontology/Cycad\n" +
                "http://dbpedia.org/ontology/CyclingLeague\n" +
                "http://dbpedia.org/ontology/Cyclist\n" +
                "http://dbpedia.org/ontology/Database\n" +
                "http://dbpedia.org/ontology/Deputy\n" +
                "http://dbpedia.org/ontology/Device\n" +
                "http://dbpedia.org/ontology/Disease\n" +
                "http://dbpedia.org/ontology/Drug\n" +
                "http://dbpedia.org/ontology/EducationalInstitution\n" +
                "http://dbpedia.org/ontology/Election\n" +
                "http://dbpedia.org/ontology/Embryology\n" +
                "http://dbpedia.org/ontology/EthnicGroup\n" +
                "http://dbpedia.org/ontology/Eukaryote\n" +
                "http://dbpedia.org/ontology/EurovisionSongContestEntry\n" +
                "http://dbpedia.org/ontology/Event\n" +
                "http://dbpedia.org/ontology/Fern\n" +
                "http://dbpedia.org/ontology/FictionalCharacter\n" +
                "http://dbpedia.org/ontology/FieldHockeyLeague\n" +
                "http://dbpedia.org/ontology/FigureSkater\n" +
                "http://dbpedia.org/ontology/Film\n" +
                "http://dbpedia.org/ontology/FilmFestival\n" +
                "http://dbpedia.org/ontology/Fish\n" +
                "http://dbpedia.org/ontology/Flag\n" +
                "http://dbpedia.org/ontology/FloweringPlant\n" +
                "http://dbpedia.org/ontology/Foaf:Document\n" +
                "http://dbpedia.org/ontology/Foaf:Person\n" +
                "http://dbpedia.org/ontology/Food\n" +
                "http://dbpedia.org/ontology/FootballMatch\n" +
                "http://dbpedia.org/ontology/FormulaOneRacer\n" +
                "http://dbpedia.org/ontology/Fungus\n" +
                "http://dbpedia.org/ontology/GaelicGamesPlayer\n" +
                "http://dbpedia.org/ontology/Galaxy\n" +
                "http://dbpedia.org/ontology/Game\n" +
                "http://dbpedia.org/ontology/Gene\n" +
                "http://dbpedia.org/ontology/GeneLocation\n" +
                "http://dbpedia.org/ontology/Geo:SpatialThing\n" +
                "http://dbpedia.org/ontology/GeopoliticalOrganisation\n" +
                "http://dbpedia.org/ontology/Ginkgo\n" +
                "http://dbpedia.org/ontology/GivenName\n" +
                "http://dbpedia.org/ontology/Gml:_Feature\n" +
                "http://dbpedia.org/ontology/Gnetophytes\n" +
                "http://dbpedia.org/ontology/GolfLeague\n" +
                "http://dbpedia.org/ontology/GolfPlayer\n" +
                "http://dbpedia.org/ontology/GovernmentAgency\n" +
                "http://dbpedia.org/ontology/GovernmentType\n" +
                "http://dbpedia.org/ontology/Governor\n" +
                "http://dbpedia.org/ontology/GrandPrix\n" +
                "http://dbpedia.org/ontology/Grape\n" +
                "http://dbpedia.org/ontology/GreenAlga\n" +
                "http://dbpedia.org/ontology/GridironFootballPlayer\n" +
                "http://dbpedia.org/ontology/HandballLeague\n" +
                "http://dbpedia.org/ontology/HistoricBuilding\n" +
                "http://dbpedia.org/ontology/HistoricPlace\n" +
                "http://dbpedia.org/ontology/HockeyTeam\n" +
                "http://dbpedia.org/ontology/Holiday\n" +
                "http://dbpedia.org/ontology/Hospital\n" +
                "http://dbpedia.org/ontology/Hotel\n" +
                "http://dbpedia.org/ontology/HumanGene\n" +
                "http://dbpedia.org/ontology/HumanGeneLocation\n" +
                "http://dbpedia.org/ontology/IceHockeyLeague\n" +
                "http://dbpedia.org/ontology/IceHockeyPlayer\n" +
                "http://dbpedia.org/ontology/Ideology\n" +
                "http://dbpedia.org/ontology/Infrastructure\n" +
                "http://dbpedia.org/ontology/InlineHockeyLeague\n" +
                "http://dbpedia.org/ontology/Insect\n" +
                "http://dbpedia.org/ontology/Instrument\n" +
                "http://dbpedia.org/ontology/Island\n" +
                "http://dbpedia.org/ontology/Journalist\n" +
                "http://dbpedia.org/ontology/Judge\n" +
                "http://dbpedia.org/ontology/LacrosseLeague\n" +
                "http://dbpedia.org/ontology/Lake\n" +
                "http://dbpedia.org/ontology/Language\n" +
                "http://dbpedia.org/ontology/LaunchPad\n" +
                "http://dbpedia.org/ontology/LawFirm\n" +
                "http://dbpedia.org/ontology/LegalCase\n" +
                "http://dbpedia.org/ontology/Legislature\n" +
                "http://dbpedia.org/ontology/Library\n" +
                "http://dbpedia.org/ontology/Lieutenant\n" +
                "http://dbpedia.org/ontology/Lighthouse\n" +
                "http://dbpedia.org/ontology/Locomotive\n" +
                "http://dbpedia.org/ontology/LunarCrater\n" +
                "http://dbpedia.org/ontology/Lymph\n" +
                "http://dbpedia.org/ontology/Magazine\n" +
                "http://dbpedia.org/ontology/Mammal\n" +
                "http://dbpedia.org/ontology/MartialArtist\n" +
                "http://dbpedia.org/ontology/Mayor\n" +
                "http://dbpedia.org/ontology/MeanOfTransportation\n" +
                "http://dbpedia.org/ontology/MemberOfParliament\n" +
                "http://dbpedia.org/ontology/MilitaryConflict\n" +
                "http://dbpedia.org/ontology/MilitaryPerson\n" +
                "http://dbpedia.org/ontology/MilitaryUnit\n" +
                "http://dbpedia.org/ontology/MixedMartialArtsEvent\n" +
                "http://dbpedia.org/ontology/MixedMartialArtsLeague\n" +
                "http://dbpedia.org/ontology/Model\n" +
                "http://dbpedia.org/ontology/Mollusca\n" +
                "http://dbpedia.org/ontology/Monarch\n" +
                "http://dbpedia.org/ontology/Monument\n" +
                "http://dbpedia.org/ontology/Moss\n" +
                "http://dbpedia.org/ontology/MotorcycleRacingLeague\n" +
                "http://dbpedia.org/ontology/Mountain\n" +
                "http://dbpedia.org/ontology/MountainPass\n" +
                "http://dbpedia.org/ontology/MountainRange\n" +
                "http://dbpedia.org/ontology/MouseGene\n" +
                "http://dbpedia.org/ontology/MouseGeneLocation\n" +
                "http://dbpedia.org/ontology/Muscle\n" +
                "http://dbpedia.org/ontology/Museum\n" +
                "http://dbpedia.org/ontology/MusicFestival\n" +
                "http://dbpedia.org/ontology/MusicGenre\n" +
                "http://dbpedia.org/ontology/Musical\n" +
                "http://dbpedia.org/ontology/MusicalArtist\n" +
                "http://dbpedia.org/ontology/MusicalWork\n" +
                "http://dbpedia.org/ontology/Name\n" +
                "http://dbpedia.org/ontology/NascarDriver\n" +
                "http://dbpedia.org/ontology/NationalCollegiateAthleticAssociationAthlete\n" +
                "http://dbpedia.org/ontology/NationalSoccerClub\n" +
                "http://dbpedia.org/ontology/NaturalPlace\n" +
                "http://dbpedia.org/ontology/Nerve\n" +
                "http://dbpedia.org/ontology/Newspaper\n" +
                "http://dbpedia.org/ontology/Non-ProfitOrganisation\n" +
                "http://dbpedia.org/ontology/OfficeHolder\n" +
                "http://dbpedia.org/ontology/OlympicResult\n" +
                "http://dbpedia.org/ontology/Olympics\n" +
                "http://dbpedia.org/ontology/Organisation\n" +
                "http://dbpedia.org/ontology/OrganisationMember\n" +
                "http://dbpedia.org/ontology/PaintballLeague\n" +
                "http://dbpedia.org/ontology/Painting\n" +
                "http://dbpedia.org/ontology/Park\n" +
                "http://dbpedia.org/ontology/PeriodicalLiterature\n" +
                "http://dbpedia.org/ontology/Person\n" +
                "http://dbpedia.org/ontology/PersonFunction\n" +
                "http://dbpedia.org/ontology/Philosopher\n" +
                "http://dbpedia.org/ontology/Place\n" +
                "http://dbpedia.org/ontology/Planet\n" +
                "http://dbpedia.org/ontology/Plant\n" +
                "http://dbpedia.org/ontology/Play\n" +
                "http://dbpedia.org/ontology/PlayboyPlaymate\n" +
                "http://dbpedia.org/ontology/PokerPlayer\n" +
                "http://dbpedia.org/ontology/PolishKing\n" +
                "http://dbpedia.org/ontology/PoliticalParty\n" +
                "http://dbpedia.org/ontology/Politician\n" +
                "http://dbpedia.org/ontology/PoloLeague\n" +
                "http://dbpedia.org/ontology/Pope\n" +
                "http://dbpedia.org/ontology/PopulatedPlace\n" +
                "http://dbpedia.org/ontology/PowerStation\n" +
                "http://dbpedia.org/ontology/President\n" +
                "http://dbpedia.org/ontology/Priest\n" +
                "http://dbpedia.org/ontology/PrimeMinister\n" +
                "http://dbpedia.org/ontology/ProgrammingLanguage\n" +
                "http://dbpedia.org/ontology/Project\n" +
                "http://dbpedia.org/ontology/ProtectedArea\n" +
                "http://dbpedia.org/ontology/Protein\n" +
                "http://dbpedia.org/ontology/PublicTransitSystem\n" +
                "http://dbpedia.org/ontology/Race\n" +
                "http://dbpedia.org/ontology/RadioControlledRacingLeague\n" +
                "http://dbpedia.org/ontology/RadioStation\n" +
                "http://dbpedia.org/ontology/RailwayLine\n" +
                "http://dbpedia.org/ontology/RailwayTunnel\n" +
                "http://dbpedia.org/ontology/RecordLabel\n" +
                "http://dbpedia.org/ontology/Referee\n" +
                "http://dbpedia.org/ontology/Reptile\n" +
                "http://dbpedia.org/ontology/ResearchProject\n" +
                "http://dbpedia.org/ontology/Restaurant\n" +
                "http://dbpedia.org/ontology/River\n" +
                "http://dbpedia.org/ontology/Road\n" +
                "http://dbpedia.org/ontology/RoadJunction\n" +
                "http://dbpedia.org/ontology/RoadTunnel\n" +
                "http://dbpedia.org/ontology/Rocket\n" +
                "http://dbpedia.org/ontology/RouteOfTransportation\n" +
                "http://dbpedia.org/ontology/Royalty\n" +
                "http://dbpedia.org/ontology/RugbyLeague\n" +
                "http://dbpedia.org/ontology/RugbyPlayer\n" +
                "http://dbpedia.org/ontology/Saint\n" +
                "http://dbpedia.org/ontology/Sales\n" +
                "http://dbpedia.org/ontology/School\n" +
                "http://dbpedia.org/ontology/Scientist\n" +
                "http://dbpedia.org/ontology/Sculpture\n" +
                "http://dbpedia.org/ontology/Senator\n" +
                "http://dbpedia.org/ontology/Settlement\n" +
                "http://dbpedia.org/ontology/Ship\n" +
                "http://dbpedia.org/ontology/ShoppingMall\n" +
                "http://dbpedia.org/ontology/Single\n" +
                "http://dbpedia.org/ontology/SiteOfSpecialScientificInterest\n" +
                "http://dbpedia.org/ontology/SkiArea\n" +
                "http://dbpedia.org/ontology/Skos:Concept\n" +
                "http://dbpedia.org/ontology/Skyscraper\n" +
                "http://dbpedia.org/ontology/SnookerChamp\n" +
                "http://dbpedia.org/ontology/SnookerPlayer\n" +
                "http://dbpedia.org/ontology/SnookerWorldRanking\n" +
                "http://dbpedia.org/ontology/SoccerClub\n" +
                "http://dbpedia.org/ontology/SoccerClubSeason\n" +
                "http://dbpedia.org/ontology/SoccerLeague\n" +
                "http://dbpedia.org/ontology/SoccerLeagueSeason\n" +
                "http://dbpedia.org/ontology/SoccerManager\n" +
                "http://dbpedia.org/ontology/SoccerPlayer\n" +
                "http://dbpedia.org/ontology/SoccerTournament\n" +
                "http://dbpedia.org/ontology/SoftballLeague\n" +
                "http://dbpedia.org/ontology/Software\n" +
                "http://dbpedia.org/ontology/Song\n" +
                "http://dbpedia.org/ontology/SpaceMission\n" +
                "http://dbpedia.org/ontology/SpaceShuttle\n" +
                "http://dbpedia.org/ontology/SpaceStation\n" +
                "http://dbpedia.org/ontology/Spacecraft\n" +
                "http://dbpedia.org/ontology/Species\n" +
                "http://dbpedia.org/ontology/SpeedwayLeague\n" +
                "http://dbpedia.org/ontology/SpeedwayTeam\n" +
                "http://dbpedia.org/ontology/Sport\n" +
                "http://dbpedia.org/ontology/SportsEvent\n" +
                "http://dbpedia.org/ontology/SportsLeague\n" +
                "http://dbpedia.org/ontology/SportsTeam\n" +
                "http://dbpedia.org/ontology/SportsTeamMember\n" +
                "http://dbpedia.org/ontology/SportsTeamSeason\n" +
                "http://dbpedia.org/ontology/Stadium\n" +
                "http://dbpedia.org/ontology/Station\n" +
                "http://dbpedia.org/ontology/Stream\n" +
                "http://dbpedia.org/ontology/SupremeCourtOfTheUnitedStatesCase\n" +
                "http://dbpedia.org/ontology/Surname\n" +
                "http://dbpedia.org/ontology/Swimmer\n" +
                "http://dbpedia.org/ontology/Tax\n" +
                "http://dbpedia.org/ontology/TeamMember\n" +
                "http://dbpedia.org/ontology/TelevisionEpisode\n" +
                "http://dbpedia.org/ontology/TelevisionSeason\n" +
                "http://dbpedia.org/ontology/TelevisionShow\n" +
                "http://dbpedia.org/ontology/TelevisionStation\n" +
                "http://dbpedia.org/ontology/TennisLeague\n" +
                "http://dbpedia.org/ontology/TennisPlayer\n" +
                "http://dbpedia.org/ontology/Theatre\n" +
                "http://dbpedia.org/ontology/TopicalConcept\n" +
                "http://dbpedia.org/ontology/Town\n" +
                "http://dbpedia.org/ontology/TradeUnion\n" +
                "http://dbpedia.org/ontology/Tunnel\n" +
                "http://dbpedia.org/ontology/University\n" +
                "http://dbpedia.org/ontology/Unknown\n" +
                "http://dbpedia.org/ontology/Valley\n" +
                "http://dbpedia.org/ontology/Vein\n" +
                "http://dbpedia.org/ontology/VicePresident\n" +
                "http://dbpedia.org/ontology/VicePrimeMinister\n" +
                "http://dbpedia.org/ontology/VideoGame\n" +
                "http://dbpedia.org/ontology/VideogamesLeague\n" +
                "http://dbpedia.org/ontology/Village\n" +
                "http://dbpedia.org/ontology/VoiceActor\n" +
                "http://dbpedia.org/ontology/VolleyballLeague\n" +
                "http://dbpedia.org/ontology/VolleyballPlayer\n" +
                "http://dbpedia.org/ontology/WaterwayTunnel\n" +
                "http://dbpedia.org/ontology/Weapon\n" +
                "http://dbpedia.org/ontology/Website\n" +
                "http://dbpedia.org/ontology/WineRegion\n" +
                "http://dbpedia.org/ontology/WomensTennisAssociationTournament\n" +
                "http://dbpedia.org/ontology/Work\n" +
                "http://dbpedia.org/ontology/WorldHeritageSite\n" +
                "http://dbpedia.org/ontology/Wrestler\n" +
                "http://dbpedia.org/ontology/WrestlingEvent\n" +
                "http://dbpedia.org/ontology/Writer\n" +
                "http://dbpedia.org/ontology/WrittenWork\n" +
                "http://dbpedia.org/ontology/Year\n" +
                "http://dbpedia.org/ontology/YearInSpaceflight";
        
        String []classesList = stringDBpediaClasses.split("\n");
        
        for(String strClassURI : classesList){
            String sparqlQuery = String.format("select count(?s) " +
                    "where {?s a <%s>}", strClassURI);

            Query query = QueryFactory
                    .create(sparqlQuery, Syntax.syntaxARQ);

            virtuosoQueryEngine = (QueryEngineHTTP)com.hp.hpl.jena.query.QueryExecutionFactory.sparqlService(
                    DBpedia_Live_ServerAddress, query);

            ResultSet rsItemLabel = virtuosoQueryEngine.execSelect();
            if(rsItemLabel.hasNext()){
                
                /*Iterator iter = rsItemLabel.next().varNames();
                while (iter.hasNext())
                    logger.info(iter.next());*/
                
                RDFNode rdfNode = rsItemLabel.next().get("callret-0");


                int numberOfInstances = rdfNode.asLiteral().getInt();
                System.out.println(strClassURI+ "\t" + numberOfInstances);
            }

        }
        
        /*String sparqlQuery = String.format("select count(?s)\n" +
                "where {?s a <%s>}", itemURI);

        virtuosoQueryEngine = (QueryEngineHTTP)com.hp.hpl.jena.query.QueryExecutionFactory.sparqlService(
                DBpedia_Live_ServerAddress, sparqlQuery);

        ResultSet rsItemLabel = virtuosoQueryEngine.execSelect();
        if(rsItemLabel.hasNext()){
            requiredLabel = rsItemLabel.next().get("?label").asLiteral().getString();

            logger.info("LABEL = " + requiredLabel);
        }


//        String sparqlQuery = "SELECT * WHERE { ?s ?p ?o.filter(?s = <http://dbpedia.org/resource/Lionel_Messi> && ?p = <http://www.w3.org/2000/01/rdf-schema#label>) }";
        return requiredLabel;*/
    }

    public static void main(String[] args) {
        getCountOfInstancesForEachClass();
    }

}
