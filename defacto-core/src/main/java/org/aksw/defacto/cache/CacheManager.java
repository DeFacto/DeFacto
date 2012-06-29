/**
 * 
 */

package org.aksw.defacto.cache;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class CacheManager {

    private static CacheManager INSTANCE;
    private static Logger logger = Logger.getLogger(CacheManager.class);
    private static Connection connection;
    private int currentKey = 0;
    
    /**
     * 
     */
    private CacheManager() {
       
        // singleton
    }
    
    /**
     * 
     */
    public static synchronized CacheManager getInstance() {
        
        if ( CacheManager.INSTANCE == null ) {
            
            CacheManager.INSTANCE = new CacheManager();
            CacheManager.connection = getConnection();
            CacheManager.INSTANCE.createDatabaseStructure();
        }
        
        return CacheManager.INSTANCE;
    }

    /**
     * 
     * @return
     */
    public synchronized int generatePrimaryKey() {
        // TODO read the highest key from the database otherwise we will overright the rimary key
        return this.currentKey++;
    }
    
    /**
     * 
     * @param query
     * @return
     */
    public synchronized ResultSet executeQuery(String query) {
        
        return executeQuery(prepareStatement(query));
    }
    
    /**
     * 
     * @param query
     * @return
     */
    public synchronized ResultSet executePreparedStatement(PreparedStatement stmt) {
        
        return executeQuery(stmt);
    }
    
    /**
     * 
     * @param query
     * @return
     */
    public synchronized void executeUpdatedPreparedStatement(PreparedStatement stmt) {
        
        executeUpdateQuery(stmt);
    }
    
    /**
     * 
     * @param query
     * @return
     */
    public synchronized void executeUpdateQuery(String query) {
        
        executeUpdateQuery(prepareStatement(query));
    }
    
    /**
     * 
     */
    public static synchronized void closeConnection() {

        try {
            
            connection.close();
        }
        catch (SQLException e) {
            
            logger.error("Could not close connection", e);
        }
    }

    /**
     * 
     * @param stmt
     * @return
     */
    private synchronized static void executeUpdateQuery(PreparedStatement stmt) {
        
        try {
            
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            
            logger.error("Query not execute update query: " + stmt, e);
        }
    }
    
    /**
     * 
     * @param stmt
     * @return
     */
    private synchronized static ResultSet executeQuery(PreparedStatement stmt) {
        
        ResultSet results = null;
        
        try {
            
            if ( stmt != null ) {
                
                getConnection();
                results = stmt.executeQuery();
            }
            else 
                System.out.println("DOUBLE CRAP....");
        }
        catch (SQLException e) {
            
            logger.error("Query not execute query: " + stmt, e);
        }
        
        return results;
    }
    
    /**
     * 
     * @param connection
     * @param query
     * @return
     */
    private synchronized static PreparedStatement prepareStatement(String query) {
        
        PreparedStatement stmt = null;
        
        try {
            
            if ( connection == null || connection.isClosed() )
                getConnection();
            
            stmt = connection.prepareStatement(query);
        }
        catch (SQLException e) {

            logger.error("Could not create sql query for: " + query , e);
        }
        
        return stmt;
    }
    
    /**
     * 
     * @return
     */
    public synchronized static Connection getConnection() {
        
        try {
            
            if (connection == null || connection.isClosed() ) {

                connection = DriverManager.getConnection("jdbc:h2:resources/cache/websites/defacto", "",  "");
//                connection.setAutoCommit(true);
            }
        }
        catch (SQLException e) {
            
            logger.error("Could not create connection to cache database!", e);
        }
        
        return connection;
    }

    private synchronized void createDatabaseStructure() {

        String searchResultTable = 
                "CREATE TABLE IF NOT EXISTS search_result ( " +
                "   id VARCHAR(100) NOT NULL, " +
                "   hits LONG, " + 
                "   query VARCHAR(256), " +
                "   url VARCHAR(1000), " +
                "   title VARCHAR(1000), " +
                "   content TEXT, " +
                "   rank INT, " +
                "   pagerank INT, " +
                "   created TIMESTAMP, " +
                "   PRIMARY KEY (id) " +
                "); ";
        
        String searchResultIndex = 
                "CREATE INDEX query_index ON search_result(query);";
        
        executeUpdateQuery(searchResultTable);
        executeUpdateQuery(searchResultIndex);
    }
    
    public static void main(String[] args) {

        CacheManager.getInstance();
    }

    /**
     * 
     * @param updateQuery
     * @return
     */
    public PreparedStatement createPreparedStatement(String updateQuery) {

        try {
            
            if ( connection == null || connection.isClosed() ) getConnection();
            PreparedStatement stmt = connection.prepareStatement(updateQuery);
            return stmt;
        }
        catch (SQLException e) {
            
            throw new RuntimeException("Could not create query: " + updateQuery);
        }
    }
}
