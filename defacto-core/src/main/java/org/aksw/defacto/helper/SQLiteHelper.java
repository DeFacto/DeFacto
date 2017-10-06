package org.aksw.defacto.helper;

import com.mysql.jdbc.*;
import edu.stanford.nlp.util.*;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evaluation.ProofExtractor;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.defacto.search.concurrent.NlpModelManager;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.util.NlpUtil;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

/**
 * Created by dnes on 08/12/16.
 */
public class SQLiteHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteHelper.class);
    private static Connection c = null;
    private static SQLiteHelper instance = null;
    //private static String db_path = "/Users/esteves/Dropbox/Doutorado_Alemanha/#Papers/#DeFacto Files/ACM_Data_Quality_Journal/defacto_wsdm.db";
    private static String db_path = "/Users/esteves/Dropbox/Doutorado_Alemanha/#Papers/#DeFacto Files/ACM_Data_Quality_Journal/defacto_wdm_fixed_profession_patterns.db";
    //private static String db_path = "/Users/esteves/Dropbox/Doutorado_Alemanha/#Papers/#DeFacto Files/ISWC2017_Challenge/defacto_iswc17_challenge.db";
    //private static String db_path = "data/database/defacto_neg.db";
    //private static String db_path = "data/database/anisa/defacto_anisa.db";
    protected SQLiteHelper() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + db_path);
            c.setAutoCommit(false);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(-1);
        }
        System.out.println("Opened database successfully");
    }
    public static SQLiteHelper getInstance() {
        if(instance == null) {
            instance = new SQLiteHelper();
        }
        return instance;
    }

    public static Connection getConnection(){
        return c;
    }

    public Integer existsRecord(PreparedStatement prep) throws Exception{

        ResultSet rs = prep.executeQuery();
        int id = 0;
        while ( rs.next() ) {
            id = rs.getInt(1);
            rs.close();
        }
        prep.close();
        return id;
    }

    public Integer saveEvidenceRoot(Integer idmodel, Evidence eaux, Integer evidencetype) throws Exception{

        Double score = eaux.getDeFactoScore();
        Double combinedscore = eaux.getDeFactoCombinedScore();
        Long tothitcount = eaux.getTotalHitCount();
        String features = eaux.getFeatures().toString();

        String sSQL = "SELECT ID FROM TB_EVIDENCE WHERE ID_MODEL = ? AND EVIDENCE_TYPE = ?";
        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setInt(1, idmodel);
        prep.setInt(2, evidencetype);

        int id = existsRecord(prep);

        if (id == 0) {
            Statement stmt = null;
            StringBuffer sBufferSQL = new StringBuffer(13);

            features = features.replaceAll("'", "''");

            sBufferSQL.append("INSERT INTO TB_EVIDENCE (id_model, score, combined_score, total_hit_count, " +
                    "features, evidence_type) VALUES ( ")
                    .append(idmodel).append(",")
                    .append(score).append(",")
                    .append(combinedscore).append(",")
                    .append(tothitcount).append(",'")
                    .append(features).append("',")
                    .append(evidencetype).append(");");

            stmt = c.createStatement();
            stmt.executeUpdate(sBufferSQL.toString());
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            id = keys.getInt(1);
            keys.close();
            stmt.close();
            LOGGER.debug(":: evidence header ok");


        }
        return id;

    }

    public boolean saveYearOccorrence(Integer idevidence, Integer idcontext, Map<String, Long> oc) throws Exception{

        Iterator it = oc.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String year = pair.getKey().toString();
            Long occurrences = (Long) pair.getValue();

            String sSQL = "SELECT ID FROM TB_YEAR_OCCURRENCES " +
                    "WHERE ID_EVIDENCE = ? AND YEAR = ? AND OCCURRENCES = ? AND CONTEXT = ?";
            PreparedStatement prep = c.prepareStatement(sSQL);
            prep.setInt(1, idevidence);
            prep.setString(2, year);
            prep.setLong(3, occurrences);
            prep.setInt(4, idcontext);

            int id = existsRecord(prep);
            if (id == 0) {
                Statement stmt = null;
                StringBuffer sBufferSQL = new StringBuffer(9);
                sBufferSQL.append("INSERT INTO TB_YEAR_OCCURRENCES (id_evidence, year, occurrences, context) VALUES (")
                        .append(idevidence).append(",")
                        .append(year).append(",")
                        .append(occurrences).append(",")
                        .append(idcontext).append(");");

                stmt = c.createStatement();
                stmt.executeUpdate(sBufferSQL.toString());
                stmt.close();
                LOGGER.debug(":: tb_year_occurrences ok");
            }

        }

        return true;

    }

    public Integer saveModel(long processing_time, DefactoModel model, String filename, String filepath, String name) throws Exception {


        int correct = model.isCorrect() ? 1 : 0;
        String suri = model.getSubjectUri();
        String slabel = model.getSubjectLabel("en");
        String puri = model.getPredicate().getURI();
        String plabel = model.getPredicate().getLocalName();
        String ouri = model.getObjectUri();
        String olabel = model.getObjectLabel("en");
        String from = model.getTimePeriod().getFrom().toString();
        String to = model.getTimePeriod().getTo().toString();
        int isTimePoint = model.getTimePeriod().isTimePoint() ? 1 : 0;
        String langs = model.getLanguages().toString();

        String sSQL =
                "SELECT ID FROM TB_MODEL WHERE FILE_NAME = ? AND FILE_PATH = ? AND PERIOD_TIMEPOINT = ? AND LANGS = ?";
        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setString(1, filename);
        prep.setString(2, filepath);
        prep.setInt(3, isTimePoint);
        prep.setString(4, langs);

        int id = existsRecord(prep);
            if (id == 0) {
                Statement stmt = null;
                StringBuffer sBufferSQL = new StringBuffer(29);

                sSQL = "INSERT INTO TB_MODEL (MODEL_NAME, MODEL_CORRECT, FILE_NAME, FILE_PATH, SUBJECT_URI, " +
                        "SUBJECT_LABEL, PREDICATE_URI, PREDICATE_LABEL, OBJECT_URI, OBJECT_LABEL, PERIOD_FROM, PERIOD_TO, " +
                        "PROCESSING_TIME, LANGS, PERIOD_TIMEPOINT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

                prep = c.prepareStatement(sSQL);
                prep.setString(1, name);
                prep.setInt(2, correct);
                prep.setString(3, filename);
                prep.setString(4, filepath);
                prep.setString(5, suri);
                prep.setString(6, slabel);
                prep.setString(7, puri);
                prep.setString(8, plabel);
                prep.setString(9, ouri);
                prep.setString(10, olabel);
                prep.setString(11, from);
                prep.setString(12, to);
                prep.setLong(13, processing_time);
                prep.setString(14, langs);
                prep.setInt(15, isTimePoint);

                prep.executeUpdate();
                ResultSet keys = prep.getGeneratedKeys();
                keys.next();
                id = keys.getInt(1);
                keys.close();
                prep.close();
                LOGGER.debug(":: model ok");
            }



        return id;
    }

    public boolean executeQueriesInBatch(List<String> queries){

        try{
            if (c.getMetaData().supportsBatchUpdates()){
                Statement stmt = c.createStatement();
                for(String query: queries) {
                    stmt.addBatch(query);
                }
                stmt.executeBatch();
                return true;
            }
            else{
                System.out.println("error: batch updates are not supported!");
                return false;}
        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
    }

    public boolean rollbackT(){
        try{
            c.rollback();
            return true;
        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
    }

    public boolean commitT(){
        try{
            c.commit();
            return true;
        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
    }

    public boolean addSearchResults(Integer idquery, String surl, String sdomain,
                                       String stitle, String sbody,
                                       Integer irank, Integer ipagerank){
        try{
            Statement stmt = null;
            String sql = "INSERT INTO TB_RESULTS_WEBSITE (id_query,url, domain, title, body, rank, pagerank) " +
                         "VALUES (" + idquery + "," + "'" + surl + "','" + sdomain + "','" + stitle + "','" +
                                      sbody + "'," + irank + "," + ipagerank + ");";
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
        return true;
    }

    public boolean assignQueryToResults(Integer idquery, Integer idresult){
        try{
            Statement stmt = null;
            String sql = "INSERT INTO TB_QUERY_RESULTS (id_query, id_result) VALUES (" + idquery + "," + idresult + ");";
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
        return true;
    }

    public boolean setQueryProcessed(Integer idquery){
        try{
            Statement stmt = null;
            String sql = "UPDATE TB_QUERY set processed = 1 " +
                    "WHERE id_query = " + idquery + ";";
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
        return true;
    }

    protected boolean setSourceCandidate(Integer idquery, Integer idresult){
        try{
            Statement stmt = null;
            String sql = "UPDATE TB_QUERY_RESULTS " +
                    "set source_candidate = 1 WHERE id_query = " + idquery +
                    " and " + "id_result = " + idresult + ";";
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
        return true;
    }

    public Integer addProof(Integer idmodel, ComplexProof pro, Integer idevidence, Evidence evidence) throws Exception{

        //TODO: has_proof should be a property of a website. I will not change it now...
        Integer has_proof = 0;
        if (evidence.getComplexProofs(pro.getWebSite()).size() > 0){
            has_proof = 1;
        }

        Integer idmetaquery = saveMetaQuery(idevidence, pro.getWebSite().getQuery());

        Integer idwebsite =  saveWebSite(idmetaquery, pro.getWebSite(), has_proof);

        Integer idpattern = savePattern(pro.getPattern());

        /*String sql = "SELECT id FROM TB_PROOF WHERE ID_WEBSITE = " + idwebsite +
                " AND ID_PATTERN = " + idpattern + " AND ID_MODEL = " + idmodel + " AND LANG = '" +
                pro.getPattern().language + "' AND FIRST_LABEL = '" + pro.getSubject().replaceAll("'", "''") +
                "' AND SECOND_LABEL = '" + pro.getObject().replaceAll("'", "''") +
                "' AND CONTEXT_TINY = '" + pro.getTinyContext().replaceAll("'", "''") + "'";
*/
        String sSQL = "SELECT ID FROM TB_PROOF WHERE ID_WEBSITE = ? AND ID_PATTERN = ? AND ID_MODEL = ? AND LANG = ?" +
                " AND FIRST_LABEL = ? AND SECOND_LABEL = ? AND CONTEXT_TINY = ?";
        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setInt(1, idwebsite);
        prep.setInt(2, idpattern);
        prep.setInt(3,idmodel);
        prep.setString(4, pro.getPattern().language);
        prep.setString(5, pro.getSubject());
        prep.setString(6, pro.getObject());
        prep.setString(7, pro.getTinyContext());

        Integer id = existsRecord(prep);

        if (id == 0) {
            String ctiny = pro.getTinyContext().replaceAll("'", "''");
            String cs = pro.getSmallContext().replaceAll("'", "''");
            String cm = pro.getMediumContext().replaceAll("'", "''");
            String cl = pro.getLargeContext().replaceAll("'", "''");
            String ctinytag = pro.getTaggedTinyContext().replaceAll("'", "''");
            String cstag = pro.getTaggedSmallContext().replaceAll("'", "''");
            String cmtag = pro.getTaggedMediumContext().replaceAll("'", "''");
            String cltag = pro.getTaggedLargeContext().replaceAll("'", "''");
            String phrase = pro.getProofPhrase().replaceAll("'", "''");
            String nphrase = pro.getNormalizedProofPhrase().replaceAll("'", "''");
            String firstlabel = pro.getSubject().replaceAll("'", "''");
            String secondlabel = pro.getObject().replaceAll("'", "''");

            sSQL = "INSERT INTO TB_PROOF (ID_WEBSITE, ID_PATTERN, ID_MODEL, CONTEXT_TINY, CONTEXT_TINY_TAGGED, " +
                    "CONTEXT_SMALL, CONTEXT_SMALL_TAGGED, CONTEXT_MEDIUM, CONTEXT_MEDIUM_TAGGED, CONTEXT_LARGE, " +
                    "CONTEXT_LARGE_TAGGED, PHRASE, NORMALISED_PHRASE, " +
                    "FIRST_LABEL, SECOND_LABEL, HAS_PATTERN_NORMALIZED_IN_BETWEEN, LANG" +
                    ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

            prep = c.prepareStatement(sSQL);
            prep.setInt(1, idwebsite);
            prep.setInt(2, idpattern);
            prep.setInt(3, idmodel);
            prep.setString(4, ctiny);
            prep.setString(5, ctinytag);
            prep.setString(6, cs);
            prep.setString(7, cstag);
            prep.setString(8, cm);
            prep.setString(9, cmtag);
            prep.setString(10, cl);
            prep.setString(11, cltag);
            prep.setString(12, phrase);
            prep.setString(13, nphrase);
            prep.setString(14, firstlabel);
            prep.setString(15, secondlabel);
            prep.setInt(16, pro.getHasPatternInBetween() == true ? 1: 0);
            prep.setString(17, pro.getLanguage());

            prep.executeUpdate();
            ResultSet keys = prep.getGeneratedKeys();
            keys.next();
            id = keys.getInt(1);
            keys.close();
            prep.close();
            LOGGER.debug(":: proof ok");
        }

        return id;

    }

    public boolean addTopicTermsEvidence(Integer idevidence, String ln, String word, Integer qtd, Integer isfromwiki)
            throws Exception{

        String sSQL = "SELECT ID FROM TB_REL_TOPICTERM_EVIDENCE WHERE ID_EVIDENCE = ? AND TOPICTERM = ? AND LANG = ?";
        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setInt(1, idevidence);
        prep.setString(2, word);
        prep.setString(3, ln);

        Integer id = existsRecord(prep);
        if (id == 0) {
            Statement stmt = null;
            String sql = "INSERT INTO TB_REL_TOPICTERM_EVIDENCE(id_evidence, lang, topicterm, frequency, " +
                    "is_from_wikipedia) VALUES (" + idevidence + ",'" + ln  + "','" + word + "'," + qtd +
                    "," + isfromwiki + ");";
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            LOGGER.debug(":: topic terms x evidence ok");
        }
         return true;

    }

    public boolean addTopicTermsMetaQuery(Integer idmetaquery, String word, Integer qtd, Integer isfromwiki)
            throws Exception{

        String sSQL = "SELECT ID FROM TB_TOPICTERM_METAQUERY WHERE ID_METAQUERY = ? AND TOPICTERM = ?";
        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setInt(1, idmetaquery);
        prep.setString(2, word);

        Integer id = existsRecord(prep);
        if (id == 0) {
            Statement stmt = null;
            String sql = "INSERT INTO TB_REL_TOPICTERM_METAQUERY(id_metaquery, topic_term, frequency, is_from_wikipedia)" +
                    " VALUES (" + idmetaquery + ",'" + word  + "'," + qtd + "," + isfromwiki + ");";
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            LOGGER.debug(":: rel topic term x metaquery ok");
        }
        return true;

    }

    public boolean addTopicTermsWebSite(Integer idwebsite, String word, Integer qtd, Integer isfromwiki) throws Exception{

        String sSQL = "SELECT ID FROM TB_REL_TOPICTERM_WEBSITE WHERE ID_WEBSITE = ? AND TOPICTERM = ?";
        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setInt(1, idwebsite);
        prep.setString(2, word);

        Integer id = existsRecord(prep);
        if (id == 0) {
            Statement stmt = null;
            String sql = "INSERT INTO TB_REL_TOPICTERM_WEBSITE " +
                    "(id_website, topicterm, frequency, is_from_wikipedia)" +
                    " VALUES (" + idwebsite + ",'" + word  + "'," + qtd + "," + isfromwiki + ");";
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            LOGGER.debug(":: rel topic term x website ok");
        }
        return true;

    }

    public Integer savePatternMetaQuery(Integer idpattern, Integer idmetaquery) throws Exception {

        String sSQL = "SELECT ID FROM TB_PATTERN_METAQUERY WHERE ID_PATTERN = ? AND ID_METAQUERY = ?";
        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setInt(1, idpattern);
        prep.setInt(2, idmetaquery);

        Integer id = existsRecord(prep);
        if (id == 0) {
            Statement stmt = null;

            StringBuffer sBufferSQL = new StringBuffer(5);
            sBufferSQL.append("INSERT INTO TB_PATTERN_METAQUERY (id_pattern, id_metaquery) VALUES (")
                    .append(idpattern).append(",")
                    .append(idmetaquery).append(");");
            String sql = sBufferSQL.toString();

            stmt = c.createStatement();
            stmt.executeUpdate(sql);
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            id = keys.getInt(1);
            keys.close();
            stmt.close();
            LOGGER.debug(":: pattern_metaquery ok");
        }
        return id;
    }

    public Integer savePattern(Pattern psite) throws Exception{

        String sSQL = "SELECT ID FROM TB_PATTERN WHERE NLP = ? AND LANG = ?";
        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setString(1, psite.naturalLanguageRepresentation);
        prep.setString(2, psite.language);

        Integer id = existsRecord(prep);
        if (id == 0) {
            sSQL = "INSERT INTO TB_PATTERN (score_boa, nlp_normalized, nlp_no_var, nlp, " +
                    "lang, nlp_score, pos, generalized, ner) VALUES (?,?,?,?,?,?,?,?,?);";

            prep = c.prepareStatement(sSQL);
            prep.setDouble(1, psite.boaScore);
            prep.setString(2, psite.naturalLanguageRepresentationNormalized);
            prep.setString(3, psite.naturalLanguageRepresentationWithoutVariables);
            prep.setString(4, psite.naturalLanguageRepresentation);
            prep.setString(5, psite.language);
            prep.setDouble(6, psite.naturalLanguageScore);
            prep.setString(7, psite.posTags);
            prep.setString(8, psite.generalized);
            prep.setString(9, psite.NER);

            prep.executeUpdate();
            ResultSet keys = prep.getGeneratedKeys();
            keys.next();
            id = keys.getInt(1);
            keys.close();
            prep.close();
            LOGGER.debug(":: pattern ok");

        }

        return id;

    }

    public Integer saveMetaQuery(Integer idevidence, MetaQuery msite) throws Exception{


        String sSQL = "SELECT ID FROM TB_METAQUERY WHERE METAQUERY = ?";
        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setString(1, msite.toString());
        Integer id = existsRecord(prep);

        if (id == 0) {

            sSQL = "INSERT INTO TB_METAQUERY (metaquery, subject_label, predicate_label, object_label, " +
            "lang, evidence_type, id_evidence) VALUES (?,?,?,?,?,?,?);";

            prep = c.prepareStatement(sSQL);
            prep.setString(1, msite.toString());
            prep.setString(2, msite.getSubjectLabel());
            prep.setString(3, msite.getPropertyLabel());
            prep.setString(4, msite.getObjectLabel());
            prep.setString(5, msite.getLanguage());
            prep.setString(6, msite.getEvidenceTypeRelation().toString());
            prep.setInt(7, idevidence);
            prep.executeUpdate();
            ResultSet keys = prep.getGeneratedKeys();
            keys.next();
            id = keys.getInt(1);
            keys.close();
            prep.close();
            LOGGER.debug(":: metaquery ok");

        }

        return id;

    }

    public Integer saveWebSite(Integer idmetaquery, WebSite w, Integer has_proof) throws Exception{

        //https://www.coppercanyonpress.org/pages/browse/book.asp?bg={19621816-B5E9-4075-AF68-E409FFCED6D4}

        String urldomain;
        String urlstr;
        try{
            URI url = new URI(w.getUrl());
            urlstr = url.toString();
            urldomain = url.getHost();
        }
        catch (Exception e){
            urlstr = w.getUrl();
            int slashslash = urlstr.indexOf("//") + 2;
            urldomain = urlstr.substring(slashslash, urlstr.indexOf('/', slashslash));
        }

        String title = w.getTitle().replaceAll("'", "''");
        String body = w.getText().replaceAll("'", "''");
        Integer rank = w.getSearchRank();
        Integer pagerank = w.getPageRank();
        Double pagerankscore =w.getPageRankScore();
        Double ind_score = w.getScore();
        Double ind_topic_majority_web = w.getTopicMajorityWebFeature();
        Double ind_topic_majority_search = w.getTopicMajoritySearchFeature();
        Double ind_topic_coverage_score = w.getTopicCoverageScore();
        String lang = w.getLanguage();

        Statement stmt = null;

        String sSQL = "SELECT ID FROM TB_WEBSITE WHERE ID_METAQUERY = ? AND URL = ? AND LANG = ?";
        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setInt(1, idmetaquery);
        prep.setString(2, urlstr);
        prep.setString(3, w.getLanguage());

        Integer id = existsRecord(prep);

        if (id == 0) {
            //StringBuffer sBufferSQL = new StringBuffer(31);
            sSQL = "INSERT INTO TB_WEBSITE (id_metaquery, url, url_domain, title, body, rank, " +
                    "pagerank, pagerank_score, ind_score, ind_topic_majority_web, ind_topic_majority_search, " +
                    "ind_topic_coverage_score, has_proof, lang" +
                    ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

            prep = c.prepareStatement(sSQL);
            prep.setInt(1, idmetaquery);
            prep.setString(2, urlstr);
            prep.setString(3, urldomain);
            prep.setString(4, w.getTitle());
            prep.setString(5, w.getText());
            prep.setInt(6, w.getSearchRank());
            prep.setInt(7, w.getPageRank());
            prep.setDouble(8, w.getPageRankScore());
            prep.setDouble(9, w.getScore());
            prep.setDouble(10, w.getTopicMajorityWebFeature());
            prep.setDouble(11, w.getTopicMajoritySearchFeature());
            prep.setDouble(12, w.getTopicCoverageScore());
            prep.setInt(13, has_proof);
            prep.setString(14, w.getLanguage());

            /*sBufferSQL.append("INSERT INTO TB_WEBSITE (id_metaquery, url, url_domain, title, body, rank, " +
                    "pagerank, pagerank_score, ind_score, ind_topic_majority_web, ind_topic_majority_search, " +
                    "ind_topic_coverage_score, has_proof, lang" +
                    ") VALUES (")
                    .append(idmetaquery).append(",'")
                    .append(url).append("','")
                    .append(urldomain).append("','")
                    .append(title).append("','")
                    .append(body).append("',")
                    .append(rank).append(",")
                    .append(pagerank).append(",")
                    .append(pagerankscore).append(",")
                    .append(ind_score).append(",")
                    .append(ind_topic_majority_web).append(",")
                    .append(ind_topic_majority_search).append(",")
                    .append(ind_topic_coverage_score).append(",")
                    .append(has_proof).append(",'")
                    .append(lang).append("');");
                    */

            //stmt = c.createStatement();
            prep.executeUpdate();
            //stmt.executeUpdate(sBufferSQL.toString(), new String[]{"sadsad"});
            //stmt.executeUpdate(sSQL, new String[]{idmetaquery});
            ResultSet keys = prep.getGeneratedKeys();
            keys.next();
            id = keys.getInt(1);
            keys.close();
            //stmt.close();
            prep.close();
            LOGGER.debug(":: website ok");
        }

        return id;

    }


    public static void main( String args[] )
    {
        try {
            SQLiteHelper h = new SQLiteHelper();
            //h.annotateSomeProofs(170000);
            h.NEstatsForSandO(170000);
        }catch (Exception e){
            System.out.print(e.toString());
        }
    }


    public void InsertNewTaggedProofs(Integer id, String tiny, String small, String medium, String large, String body) throws Exception{

        //String uSQL = "UPDATE TB_PROOF SET CONTEXT_TINY_TAGGED2 = ?, CONTEXT_SMALL_TAGGED2 = ?, CONTEXT_MEDIUM_TAGGED2 = ?, " +
       //         "CONTEXT_LARGE_TAGGED2 = ? WHERE ID = ?;";

        String uSQL = "UPDATE TB_PROOF SET BODY_TAGGED2 = ? WHERE ID = ?;";
        PreparedStatement prep = c.prepareStatement(uSQL);

        prep.setString(1, body);
        prep.setInt(2, id);

        prep.executeUpdate();
        c.commit();
        prep.close();

    }

    public void NEstatsForSandO(int limit) throws Exception{
        String sSQL = "SELECT p.context_large_tagged2, p.first_label, p.second_label, model.predicate_label " +
                "FROM TB_PROOF p, TB_MODEL model WHERE model.predicate_label = 'spouse' and p.id_model = model.id and p.LANG = 'en' LIMIT ?";
        long sper = 0;
        long sloc = 0;
        long sorg = 0;

        long oper = 0;
        long oloc = 0;
        long oorg = 0;

        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setInt(1, limit);
        ResultSet rs = prep.executeQuery();
        while (rs.next()) {
            String[] splittxt = rs.getString(1).split("-=-");
            for (int i=0; i< splittxt.length - 1; i++){
                String[] NERandTAG = splittxt[i].split("_");
                if (rs.getString(2).toUpperCase().equals(NERandTAG[0].toUpperCase())){
                    if (NERandTAG[1].equals("PERSON"))
                        sper++;
                    if (NERandTAG[1].equals("PLACE"))
                        sloc++;
                    if (NERandTAG[1].equals("ORGANIZATION"))
                        sorg++;
                }
                if (rs.getString(3).toUpperCase().equals(NERandTAG[0].toUpperCase())){
                    if (NERandTAG[1].equals("PERSON"))
                        oper++;
                    if (NERandTAG[1].equals("PLACE"))
                        oloc++;
                    if (NERandTAG[1].equals("ORGANIZATION"))
                        oorg++;
                }

            }
        }
        System.out.println(sper);
        System.out.println(sloc);
        System.out.println(sorg);
        System.out.println(oper);
        System.out.println(oloc);
        System.out.println(oorg);

    }

    public void annotateSomeProofs(int limit)throws Exception{

        String sSQL = "SELECT p.id, p.context_tiny, p.context_small, p.context_medium, p.context_large, w.body " +
                "FROM TB_PROOF p, TB_WEBSITE w WHERE w.id = p.id_website and p.LANG = 'en' LIMIT ?";
        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setInt(1, limit);
        ResultSet rs = prep.executeQuery();

        StanfordNLPNamedEntityRecognition nerTagger = NlpModelManager.getInstance().getNlpModel();

        String merged_large;
        String merged_medium;
        String merged_small;
        String merged_tiny;
        String merged_body;

        while (rs.next()) {

            merged_tiny = edu.stanford.nlp.util.StringUtils.join(
                    NlpUtil.mergeConsecutiveNerTags(nerTagger.getAnnotatedSentences(rs.getString(2))), "-=-");
            merged_small = edu.stanford.nlp.util.StringUtils.join(
                    NlpUtil.mergeConsecutiveNerTags(nerTagger.getAnnotatedSentences(rs.getString(3))), "-=-");
            merged_medium = edu.stanford.nlp.util.StringUtils.join(
                    NlpUtil.mergeConsecutiveNerTags(nerTagger.getAnnotatedSentences(rs.getString(4))), "-=-");
            merged_large = edu.stanford.nlp.util.StringUtils.join(
                    NlpUtil.mergeConsecutiveNerTags(nerTagger.getAnnotatedSentences(rs.getString(5))), "-=-");
            merged_body = edu.stanford.nlp.util.StringUtils.join(
                    NlpUtil.mergeConsecutiveNerTags(nerTagger.getAnnotatedSentences(rs.getString(6))), "-=-");

            InsertNewTaggedProofs(rs.getInt(1), merged_tiny, merged_small, merged_medium, merged_large, merged_body);
            merged_tiny = "";
            merged_small = "";
            merged_medium = "";
            merged_large = "";
            merged_body = "";
        }

    }

    public Map<Integer, String> getTopNWebSitesURLNotQueriesSameAs(Integer limit, Integer offset) throws Exception{

        Map<Integer, String> ret = new HashMap<>();
        String sSQL = "SELECT ID, URL FROM TB_WEBSITE WHERE ID NOT IN (SELECT ID_WEBSITE FROM TB_WEBSITE_SAMEAS) " +
                "LIMIT ? OFFSET ?";
        PreparedStatement prep = c.prepareStatement(sSQL);
        prep.setInt(1, limit);
        prep.setInt(2, offset);
        ResultSet rs = prep.executeQuery();
        while (rs.next()) {
            ret.put(rs.getInt(1), rs.getString(2));
        }

        return ret;

    }

    public void InsertSameAsResources(Integer root, ArrayList<String> similars) throws Exception{

        String uSQL = "INSERT INTO TB_WEBSITE_SAMEAS(ID_WEBSITE, URL) VALUES (?, ?);";
        PreparedStatement prep = c.prepareStatement(uSQL);

        for (String similar: similars){
            prep.setInt(1, root);
            prep.setString(2, similar);
            prep.executeUpdate();
        }

        c.commit();
        prep.close();

    }

    public void UpdateHTMLforWebSite(HashMap<Integer, String> content) throws Exception{

        String uSQL = "UPDATE TB_WEBSITE SET BODY_ORIGINAL = ?, ERROR_PROCESSING = 0 WHERE ID = ?;";
        PreparedStatement prep = c.prepareStatement(uSQL);

        for (Map.Entry<Integer, String> entry : content.entrySet()) {
            prep.setInt(2, entry.getKey());
            prep.setString(1, entry.getValue());
            prep.executeUpdate();
        }

        c.commit();
        prep.close();

    }

}
