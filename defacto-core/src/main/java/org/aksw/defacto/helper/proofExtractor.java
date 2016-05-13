package org.aksw.defacto.helper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.crawl.EvidenceCrawler;
import org.aksw.defacto.search.engine.SearchEngine;
import org.aksw.defacto.search.engine.bing.AzureBingSearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.query.QueryGenerator;
import org.aksw.defacto.util.TimeUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.*;

/**
 * Created by dnes on 30/10/15.
 */
public class proofExtractor {

    public static org.apache.log4j.Logger LOGDEV    = org.apache.log4j.Logger.getLogger("developer");
    public static PrintWriter writer;
    public static PrintWriter writer_overview;
    public static String separator = ";";
    private static final File folder = new File("/Users/dnes/github/FactBench/v1/train/correct");
    private static List<String> files = new ArrayList<>();

    private static void setFilesModelFiles(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                setFilesModelFiles(fileEntry);
            } else {
                if (FilenameUtils.getExtension(fileEntry.getAbsolutePath()).equals("ttl")) {
                    files.add(fileEntry.getAbsolutePath());
                }
            }
        }
    }

    private static void startProcess(Defacto.TIME_DISTRIBUTION_ONLY onlyTimes){

        Defacto.init();

        setFilesModelFiles(folder);

        for(String f:files){

            Model m = null;
            DefactoModel model = null;
            InputStream is;
            try{
                m = ModelFactory.createDefaultModel();
                is = new FileInputStream(f);
                m.read(is, null, "TURTLE");
                model = new DefactoModel(m, "Nobel Model", true, Arrays.asList("en"));
            }catch (Exception e){
                LOGDEV.fatal(e.toString());
            }

            LOGDEV.info("Extracting Proofs for: " + model);
            Defacto.onlyTimes = onlyTimes;

            LOGDEV.debug(" [1] starting generating the search engines queries for counter examples");

            long start = System.currentTimeMillis();
            QueryGenerator queryGenerator = new QueryGenerator(model);
            Map<Pattern,MetaQuery> queries = new HashMap<>();
            for ( String language : model.languages ) {
                Map<Pattern,MetaQuery> q = queryGenerator.getCounterExampleSearchEngineQueries(language);
                queries.putAll(q);
            }
            if ( queries.size() <= 0 ) {
                LOGDEV.debug(" -> none query has been generated for the model: " + model);
            }
            LOGDEV.debug(" -> Preparing queries took " + TimeUtil.formatTime(System.currentTimeMillis() - start));

            int i;

            //for (Map.Entry<Pattern, MetaQuery> entry : queries.entrySet())
            // {
            //     LOGDEV.debug(" -> query: " + entry.getKey() + "/" + entry.getValue());
            //}


            SearchEngine engine = new AzureBingSearchEngine();
            // download the search results in parallel
            long startCrawl = System.currentTimeMillis();
            EvidenceCrawler crawler = new EvidenceCrawler(model, queries);
            Evidence evidence = crawler.crawlCounterEvidence(engine);
            LOGDEV.debug(" -> crawling counter evidence took " + TimeUtil.formatTime(System.currentTimeMillis() - startCrawl));

            String _uri;
            URI uri = null;
            String domain = null;

            List<Pattern> patterns = evidence.getBoaPatterns();
            for (Pattern p: patterns){
                LOGDEV.debug(" -> Pattern: " + p.toString());
            }
            if (patterns.size() == 0){
                LOGDEV.debug(" -> No pattern has been found for the evidence!");
            }

            List<WebSite> websites = evidence.getAllWebSites();
            Map<Pattern, List<WebSite>> patternAndWebsites = evidence.getWebSites();

            //LOGDEV.info(websites.size());
            //LOGDEV.info(patternAndWebsites.values().size());

            List<ComplexProof> proofs;

            LOGDEV.info("SUBJECT;OBJECT;PROPERTY;PATTERN;TOT WEBSITE;TOT CP [S->P<-O];TOT_COMPLEX_PROOFS;TOT_COMPLEX_PROOFS_IN_BETWEEN");
            LOGDEV.info("**********************************************************************************************");
            LOGDEV.info("Property: " + model.getPropertyUri());
            LOGDEV.info("");
            for ( Map.Entry<Pattern, List<WebSite>> patternToWebSites : evidence.getWebSites().entrySet()) {

                int numberOfWebsitesWithComplexProofs = 0;
                int numberOfWebsitesWithComplexProofsAndPatternInBetween = 0;

                for (WebSite website : patternToWebSites.getValue()) {
                    numberOfWebsitesWithComplexProofs+=evidence.getComplexProofs(website).size();
                    numberOfWebsitesWithComplexProofsAndPatternInBetween+=evidence.getComplexProofsPInBetween(website).size();
                }

                //LOGDEV.info(":: Pattern = " + patternToWebSites.getKey().naturalLanguageRepresentation.toString() + " - Total [S->P<-O] = " + evidence.getComplexProofsPInBetween(patternToWebSites.getKey()).size() );
                LOGDEV.info(
                        model.getSubjectLabel("en") + ";" +
                                model.getObjectLabel("en") + ";" +
                                model.getPropertyUri() + ";" +
                                patternToWebSites.getKey().naturalLanguageRepresentation.toString() + ";" +
                                patternToWebSites.getValue().size() + ";" +
                                evidence.getComplexProofsPInBetween(patternToWebSites.getKey()).size() + ";" +
                                numberOfWebsitesWithComplexProofs + ";" +
                                numberOfWebsitesWithComplexProofsAndPatternInBetween + ";");

                //for (WebSite website : patternToWebSites.getValue()) {
                //    LOGDEV.debug("website: " + website.getUrl());
                //}
                //LOGDEV.debug("");
            }



            LOGDEV.info("**********************************************************************************************");
            //LOGDEV.info("Statistics for Evidence => " + evidence.getModel().getName().toString());
            //LOGDEV.info("");
            //LOGDEV.info(":: Total of Websites = " + websites.size());
            //LOGDEV.info(":: Total of Complex Proofs = " + evidence.getComplexProofs().size());
            //LOGDEV.info(":: Total of Complex Proofs [S->P<-O] = " + evidence.getComplexProofsPInBetween().size());
            //LOGDEV.info("**********************************************************************************************");


            //HEADERS
            writer.println("uri" + separator + "domain"+ separator + "total-proofs" + separator + "subject label" + separator + "property label" + separator + "object label" + separator + "language");
            writer_overview.println("uri" + separator + "domain" + separator + "total-proofs" + separator + "subject label" + separator + "property label" + separator + "object label" + separator + "language");


            //System.out.println(":: nr. websites: " + evidence.getAllWebSites().size());
            //List<WebSite> websitesWithproofsInBetween = evidence.getAllWebSitesWithComplexProofAndAtLeastOneBOAPatternInBetween();
            //System.out.println(":: nr. websites with proofs with pattern in between S and O: " + websitesWithproofsInBetween.size());

           /*
            for (WebSite w: websites) {

                try {
                    uri = new URI(w.getUrl().toString());
                    domain = uri.getHost();
                } catch (URISyntaxException e) {

                }
                _uri = w.getUrl();


                LOGDEV.debug(":: website: " + _uri);
                LOGDEV.debug(":: website domain: " + domain);

                proofs = evidence.getComplexProofs(w);
                LOGDEV.debug(":: nr. proofs: " + proofs.size());

                List<ComplexProof> proofsInBetween = evidence.getComplexProofsPInBetween(w);
                LOGDEV.debug(":: nr. proofs with pattern in between: " + proofsInBetween.size());

                LOGDEV.debug(":: query: " + w.getQuery().toString());
                LOGDEV.debug("--------------------------------------------------------------------------");


                writer_overview.println(_uri + separator + domain + separator + proofs.size() + separator + w.getQuery().getSubjectLabel() + separator + w.getQuery().getPropertyLabel() + separator + w.getQuery().getObjectLabel() + separator + w.getLanguage());

                for (ComplexProof proof: proofs){

                    writer.println(_uri + separator + domain + separator + proofs.size() + separator + proof.getWebSite().getLanguage() + separator + proof.getSmallContext());

                    //System.out.println("Proof pattern: " + proof.getPattern().naturalLanguageRepresentation);

                    LOGDEV.debug("Proof language: " + proof.getWebSite().getLanguage());
                    LOGDEV.debug("Proof website url: " + proof.getWebSite().getUrl());
                    //System.out.println("Proof tiny context: " + proof.getTinyContext());
                    //System.out.println("Proof small context: " + proof.getSmallContext());
                    //System.out.println("Proof medium context: " + proof.getMediumContext());
                    LOGDEV.debug("Proof large context: " + proof.getLargeContext());
                    LOGDEV.debug("website text:" + proof.getWebSite().getText());
                    LOGDEV.debug("");
                }
                LOGDEV.debug("--------------------------------------------------------------------------");
            }

            */

            LOGDEV.info(" -> process finished");

            m = null;

        }

    }

    public static void main(String[] args) throws Exception {

        writer = new PrintWriter("proofs_neg.csv", "UTF-8");
        writer_overview = new PrintWriter("proofs_neg_stats.csv", "UTF-8");




        startProcess(Defacto.TIME_DISTRIBUTION_ONLY.NO);

        writer.close();
        writer_overview.close();

    }

    private static DefactoModel getOneExample(){
        Model model = ModelFactory.createDefaultModel();
        model.read(DefactoModel.class.getClassLoader().getResourceAsStream("Nobel1909.ttl"), null, "TURTLE");
        return new DefactoModel(model, "Nobel Model", true, Arrays.asList("en"));
    }


}
