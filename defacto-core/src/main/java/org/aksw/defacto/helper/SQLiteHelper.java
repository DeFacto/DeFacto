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

    public static boolean addMetaQuery(String metaquery, String suri, String slen,
                                   String puri, String plen,
                                   String ouri, String olen,
                                   Integer idlang){
        try{
            Statement stmt = null;
            String sql = "INSERT INTO TB_QUERY " +
                          "(METAQUERY, " +
                          "SUBJECT_URI, SUBJECT_LABEL_EN," +
                          "PREDICATE_URI, PREDICATE_LABEL_EN," +
                          "OBJECT_URI, OBJECT_LABEL_EN," +
                          "ID_LANGUAGE, PROCESSING_DATE," +
                          ") VALUES (" + metaquery + "," +
                                 "'" + suri + "','" + slen + "'," +
                                 "'" + puri + "','" + plen + "'," +
                                 "'" + ouri + "','" + olen + "'," +
                                       idlang + ");";
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

}
