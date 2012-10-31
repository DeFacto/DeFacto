/**
 * 
 */
package org.aksw.defacto.search.cache;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.aksw.defacto.cache.Cache;
import org.aksw.defacto.cache.CacheManager;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.DefaultSearchResult;
import org.aksw.defacto.search.result.SearchResult;
import org.apache.log4j.Logger;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class H2DatabaseSearchResultCache implements Cache<SearchResult> {

    private Logger logger = Logger.getLogger(H2DatabaseSearchResultCache.class);
    
    @Override
    public boolean contains(String identifier) {

        String containsQuery = "SELECT query FROM search_result WHERE query = ?"; 
        PreparedStatement stmt = CacheManager.getInstance().createPreparedStatement(containsQuery);
        
        try {

            stmt.setString(1, identifier);
            ResultSet results = CacheManager.getInstance().executePreparedStatement(stmt);
            // more then one row, so the entry is there
            while ( results.next() ) return true;
            
            return false;
        }
        catch (SQLException e) {

            e.printStackTrace();
        }
        return false;
    }

    @Override
    public SearchResult getEntry(String identifier) {
        
        logger.info("Trying to load " + identifier + " from cache!");
        
        String containsQuery = "SELECT * FROM search_result WHERE query = ?"; 
        PreparedStatement stmt = CacheManager.getInstance().createPreparedStatement(containsQuery);
        
        MetaQuery metaQuery = null;
        Long hitCount = null;
        List<WebSite> websites = new ArrayList<WebSite>();
        
        try {

            stmt.setString(1, identifier);
            ResultSet results = CacheManager.getInstance().executePreparedStatement(stmt);
        
            while ( results.next() ) {
                
                if ( metaQuery == null ) metaQuery = new MetaQuery(results.getString("query"));
                if ( hitCount == null ) hitCount = results.getLong("hits");
                if ( !results.getString("url").isEmpty() ) { // empty cache hits should not become a website

                    WebSite site = new WebSite(metaQuery, results.getString("url"));
                    site.setRank(results.getInt("rank"));
                    site.setPageRank(results.getInt("pagerank"));
                    site.setText(results.getString("content"));
                    site.setTitle(results.getString("title"));
                    site.setCached(true);
                    websites.add(site);
                }
            }
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return new DefaultSearchResult(websites, hitCount, metaQuery);
    }

    @Override
    public SearchResult removeEntryByPrimaryKey(String primaryKey) {

        throw new RuntimeException("not yet implemented");
    }

    @Override
    public boolean updateEntry(SearchResult object) {

        throw new RuntimeException("not yet implemented");    
    }

    @Override
    public List<SearchResult> addAll(List<SearchResult> listToAdd) {
        
        for ( SearchResult result : listToAdd ) 
            this.add(result);
                
        return listToAdd;
    }

    @Override
    public SearchResult add(SearchResult entry) {

        String updateQuery = 
                "INSERT INTO search_result (id, hits, query, url, title, content, rank, pagerank, created) " +
                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        if ( entry.getWebSites().isEmpty() ) {
            
            PreparedStatement stmt = CacheManager.getInstance().createPreparedStatement(updateQuery);
            
            try {
                
                stmt.setString(1, UUID.randomUUID().toString());
                stmt.setLong(2, entry.getTotalHitCount());
                stmt.setString(3, entry.getQuery().toString());
                stmt.setString(4, "");
                stmt.setString(5, "");
                stmt.setString(6, "");
                stmt.setInt(7, -1);
                stmt.setInt(8, -1);
                stmt.setDate(9, new java.sql.Date(System.currentTimeMillis()));
                
                CacheManager.getInstance().executeUpdatedPreparedStatement(stmt);
            }
            catch (SQLException e) {
                
                throw new RuntimeException("THIS SHOULD NOT HAPPEN!", e); 
            }
        }
        else {

            for ( WebSite site : entry.getWebSites() ) {

                PreparedStatement stmt = CacheManager.getInstance().createPreparedStatement(updateQuery);
                
                try {
                    
                    stmt.setString(1, UUID.randomUUID().toString());
                    stmt.setLong(2, entry.getTotalHitCount());
                    stmt.setString(3, entry.getQuery().toString());
                    stmt.setString(4, site.getUrl());
                    stmt.setString(5, site.getTitle());
                    stmt.setString(6, site.getText());
                    stmt.setInt(7, site.getSearchRank());
                    stmt.setInt(8, site.getPageRank());
                    stmt.setDate(9, new java.sql.Date(System.currentTimeMillis()));
                    
                    CacheManager.getInstance().executeUpdatedPreparedStatement(stmt);
                }
                catch (SQLException e) {
                    
                    throw new RuntimeException("THIS SHOULD NOT HAPPEN!", e); 
                }
            }
        }
        logger.info("Added query: " + entry.getQuery() + " to cache!");
        return entry;
    }
}
    
