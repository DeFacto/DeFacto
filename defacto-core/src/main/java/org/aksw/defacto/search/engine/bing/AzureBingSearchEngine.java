package org.aksw.defacto.search.engine.bing;

import net.billylieurance.azuresearch.AbstractAzureSearchQuery.AZURESEARCH_QUERYTYPE;
import net.billylieurance.azuresearch.AbstractAzureSearchResult;
import net.billylieurance.azuresearch.AzureSearchCompositeQuery;
import net.billylieurance.azuresearch.AzureSearchResultSet;
import net.billylieurance.azuresearch.AzureSearchWebResult;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.engine.DefaultSearchEngine;
import org.aksw.defacto.search.query.BingQuery;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.DefaultSearchResult;
import org.aksw.defacto.search.result.SearchResult;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Date: 2/6/12
 * Time: 7:11 PM
 * Class BingSearchEngine contains the facilities required to contact Bing search engine to get the search results for a
 * set of keywords
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Mohamed Morsey <morsey@informatik.uni-leipzig.de>
 */
public class AzureBingSearchEngine extends DefaultSearchEngine {

    private String NUMBER_OF_SEARCH_RESULTS;
    private static String BING_API_KEY;
    private static Logger logger =  Logger.getLogger(AzureBingSearchEngine.class);

    public AzureBingSearchEngine() {
        
        if ( Defacto.DEFACTO_CONFIG != null ) {

            BING_API_KEY = Defacto.DEFACTO_CONFIG.getStringSetting("crawl", "BING_API_KEY");
            NUMBER_OF_SEARCH_RESULTS = Defacto.DEFACTO_CONFIG.getStringSetting("crawl", "NUMBER_OF_SEARCH_RESULTS");
        }
    }
    
    @Override
    public Long getNumberOfResults(MetaQuery query) {
        
        return 0L;
    }
    
    public static void main(String[] args) {
        
//        MetaQuery query0 = new MetaQuery(String.format("%s|-|%s|-|%s|-|%s", "Obama", "?D? is president of ?R?", "United States", "en"));
//        MetaQuery query  = new MetaQuery(String.format("%s|-|%s|-|%s|-|%s", "Montebelluna", "?R? Wii version of `` ?D?", "Procter & Gamble", "en"));
//        MetaQuery query1 = new MetaQuery(String.format("%s|-|%s|-|%s|-|%s", "Gloria Estefan", "??? NONE ???", "Remember Me with Love", "en"));
//        MetaQuery query2 = new MetaQuery(String.format("%s|-|%s|-|%s|-|%s", "Avram Hershko", "?D? is a component of ?R?", "United States Marine Corps", "en"));

        Pattern p = new Pattern("?D politician ?R");
        MetaQuery query = new MetaQuery("Franck Ribery|-| politician |-|Galatasaray|-|en", p);
        Defacto.init();
        p.naturalLanguageRepresentation = "?D? was not prizewinning ?R?";

        MetaQuery q = new MetaQuery("Guglielmo Marconi|-| not prizewinning |-|Nobel Prize in Physics|-|en", p);

        //MetaQuery q = new MetaQuery("Ghostbusters II|-|?D? NONE ?R?|-|Bill Murray|-|fr");
        AzureBingSearchEngine engine = new AzureBingSearchEngine();
        System.out.println(BING_API_KEY);
        //System.out.println(engine.query(q, null).getTotalHitCount());
        System.out.println(engine.query(query, p, "v5").getWebSites().size());
        
//        URI uri;
//        try {
//            String query = "'Obama' AND 'is president of' AND 'United States'";
//                uri = new URI("https", "api.datamarket.azure.com", "/Data.ashx/Bing/SearchWeb/v1/Web",
//                        "Query='"+query+"'", null );
//                //Bing and java URI disagree about how to represent + in query parameters.  This is what we have to do instead...
//                uri = new URI(uri.getScheme() + "://" + uri.getAuthority()  + uri.getPath() + "?" + uri.getRawQuery().replace("+", "%2b"));
//                System.out.println(uri);
//                
//         //log.log(Level.WARNING, uri.toString());
//        } catch (URISyntaxException e1) {
//                e1.printStackTrace();
//                return;
//        }
        
        
        
//        System.out.println(engine.query(query1, null).getWebSites().size());
//        System.out.println(engine.query(query2, null).getWebSites().size());
    }


