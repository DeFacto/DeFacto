package org.aksw.defacto.evaluation;

import au.com.bytecode.opencsv.CSVReader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.thoughtworks.xstream.mapper.Mapper;
import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.helper.DefactoUtils;
import org.aksw.defacto.helper.SQLiteHelper;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.model.DefactoResource;
import org.aksw.defacto.rest.RestModel;
import org.aksw.defacto.search.crawl.EvidenceCrawler;
import org.aksw.defacto.search.engine.SearchEngine;
import org.aksw.defacto.search.engine.bing.AzureBingSearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.query.QueryGenerator;
import org.aksw.defacto.topic.frequency.Word;
import org.aksw.defacto.util.TimeUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

import static java.lang.System.exit;

/**
 * Created by dnes on 30/10/15.
 */
public class ProofExtractor {



    private static final Logger         LOGGER = LoggerFactory.getLogger(ProofExtractor.class);
    public static PrintWriter           writer;
    public static PrintWriter           writer_overview;
    public static String                separator = ";";
    private static final File           folder_pos_anisa = new File("/data/anisa/");

    private static final File           wsdm_nationality = new File("/data/wsdm/nationality.test.tsv");
    private static final File           wsdm_profession = new File("/data/wsdm/profession.test.tsv");

    private static final File           folder_pos = new File("/home/esteves/github/FactBench/test/correct/");
    private static final File           folder_neg = new File("/home/esteves/github/FactBench/test/wrong/domainrange/");
    private static List<String>         files_pos = new ArrayList<>();
    private static List<String>         files_neg = new ArrayList<>();

    private static int                  dataset = 2; //0=FB, 1=Anisa, 2=WSDM, 3=ISWC

    private static String               cacheQueueProof = "PROCESSING_QUEUE_PROOFS.csv";
    private static String               cacheProofValues = "EVAL_COUNTER_PROOFS.csv";
    private static ArrayList<String>    cache = new ArrayList<>();
    private static ArrayList<String>    cacheBkp = new ArrayList<>();
    private static DefactoUtils util;
    private static PrintWriter          out;

