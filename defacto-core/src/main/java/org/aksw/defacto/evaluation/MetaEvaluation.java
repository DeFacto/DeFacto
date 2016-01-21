package org.aksw.defacto.evaluation;

/**
 * Created by dnes on 04/01/16.
 */
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.hp.hpl.jena.rdf.model.*;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.*;
import org.aksw.defacto.reader.DefactoModelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A naive strategy to generate metadata for correct triples x changed ones
 */
public class MetaEvaluation {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaEvaluation.class);
    private static String getWhichPartFromRandom = ""; //get the resource to update -> TO
    private static String resourceToBeChanged; //select which resource should be swapped -> FROM
    private static int totalFilesProcessed = 0;
    private static int totalFoldersProcessed = 0;
    private static int totalRandomFilesProcessed = 0;
    private static int totalFoldersToBeProcessed = 0;
    private static int nrModelsToCompare = 4;  //the number of models to be computed
    private static File randomFolder;
    private static String fileName;
    private static String newRandomFileName = "";
    private static double trainPercSize = 0.05;

    private static boolean applyLogicalRestriction = false; //true = check rule for swapping the resource (scenario 1), false = no rule, 100% random swapping (scenario 2)
    private static ArrayList<String> cache = new ArrayList<>(); //keeps in-memory cache and store into a csv file in order to restart the process
    private static ArrayList<String> cacheBkp;
    static PrintWriter out;
    static boolean generateFiles = false;
    static String cacheProcessingLog;

    public static void main(String[] args) {

        LOGGER.info("Starting the process");

        try{

            Defacto.init();

            long startTimeOverallProcess = System.currentTimeMillis();
            int sizePropertyFolder = 0;
            List<String> languages = Arrays.asList("en");
            List<DefactoModel> models = new ArrayList<>();


            String trainUnknown = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + Defacto.DEFACTO_CONFIG.getStringSetting("eval", "test-directory") + "unknown\\random\\";
            if (applyLogicalRestriction)
                trainUnknown = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + Defacto.DEFACTO_CONFIG.getStringSetting("eval", "test-directory") + "unknown\\rulebased\\";

            if (generateFiles)
                createEvaluationFiles(trainUnknown);

            fileName = "EVAL_META_01.csv";
            cacheProcessingLog = "PROCESSING_QUEUE_01.csv";

            if (applyLogicalRestriction) {
                fileName = "EVAL_META_02.csv";
                cacheProcessingLog = "PROCESSING_QUEUE_02.csv";
            }

            out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));

            cache = readCacheFromCSV(cacheProcessingLog);
            cacheBkp = (ArrayList<String>) cache.clone();

            String testDirectory = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory")
                    + Defacto.DEFACTO_CONFIG.getStringSetting("eval", "test-directory") + "correct/";

            LOGGER.info("Apply logical restriction: " + applyLogicalRestriction);

            LOGGER.info("Selecting folders which contain FP...");
            ArrayList<File> folders = getFunctionalPropertyFolders(testDirectory);
            LOGGER.info("Total of folders to be processed: " + String.valueOf(folders.size()));
            LOGGER.info("==============================================================================================================");

            //start the process for each property (folder)
            for(File currentFolder : folders){
                totalFoldersProcessed++;

                    models.clear();

                    sizePropertyFolder = currentFolder.listFiles().length;
                    LOGGER.info(":: reading the folder '" + currentFolder.getName() + "' which contains " + sizePropertyFolder + " models (files)");

                    //add all the models which presents functional property then we can deal with exclusions
                    models.addAll(DefactoModelReader.readModels(currentFolder.getAbsolutePath(), true, languages));

                    LOGGER.info(":: starting the process for " + models.size() + " models...");

                    int totalTrainFiles = (int)Math.ceil(models.size() * trainPercSize);
                    LOGGER.info("Total of training files: " + totalTrainFiles);

                    //starting computation for each model
                    for (int i=0; i < totalTrainFiles; i++){

                        totalFilesProcessed++;

                        LOGGER.info("Main Model: Starting DeFacto for [" + models.get(i).getSubjectLabel("en") + "] [" + models.get(i).getPredicate().getLocalName() + "] [" + models.get(i).getObjectLabel("en") + "]");

                        if (!cache.contains(models.get(i).getFile().getAbsolutePath())){
                            Evidence e = Defacto.checkFact(models.get(i), Defacto.TIME_DISTRIBUTION_ONLY.NO);

                            LOGGER.info("...done! Score = " + e.getDeFactoScore().toString());

                            Integer df_num_cp = 0;
                            Integer df_num_tt = 0;
                            Long df_num_hc = e.getTotalHitCount();
                            Double df_score = e.getDeFactoScore();

                            if (e.getComplexProofs() != null)
                                df_num_cp = e.getComplexProofs().size();

                            if (e.getTopicTerms() != null)
                                df_num_tt = e.getTopicTerms().size();

                            out.println(models.get(i).getFile().getAbsolutePath() + ";" +
                                    models.get(i).getFile().getAbsolutePath() + ";" + df_num_cp + ";" + df_num_tt + ";" + df_num_hc+ ";" + df_score+ ";" + models.get(i).getName()+ ";" +
                                    models.get(i).getSubjectUri()+ ";" + models.get(i).getSubjectLabel("en")+ ";" +
                                    models.get(i).getPredicate().getURI()+ ";" + models.get(i).getPredicate().getLocalName()+ ";" +
                                    models.get(i).getObjectUri()+ ";" + models.get(i).getObjectLabel("en")+ ";" + "O");
                            out.flush();

                            cache.add(models.get(i).getFile().getAbsolutePath());
                            
                            LOGGER.debug("Model " + models.get(i).getName() + " has been processed");

                        }
                        else{
                            LOGGER.debug("Model already processed");
                        }

                        LOGGER.info("Starting to compute random models...");

                        //compute the scores for aux models
                        for ( int m = 0; m < nrModelsToCompare ; m++ ) {

                            totalRandomFilesProcessed++;

                            int auxIndex = m;
                            DefactoModel tempModel = null;

                            try{
                                String filenameU = trainUnknown + "/" +  currentFolder.getName()  + "/" +  models.get(i).getFile().getName().substring(0,  models.get(i).getFile().getName().length() - 4) + "_" + m + ".ttl";
                                LOGGER.info("reading: " + filenameU);
                                tempModel=  DefactoModelReader.readModel(filenameU , false, languages);
                                if (tempModel == null){
                                    LOGGER.info("model is NULL!!!!");
                                }
                            }catch (Exception e){
                                LOGGER.error(e.toString());
                            }

                            LOGGER.info("Random: Starting DeFacto for [" + tempModel.getSubjectLabel("en") +
                                    "] [" + tempModel.getPredicate().getLocalName() + "] [" + tempModel.getObjectLabel("en") + "]");

                            if (!cache.contains(tempModel.getFile().getAbsolutePath())) {

                                Evidence e = Defacto.checkFact(tempModel, Defacto.TIME_DISTRIBUTION_ONLY.NO);

                                LOGGER.info("...done! Score = " + e.getDeFactoScore().toString());

                                Integer df_num_cp = 0;
                                Integer df_num_tt = 0;
                                if (e.getComplexProofs() != null)
                                    df_num_cp = e.getComplexProofs().size();

                                if (e.getTopicTerms() != null)
                                    df_num_tt = e.getTopicTerms().size();

                                Long df_num_hc = e.getTotalHitCount();
                                Double df_score = e.getDeFactoScore();

                                out.println(models.get(i).getFile().getAbsolutePath() + ";" +
                                        tempModel.getFile().getAbsolutePath() + ";" + df_num_cp + ";" +  df_num_tt + ";" + df_num_hc + ";" + df_score + ";" + tempModel.getName()+ ";" +
                                        tempModel.getSubjectUri()+ ";" + tempModel.getSubjectLabel("en")+ ";" +
                                        tempModel.getPredicate().getURI()+ ";" + tempModel.getPredicate().getLocalName()+ ";" +
                                        tempModel.getObjectUri()+ ";" + tempModel.getObjectLabel("en")+ ";" + "C");
                                out.flush();
                                cache.add(tempModel.getFile().getAbsolutePath());

                                LOGGER.info("Changed Model '" + tempModel.getName() + "' has been processed");

                            }
                            else{
                                LOGGER.debug("Model already processed");
                            }

                        }

                        LOGGER.info("File " +  models.get(i).getName() + " has been processed");

                        LOGGER.info(":: Synchronizing cache");
                        for(String m: cache){
                            if (!cacheBkp.contains(m))
                                writeToCSV(cacheProcessingLog, m);
                        }
                        cache = readCacheFromCSV(cacheProcessingLog);
                        cacheBkp = (ArrayList<String>) cache.clone();
                        out.flush();

                }
                LOGGER.info("The folder " + currentFolder.getName() + " has been processed successfully");
            }

            long stopTimeOverallProcess = System.currentTimeMillis();
            long elapsedTime = stopTimeOverallProcess - startTimeOverallProcess;

            LOGGER.info("==============================================================================================================");
            LOGGER.info("Done!");
            LOGGER.info("Total folders processed: " + totalFilesProcessed);
            LOGGER.info("Total files processed: " + totalFilesProcessed);
            LOGGER.info("Total random files processed: " + totalFilesProcessed);
            LOGGER.info(String.format("Processing Time: %02d hour, %02d min, %02d sec",
                    TimeUnit.MILLISECONDS.toHours(elapsedTime),
                    TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
                    TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime)))
            );

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                LOGGER.info(":: Error -> Synchronizing cache");
                for(String m: cache){
                    if (!cacheBkp.contains(m))
                        writeToCSV(cacheProcessingLog, m);
                }
                cache = readCacheFromCSV(cacheProcessingLog);
                cacheBkp = cache;
                out.flush();
                out.close();
            }catch (Exception e2){
                LOGGER.error(e2.toString());
            }
        }

    }

    private static  ArrayList<File> getFunctionalPropertyFolders(String directory) throws Exception{
        File files = new File(directory);
        ArrayList<File> folders = new ArrayList<>();
        for(File f : files.listFiles()){
            if (f.isDirectory() && isFunctional(f.getName())){
                folders.add(f); totalFoldersToBeProcessed++;
                LOGGER.info("Folder '" + f.getName() + "' is functional and has been selected for evaluation");
            }
        }
        return folders;
    }

    /**
     * create the files for meta evaluation
     */
    private static void createEvaluationFiles(String trainUnknown){

        try {

            Defacto.init();

            List<String> languages = Arrays.asList("en");
            List<DefactoModel> models = new ArrayList<>();
            List<DefactoModel> modelsRandom = new ArrayList<>();

            String FactBenchTestDirectory = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + Defacto.DEFACTO_CONFIG.getStringSetting("eval", "test-directory") + "correct/";
            int sizePropertyFolder = 0;

            LOGGER.info("Creating evaluation set for meta analysis");
            for (File file: new File(trainUnknown).listFiles()) {
                //        if (!file.isDirectory()) file.delete();
            }

            LOGGER.info("Cleaning up the 'unknown' directory");

            ArrayList<File> folders = getFunctionalPropertyFolders(FactBenchTestDirectory);

            LOGGER.info("Total of folders to be processed: " + String.valueOf(totalFoldersToBeProcessed));
            LOGGER.info("=====================================================================================================");

            for(File currentFolder : folders){

                    totalFoldersProcessed++;

                    models.clear();

                    sizePropertyFolder = currentFolder.listFiles().length;

                    LOGGER.info(":: reading the folder '" + currentFolder.getName() + "' which contains " + sizePropertyFolder + " models (files)");

                    models.addAll(DefactoModelReader.readModels(currentFolder.getAbsolutePath(), true, languages));

                    LOGGER.info(":: starting the process for " + models.size() + " models...");

                    for (int i=0; i < models.size(); i++){

                        totalFilesProcessed++;
                        ArrayList<File> selectedFiles = null;

                        if (applyLogicalRestriction){

                            resourceToBeChanged = PropertyConfigurationSingleton.getInstance().getConfigurations().get(currentFolder.getName()).getResourceToBeChangedForRubbish();

                            //this folder will be used to collect new (random) resources
                            randomFolder = getRandomFolder(folders, currentFolder, resourceToBeChanged);

                            LOGGER.info(":: source: " + randomFolder.getName());

                            //get inside the selected folder, N models
                            selectedFiles = getNRandomFiles(nrModelsToCompare, randomFolder);

                            //add all random selected models
                            for (int j=0; j<selectedFiles.size(); j++){
                                modelsRandom.add(DefactoModelReader.readModel(selectedFiles.get(j).getAbsolutePath()));
                                LOGGER.info(":: source file " + selectedFiles.get(j).getAbsolutePath() + " has been selected as source for " + currentFolder.getName());
                            }

                        }else{
                            LOGGER.info(":: logical restriction is not required...");
                        }

                        int auxIndex = 0;
                        //compute the scores for aux models
                        for ( int m = 0; m < nrModelsToCompare ; m++ ) {

                            totalRandomFilesProcessed++;

                            auxIndex = m;

                            //what´s the evaluation method: EVAL1:RB1 or EVAL1:RB2
                            if (!applyLogicalRestriction){

                                modelsRandom.clear();
                                resourceToBeChanged = getRandomResource();
                                LOGGER.debug(":: random resourceToBeChanged = " + resourceToBeChanged);
                                randomFolder = getRandomFolder(folders, currentFolder, resourceToBeChanged);
                                LOGGER.info(":: source: " + randomFolder.getName());
                                selectedFiles = getNRandomFiles(1, randomFolder);
                                auxIndex = 0;

                                //add all random selected models
                                for (int j=0; j<selectedFiles.size(); j++){
                                    modelsRandom.add(DefactoModelReader.readModel(selectedFiles.get(j).getAbsolutePath()));
                                    LOGGER.info(":: source file " + selectedFiles.get(j).getAbsolutePath() + " has been selected as source for " + currentFolder.getName());
                                }
                            }

                            LOGGER.debug("Starting to compute random models...");

                            mixModelsUpAndCreateFile(models.get(i), modelsRandom.get(auxIndex), m, (trainUnknown + currentFolder.getName() + "\\"));

                        }

                        if (modelsRandom!=null)
                            modelsRandom.clear();
                        if (selectedFiles!=null)
                            selectedFiles.clear();

                        LOGGER.info("File " +  models.get(i).getName() + " has been processed");

                    }
                    LOGGER.info("The folder " + currentFolder.getName() + " has been processed successfully");

                /***********************************************************************************************************************************/
            }
        }catch (Exception e){
            LOGGER.error(e.toString());
        }

    }

    /**
     *
     * @param m1 the first model
     * @param m2 the second model
     * @param sURI the subject URI of the selected subject
     * @param oURI the object URI of the selected object
     * @return Model
     */
    private static void mixModels(Model m1, Model m2,
                                  String S1, String O1,
                                  String S2, String O2,
                                  int index, String destinationFolder, String sourceFileName) throws Exception {

        LOGGER.debug(":: starting the jena model generation");

        Model m = ModelFactory.createDefaultModel();

        m.setNsPrefix("fbase", "http://rdf.freebase.com/ns");
        m.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        m.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
        m.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        m.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        m.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");


        Property auxPredicate = null;
        RDFNode P1 = null;
        Resource Stemp = null;

        //OxS, SxS, OxO, SxO use cases
        if (resourceToBeChanged.equals("S")) {
            //O and P from M1 + P' from O' = P
            StmtIterator listIter1 = m1.listStatements();
            while (listIter1.hasNext()) {
                Statement stmt = listIter1.next();
                //adding O1
                if (stmt.getSubject().getURI().equals(O1)) {
                    m.add(stmt);
                }
                //adding P1
                if (stmt.getSubject().getURI().matches("^.*__[0-9]*$")) {
                    m.add(stmt);
                    P1 = stmt.getSubject();
                }
                //reading S1
                if (stmt.getSubject().getURI().equals(S1) && stmt.getObject().isResource() && stmt.getObject().asResource().getURI().matches("^.*__[0-9]*$")) {
                    auxPredicate = stmt.getPredicate();
                }
            }
            //adding S2
            StmtIterator listIter2 = m2.listStatements();
            while (listIter2.hasNext()) {
                Statement stmt2 = listIter2.next();
                if (getWhichPartFromRandom.equals("S")) {
                    if (stmt2.getSubject().getURI().equals(S2)) {
                        Stemp = stmt2.getSubject();
                        if (stmt2.getObject().isResource() && stmt2.getObject().asResource().getURI().matches("^.*__[0-9]*$")) {
                            m.add(stmt2.getSubject(), auxPredicate, P1);
                        } else {
                            m.add(stmt2);
                        }
                    }
                } else if (getWhichPartFromRandom.equals("O")) {
                    if (stmt2.getSubject().getURI().equals(O2)) {
                        Stemp = stmt2.getSubject();
                        m.add(stmt2);
                    }
                }
                if (getWhichPartFromRandom.equals("O")) {
                    m.add(Stemp, auxPredicate, P1);
                }
            }
        }
        else if (resourceToBeChanged.equals("O")) { //[O -> O] and //[O -> S]


            //O2
            StmtIterator listIter2 = m2.listStatements();
            while (listIter2.hasNext()) {
                Statement stmt2 = listIter2.next();
                if (getWhichPartFromRandom.equals("O")) {
                    if (stmt2.getSubject().getURI().equals(O2)) {
                        m.add(stmt2);
                        Stemp = stmt2.getSubject();
                    }
                } else if (getWhichPartFromRandom.equals("S")) {
                    if (stmt2.getSubject().getURI().equals(S2)) {
                        if (!(stmt2.getObject().isResource() &&
                                stmt2.getObject().asResource().getURI().matches("^.*__[0-9]*$"))) {
                            m.add(stmt2);
                            Stemp = stmt2.getSubject();
                        }
                    }
                }
            }

            //S1 and P1
            StmtIterator listIter1 = m1.listStatements();
            while (listIter1.hasNext()) {
                Statement stmt1 = listIter1.next();
                if (stmt1.getSubject().getURI().equals(S1)) {
                    m.add(stmt1);
                }
                if (stmt1.getSubject().getURI().matches("^.*__[0-9]*$")) {
                    if (stmt1.getObject().isResource()) {
                        m.add(stmt1.getSubject(), stmt1.getPredicate(), Stemp);
                    } else {
                        m.add(stmt1);
                    }
                }
            }
        }
/*
        if (getWhichPartFromRandom.equals("O")) {
            //O from M2
            StmtIterator listIter2 = m2.listStatements();
            while (listIter2.hasNext()) {
                Statement stmt2 = listIter2.next();
                if (stmt2.getSubject().getURI().equals(resourceToBeObject)) {
                    m.add(stmt2);
                    auxObject = stmt2.getSubject();
                }
            }
            //S and P updated from M1
            StmtIterator listIter1 = m1.listStatements();
            while (listIter1.hasNext()) {
                Statement stmt = listIter1.next();
                //S from S
                if (stmt.getSubject().getURI().equals(resourceToBeSubject)) {
                    m.add(stmt);
                    if (stmt.getObject().isResource() && stmt.getObject().asResource().getURI().matches("^.*__[0-9]*$")) {
                        auxPredicate = stmt.getPredicate(); //just to naming and save the file correctly
                    }
                }
                //S from O
                else if (stmt.getObject().isResource() && stmt.getObject().asResource().getURI().equals(resourceToBeSubject)) {
                    m.add(stmt);
                }
                //P
                if (stmt.getSubject().getURI().matches("^.*__[0-9]*$")) {
                    //change this statement
                    if (stmt.getObject().isResource()) {
                        m.add(stmt.getSubject(), stmt.getPredicate(), auxObject);
                    } else {
                        m.add(stmt);
                    }
                }
            }
        }

        if (getWhichPartFromRandom.equals("S")) {
            //M1: add O and P, getting O' from S
            StmtIterator listIter = m1.listStatements();
            while (listIter.hasNext()) {
                Statement stmt = listIter.next();
                //get O and P
                if (stmt.getSubject().getURI().equals(resourceToBeObject) || stmt.getSubject().getURI().matches("^.*__[0-9]*$")) {
                    m.add(stmt);
                }
                else {
                    //get P and O' from S to be used later...
                    if (stmt.getObject().isResource() && stmt.getObject().asResource().getURI().matches("^.*__[0-9]*$")) {
                        auxPredicate = stmt.getPredicate();
                        auxObject = stmt.getObject();
                    }
                }
            }
            //M2: add updated S
            StmtIterator listIter2 = m2.listStatements();
            while (listIter2.hasNext()) {
                Statement stmt2 = listIter2.next();
                if (stmt2.getSubject().getURI().equals(resourceToBeSubject)){
                    if (stmt2.getObject().isResource() && stmt2.getObject().asResource().getURI().matches("^.*__[0-9]*$")) {
                        m.add(stmt2.getSubject(), auxPredicate, auxObject);
                    }else{
                        m.add(stmt2);
                    }
                }
            }
        }

*/

        newRandomFileName = destinationFolder + sourceFileName.substring(0, sourceFileName.length() - 4) + "_" + index + ".ttl";
        FileWriter out = new FileWriter(newRandomFileName);
        m.write(out, "TTL");

        LOGGER.info(":: New file model has been generated: " + newRandomFileName);



    }

    /***
     * DeFacto´s model is built up over jena´s model representation. This function correctly mix things up.
     * @param original
     * @param random
     * @param index
     * @param languages
     * @return
     * @throws Exception
     */
    private static void mixModelsUpAndCreateFile(DefactoModel original, DefactoModel random, int index, String destinationFolder) throws Exception{


        String sFinal = original.getSubjectUri();
        String oFinal = original.getObjectUri();
        newRandomFileName= "";

        if (original == null || random ==null){
            throw new Exception("null error at model´s level (original / random)");
        }

        LOGGER.info("Default Model: [" + original.getSubjectLabel("en") + "] [" + original.getPredicate() + "] [" + original.getObjectLabel("en") + "]");
        LOGGER.info("Random Model: [" + random.getSubjectLabel("en") + "] [" + random.getPredicate() + "] [" + random.getObjectLabel("en") + "]");

        if (resourceToBeChanged.equals("S") && getWhichPartFromRandom.equals("S")){
            LOGGER.info("S1: Swapping the subject from [" + original.getSubjectLabel("en") + "] to [" + random.getSubject().getLabel("en") + "]");
            sFinal = random.getSubjectUri();
        }
        else if (resourceToBeChanged.equals("O") && (getWhichPartFromRandom.equals("S"))){
            LOGGER.info("S2: Swapping the object from [" + original.getObjectLabel("en") + "] to [" + random.getSubject().getLabel("en") + "]");
            oFinal = random.getSubjectUri();
        }
        else if (resourceToBeChanged.equals("S") && getWhichPartFromRandom.equals("O")){
            LOGGER.info("S3: Swapping the subject from [" + original.getSubjectLabel("en") + "] to [" + random.getObject().getLabel("en") + "]");
            sFinal = random.getObjectUri();
        }
        else if (resourceToBeChanged.equals("O") && getWhichPartFromRandom.equals("O")){
            LOGGER.info("S4: Swapping the object from [" + original.getObjectLabel("en") + "] to [" + random.getObject().getLabel("en") + "]");
            oFinal = random.getObjectUri();
        }
        else{
            throw new Exception("Strange...it should not happen :/ getWhichPart = " + getWhichPartFromRandom);
        }

        LOGGER.debug("final S =" + sFinal);
        LOGGER.debug("final O =" + oFinal);
        LOGGER.debug("index =" + index);
        LOGGER.debug("destinationFolder =" + destinationFolder);

        String f = new File(original.getName()).getName();

        LOGGER.debug("sourceFileName=" + f);

         mixModels(original.model, random.model,
                 original.getSubject().getUri(), original.getObject().getUri(),
                 random.getSubject().getUri(), random.getObject().getUri(),
                 index, destinationFolder, f);

        //finalModel = mixModels(original.model, random.model, subjectURI, objectURI, index, destinationFolder, new File(original.getName()).getName());

        /*
        //this code does not work for FreeBase
        RestModel rm = new RestModel();
        Triple t = new Triple(
                NodeFactory.createURI(subjectURI),
                NodeFactory.createURI(predicateURI),
                NodeFactory.createURI(objectURI));

        finalModel =
            rm.getModel(t, original.getTimePeriod().getFrom().toString(), original.getTimePeriod().getTo().toString());
        finalModel.setName("temp" + index);
        finalModel.setCorrect(false);
        */
        //return finalModel;
    }

    /**
     * check whether the property is functional
     * @param propertyName
     * @return
     */
    private static boolean isFunctional(String propertyName) {
        return PropertyConfigurationSingleton.getInstance().getConfigurations().get(propertyName).isFunctionalProperty();
    }

    /***
     * get a random resource (subject or object)
     * @return
     */
    private static String getRandomResource(){
        Random r = new Random();
        if (r.nextInt(2) == 1)
            return "S";
        return "O";
    }

    /***
     * TODO: test it
     * @param r
     * @return
     */
    private  HashMap<String, PropertyConfiguration> getResourcesToBeChangedByType(String r){

        Map<String, PropertyConfiguration> filter =
                PropertyConfigurationSingleton.getInstance().getConfigurations().entrySet()
                        .stream()
                        .filter(p -> p.getValue().getResourceToBeChangedForRubbish().equals(r))
                        .collect(
                                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                        );

        return (HashMap)filter;

    }

    /***
     * returns a random folder (property) of FactBench to be used as source to the new model generation process based on the structure of the provided model
      * @param current
     * @return
     */
    private static File getRandomFolder(ArrayList<File> folders, File current, String resourceType) throws Exception{
        File randomFolder = current;
        int randomM;
        boolean allowed = false;
        getWhichPartFromRandom = "";
        ArrayList<Integer> selected = new ArrayList<>(); //controls the flow
        PropertyConfiguration cCurrent, cRandom;

        if (folders.size() > 1) {

            cCurrent = PropertyConfigurationSingleton.getInstance().getConfigurations().get(current.getName());

            //checking configuration
            if (!PropertyConfigurationSingleton.getInstance().getConfigurations().containsKey(current.getName()))
                throw new Exception("Configuration has not been found -> " + current.getName());

            while ((randomFolder.equals(current)) && selected.size() < folders.size()) {

                Random rand = new Random();
                randomM = rand.nextInt(folders.size());
                randomFolder = folders.get(randomM);
                if (!selected.contains(randomM)) {
                    selected.add(randomM);
                }

                //checking configuration
                if (!PropertyConfigurationSingleton.getInstance().getConfigurations().containsKey(randomFolder.getName()))
                    throw new Exception("Configuration has not been found -> " + randomFolder.getName());

                //check whether current and chosen share the same configuration
                if (!current.getName().equals(randomFolder.getName())) {
                    //if (onlyFuncional && isFunctional(randomFolder.getName())){

                    cRandom = PropertyConfigurationSingleton.getInstance().getConfigurations().get(randomFolder.getName());

                    if (resourceType.equals("S")){
                        if (cCurrent.getSubjectClass().equals(cRandom.getSubjectClass())) {
                            allowed = true;
                            getWhichPartFromRandom = "S";
                        } else if (cCurrent.getSubjectClass().equals(cRandom.getObjectClass())) {
                            allowed = true;
                            getWhichPartFromRandom = "O";
                        }
                    }else if (resourceType.equals("O")) {
                        if (cCurrent.getObjectClass().equals(cRandom.getSubjectClass())) {
                            allowed = true;
                            getWhichPartFromRandom = "S";
                        } else if (cCurrent.getObjectClass().equals(cRandom.getObjectClass())) {
                            allowed = true;
                            getWhichPartFromRandom = "O";
                        }
                    }

                    if (!allowed) {
                        randomFolder = current;
                    }
                }
                //}
            }

            if (randomFolder.equals(current)) {
                //no further options have been selected, rely on the same folder, which is kind of senseless (although the last option for desired task)
                getWhichPartFromRandom = resourceType;
                LOGGER.warn("Attention, there is no similar property available. The evaluation is more likely to be senseless to the property " + current.getName());
            }
        }

        return randomFolder;
    }

    /***
     * get randomly n files inside a folder
     * @param n
     * @param selectedFolder
     * @return the selected files
     */
    private static ArrayList<File> getNRandomFiles(int n, File selectedFolder){

        Random rand = new Random();
        int size, aux, randomM;
        ArrayList<File> selected = new ArrayList(n);
        File[] files;

        try{
            aux = 0;

            files = selectedFolder.listFiles();
            size = files.length;

            while (aux<n){
                randomM = rand.nextInt(size);
                if (!selected.contains(files[randomM])){
                    aux++;
                    selected.add(files[randomM]);
                }
            }

        }catch (Exception e){
            LOGGER.error(e.toString());
        }
        return selected;
    }

    private static ArrayList<String> readCacheFromCSV(String fn) throws Exception{

        ArrayList<String> cache = new ArrayList<>();

        try {

            createCacheFile(fn);

            CSVReader reader = new CSVReader(new FileReader(fn), ',' , '\'' , '\t');

            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                cache.add(nextLine[0]);
            }
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cache;

    }

    private static void createCacheFile(String fileName) {

        try {
            File f = new File(fileName);
            if(!f.exists()) {
                f.createNewFile();
            }

        }catch (Exception e){
            LOGGER.error(e.toString());
        }

    }

    private static void writeToCSV(String fileName, String text) throws IOException {

        createCacheFile(fileName);

        try {

            CSVWriter writer = new CSVWriter(new FileWriter(fileName, true), ',' , '\0' , '\t');
            String[] t = new String[] {text};
            writer.writeNext(t);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
