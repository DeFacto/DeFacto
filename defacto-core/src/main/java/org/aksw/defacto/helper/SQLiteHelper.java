package org.aksw.defacto.helper;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evaluation.ProofExtractor;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.query.MetaQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Created by dnes on 08/12/16.
 */
public class SQLiteHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteHelper.class);
    private static Connection c = null;
    private static SQLiteHelper instance = null;
    private static String db_path = "/Users/esteves/Desktop/defacto.db";
    protected SQLiteHelper() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + db_path);
            c.setAutoCommit(false);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }
    public static SQLiteHelper getInstance() {
        if(instance == null) {
            instance = new SQLiteHelper();
        }
        return instance;
    }

    private Integer existsRecord(String query){
        try{
            Statement stmt = null;
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( query );
            int id = 0;
            while ( rs.next() ) {
                id = rs.getInt(1);
                rs.close();
            }
            stmt.close();
            return id;
        }catch (Exception e){
            return -1;
        }

    }

    public Integer saveEvidenceRoot(Integer idmodel, Evidence eaux, Integer evidencetype) throws Exception{

        Double score = eaux.getDeFactoScore();
        Double combinedscore = eaux.getDeFactoCombinedScore();
        Long tothitcount = eaux.getTotalHitCount();
        String features = eaux.getFeatures().toString();

        int curid = existsRecord("select id from tb_evidence where id_model = " + idmodel + " and " +
                "evidence_type = " + evidencetype);
        if (curid < 1) {
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
            Integer id = stmt.executeUpdate(sBufferSQL.toString());
            stmt.close();
            LOGGER.info(":: evidence header ok");
            return id;
        }
        else{
            throw new Exception("Err: evidence already exists for this model! Please check database for reprocessing");
        }

    }

    public Integer saveModel(DefactoModel model, String filename, String filepath) throws Exception{

        Integer id = -1;
        String name = model.getName();
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

        int curid = existsRecord("select id from tb_model where file_name = '" + filename +
                "' and file_path = '" + filepath + "' and timepoint = " + isTimePoint);
        if (curid < 1) {
            Statement stmt = null;
            StringBuffer sBufferSQL = new StringBuffer(27);
            sBufferSQL.append("INSERT INTO TB_MODEL (model_name, model_correct, file_name, file_path, subject_uri, " +
                    "subject_label, predicate_uri, predicate_label, object_uri, object_label, period_from, period_to, " +
                    "period_timepoint) VALUES ('")
                    .append(name).append("',")
                    .append(correct).append(",'")
                    .append(filename).append("','")
                    .append(filepath).append("','")
                    .append(suri).append("','")
                    .append(slabel).append("','")
                    .append(puri).append("','")
                    .append(plabel).append("','")
                    .append(ouri).append("','")
                    .append(olabel).append("','")
                    .append(from).append("','")
                    .append(to).append("',")
                    .append(isTimePoint).append(");");

            stmt = c.createStatement();
            id = stmt.executeUpdate(sBufferSQL.toString());
            stmt.close();
        }
        else{
            throw new Exception("Err: model already processed! Please check database for reprocessing");
        }

        LOGGER.info(":: model ok");
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

    public boolean addProof(Integer idwebsite, Integer idpattern, Integer idmodel, ComplexProof pro) throws Exception{

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
        String firstlabel = pro.getSubject();
        String secondlabel = pro.getObject();

        Statement stmt = null;

        StringBuffer sBufferSQL = new StringBuffer(37);
        sBufferSQL.append("INSERT INTO TB_PROOF (id_website, id_pattern, id_model, context_tiny, context_tiny_tagged," +
                " context_small, context_small_tagged, context_medium, context_medium_tagged, context_large, context_large_tagged," +
                " phrase, normalised_phrase, " +
                "first_label, second_label, has_pattern_in_between, lang" +
                ") VALUES (")
                .append(idwebsite).append(",")
                .append(idpattern).append(",")
                .append(idmodel).append(",'")
                .append(ctiny).append("','")
                .append(ctinytag).append("','")
                .append(cs).append("','")
                .append(cstag).append("','")
                .append(cm).append("','")
                .append(cmtag).append("','")
                .append(cl).append("','")
                .append(cltag).append("','")
                .append(phrase).append("','")
                .append(nphrase).append("','")
                .append(firstlabel).append("','")
                .append(secondlabel).append("',")
                .append(pro.getHasPatternInBetween() == true ? 1: 0).append(",'")
                .append(pro.getLanguage()).append("');");

        stmt = c.createStatement();
        Integer id = stmt.executeUpdate(sBufferSQL.toString());
        stmt.close();
        LOGGER.info(":: proof ok");
        return true;

    }

    public boolean addTopicTermsEvidence(Integer idevidence, String term, String word, Integer qtd, Integer isfromwiki)
            throws Exception{

        Statement stmt = null;
        String sql = "INSERT INTO TB_REL_TOPICTERM_EVIDENCE(id_evidence, term, topicterm, frequency, is_from_wikipedia)" +
                " VALUES (" + idevidence + ",'" + term  + "','" + word + "'," + qtd + "," + isfromwiki + ");";
        stmt = c.createStatement();
        Integer id = stmt.executeUpdate(sql);
        stmt.close();
        LOGGER.info(":: topic terms x evidence ok");
        return true;

    }

    public boolean addTopicTermsMetaQuery(Integer idmetaquery, String word, Integer qtd, Integer isfromwiki)
            throws Exception{

        Statement stmt = null;
        String sql = "INSERT INTO TB_REL_TOPICTERM_METAQUERY(id_metaquery, topic_term, frequency, is_from_wikipedia)" +
                " VALUES (" + idmetaquery + ",'" + word  + "'," + qtd + "," + isfromwiki + ");";
        stmt = c.createStatement();
        Integer id = stmt.executeUpdate(sql);
        stmt.close();
        LOGGER.info(":: rel topic term x metaquery ok");
        return true;

    }

    public boolean addTopicTermsWebSite(Integer idwebsite, String word, Integer qtd, Integer isfromwiki) throws Exception{

        Statement stmt = null;
        String sql = "INSERT INTO TB_REL_TOPICTERM_WEBSITE " +
                "(id_website, topicterm, frequency, is_from_wikipedia)" +
                " VALUES (" + idwebsite + ",'" + word  + "'," + qtd + "," + isfromwiki + ");";
        stmt = c.createStatement();
        Integer id = stmt.executeUpdate(sql);
        stmt.close();
        LOGGER.info(":: rel topic term x website ok");
        return true;

    }

    public Integer savePattern(Integer idevidence, Pattern psite) throws Exception{

        Double boa_score = psite.boaScore;
        String nlpn = psite.naturalLanguageRepresentationNormalized;
        String nlpnovar = psite.naturalLanguageRepresentationWithoutVariables;
        String nlp = psite.naturalLanguageRepresentation;
        String ln = psite.language;
        String pos = psite.posTags;
        String ner = psite.NER;
        String generalized = psite.generalized;
        Double nlp_score = psite.naturalLanguageScore;

        Statement stmt = null;

        StringBuffer sBufferSQL = new StringBuffer(21);
        sBufferSQL.append("INSERT INTO TB_PATTERN (id_evidence, score_boa, nlp_normalized, nlp_no_var, nlp, " +
                "lang, nlp_score, pos, generalized, ner" +
                ") VALUES (")
                .append(idevidence).append(",")
                .append(boa_score).append(",'")
                .append(nlpn).append("','")
                .append(nlpnovar).append("','")
                .append(nlp).append("','")
                .append(ln).append("',")
                .append(nlp_score).append(",'")
                .append(pos).append("','")
                .append(generalized).append("','")
                .append(ner).append("');");

        stmt = c.createStatement();
        Integer id = stmt.executeUpdate(sBufferSQL.toString());
        stmt.close();
        LOGGER.info(":: pattern ok");
        return id;

    }

    public Integer saveMetaQuery(Integer idpattern, MetaQuery msite) throws Exception{

        String metaquery = msite.toString();
        String sl = msite.getSubjectLabel();
        String pl = msite.getPropertyLabel();
        String ol = msite.getObjectLabel();
        String lang = msite.getLanguage();
        String evidencetype = msite.getEvidenceTypeRelation().toString();

        Statement stmt = null;
        String sql = "INSERT INTO TB_METAQUERY " +
                "(id_pattern, metaquery, " +
                "subject_label, predicate_label, object_label, lang, evidence_type)" +
                " VALUES (" + idpattern + ",'" + metaquery  + "','" + sl + "','" + pl  + "','" + ol + "','" + lang
                + "','" + evidencetype + "');";
        stmt = c.createStatement();
        Integer id = stmt.executeUpdate(sql);
        stmt.close();
        LOGGER.info(":: metaquery ok");
        return id;

    }

    public Integer saveWebSite(Integer idmetaquery, Integer idpattern, WebSite w) throws Exception{

        URI url = new URI(w.getUrl().toString());
        String urldomain = url.getHost();
        String title = w.getTitle();
        String body = w.getText();
        Integer rank = w.getSearchRank();
        Integer pagerank = w.getPageRank();
        Double pagerankscore =w.getPageRankScore();
        Double ind_score = w.getScore();
        Double ind_topic_majority_web = w.getTopicMajorityWebFeature();
        Double ind_topic_majority_search = w.getTopicMajoritySearchFeature();
        Double ind_topic_coverage_score = w.getTopicCoverageScore();
        String lang = w.getLanguage();

        Statement stmt = null;

        title = title.replaceAll("'", "''");
        body = body.replaceAll("'", "''");

        StringBuffer sBufferSQL = new StringBuffer(29);
        sBufferSQL.append("INSERT INTO TB_WEBSITE (id_metaquery, id_pattern, url, url_domain, title, body, rank, " +
                "pagerank, pagerank_score, ind_score, ind_topic_majority_web, ind_topic_majority_search, " +
                "ind_topic_coverage_score, lang" +
                ") VALUES (")
                .append(idmetaquery).append(",")
                .append(idpattern).append(",'")
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
                .append(ind_topic_coverage_score).append(",'")
                .append(lang).append("');");

        stmt = c.createStatement();
        Integer id = stmt.executeUpdate(sBufferSQL.toString());
        stmt.close();
        LOGGER.info(":: website ok");
        return id;


    }


    public boolean saveQuery(String metaquery, String suri, String slen,
                                   String puri, String plen,
                                   String ouri, String olen,
                                   Integer idlang, Long hits, Integer sourcecandidate, String fname, String fpath){
        try{
            Statement stmt = null;
            String sql = "INSERT INTO TB_QUERY " +
                          "(metaquery, " +
                          "subject_uri, subject_label_en," +
                          "predicate_uri, predicate_label_en," +
                          "object_uri, object_label_en," +
                          "id_language, processing_date, hits, file_ref, file_ref_path)" +
                          " VALUES (" + metaquery + ",'" +
                                        suri  + "','" + slen + "','" +
                                        puri  + "','" + plen + "','" +
                                        ouri  + "','" + olen + "'," +
                                        idlang + "," + hits  +  ",'" +
                                        fname + "','" + fpath + "');";
            stmt = c.createStatement();
            stmt.executeUpdate(sql);
        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
        return true;
    }


    public static void main( String args[] )
    {

    }

}
