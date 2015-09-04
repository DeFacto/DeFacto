package org.aksw.defacto.search.engine.wikipedia;


import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.engine.DefaultSearchEngine;

import org.aksw.defacto.search.query.MetaQuery;

import org.aksw.defacto.search.query.SolrWikiQuery;
import org.aksw.defacto.search.result.DefaultSearchResult;
import org.aksw.defacto.search.result.SearchResult;
import org.aksw.defacto.util.JsonReader;
import org.aksw.defacto.wikipedia.WikipediaSearchResult;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;

import java.net.URL;
import java.util.*;

/**
 * Created by esteves on 01.09.15.
 */
public class WikiSearchEngine extends DefaultSearchEngine {

    private HttpSolrServer server;
    private String NUMBER_OF_SEARCH_RESULTS;
    private static Logger logger =  Logger.getLogger(WikiSearchEngine.class);
    private String inputField;
    private String outputFields;

    public static void main(String[] args) {
        Defacto.init();

        MetaQuery q = new MetaQuery("Ghostbusters II|-|?D? NONE ?R?|-|Bill Murray|-|en");
        WikiSearchEngine engine = new WikiSearchEngine();
        System.out.println(engine.query(q, null).getTotalHitCount());
    }

    public WikiSearchEngine(){

        server = new HttpSolrServer(Defacto.DEFACTO_CONFIG.getStringSetting("local_corpora", "wiki_service_url"));
        server.setRequestWriter(new BinaryRequestWriter());

        inputField = Defacto.DEFACTO_CONFIG.getStringSetting("local_corpora", "wiki_fields_in");
        outputFields = Defacto.DEFACTO_CONFIG.getStringSetting("local_corpora", "wiki_fields_out");

        if ( Defacto.DEFACTO_CONFIG != null ) {
            NUMBER_OF_SEARCH_RESULTS = Defacto.DEFACTO_CONFIG.getStringSetting("crawl", "NUMBER_OF_SEARCH_RESULTS");
        }

    }

    @Override
    public Long getNumberOfResults(MetaQuery query) {

        return 0L;
    }

