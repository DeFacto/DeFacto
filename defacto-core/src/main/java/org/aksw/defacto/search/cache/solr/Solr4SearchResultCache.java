/**
 * 
 */
package org.aksw.defacto.search.cache.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.cache.Cache;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.DefaultSearchResult;
import org.aksw.defacto.search.result.SearchResult;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

/**
 * @author gerb
 *
 */
public class Solr4SearchResultCache implements Cache<SearchResult> {
	
	private HttpSolrServer server;
	private Logger logger = Logger.getLogger(Solr4SearchResultCache.class);
	
	public Solr4SearchResultCache(){

		server = new HttpSolrServer(Defacto.DEFACTO_CONFIG.getStringSetting("crawl", "solr_searchresults"));
		server.setRequestWriter(new BinaryRequestWriter());
	}
	
	@Override
	public boolean contains(String identifier) {
		
		SolrQuery query = new SolrQuery(Constants.LUCENE_SEARCH_RESULT_QUERY_FIELD + ":\"" + identifier + "\"").setRows(1);
        QueryResponse response = this.querySolrServer(query);
        SolrDocumentList docList = response.getResults();
        return docList == null ? false : docList.size() > 0 ? true : false;
	}

	@Override
	public SearchResult getEntry(String identifier) {
		
		List<WebSite> websites = new ArrayList<WebSite>();
        MetaQuery metaQuery = null;
        Long hitCount = 0L;
        
    	SolrQuery query = new SolrQuery(Constants.LUCENE_SEARCH_RESULT_QUERY_FIELD + ":\"" + identifier + "\"").setRows(200);
    	query.addField(Constants.LUCENE_SEARCH_RESULT_QUERY_FIELD);
    	query.addField(Constants.LUCENE_SEARCH_RESULT_HIT_COUNT_FIELD);
    	query.addField(Constants.LUCENE_SEARCH_RESULT_URL_FIELD);
    	query.addField(Constants.LUCENE_SEARCH_RESULT_RANK_FIELD);
    	query.addField(Constants.LUCENE_SEARCH_RESULT_PAGE_RANK_FIELD);
    	query.addField(Constants.LUCENE_SEARCH_RESULT_CONTENT_FIELD);
    	query.addField(Constants.LUCENE_SEARCH_RESULT_TITLE_FIELD);
    	query.addField(Constants.LUCENE_SEARCH_RESULT_TAGGED_FIELD);
    	query.addField(Constants.LUCENE_SEARCH_RESULT_LANGUAGE);
        QueryResponse response = this.querySolrServer(query);
        
        for ( SolrDocument doc : response.getResults()) {
            
            metaQuery = new MetaQuery((String) doc.get(Constants.LUCENE_SEARCH_RESULT_QUERY_FIELD));
            hitCount = (Long) doc.get(Constants.LUCENE_SEARCH_RESULT_HIT_COUNT_FIELD);
            
            if ( !((String)doc.get(Constants.LUCENE_SEARCH_RESULT_URL_FIELD)).isEmpty() ) { // empty cache hits should not become a website

                WebSite site = new WebSite(metaQuery, (String)doc.get(Constants.LUCENE_SEARCH_RESULT_URL_FIELD));
                site.setRank((Integer) doc.get(Constants.LUCENE_SEARCH_RESULT_RANK_FIELD));
                site.setPageRank((Integer) doc.get(Constants.LUCENE_SEARCH_RESULT_PAGE_RANK_FIELD));
                site.setText((String) doc.get(Constants.LUCENE_SEARCH_RESULT_CONTENT_FIELD));
                site.setTitle((String) doc.get(Constants.LUCENE_SEARCH_RESULT_TITLE_FIELD));
                site.setTaggedText((String) doc.get(Constants.LUCENE_SEARCH_RESULT_TAGGED_FIELD));
                site.setLanguage((String) doc.get(Constants.LUCENE_SEARCH_RESULT_LANGUAGE));
                site.setCached(true);
                websites.add(site);
            }
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
        
		for ( SearchResult result : listToAdd ) this.add(result);
		try {
			this.server.commit();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listToAdd;
	}

	@Override
	/**
	 * does not commit changes!
	 */
	public SearchResult add(SearchResult entry) {
		
		try {
			
			this.server.add(searchResultToDocument(entry));
			this.logger.info("Added " + entry.getQuery().toString() + " to cache!");
		} 
		catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return entry;
	}
	
	private List<SolrInputDocument> searchResultToDocument(SearchResult entry) {

        List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
        
        if ( entry.getWebSites().isEmpty() ) {
            
        	SolrInputDocument solrDocument = new SolrInputDocument();
        	solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_ID_FIELD, String.valueOf(entry.getQuery().toString().hashCode()));
            solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_HIT_COUNT_FIELD, entry.getTotalHitCount());
            solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_RANK_FIELD, -1);
            solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_PAGE_RANK_FIELD, -1);
            solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_CREATED_FIELD, new Date().getTime());
            solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_URL_FIELD, "");
            solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_TITLE_FIELD, "");
            solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_CONTENT_FIELD, "");
            solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_TAGGED_FIELD, "");
            solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_QUERY_FIELD, entry.getQuery().toString());
            solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_LANGUAGE, entry.getQuery().getLanguage());
            documents.add(solrDocument);
        }
        else {

            for ( WebSite site : entry.getWebSites() ) {
            	
            	SolrInputDocument solrDocument = new SolrInputDocument();
            	solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_ID_FIELD, (entry.getQuery().toString() + "\t" + site.getUrl()).hashCode());
                solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_HIT_COUNT_FIELD, entry.getTotalHitCount());
                solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_RANK_FIELD, site.getSearchRank());
                solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_PAGE_RANK_FIELD, site.getPageRank());
                solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_CREATED_FIELD, new Date().getTime());
                solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_URL_FIELD, site.getUrl());
                solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_TITLE_FIELD, site.getTitle());
                solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_CONTENT_FIELD, site.getText());
                solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_TAGGED_FIELD, site.getTaggedText());
                solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_QUERY_FIELD, entry.getQuery().toString());
                solrDocument.addField(Constants.LUCENE_SEARCH_RESULT_LANGUAGE, entry.getQuery().getLanguage());
                documents.add(solrDocument);
            }
        }
        
        return documents;
    }
	
	/**
	 * 
	 * @param query
	 * @return
	 */
	private QueryResponse querySolrServer(SolrQuery query) {
		
		try {
			
			return this.server.query(query);
		}
		catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
