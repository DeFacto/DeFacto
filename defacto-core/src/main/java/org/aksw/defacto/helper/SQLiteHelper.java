package org.aksw.defacto.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Created by dnes on 08/12/16.
 */
public class SQLiteHelper {

    private static Connection c = null;
    private static SQLiteHelper instance = null;
    protected SQLiteHelper() {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:defacto.db");
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


    public static boolean commitTransaction(){
        try{
            c.commit();
            return true;
        } catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
    }

    public static boolean addSearchResults(Integer idquery, String surl, String sdomain,
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

    public static boolean assignQueryToResults(Integer idquery, Integer idresult){
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

    public static boolean saveMetaQuery(String metaquery, String suri, String slen,
                                   String puri, String plen,
                                   String ouri, String olen,
                                   Integer idlang, Long hits){
        try{
            Statement stmt = null;
            String sql = "INSERT INTO TB_QUERY " +
                          "(metaquery, " +
                          "subject_uri, subject_label_en," +
                          "predicate_uri, predicate_label_en," +
                          "object_uri, object_label_en," +
                          "id_language, processing_date, hits" +
                          ") VALUES (" + metaquery + "," +
                                 "'" + suri + "','" + slen + "'," +
                                 "'" + puri + "','" + plen + "'," +
                                 "'" + ouri + "','" + olen + "'," +
                                       idlang + "," + hits + ");";
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