    private static void setFilesModelFiles(final File folder, String type) {

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                setFilesModelFiles(fileEntry, type);
            } else {
                if (FilenameUtils.getExtension(fileEntry.getAbsolutePath()).equals("ttl") ||
                FilenameUtils.getExtension(fileEntry.getAbsolutePath()).equals("tsv")) {
                    if (type.equals("POS"))
                        files_pos.add(fileEntry.getAbsolutePath());
                    else
                        files_neg.add(fileEntry.getAbsolutePath());
                }
            }
        }
    }

    private static void saveWebSiteAndRelated(Integer idevidence, WebSite w, Integer has_proof) throws Exception {

        Integer idmetaquery = SQLiteHelper.getInstance().saveMetaQuery(idevidence, w.getQuery());
        /* TB_WEBSITE */
        Integer idwebsite = SQLiteHelper.getInstance().saveWebSite(idmetaquery, w, has_proof);

        /* TB_REL_TOPICTERM_WEBSITE */
        for (Word word: w.getOccurringTopicTerms()){
            SQLiteHelper.getInstance().addTopicTermsWebSite(idwebsite, word.getWord(), word.getFrequency(),
                    word.isFromWikipedia() == true ? 1: 0);}

    }

    private static void saveMetadata(long totalTime, Evidence _evidence,
                                     String name,
                                     Constants.EvidenceType evidencetype,
                                     String f) throws Exception{

        try{
            Evidence eaux;
            Integer eauxnum;

            LOGGER.info("saving metadata -> " + name + evidencetype.toString() + f);

            if (evidencetype.equals(Constants.EvidenceType.POS)) {
                eaux = _evidence;
            }else {
                eaux = _evidence.getNegativeEvidenceObject();
            }

            if (eaux.getModel().correct == true){
                eauxnum = 1;
            }else{
                eauxnum = 0;
            }

            DefactoModel model = eaux.getModel();
            Path p1 = Paths.get(f);
            String filename = p1.getFileName().toString();

            /** TB_MODEL **/
            Integer idmodel = SQLiteHelper.getInstance().saveModel(totalTime, model, filename,
                    p1.getParent().toString(), name);

            /** TB_EVIDENCE HEADER **/
            Integer idevidence = SQLiteHelper.getInstance().saveEvidenceRoot(idmodel, eaux, eauxnum);

            /** TB_YEAR_EVIDENCE **/
            SQLiteHelper.getInstance().saveYearOccorrence(idevidence, 1, eaux.tinyContextYearOccurrences);
            SQLiteHelper.getInstance().saveYearOccorrence(idevidence, 2, eaux.smallContextYearOccurrences);
            SQLiteHelper.getInstance().saveYearOccorrence(idevidence, 3, eaux.mediumContextYearOccurrences);
            SQLiteHelper.getInstance().saveYearOccorrence(idevidence, 4, eaux.largeContextYearOccurrences);

            /* TB_PATTERN x TB_METAQUERY */
            Map<Pattern, MetaQuery> q = eaux.getQueries();
            Iterator it = q.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Pattern p = (Pattern) pair.getKey();
                MetaQuery m = (MetaQuery) pair.getValue();

                Integer idpattern = SQLiteHelper.getInstance().savePattern(p);
                Integer idmetaquery = SQLiteHelper.getInstance().saveMetaQuery(idevidence, m);

                //LOGGER.info(":: pattern - " + idpattern + " - " + p.toString());
                //LOGGER.info(":: metaquery - " + idmetaquery + " - " + m.toString());

                SQLiteHelper.getInstance().savePatternMetaQuery(idpattern, idmetaquery);

                /* TB_REL_TOPICTERM_METAQUERY */
                for (Word wordtt: m.getTopicTerms()) {
                    SQLiteHelper.getInstance().addTopicTermsMetaQuery(idmetaquery, wordtt.getWord(), wordtt.getFrequency(),
                            wordtt.isFromWikipedia() == true ? 1: 0);}
            }

            /** TB_REL_TOPIC_TERM_EVIDENCE **/
            for (Map.Entry<String, List<Word>> entry : eaux.getTopicTerms().entrySet())
            {
                for (Word wordtt: entry.getValue()) {
                    SQLiteHelper.getInstance().addTopicTermsEvidence(idevidence, entry.getKey(), wordtt.getWord(),
                            wordtt.getFrequency(), wordtt.isFromWikipedia() == true ? 1 : 0);}
            }



            //all other websites with proof
            List<WebSite> sitesproof = eaux.getAllWebSitesWithComplexProof();
            //all other websites without proof
            List<WebSite> sitesnoproof = eaux.getAllWebSitesWithoutComplexProof();

            for (WebSite site: sitesproof){
                saveWebSiteAndRelated(idevidence, site, 1);
            }
            for (WebSite site: sitesnoproof){
                saveWebSiteAndRelated(idevidence, site, 0);
            }

            //all proofs
            Set<ComplexProof> setproofs = eaux.getComplexProofs();
            Iterator<ComplexProof> iterator = setproofs.iterator();
            while(iterator.hasNext()) { ComplexProof pfr = iterator.next();
                SQLiteHelper.getInstance().addProof(idmodel, pfr, idevidence, eaux);}

            if ((sitesproof.size() + sitesnoproof.size() != eaux.getAllWebSites().size())){
                LOGGER.debug(String.valueOf(sitesproof.size()));
                LOGGER.debug(String.valueOf(sitesnoproof.size()));
                LOGGER.debug(String.valueOf(eaux.getAllWebSites().size()));
                throw new Exception("that' strange!");
            }

            //commit transaction for model N
            SQLiteHelper.getInstance().commitT();

        }catch (Exception e){
            SQLiteHelper.getInstance().rollbackT();
            throw e;
        }

    }

    private static void export(Map<String, DefactoModel> defactoModels) {

        Iterator it = defactoModels.entrySet().iterator();
        Integer sequencial = 0;
        while (it.hasNext()) {
            try{

                long startTime = System.currentTimeMillis();
                Map.Entry pair = (Map.Entry)it.next();

                String real_filename = pair.getKey().toString().substring(0, pair.getKey().toString().indexOf("tsv_") + 3);
                 sequencial = Integer.valueOf(pair.getKey().toString().substring(pair.getKey().toString().indexOf("tsv_") + 4, pair.getKey().toString().length()));
                //if (sequencial!=380)
                //    continue;
                DefactoModel model = (DefactoModel)pair.getValue();


                LOGGER.info(":: checking model [" + sequencial + "] : " + real_filename);
                final Evidence evidence = Defacto.checkFact(model, Defacto.TIME_DISTRIBUTION_ONLY.NO);
                LOGGER.info("Overall Score: " + evidence.getDeFactoScore());
                LOGGER.info("Overall Counterargument Score: " + evidence.getDeFactoCounterargumentScore());
                long endTime   = System.currentTimeMillis();
                long totalTime = endTime - startTime;

                String out = String.format("Processing Time: %02d hour, %02d min, %02d sec",
                        TimeUnit.MILLISECONDS.toHours(totalTime),
                        TimeUnit.MILLISECONDS.toMinutes(totalTime),
                        TimeUnit.MILLISECONDS.toSeconds(totalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalTime))
                );

                LOGGER.info(out);
                LOGGER.info(":: ok, saving metadata...");
                String name = pair.getKey().toString().replace("/data/wsdm/", "");
                saveMetadata(totalTime, evidence, name, Constants.EvidenceType.POS, pair.getKey().toString());
                LOGGER.info(":: done...");

                it.remove();

            }catch (Exception e){
                LOGGER.error(":: error! " + e.toString());
                LOGGER.error(":: line: " + sequencial.toString());
            }
        }

    }

    private static int modelSaved(String filename, String path) throws Exception{

        String sSQL = "SELECT ID FROM TB_MODEL WHERE FILE_NAME = ? AND FILE_PATH = ? AND LANGS = ? " +
                "AND MODEL_CORRECT = ?";
        PreparedStatement prep = SQLiteHelper.getInstance().getConnection().prepareStatement(sSQL);

        prep.setString(1, filename);
        prep.setString(2, path);
        prep.setString(3, "[de, en, fr]");
        prep.setInt(4, 1);

        return SQLiteHelper.getInstance().existsRecord(prep);

    }

    public static void exportMetadataDB() throws Exception{

        //setFilesModelFiles(folder_pos, "POS");
        //setFilesModelFiles(folder_neg, "NEG");

        Long totalModelsSaved = 0L;
        Map<String, DefactoModel> map = new HashMap<>();
        RestModel restmodel = new RestModel();

        if (dataset == 3) { //ISWC 2017 Challenge dataset

        }

        else if (dataset == 2) { //WSDM dataset
            //files_pos.add(wsdm_nationality.getAbsolutePath());
            files_pos.add(wsdm_profession.getAbsolutePath());
            System.out.println("->" + files_pos.size());
            for(String f:files_pos) {
                Integer auxmodel = 0;
                Path p1 = Paths.get(f);
                CSVReader reader = new CSVReader(
                        new InputStreamReader(
                                new FileInputStream(System.getProperty("user.dir") + f),"UTF8"),
                        '\t', '\'', 0);

                String[] nextLine;

                int counter = 0;
                while ((nextLine = reader.readNext()) != null) {
                    counter++;
                    auxmodel++;

                    if (f.equalsIgnoreCase(wsdm_profession.getAbsolutePath())) {

                        if (auxmodel == 323 || auxmodel == 324){
                            nextLine[0] = "http://dbpedia.org/resource/Raúl_Zamudio";
                        }
                        if (auxmodel == 510 || auxmodel == 511){
                            nextLine[0] = "http://dbpedia.org/resource/Yūji";
                        }
                        if (auxmodel == 380){
                            nextLine[1] = "http://dbpedia.org/resource/Scorer";
                        }

                        if (auxmodel == 492 || auxmodel == 493){
                            nextLine[0] = "http://dbpedia.org/resource/Ashley";
                        }

                        if (auxmodel == 284){
                            nextLine[1] = "http://dbpedia.org/resource/Film_score_composer";
                        }

                        if (auxmodel >= 179 && auxmodel <= 186){
                            nextLine[0] = "http://dbpedia.org/resource/Noël_Coward";
                        }

                        if (auxmodel == 90){
                            nextLine[1] = "http://dbpedia.org/resource/Film_score_composer";
                        }

                        if (auxmodel >= 334 && auxmodel <= 337){
                            nextLine[0] = "http://dbpedia.org/resource/Warren_Moore";
                        }

                        if (auxmodel == 238){
                            nextLine[1] = "http://dbpedia.org/resource/Film_score_composer";
                        }

                        if (auxmodel == 83){
                            nextLine[1] = "http://dbpedia.org/resource/Aviator";
                        }

                        if (auxmodel == 126){
                            nextLine[1] = "http://dbpedia.org/resource/Film_score_composer";
                        }

                        if (auxmodel == 345 || auxmodel == 290){
                            nextLine[1] = "http://dbpedia.org/resource/Game_show_host";
                        }

                        if (auxmodel == 169){
                            nextLine[1] = "http://dbpedia.org/resource/Film_score_composer";
                        }

                        if (auxmodel == 245 || auxmodel == 332 || auxmodel == 112 || auxmodel == 66 ||
                                auxmodel == 184 || auxmodel == 150 || auxmodel == 37){
                            nextLine[1] = "http://dbpedia.org/resource/Film_score_composer";
                        }

                        if (auxmodel == 475 || auxmodel == 474){
                            nextLine[0] = "http://dbpedia.org/resource/Fethiye_Çetin";
                        }

                        if (auxmodel >= 202 && auxmodel <= 204){
                            nextLine[0] = "http://dbpedia.org/resource/Moliere";
                        }

                        if (auxmodel >= 334 && auxmodel <= 337){
                            nextLine[0] = "http://dbpedia.org/resource/Earl_Warren";
                        }

                        if (auxmodel >= 413 && auxmodel <= 413){
                            nextLine[1] = "http://dbpedia.org/resource/Game_show_host";
                        }

                        if (auxmodel >= 435 && auxmodel <= 435){
                            nextLine[1] = "http://dbpedia.org/resource/Sculptor";
                        }

                        if (auxmodel == 448 || auxmodel == 448){
                            nextLine[1] = "http://dbpedia.org/resource/Ice_hockey";
                        }

                        if (auxmodel == 506 || auxmodel == 506){
                            nextLine[1] = "http://dbpedia.org/resource/Art_Director";
                        }

                        if (auxmodel == 509 || auxmodel == 509){
                            nextLine[1] = "http://dbpedia.org/resource/Film_score_composer";
                        }

                        if (auxmodel >= 510 && auxmodel <= 511){
                            nextLine[0] = "http://dbpedia.org/resource/Yūji";
                        }

                    }


                    String filenameaux = f + "_" + auxmodel.toString();

                    if (modelSaved(filenameaux.replace("/data/wsdm/", ""), p1.getParent().toString()) != 0){
                        totalModelsSaved++;
                        LOGGER.info("model [" + filenameaux + "] already cached!");
                        continue;
                    }

                    Triple triple;
                    if (f.equalsIgnoreCase(wsdm_nationality.getAbsolutePath())) {
                        triple =
                                new Triple(NodeFactory.createURI(nextLine[0]),
                                           NodeFactory.createURI("http://dbpedia.org/ontology/nationality"),
                                           NodeFactory.createURI(nextLine[1]));
                    } else {
                        triple =
                                new Triple(NodeFactory.createURI(nextLine[0]),
                                           NodeFactory.createURI("http://dbpedia.org/ontology/profession"),
                                           NodeFactory.createURI(nextLine[1]));
                    }

                    DefactoModel model = restmodel.getModel(triple, "1800", "2017");
                    model.setFile(new File(f));
                    model.setCorrect(true);

                    map.put(filenameaux, model);
                    LOGGER.info(auxmodel.toString() + " : " + triple.toString());
                    //if (counter == 2)
                    //    break;
                }
            }
        }
        else if (dataset == 1){ //Anisa
            setFilesModelFiles(folder_pos_anisa, "POS");
            System.out.println("->" + files_pos.size());
            for(String f:files_pos) {
                Integer auxmodel = 0;
                Path p1 = Paths.get(f);
                CSVReader reader = new CSVReader(new FileReader(f),  '\t');
                String [] nextLine;

                int counter = 0;
                while ((nextLine = reader.readNext()) != null) {
                    counter++;
                    auxmodel++;
                    String filenameaux = f + "_" + auxmodel.toString();

                    if (modelSaved(filenameaux, p1.getParent().toString())!=0)
                        continue;

                    Triple triple =
                            new Triple(NodeFactory.createURI(nextLine[0]), NodeFactory.createURI(nextLine[1]),
                            NodeFactory.createURI(nextLine[2]));
                    DefactoModel model = restmodel.getModel(triple, nextLine[3], nextLine[4]);
                    model.setFile(new File(f));
                    model.setCorrect(true);

                    map.put(filenameaux, model);
                    System.out.println(auxmodel);
                    if (counter == 10)
                        break;
                }
            }
        }else{
            for(String f:files_pos) {

                Path p1 = Paths.get(f);
                String filename = p1.getFileName().toString();

                if (modelSaved(filename, p1.getParent().toString())!=0)
                    continue;

                DefactoModel model = null;
                Model m = ModelFactory.createDefaultModel();
                InputStream is = new FileInputStream(f);
                m.read(is, null, "TURTLE");
                model = new DefactoModel(m, filename.replace(FilenameUtils.getExtension(f), "").replace(".", ""),
                        false, Arrays.asList("en", "fr", "de"), filename);

                map.put(f, model);


            }
        }

        LOGGER.info("total models cached: " + totalModelsSaved.toString());
        LOGGER.info("total models to be checked: " + String.valueOf(map.size()));
        export(map);

    }

    private static void startProcess(Defacto.TIME_DISTRIBUTION_ONLY onlyTimes){

        try{

        Defacto.init();

        boolean correct = true;

        util = new DefactoUtils();

        //Factbench dataset directory (correct)
        setFilesModelFiles(folder_pos, "POS");

        cache = util.readCacheFromCSV(cacheQueueProof);
        cacheBkp = (ArrayList<String>) cache.clone();

        out = new PrintWriter(new BufferedWriter(new FileWriter(cacheProofValues, true)));

        for(String f:files_pos) {

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

    public static void main(String[] args) {

        try {
            if (1 == 2) {
                exportMetadataFile();
            } else {
                exportMetadataDB();
            }
        }catch (Exception e){
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter( writer );
            getCause(e).printStackTrace( printWriter );
            printWriter.flush();
            LOGGER.error(writer.toString());
        }
    }

    private static Throwable getCause(Throwable e) {
        Throwable cause = null;
        Throwable result = e;
        while(null != (cause = result.getCause())  && (result != cause) ) {
            result = cause;
        }
        return result;
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

    private static DefactoModel getOneExample() throws Exception{
        Model model = ModelFactory.createDefaultModel();
        URL url = DefactoModel.class.getClassLoader().getResource("Einstein.ttl");
        model.read(url.openStream(), null, "TURTLE");
        return new DefactoModel(model, "Einstein Model", true, Arrays.asList("en", "fr", "de"), url.getPath());
    }

    private static void getWebpagesOriginalBody(){

    }



}
