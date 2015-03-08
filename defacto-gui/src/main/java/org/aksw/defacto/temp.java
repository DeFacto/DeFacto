package org.aksw.defacto;

import org.aksw.defacto.util.FactBenchExample;
import org.aksw.defacto.util.FactBenchExamplesLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Set;

/**
 * Created by root on 2/22/15.
 */
public class temp {

   public static void main(String[] args){

       String dbHost = "localhost";
       String dbPort = "3306";
       //String database = "dbpedia_metrics";
       String database = "myschema";
       String dbUser = "root";



       System.out.println("MySQL Connect Example.");
       Connection conn = null;
       String url = "jdbc:mysql://localhost:3306/";
       String dbName = "myschema";
       String driver = "com.mysql.jdbc.Driver";
       String userName = "root";
       String password = "l567794";
       try {
           Class.forName(driver).newInstance();
           //conn = DriverManager.getConnection(url + dbName, userName, password);

           Connection conn2 = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database + "?" + "user=" + dbUser + "&password=l567794" );

           System.out.println("Connected to the database");
           conn2.close();
           System.out.println("Disconnected from database");
       } catch (Exception e) {
           e.printStackTrace();
       }

   }
}