    private SearchResult query_v5(MetaQuery query, Pattern pattern){

        HttpClient httpclient = HttpClients.createDefault();

        try {

            URIBuilder builder = new URIBuilder("https://api.cognitive.microsoft.com/bing/v5.0/search");
            String strquery = URLEncoder.encode(this.generateQuery(query), Charset.defaultCharset().name());
            String mkt;
            if (query.getLanguage().equals("en")) mkt = "en-US";
            else if (query.getLanguage().equals("de")) mkt="de-DE";
            else if (query.getLanguage().equals("fr")) mkt="fr-FR";
            else throw new Exception("language not implemented");

            strquery = this.generateQuery(query);
            builder.setParameter("q", strquery);
            builder.setParameter("count", NUMBER_OF_SEARCH_RESULTS);
            builder.setParameter("offset", "0");
            builder.setParameter("responseFilter", "webpages");
            builder.setParameter("mkt", mkt);

            URI uri = builder.build();
            HttpGet request = new HttpGet(uri);
            request.setHeader("Ocp-Apim-Subscription-Key", BING_API_KEY);

            HttpResponse resp = httpclient.execute(request);
            int statusCode = resp.getStatusLine().getStatusCode();
            if (statusCode != 200)
            {
                if (statusCode == 429) {
                    System.out.print("too many requests - max is 5/sec : " + statusCode);
                    System.exit(-429);
                }
                throw new RuntimeException("Failed with HTTP error code : " + statusCode);
            }
            HttpEntity entity = resp.getEntity();
            List<WebSite> resultsws = new ArrayList<WebSite>();
            Long resultsLength = 0L;
            Long totalreturnedvalues = 0L;

            if (entity != null)
            {
                JSONObject json = new JSONObject(EntityUtils.toString(entity));
                if (json.has("webPages")){
                    JSONObject d = json.getJSONObject("webPages");
                    JSONArray results = d.getJSONArray("value");
                    totalreturnedvalues = Long.valueOf(d.getString("totalEstimatedMatches"));
                    resultsLength = (Long.valueOf(results.length()));

                    int aux = 1;
                    for (int i = 0; i < resultsLength; i++) {
                        if ( aux > Integer.valueOf(NUMBER_OF_SEARCH_RESULTS) ) break;
                        final JSONObject aResult = results.getJSONObject(i);
                        if ((aResult.get("displayUrl").toString().startsWith("http://images.webgiftr.com/")
                                || (aResult.get("displayUrl").toString().startsWith("http://www.calza.com/")))) continue;

                        WebSite website = new WebSite(query, aResult.get("displayUrl").toString());
                        website.setTitle(aResult.get("name").toString());
                        website.setRank(i++);
                        website.setLanguage(query.getLanguage());
                        resultsws.add(website);
                        aux++;
                    }
                }
                else
                {
                    return new DefaultSearchResult(new ArrayList<WebSite>(), 0L, query, pattern, false);
                }

            }

            return new DefaultSearchResult(resultsws, totalreturnedvalues, query, pattern, false);



            // Request body
            //StringEntity reqEntity = new StringEntity("{body}");
            //request.setEntity(reqEntity);

/*
            final String accountKey = BING_API_KEY;
            final String bingUrlPattern = "https://api.cognitive.microsoft.com/bing/v5.0/search?q=%s&responseFilter=webpages";

            final String strquery = URLEncoder.encode(this.generateQuery(query), Charset.defaultCharset().name());
            final String bingUrl = String.format(bingUrlPattern, strquery);

            final String accountKeyEnc = Base64.getEncoder().encodeToString((accountKey + ":" + accountKey).getBytes());
            Long resultsLength = 0l;
            final URL url = new URL(bingUrl);
            final URLConnection connection = url.openConnection();
            connection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
            List<WebSite> resultsws = new ArrayList<WebSite>();

            try (final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                final StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

        */
        } catch (Exception e) {
            e.printStackTrace();
            return new DefaultSearchResult(new ArrayList<WebSite>(), 0L, query, pattern, false);
        }
    }

    private SearchResult query_v2(MetaQuery query, Pattern pattern){

        try {
            AzureSearchCompositeQuery aq = new AzureSearchCompositeQuery();
            aq.setAppid(BING_API_KEY);
            aq.setLatitude("47.603450");
            aq.setLongitude("-122.329696");
            if ( query.getLanguage().equals("en") ) aq.setMarket("en-US");
            else if ( query.getLanguage().equals("de") )aq.setMarket("de-DE");
            else aq.setMarket("fr-FR");

            aq.setSources(new AZURESEARCH_QUERYTYPE[] { AZURESEARCH_QUERYTYPE.WEB });

            String strQuery = this.generateQuery(query);
            logger.debug("BING Query: " + strQuery);
            aq.setQuery(strQuery);
            aq.doQuery();

            AzureSearchResultSet<AbstractAzureSearchResult> ars = aq.getQueryResult();
            // query bing and get only the urls and the total hit count back
            List<WebSite> results = new ArrayList<WebSite>();

            int i = 1;
            for (AbstractAzureSearchResult result : ars){

                if ( i > Integer.valueOf(NUMBER_OF_SEARCH_RESULTS) ) break;

                if ( ((AzureSearchWebResult) result).getUrl().startsWith("http://images.webgiftr.com/")
                        || ((AzureSearchWebResult) result).getUrl().startsWith("http://www.calza.com/")) continue;

                WebSite website = new WebSite(query, ((AzureSearchWebResult) result).getUrl());
                website.setTitle(result.getTitle());
                website.setRank(i++);
                website.setLanguage(query.getLanguage());
                results.add(website);
            }

            return new DefaultSearchResult(results, ars.getWebTotal(), query, pattern, false);
        }
        catch (Exception e) {

            e.printStackTrace();
            return new DefaultSearchResult(new ArrayList<WebSite>(), 0L, query, pattern, false);
        }
    }

    @Override
    public SearchResult query(MetaQuery query, Pattern pattern, String version) {

        if (version.equals("v2")){
            return query_v2(query, pattern);
        }else if (version.equals("v5")){
            return query_v5(query, pattern);
        }else{
            logger.error("-> bing version not implemented!");
            return new DefaultSearchResult(new ArrayList<WebSite>(), 0L, query, pattern, false);
        }

    }

    @Override
    public String generateQuery(MetaQuery query) {
        return new BingQuery().generateQuery(query);
    }

}
