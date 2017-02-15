package org.aksw.defacto.evaluation;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.helper.DefactoUtils;
import org.aksw.defacto.helper.SQLiteHelper;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.crawl.EvidenceCrawler;
import org.aksw.defacto.search.engine.SearchEngine;
import org.aksw.defacto.search.engine.bing.AzureBingSearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.query.QueryGenerator;
import org.aksw.defacto.topic.frequency.Word;
import org.aksw.defacto.util.TimeUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by dnes on 30/10/15.
 */
public class ProofExtractor {



    private static final Logger         LOGGER = LoggerFactory.getLogger(ProofExtractor.class);
    public static PrintWriter           writer;
    public static PrintWriter           writer_overview;
    public static String                separator = ";";
    private static String               path = "/Users/esteves/Github/FactBench/test/correct/";
    private static final File           folder = new File(path);
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

    private static void saveWebSiteAndRelated(WebSite w, Integer idevidence, Integer idmodel) throws Exception {

        try{

            URI uri = new URI(w.getUrl().toString());
            String domain = uri.getHost();

            /* EXTRACTING: tb_pattern */
            Pattern psite = w.getQuery().getPattern();
            Integer idpattern = SQLiteHelper.getInstance().savePattern(idevidence, psite.boaScore,
                    psite.naturalLanguageRepresentationNormalized, psite.naturalLanguageRepresentationWithoutVariables,
                    psite.naturalLanguageRepresentation, psite.language, psite.posTags, psite.NER, psite.generalized,
                    psite.naturalLanguageScore);

            /* EXTRACTING: tb_metaquery */
            MetaQuery msite = w.getQuery();
            Integer idmetaquery = SQLiteHelper.getInstance().saveMetaQuery(idpattern, msite.toString(), msite.getSubjectLabel(),
                    msite.getPropertyLabel(), msite.getObjectLabel(), msite.getLanguage(),
                    msite.getEvidenceTypeRelation().toString());

            /* EXTRACTING: tb_website */
            Integer idwebsite = SQLiteHelper.getInstance().saveWebSite(idmetaquery, idpattern, w.getUrl(), domain,
                    w.getTitle(), w.getText(), w.getSearchRank(), w.getPageRank(), w.getPageRankScore(), w.getScore(),
                    w.getTopicMajorityWebFeature(), w.getTopicMajoritySearchFeature(),
                    w.getTopicCoverageScore(), w.getLanguage());

            /*******************************************
             * EXTRACTING: tb_rel_metaquery_topicterm
             * *****************************************/
            for (Word wordtt: msite.getTopicTerms()) {
                SQLiteHelper.getInstance().addTopicTermsMetaQuery(idmetaquery, wordtt.getWord(), wordtt.getFrequency(),
                        wordtt.isFromWikipedia() == true ? 1: 0);
            }



            /***************************************
             * EXTRACTING: tb_rel_website_topicterm
             * *************************************/
            for (Word word: w.getOccurringTopicTerms()){
                SQLiteHelper.getInstance().addTopicTermsWebSite(idwebsite, word.getWord(), word.getFrequency(),
                        word.isFromWikipedia() == true ? 1: 0);
            }

            //TODO: check this later!
            //List<ComplexProof> proofs = evidence.getComplexProofs(w);

            /************************
             * EXTRACTING: tb_proof
             * **********************/
            //for (ComplexProof pro: proofs){
            //    SQLiteHelper.getInstance().addProof(idwebsite, idpattern, idmodel,
            //            pro.getHasPatternInBetween() == true ? 1:0, pro.getTinyContext(),
            //            pro.getSmallContext(), pro.getMediumContext(), pro.getLargeContext(),
            //            pro.getProofPhrase(), pro.getNormalizedProofPhrase(), pro.getLanguage());
           // }

        }catch (Exception e){
            throw e;
        }
    }

