package org.aksw.defacto.evaluation;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.helper.DefactoUtils;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.crawl.EvidenceCrawler;
import org.aksw.defacto.search.engine.SearchEngine;
import org.aksw.defacto.search.engine.bing.AzureBingSearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.query.QueryGenerator;
import org.aksw.defacto.util.TimeUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by dnes on 30/10/15.
 */
public class ProofExtractor {

    private static final Logger         LOGGER = LoggerFactory.getLogger(ProofExtractor.class);
    public static PrintWriter           writer;
    public static PrintWriter           writer_overview;
    public static String                separator = ";";
    private static final File           folder = new File("/Users/dnes/Github/FactBench/test/correct/");
    private static List<String>         files = new ArrayList<>();

    private static String               cacheQueueProof = "PROCESSING_QUEUE_PROOFS.csv";
    private static String               cacheProofValues = "EVAL_COUNTER_PROOFS.csv";
    private static ArrayList<String>    cache = new ArrayList<>();
    private static ArrayList<String>    cacheBkp = new ArrayList<>();
    private static DefactoUtils util;
    private static PrintWriter          out;

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

        try{

        Defacto.init();

        util = new DefactoUtils();

        //Factbench dataset directory (correct)
        setFilesModelFiles(folder);

        cache = util.readCacheFromCSV(cacheQueueProof);
        cacheBkp = (ArrayList<String>) cache.clone();

        out = new PrintWriter(new BufferedWriter(new FileWriter(cacheProofValues, true)));

        for(String f:files) {

            if (!cache.contains(f)) {

                DefactoModel model = null;
                try {
                    Model m = ModelFactory.createDefaultModel();
                    InputStream is = new FileInputStream(f);
                    m.read(is, null, "TURTLE");
                    model = new DefactoModel(m, "Nobel Model", true, Arrays.asList("en"));
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }

                LOGGER.info("Extracting Proofs for: " + model);
                Defacto.onlyTimes = onlyTimes;

                LOGGER.debug(" [1] starting generating the search engines queries for counter examples");

                long start = System.currentTimeMillis();
                QueryGenerator queryGenerator = new QueryGenerator(model);
                Map<Pattern, MetaQuery> queries = new HashMap<>();
                for (String language : model.languages) {
                    Map<Pattern, MetaQuery> q = queryGenerator.getCounterExampleSearchEngineQueries(language);
                    queries.putAll(q);
                }
                if (queries.size() <= 0) {
                    LOGGER.error(" -> none query has been generated for the model: " + model);
                }
                LOGGER.debug(" -> Preparing queries took " + TimeUtil.formatTime(System.currentTimeMillis() - start));


                for (Map.Entry<Pattern, MetaQuery> entry : queries.entrySet()) {
                    LOGGER.debug(" -> query: " + entry.getKey() + "/" + entry.getValue());
                }

                SearchEngine engine = new AzureBingSearchEngine();
                // download the search results in parallel
                long startCrawl = System.currentTimeMillis();
                EvidenceCrawler crawler = new EvidenceCrawler(model, queries);
                Evidence evidence = crawler.crawlCounterEvidence(engine);
                LOGGER.debug(" -> crawling counter evidence took " + TimeUtil.formatTime(System.currentTimeMillis() - startCrawl));

                List<Pattern> patterns = evidence.getBoaPatterns();
                for (Pattern p : patterns) {
                    LOGGER.debug(" -> Pattern: " + p.toString());
                }
                if (patterns.size() == 0) {
                    LOGGER.debug(" -> No pattern has been found for the evidence!");
                }

                for (Map.Entry<Pattern, List<WebSite>> patternToWebSites : evidence.getWebSites().entrySet()) {

                    int numberOfWebsitesWithComplexProofs = 0;
                    int numberOfWebsitesWithComplexProofsAndPatternInBetween = 0;

                    for (WebSite website : patternToWebSites.getValue()) {
                        numberOfWebsitesWithComplexProofs += evidence.getComplexProofs(website).size();
                        numberOfWebsitesWithComplexProofsAndPatternInBetween += evidence.getComplexProofsPInBetween(website).size();
                    }

                    out.println(f +
                                model.getSubjectLabel("en") + ";" +
                                model.getObjectLabel("en") + ";" +
                                model.getPropertyUri() + ";" +
                                patternToWebSites.getKey().naturalLanguageRepresentation.toString() + ";" +
                                patternToWebSites.getValue().size() + ";" +
                                numberOfWebsitesWithComplexProofs + ";" +
                                evidence.getComplexProofsPInBetween(patternToWebSites.getKey()).size() + ";" +
                                numberOfWebsitesWithComplexProofsAndPatternInBetween + ";" +
                                evidence.getDeFactoScore().toString()  + ";");

                    out.flush();


                    //for (WebSite website : patternToWebSites.getValue()) {
                    //    LOGDEV.debug("website: " + website.getUrl());}

                }

                cache.add(f);


                //System.out.println(":: nr. websites: " + evidence.getAllWebSites().size());
                //List<WebSite> websitesWithproofsInBetween = evidence.getAllWebSitesWithComplexProofAndAtLeastOneBOAPatternInBetween();
                //System.out.println(":: nr. websites with proofs with pattern in between S and O: " + websitesWithproofsInBetween.size());


                String _uri;
                URI uri = null;
                String domain = null;
                List<WebSite> websites = evidence.getAllWebSites();
                List<ComplexProof> proofs;

            for (WebSite w: websites) {
                try {
                    uri = new URI(w.getUrl().toString());
                    domain = uri.getHost();
                } catch (URISyntaxException e) {
                    LOGGER.error(e.toString());
                }
                _uri = w.getUrl();


                proofs = evidence.getComplexProofs(w);

                List<ComplexProof> proofsInBetween = evidence.getComplexProofsPInBetween(w);
                int totalProofsInBetween = 0;
                if (proofsInBetween != null) {
                    totalProofsInBetween = proofsInBetween.size();
                }


                writer_overview.println(f + separator + _uri + separator + domain + separator + proofs.size() + separator + w.getQuery().getSubjectLabel() + separator + w.getQuery().getPropertyLabel() + separator + w.getQuery().getObjectLabel() + separator + w.getLanguage());
                int i = 1;
                for (ComplexProof proof: proofs){
                    writer.println(f + separator +
                            _uri + separator +
                            domain + separator +
                            i + separator +
                            totalProofsInBetween + separator +
                            proof.getWebSite().getLanguage() + separator +
                            proof.getMediumContext() + separator +
                            w.getQuery().toString()
                    );
                    i++;

                }

            }

            LOGGER.info(":: Synchronizing cache");
            for(String s: cache){
                if (!cacheBkp.contains(s))
                    util.writeToCSV(cacheQueueProof, s);
            }
            cache = util.readCacheFromCSV(cacheQueueProof);
            cacheBkp = (ArrayList<String>) cache.clone();
            out.flush();
            writer.flush();
            writer_overview.flush();

            }
        }

        }catch (Exception e){
            LOGGER.error(e.toString());
        }
        finally {
            writer_overview.flush();
            writer_overview.close();
            writer.flush();
            writer.close();
            out.flush();
            out.close();
            LOGGER.info(":: Synchronizing cache");
            try {
                for (String s : cache) {
                    if (!cacheBkp.contains(s))
                        util.writeToCSV(cacheQueueProof, s);
                }
                cache = util.readCacheFromCSV(cacheQueueProof);
                cacheBkp = (ArrayList<String>) cache.clone();
            }catch (Exception e){
                LOGGER.error(e.toString());
            }
        }


    }

    public static void main(String[] args) throws Exception {

        writer = new PrintWriter("proofs_neg.csv", "UTF-8");
        writer_overview = new PrintWriter("proofs_neg_stats.csv", "UTF-8");

        writer.println("uri" + separator + "domain" + separator + "total-proofs" + separator + "subject label" + separator + "property label" + separator + "object label" + separator + "language");
        writer.flush();
        writer_overview.println("uri" + separator + "domain" + separator + "total-proofs" + separator + "subject label" + separator + "property label" + separator + "object label" + separator + "language");
        writer_overview.flush();

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