    @Override
    public SearchResult query(MetaQuery query, Pattern pattern) {

        Map<String, WikipediaSearchResult> lc_results = new HashMap<>();

        try {

            String _query = this.generateQuery(query);
            System.out.println(_query);

            if (query.getLanguage().equals("en")) {

                /*****************************************************
                 query indexed wikipedia (_query)
                 *****************************************************/

                HttpSolrServer solr2 = new HttpSolrServer("http://localhost:8123/solr/wiki_" + query.getLanguage());

                SolrQuery solrqry = new SolrQuery("text" + ":\"" + "Ghostbusters II" + "\"").setRows(1000);
                solrqry.setFields("id", "titleText");
                QueryResponse response = solr2.query(solrqry);

                int iaux = 1;
                String siteurl = "";
                String ids = "";
                List<WebSite> results = new ArrayList<WebSite>();

                for (SolrDocument doc : response.getResults()) {
                    System.out.println(doc.toString());

                    WikipediaSearchResult wsr = new WikipediaSearchResult();
                    wsr.setPageID((String) doc.get("id"));
                    wsr.setPageTitle((String) doc.get("titleText"));

                    //not so performatic though, better sparql query...will change it later
                    wsr.setPageURL(JsonReader.getElementValueFromURL("https://" + query.getLanguage() + ".wikipedia.org/w/api.php?action=query&format=json&prop=info&pageids=" + wsr.getPageID().toString() + "&inprop=url",
                            "query;pages;" + wsr.getPageID().toString() + ";fullurl"));
                    lc_results.put((String) doc.get("id"), wsr);

                    ids += doc.get("id") + ",";
                }

                ids = ids.substring(0, ids.length() - 1);

                /*****************************************************
                 get the wikipedia page url and related dbpedia external
                 links based on the wikipedia IDs
                 *****************************************************/

                //https://en.wikipedia.org/w/api.php?action=query&prop=info&pageids=41227847&inprop=url

                /*String sparqlQuery = "" +
                        "prefix foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "prefix dbpedia-owl: <http://dbpedia.org/ontology/>" +
                        "SELECT ?s ?wikiurl \n" +
                        "WHERE { \n" +
                        "?s dbpedia-owl:wikiPageID " + wikiid + ". \n" +
                        "?s foaf:isPrimaryTopicOf ?wikiurl . \n" +
                        "} \n" +
                        "LIMIT 1";
                */

                /*String sparqlQuery = "" +
                        "prefix foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "prefix dbpedia-owl: <http://dbpedia.org/ontology/>" +
                        "select ?o ?x \n" +
                        "WHERE { \n" +
                        "?s dbpedia-owl:wikiPageID ?o . \n" +
                        "?s foaf:isPrimaryTopicOf ?x . \n" +
                        " FILTER(?o IN (" + ids + ")) . \n" +
                        "} \n" +
                        "LIMIT 1000";
                */

                String sparqlQuery = "" +
                        "prefix foaf: <http://xmlns.com/foaf/0.1/>\n" +
                        "prefix dbpedia-owl: <http://dbpedia.org/ontology/>" +
                        "select ?o ?x ?z \n" +
                        "WHERE { \n" +
                        "?s dbpedia-owl:wikiPageID ?o . \n" +
                        "?s foaf:isPrimaryTopicOf ?x . \n" +
                        "OPTIONAL {?s dbpedia-owl:wikiPageExternalLink ?z .} \n" +
                        " FILTER(?o IN (" + ids + ")) . \n" +
                        "} \n" +
                        "LIMIT 1000000";

                // && (langMatches(lang(?s),"en"))

                System.out.println(sparqlQuery);

                QueryExecution qexec = null;

                qexec = new QueryEngineHTTP("http://dbpedia.org/sparql", sparqlQuery);
                ((QueryEngineHTTP) qexec).addDefaultGraph("http://dbpedia.org");

                ResultSet rs = qexec.execSelect();

                Integer _id;
                String _wiki_url, _external_url;

                if (rs.hasNext()) {

                    QuerySolution _qs = rs.next();

                    _id = (Integer) _qs.getLiteral("o").getValue();


                    WikipediaSearchResult _wsr = lc_results.get(_id.toString());
                    if (_wsr != null){
                        _wsr.setPageURL(_qs.getResource("x").getURI());
                        if (_qs.getResource("z") != null){
                            _wsr.addExternalLink(_qs.getResource("z").getURI());
                        }

                        while (rs.hasNext()) {
                            QuerySolution qs = rs.next();

                            if (qs.getLiteral("o").getValue() != _id) {
                                _wsr = lc_results.get(qs.getLiteral("o").getValue().toString());
                                _wsr.setPageURL(qs.getResource("x").getURI());
                            }


                            if (qs.getResource("z") != null) {
                                _wsr.addExternalLink(qs.getResource("z").getURI());
                            }

                            _id = (Integer) qs.getLiteral("o").getValue();

                        }

                    }else
                    {
                        throw new Exception("Error: id " + _id.toString() + " has not been found");
                    }

                }

                /*****************************************************
                 adding all to websites (wiki and external links)
                 *****************************************************/
                for (WikipediaSearchResult r : lc_results.values()) {

                    WebSite website = new WebSite(query, r.getPageURL());
                    website.setTitle(r.getPageTitle());
                    website.setRank(iaux++);
                    website.setLanguage(query.getLanguage());
                    results.add(website);

                    for (String externalLink : r.getExternalLinksfromDBPedia()) {
                        URL aURL = new URL(externalLink);

                        WebSite website2 = new WebSite(query, externalLink);
                        website2.setTitle(aURL.getHost());
                        website2.setRank(iaux++);
                        website2.setLanguage(query.getLanguage());
                        results.add(website2);

                    }

                }

                return new DefaultSearchResult(results, response.getResults().getNumFound(), query, pattern, false);

            }
            else{
                return new DefaultSearchResult(new ArrayList<WebSite>(), 0L, query, pattern, false);
            }
        }
        catch (Exception e) {

            e.printStackTrace();
            return new DefaultSearchResult(new ArrayList<WebSite>(), 0L, query, pattern, false);
        }
    }

    @Override
    public String generateQuery(MetaQuery query) {

        return new SolrWikiQuery().generateQuery(query);
    }


}