    private static void saveMetadata(Evidence _evidence, Constants.EvidenceType evidencetype, String f) throws Exception{

        try{
            Evidence eaux;
            Integer eauxnum;

            if (evidencetype.equals(Constants.EvidenceType.POS)) {
                eaux = _evidence;
                eauxnum = 1;
            }else {
                eaux = _evidence.getNegativeEvidenceObject();
                eauxnum = 0;
            }

            DefactoModel model = eaux.getModel();
            Path p1 = Paths.get(f);
            String filename = p1.getFileName().toString();


            /** tb_model **/
            Integer idmodel = SQLiteHelper.getInstance().saveModel(model.getName(), model.isCorrect() ? 1 : 0, filename,
                    p1.getParent().toString(), model.getSubjectUri(), model.getPredicate().getURI(), model.getObjectUri(),
                    model.getTimePeriod().getFrom().toString(), model.getTimePeriod().getTo().toString(),
                    model.getTimePeriod().isTimePoint() ? 1 : 0);

            /** tb_evidence **/
            Integer idevidence = SQLiteHelper.getInstance().saveEvidence(idmodel, eaux.getDeFactoScore(),
                    eaux.getDeFactoCombinedScore(), eaux.getTotalHitCount(), eaux.getFeatures().toString(),
                    eauxnum);

            /** tb_rel_topicterm_evidence **/
            for (Map.Entry<String, List<Word>> entry : eaux.getTopicTerms().entrySet())
            {
                for (Word wordtt: entry.getValue()) {
                    SQLiteHelper.getInstance().addTopicTermsEvidence(idevidence, entry.getKey(), wordtt.getWord(),
                            wordtt.getFrequency(), wordtt.isFromWikipedia() == true ? 1 : 0);
                }
            }

            Integer verification = 0;
            //1. get all websites without proofs (remaining)
            List<WebSite> websites =
                    eaux.getAllWebSitesWithoutComplexProof();
            for (WebSite wsnp: websites){
                saveWebSiteAndRelated(wsnp, idevidence, idmodel); verification ++;
            }

            //2. get all websites, derived by proofs
            Set<ComplexProof> setproofs = eaux.getComplexProofs();
            Iterator<ComplexProof> iterator = setproofs.iterator();
            while(iterator.hasNext()) {
                ComplexProof pfr = iterator.next();
                WebSite wswp = pfr.getWebSite();
                saveWebSiteAndRelated(wswp, idevidence, idmodel); verification ++;
            }

            if (verification != _evidence.getAllWebSites().size()){
                SQLiteHelper.getInstance().rollbackT();
                throw new Exception(":: hum...something is not well modeled");
            }

            //commit transaction for model N
            SQLiteHelper.getInstance().commitT();

        }catch (Exception e){
            SQLiteHelper.getInstance().rollbackT();
            throw e;
        }

    }

    public static void exportMetadataDB(){

        try{

            long startTime = System.currentTimeMillis();
            DefactoModel model = getOneExample();
            final Evidence evidence = Defacto.checkFact(model, Defacto.TIME_DISTRIBUTION_ONLY.NO);
            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            String out = String.format("Processing Time: %02d hour, %02d min, %02d sec",
                    TimeUnit.MILLISECONDS.toHours(totalTime),
                    TimeUnit.MILLISECONDS.toMinutes(totalTime),
                    TimeUnit.MILLISECONDS.toSeconds(totalTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalTime))
            );

            LOGGER.info(out);
            LOGGER.info(":: saving metadata...");
            saveMetadata(evidence, Constants.EvidenceType.POS, model.getFile().getAbsolutePath());
            LOGGER.info(":: done...");


        }catch (Exception e){
            LOGGER.error(e.toString());
        }


    }

    private static void startProcess(Defacto.TIME_DISTRIBUTION_ONLY onlyTimes){

        try{

        Defacto.init();

        boolean correct = true;

        util = new DefactoUtils();

        //Factbench dataset directory (correct)
        setFilesModelFiles(folder);

        cache = util.readCacheFromCSV(cacheQueueProof);
        cacheBkp = (ArrayList<String>) cache.clone();

        out = new PrintWriter(new BufferedWriter(new FileWriter(cacheProofValues, true)));

        for(String f:files) {

            if (!cache.contains(f)) {

                Path p1 = Paths.get(f);
                String filename = p1.getFileName().toString();

                DefactoModel model = null;
                try {
                    Model m = ModelFactory.createDefaultModel();
                    InputStream is = new FileInputStream(f);
                    m.read(is, null, "TURTLE");
                    model = new DefactoModel(m, filename.replace(FilenameUtils.getExtension(f), ""), correct, Arrays.asList("en"));
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


            for (WebSite w: websites) {
                try {
                    uri = new URI(w.getUrl().toString());
                    domain = uri.getHost();
                } catch (URISyntaxException e) {
                    LOGGER.error(e.toString());
                }
                _uri = w.getUrl();


                List<ComplexProof> proofs = evidence.getComplexProofs(w);

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

            /**************************************
             * EXTRACTING: exporting weka features
             * ************************************/
            //BufferedWriter writer2 = new BufferedWriter(new FileWriter(model.name + ".arff"));
            //writer2.write(evidence.getFeatures().toString());
            //writer2.flush();
            //writer2.close();



            } //end caching (file)



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

        if (1==2){
            exportMetadataFile();
        }else{
            exportMetadataDB();
        }

    }

    private static void exportMetadataFile() throws Exception{

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
