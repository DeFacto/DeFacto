package org.aksw.defacto.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Created by dnes on 08/12/16.
 */
public class SQLiteHelper {

    private static Connection c = null;
    private static SQLiteHelper instance = null;
    protected SQLiteHelper() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:/Users/dnes/Desktop/defacto.db");
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

    public Integer saveModel(String name, int correct, String filename, String filepath,
                             String suri, String puri, String ouri, String from, String to, int isTimePoint){
        try {
            int curid = existsRecord("select id from tb_model where file_name = '" + filename +
                    "' and file_path = '" + filepath + "'");
            if (curid < 1) {
                Statement stmt = null;
                String sql = "INSERT INTO TB_MODEL (model_name, model_correct, file_name, file_path, resource_s_uri, " +
                        "resource_p_uri, resource_o_uri, period_from, period_to, period_timepoint) " +
                        "VALUES ('" + name + "'," + correct + ",'" + filename + "','" + filepath + "','" +
                        suri + "','" + puri + "','" + ouri + "','" + from + "','" + to + "'," + isTimePoint + ");";
                stmt = c.createStatement();
                System.out.print(sql);
                Integer id = stmt.executeUpdate(sql);
                stmt.close();
                return id;
            }
            else{
                return curid;
            }
        } catch (Exception e){
            System.out.println(e.toString());
            return -1;
        }
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

    public boolean addProof(Integer idwebsite, Integer idpattern, Integer idmodel,
                            Integer candidate, String ctiny, String cs, String cm, String cl,
                            String phrase, String nphrase, String lang){

        try{
            Statement stmt = null;
            String sql = "INSERT INTO TB_PROOF " +
                    "(id_website, id_pattern, id_model, candidate, context_tiny, context_small, context_medium, " +
                    "context_large, phrase, normalised_phrase, lang)" +
                    " VALUES (" + idwebsite + "," + idpattern  + "," + idmodel + "," + candidate + ",'" +
                    ctiny + "','" + cs  + "','" + cm + "','" + cl + "','" + phrase + "','" + nphrase + "','" + lang + "');";
            stmt = c.createStatement();
            Integer id = stmt.executeUpdate(sql);
            stmt.close();
            return true;

        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }

    }

    public boolean addTopicTermsMetaQuery(Integer idmetaquery, String word, Integer qtd, Integer isfromwiki){

        try{
            Statement stmt = null;
            String sql = "INSERT INTO TB_REL_METAQUERT_TOPICTERM " +
                    "(id_metaquery, topic_term, frequency, is_from_wikipedia)" +
                    " VALUES (" + idmetaquery + ",'" +
                    word  + "'," + qtd + "," +
                    isfromwiki + ");";
            stmt = c.createStatement();
            Integer id = stmt.executeUpdate(sql);
            stmt.close();
            return true;

        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }

    }

    public boolean addTopicTermsWebSite(Integer idwebsite, String word, Integer qtd, Integer isfromwiki){

        try{
            Statement stmt = null;
            String sql = "INSERT INTO TB_REL_WEBSITE_TOPICTERM " +
                    "(id_website, topic_term, frequency, is_from_wikipedia)" +
                    " VALUES (" + idwebsite + ",'" +
                    word  + "'," + qtd + "," +
                    isfromwiki + ");";
            stmt = c.createStatement();
            Integer id = stmt.executeUpdate(sql);
            stmt.close();
            return true;

        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }

    }

    public Integer savePattern(Double boa_score, String nlpn, String nlpnovar, String nlp, String ln, String pos,
                               String ner, String generalized, Double nlp_score){

        try{
            Statement stmt = null;
            String sql = "INSERT INTO TB_PATTERN " +
                    "(score_boa, nlp_normalized, nlp_no_var, nlp, lang, nlp_score, pos, generalized, ner VALUES(" +
                     boa_score + ",'" + nlpn  + "','" + nlpnovar + "','" + nlp + "','" + ln + "'," + nlp_score + ",'" +
                    pos + "','" + generalized + "','" + ner + "');";
            stmt = c.createStatement();
            Integer id = stmt.executeUpdate(sql);
            stmt.close();
            return id;

        } catch (Exception e){
            System.out.println(e.toString());
            return -1;
        }

    }

    public Integer saveMetaQuery(Integer idpattern, String metaquery, String sl, String pl, String ol,
                               String lang, String evidencetype){
        try{
            Statement stmt = null;
            String sql = "INSERT INTO TB_METAQUERY " +
                    "(id_pattern, metaquery, " +
                    "subject_label, predicate_label, object_label, lang, evidence_type)" +
                    " VALUES (" + idpattern + ",'" + metaquery  + "','" + sl + "','" + pl  + "','" + ol + "','" + lang
                    + "','" + evidencetype + "');";
            stmt = c.createStatement();
            Integer id = stmt.executeUpdate(sql);
            stmt.close();
            return id;

        } catch (Exception e){
            System.out.println(e.toString());
            return -1;
        }

    }

    public Integer saveWebSite(String url, String urldomain, String title,
                             String body, Integer rank,
                             Integer pagerank, Double pagerankscore, Double ind_score,
                             Double ind_topic_majority_web, Double ind_topic_majority_search,
                               Double ind_topic_coverage_score, String lang){
        try{
            Statement stmt = null;
            String sql = "INSERT INTO TB_WEBSITE " +
                    "(url, url_domain, " +
                    "title, body, rank, pagerank, pagerank_score ind_score, ind_topic_majority_web" +
                    "ind_topic_majority_search, ind_topic_coverage_score, lang)" +
                    " VALUES ('" + url + "','" +
                    urldomain  + "','" + title + "','" +
                    body  + "'," + rank + "," +
                    pagerank  + "," + pagerankscore + "," + ind_score + "," +
                    ind_topic_majority_web + "," + ind_topic_majority_search  +  "," +
                    ind_topic_coverage_score + ",'" + lang + "');";
            stmt = c.createStatement();
            Integer id = stmt.executeUpdate(sql);
            stmt.close();
            return id;

        } catch (Exception e){
            System.out.println(e.toString());
            return -1;
        }

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
