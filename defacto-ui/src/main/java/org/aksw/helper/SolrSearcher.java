package org.aksw.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 6/7/12
 * Time: 11:17 PM
 * Searches the Lucene index of DBpedia for resource labels, which is much faster than using SPARQL query with regex
 */
public class SolrSearcher {

    private static final String SOLR_SERVER_ADDRESS = "http://dbpedia.aksw.org:8080/solr/dbpedia_resources";

    private static Logger logger = Logger.getLogger(SolrSearcher.class);

    protected static final int LIMIT = 10;
    protected static final int OFFSET = 0;

    //protected static final String SOLR_DBPEDIA_CLASSES = "http://dbpedia.aksw.org:8080/solr/dbpedia_classes";

    protected HttpSolrServer server;

    public SolrSearcher(String serverURL)
    {
    	server = new HttpSolrServer(serverURL);
        server.setRequestWriter(new BinaryRequestWriter());
    }

    public SolrSearcher()
    {
    	server = new HttpSolrServer(SOLR_SERVER_ADDRESS);
        server.setRequestWriter(new BinaryRequestWriter());
    }

//    public SolrSearch() {this(Defaults.solrServerURL());}

    public LinkedHashMap<String, String> getResources(String query) {return getResources(query, LIMIT);	}

    public  LinkedHashMap<String, String> getResources(String query, int limit) {return getResources(query, limit, OFFSET);}


    public LinkedHashMap<String, String> getResources(String query, int limit, int offset) {
        LinkedHashMap<String, String> resources = new LinkedHashMap<String, String>();

        SolrQuery q = new SolrQuery(buildQueryString(query));
        q.setRows(limit);
        q.setStart(offset);
        try {
            logger.info("QUERY = " + q);
            QueryResponse response = server.query(q);
            SolrDocumentList docList = response.getResults();
            for(SolrDocument d : docList){
                resources.put((String) d.get("uri"), (String) d.get("label"));
            }
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        return resources;

    }

    public List<String> getResources(String query, String type) {return getResources(query, type, LIMIT, OFFSET);}
    public List<String> getResources(String query, String type, int limit) {return getResources(query, type, limit, OFFSET);}

    public List<String> getResources(String query, String type, int limit, int offset) {
        List<String> resources = new ArrayList<String>();

        SolrQuery q = new SolrQuery(buildQueryString(query, type));
        q.setRows(limit);
        q.setStart(offset);
        try {
            QueryResponse response = server.query(q);
            SolrDocumentList docList = response.getResults();
            for(SolrDocument d : docList){
                resources.add((String) d.get("uri"));
            }
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        return resources;
    }


    protected String buildQueryString(String query) {return "label:\"" + query + "\"";}
    protected String buildQueryString(String query, String type){return "label:\"" + query + "\" AND types:\"" + type + "\"";}


    public static void main(String []args){
        logger.info("Hello");
        SolrSearcher x = new SolrSearcher("http://dbpedia.aksw.org:8080/solr/dbpedia_resources");
        HashMap<String, String> lst = x.getResources("michael jackson");
        for(String str:lst.keySet())
            logger.info(str);
    }

}
