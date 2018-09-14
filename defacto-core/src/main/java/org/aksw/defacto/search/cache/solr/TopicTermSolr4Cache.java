/**
 * 
 */
package org.aksw.defacto

.search.cache.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.cache.Cache;
import org.aksw.defacto.topic.TopicTerm;
import org.aksw.defacto.topic.frequency.Word;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

/**
 * @author gerb
 *
 */
public class TopicTermSolr4Cache implements Cache<TopicTerm> {

	private HttpSolrServer server;
	
	public TopicTermSolr4Cache(){

		server = new HttpSolrServer(Defacto.DEFACTO_CONFIG.getStringSetting("crawl", "solr_topicterms"));
		server.setRequestWriter(new BinaryRequestWriter());
	}
	
	@Override
	public boolean contains(String identifier) {
		
		SolrQuery query = new SolrQuery(Constants.LUCENE_TOPIC_TERM_LABEL + ":\"" + identifier + "\"").setRows(1);
        QueryResponse response = this.querySolrServer(query);
        SolrDocumentList docList = response.getResults();
		return docList == null ? false : docList.size() > 0 ? true : false;
	}

	@Override
	public TopicTerm getEntry(String identifier) {
		
		TopicTerm term = new TopicTerm(identifier);
        
    	SolrQuery query = new SolrQuery(Constants.LUCENE_TOPIC_TERM_LABEL + ":\"" + identifier + "\"").setRows(1);
    	query.addField(Constants.LUCENE_TOPIC_TERM_RELATED_TERM);
        QueryResponse response = this.querySolrServer(query);
        List<Word> relatedWords = new ArrayList<Word>();
        for ( SolrDocument doc : response.getResults()) {
        	
        	if ( doc.containsKey(Constants.LUCENE_TOPIC_TERM_RELATED_TERM)) {
        		
        		for ( String token :  (List<String>) doc.get(Constants.LUCENE_TOPIC_TERM_RELATED_TERM)) {
            		// mega hack to encode the occurrence of the same word for a given topic term
            		String[] split = token.split(Constants.TOPIC_TERM_SEPARATOR);
            		relatedWords.add(new Word(split[0], Integer.valueOf(split[1])));
            	}
        	}
        }
        
        term.relatedTopics = relatedWords;
        
        return term;
	}

	@Override
	public TopicTerm removeEntryByPrimaryKey(String primaryKey) {
		
		throw new RuntimeException("not yet implemented");
	}

	@Override
	public boolean updateEntry(TopicTerm object) {
		
		throw new RuntimeException("not yet implemented");
	}

	@Override
	public List<TopicTerm> addAll(List<TopicTerm> listToAdd) {
		
		for ( TopicTerm result : listToAdd ) this.add(result); 
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
	public TopicTerm add(TopicTerm entry) {
			
			try {
				this.server.add(topicTermToDocument(entry));
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return entry;
	}
	
	private SolrInputDocument topicTermToDocument(TopicTerm entry) {
		
		SolrInputDocument solrDocument = new SolrInputDocument();
		solrDocument.addField(Constants.LUCENE_TOPIC_TERM_LABEL, entry.label);
		for ( Word related : entry.relatedTopics )
			solrDocument.addField(Constants.LUCENE_TOPIC_TERM_RELATED_TERM, 
					related.getWord() + Constants.TOPIC_TERM_SEPARATOR + related.getFrequency());
		
		return solrDocument;
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
