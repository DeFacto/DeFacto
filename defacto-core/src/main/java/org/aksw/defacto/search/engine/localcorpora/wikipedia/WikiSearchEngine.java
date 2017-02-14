package org.aksw.defacto.search.engine.localcorpora.wikipedia;


import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.engine.SearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.query.SolrWikiQuery;
import org.aksw.defacto.search.result.DefaultSearchResult;
import org.aksw.defacto.search.result.SearchResult;
import org.aksw.defacto.wikipedia.WikipediaSearchResult;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by esteves on 01.09.15.
 */
public class WikiSearchEngine implements SearchEngine {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(WikiSearchEngine.class);

    private HttpSolrServer server_wiki_en;
    private HttpSolrServer server_wiki_fr;
    private HttpSolrServer server_wiki_de;

    private String NUMBER_OF_SEARCH_RESULTS_LC;
    private String inputField;
    private String outputFields;

    public WikiSearchEngine(){

        server_wiki_en = new HttpSolrServer(Defacto.DEFACTO_CONFIG.getStringSetting("local_corpora", "wiki_service_en_url"));
        server_wiki_en.setRequestWriter(new BinaryRequestWriter());

        server_wiki_fr = new HttpSolrServer(Defacto.DEFACTO_CONFIG.getStringSetting("local_corpora", "wiki_service_fr_url"));
        server_wiki_fr.setRequestWriter(new BinaryRequestWriter());

        server_wiki_de = new HttpSolrServer(Defacto.DEFACTO_CONFIG.getStringSetting("local_corpora", "wiki_service_de_url"));
        server_wiki_de.setRequestWriter(new BinaryRequestWriter());

        inputField = Defacto.DEFACTO_CONFIG.getStringSetting("local_corpora", "wiki_fields_in");
        outputFields = Defacto.DEFACTO_CONFIG.getStringSetting("local_corpora", "wiki_fields_out");

        if ( Defacto.DEFACTO_CONFIG != null ) {
            NUMBER_OF_SEARCH_RESULTS_LC = Defacto.DEFACTO_CONFIG.getStringSetting("crawl", "NUMBER_OF_SEARCH_RESULTS_LC");
        }

    }

    public static void main(String[] args) {
        Defacto.init();

        try{
            WikiSearchEngine engine = new WikiSearchEngine();

            Pattern p = new Pattern();
            p.naturalLanguageRepresentation = "";

            MetaQuery q = new MetaQuery("Ghostbusters II|-|?D? NONE ?R?|-|Bill Murray|-|en", p);
            SearchResult result = engine.getSearchResults(q, null);
            System.out.println("done!");

            System.out.println("total of websites: " + result.getWebSites().size());
            System.out.println("total hit count: " + result.getTotalHitCount());
            System.out.println("is cached: " + result.isCached());

        }catch (Exception e){
            System.out.println(e.toString());
        }

    }

    @Override
    public Long getNumberOfResults(MetaQuery query) {

        return 0L;
    }

    @Override
    public SearchResult getSearchResults(MetaQuery query, Pattern pattern) {
        return query(query, pattern);
    }

    @Override
    public SearchResult query(MetaQuery query, Pattern pattern){

        Map<String, WikipediaSearchResult> lc_results = new HashMap<>();
        List<WebSite> websites = new ArrayList<>();
        String language = query.getLanguage().toString();
        String solrGeneratedStringQuery;

        SolrServer server = null;

        try {

            if (language.equals("en")) {
                server = server_wiki_en;
            }else if (language.equals("fr")){
                server = server_wiki_fr;
            }else if (language.equals("de")){
                server = server_wiki_de;
            }else{
                throw new Exception("undefined language");
            }

            solrGeneratedStringQuery = generateQuery(query);
            System.out.println("Solr generated query: " + solrGeneratedStringQuery);

            //delete ir after fix the generation process for solr
            solrGeneratedStringQuery = "Ghostbusters II";

            //exclude it as I finish the de and fr indexing from wikipedia!
            if (language.equals("en")) {

                /*****************************************************
                 query indexed wikipedia (_query)
                 *****************************************************/

                SolrQuery solrqry = new SolrQuery("text" + ":\"" + solrGeneratedStringQuery + "\"").setRows(Integer.parseInt(NUMBER_OF_SEARCH_RESULTS_LC));
                solrqry.setFields("id", "titleText");

                System.out.println("Solr query:" + solrqry);

                QueryResponse response = server.query(solrqry);

                int iaux = 1;
                String siteurl = "";
                String ids = "";


                for (SolrDocument doc : response.getResults()) {
                    System.out.println(doc.toString());

                    WikipediaSearchResult wsr = new WikipediaSearchResult();
                    wsr.setPageID((String) doc.get("id"));
                    wsr.setPageTitle((String) doc.get("titleText"));

                    /*
                    not so performatic though, better sparql query...will change it later
                    this is not necessary anymore, since we get the wiki url from dbpedia query

                    wsr.setPageURL(JsonReader.getElementValueFromURL("https://" + language + ".wikipedia.org/w/api.php?action=query&format=json&prop=info&pageids=" + wsr.getPageID().toString() + "&inprop=url",
                            "query;pages;" + wsr.getPageID().toString() + ";fullurl"));
                    */

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
                 implement crawler to find correlated websites from external links
                 https://github.com/yasserg/crawler4j/blob/master/src/test/java/edu/uci/ics/crawler4j/examples/basic/BasicCrawlController.java
                 *****************************************************/




                /*****************************************************
                 adding all to websites (wiki and external links)
                 *****************************************************/
                for (WikipediaSearchResult r : lc_results.values()) {

                    WebSite website = new WebSite(query, r.getPageURL());
                    website.setTitle(r.getPageTitle());
                    website.setRank(iaux++);
                    website.setLanguage(language);
                    websites.add(website);

                    System.out.println("website: " + r.getPageURL());

                    for (String externalLink : r.getExternalLinksfromDBPedia()) {
                        URL aURL = new URL(externalLink);

                        WebSite website2 = new WebSite(query, externalLink);
                        website2.setTitle(aURL.getHost());
                        website2.setRank(iaux++);
                        website2.setLanguage(language);
                        websites.add(website2);

                        System.out.println("website: " + externalLink);

                    }

                }

                return new DefaultSearchResult(websites, response.getResults().getNumFound(), query, false);

            }
            else{
                return new DefaultSearchResult(new ArrayList<WebSite>(), 0L, query, false);
            }
        }
        catch (Exception e) {

            e.printStackTrace();
            return new DefaultSearchResult(new ArrayList<WebSite>(), 0L, query, false);
        }

    }

    @Override
    public String generateQuery(MetaQuery query) {
        return new SolrWikiQuery().generateQuery(query);
    }

}
