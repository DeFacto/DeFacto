package org.aksw.defacto.evaluation;

/**
 * Created by dnes on 04/01/16.
 */


import com.hp.hpl.jena.rdf.model.*;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.model.*;
import org.aksw.defacto.reader.DefactoModelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * A naive strategy to generate metadata for correct triples x changed ones
 */
public class RubbishEvaluation {

    private static final Logger LOGGER = LoggerFactory.getLogger(RubbishEvaluation.class);
    private static PrintWriter writer;

    private static String getWhichPartFromRandom = ""; //get the resource to update -> TO
    private static String resourceToBeChanged; //select which resource should be swapped -> FROM
    private static boolean applyLogicalRestriction = false; //true = check rule for swapping the resource (scenario 1), false = no rule, 100% random swapping (scenario 2)

    private static int totalFilesProcessed = 0;
    private static int totalFoldersProcessed = 0;
    private static int totalRandomFilesProcessed = 0;

    private static int totalFoldersToBeProcessed = 0;
    private static int nrModelsToCompare = 4;  //the number of models to be computed
    private static File randomFolder;

    private static String header = "score;model;subject;subject_label;predicate;object;object_label;model_type;random_source_folder;random_source_file;tmp_model_file_name";
    private static String fileName = "EVAL01_META_B.txt";
    private static String newRandomFileName = "";

    private static List<PropertyConfiguration> cacheEvaluation = new ArrayList<>();

    public static void main(String[] args) {

        LOGGER.info("Starting the process");

        startCache();

        long startTimeOverallProcess = System.currentTimeMillis();

        try{

            Defacto.init();

            writer = new PrintWriter(fileName, "UTF-8");
            writer.println(header);

            List<String> languages = Arrays.asList("en");
            List<DefactoModel> models = new ArrayList<>(); //the list of true models
            List<DefactoModel> modelsRandom = new ArrayList<>(); //the aux models that will be used to generate different models

            //the number of models for each property (folder)
            int sizePropertyFolder = 0;

            String trainDirectory = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory")
                    + Defacto.DEFACTO_CONFIG.getStringSetting("eval", "test-directory") + "correct/";

            LOGGER.info("Selecting folders which contain FP...");
            LOGGER.info("Apply logical restriction: " + applyLogicalRestriction);

            File files = new File(trainDirectory);
            //getting all folders (properties)
            ArrayList<File> folders = new ArrayList<>();
            for(File f : files.listFiles()){
                //this test must be performed only for FP
                if (f.isDirectory() && isFunctional(f.getName())){
                    folders.add(f); totalFoldersToBeProcessed++;
                    LOGGER.info("Folder '" + f.getName() + "' is functional and has been selected for evaluation");
                }
            }
            LOGGER.info("Total of folders to be processed: " + String.valueOf(totalFoldersToBeProcessed));
            LOGGER.info("------------------------------------------------------------------------------------------------------------");

            //start the process for each property (folder)
            for(File currentFolder : folders){
                totalFoldersProcessed++;

                    models.clear();

                    sizePropertyFolder = currentFolder.listFiles().length;
                    LOGGER.info(":: reading the folder '" + currentFolder.getName() + "' which contains " + sizePropertyFolder + " models (files)");

                    //add all the models which presents functional property then we can deal with exclusions
                    models.addAll(DefactoModelReader.readModels(currentFolder.getAbsolutePath(), true, languages));

                    LOGGER.info(":: starting the process for " + models.size() + " models...");

                    //starting computation for each model
                    for (int i=0; i < models.size(); i++){
                        totalFilesProcessed++;

                        LOGGER.info("Main Model: Starting DeFacto for [" + models.get(i).getSubjectLabel("en") +
                                "] [" + models.get(i).getPredicate().getLocalName() + "] ["
                                + models.get(i).getObjectLabel("en") + "]");

                        if (!cacheEvaluation.contains(new PropertyConfiguration(models.get(i).getSubjectUri(), models.get(i).getPredicate().getURI(), models.get(i).getObjectUri()))) {

                            Double score = Defacto.checkFact(models.get(i), Defacto.TIME_DISTRIBUTION_ONLY.NO).getDeFactoScore();
                            LOGGER.info("...done! Score = " + score.toString());

                            //compute the score for main model
                            writer.println(score.toString() +
                                    ";" + models.get(i).getName() +
                                    ";" + models.get(i).getSubjectUri() +
                                    ";" + models.get(i).getSubjectLabel("en") +
                                    ";" + models.get(i).getPredicate().getLocalName() +
                                    ";" + models.get(i).getObjectUri() +
                                    ";" + models.get(i).getObjectLabel("en") +
                                    ";" + "O" +
                                    ";" + "" +
                                    ";" + "");
                            writer.flush();

                            LOGGER.info("Model " + models.get(i).getName() + " has been processed");
                        }else{
                            LOGGER.info("Model already processed");
                        }
                        /*********************************************************************************************************************************/

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

                            LOGGER.info("Starting to compute random models...");

                            DefactoModel tempModel = mixModelsUp(models.get(i), modelsRandom.get(auxIndex), m);


                            LOGGER.info("Random: Starting DeFacto for [" + tempModel.getSubjectLabel("en") +
                                    "] [" + tempModel.getPredicate().getLocalName() + "] [" + tempModel.getObjectLabel("en") + "]");


                            if (!cacheEvaluation.contains(new PropertyConfiguration(tempModel.getSubjectUri(),tempModel.getPredicate().getURI(),tempModel.getObjectUri()))) {

                                Double scoreTemp = Defacto.checkFact(tempModel, Defacto.TIME_DISTRIBUTION_ONLY.NO).getDeFactoScore();
                                LOGGER.info("...done! Score = " + scoreTemp.toString());

                                writer.println(scoreTemp.toString() +
                                                ";" + tempModel.getName() +
                                                ";" + tempModel.getSubjectUri() +
                                                ";" + tempModel.getSubjectLabel("en") +
                                                ";" + tempModel.getPredicate().getLocalName() +
                                                ";" + tempModel.getObjectUri() +
                                                ";" + tempModel.getObjectLabel("en") +
                                                ";" + "C" +
                                                ";" + randomFolder.getName() +
                                                ";" + modelsRandom.get(auxIndex).name +
                                                ";" + newRandomFileName);

                                LOGGER.info("Changed Model '" + tempModel.getName() + "' has been processed");
                                writer.flush();

                            }
                            else{
                                LOGGER.info("Model already processed");
                            }


                        }

                        if (modelsRandom!=null)
                            modelsRandom.clear();
                        if (selectedFiles!=null)
                            selectedFiles.clear();

                        LOGGER.info("File " +  models.get(i).getName() + " has been processed");
                        writer.flush();
                }

                LOGGER.info("The folder " + currentFolder.getName() + " has been processed successfully");
                writer.flush();
            }

            //Collections.shuffle(models, new Random(100));
            writer.close();

            long stopTimeOverallProcess = System.currentTimeMillis();
            long elapsedTime = stopTimeOverallProcess - startTimeOverallProcess;

            LOGGER.info("Done!");
            LOGGER.info("Total folders processed: " + totalFilesProcessed);
            LOGGER.info("Total files processed: " + totalFilesProcessed);
            LOGGER.info("Total random files processed: " + totalFilesProcessed);

            String.format("Processing Time: %02d hour, %02d min, %02d sec",
                    TimeUnit.MILLISECONDS.toHours(elapsedTime),
                    TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
                    TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime))
            );

        }catch (Exception e){
            LOGGER.error(e.toString());
        }

    }


    private static void startCache(){

        PropertyConfiguration p1 = new PropertyConfiguration("http://dbpedia.org/resource/Bill_Clinton","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Hope,_Arkansas");
        PropertyConfiguration p2 = new PropertyConfiguration("http://dbpedia.org/resource/Bill_Clinton","http://dbpedia.org/ontology/birthPlace","http://rdf.freebase.com/ns/m.018f94");
        PropertyConfiguration p3 = new PropertyConfiguration("http://dbpedia.org/resource/Bill_Clinton","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Beverly_Hills,_California");
        PropertyConfiguration p4 = new PropertyConfiguration("http://dbpedia.org/resource/Bill_Clinton","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Setagaya,_Tokyo");
        PropertyConfiguration p5 = new PropertyConfiguration("http://dbpedia.org/resource/Chris_Kaman","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Hope,_Arkansas");
        PropertyConfiguration p6 = new PropertyConfiguration("http://dbpedia.org/resource/Michael_Jackson","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Gary,_Indiana");
        PropertyConfiguration p7 = new PropertyConfiguration("http://dbpedia.org/resource/Ferdinand_von_Mueller","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Gary,_Indiana");
        PropertyConfiguration p8 = new PropertyConfiguration("http://dbpedia.org/resource/James_Buchanan","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Gary,_Indiana");
        PropertyConfiguration p9 = new PropertyConfiguration("http://dbpedia.org/resource/Richard_Jefferson","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Gary,_Indiana");
        PropertyConfiguration p10 = new PropertyConfiguration("http://dbpedia.org/resource/Michael_Jackson","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/West_Hollywood,_California");
        PropertyConfiguration p11 = new PropertyConfiguration("http://dbpedia.org/resource/Paul_McCartney","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Liverpool");
        PropertyConfiguration p12 = new PropertyConfiguration("http://dbpedia.org/resource/Tayshaun_Prince","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Liverpool");
        PropertyConfiguration p13 = new PropertyConfiguration("http://rdf.freebase.com/ns/m.01t_54y","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Liverpool");
        PropertyConfiguration p14 = new PropertyConfiguration("http://dbpedia.org/resource/Paul_McCartney","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Nashville,_Tennessee");
        PropertyConfiguration p15 = new PropertyConfiguration("http://dbpedia.org/resource/Paul_McCartney","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Santa_Barbara,_California");
        PropertyConfiguration p16 = new PropertyConfiguration("http://dbpedia.org/resource/Frank_Sinatra","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Hoboken,_New_Jersey");
        PropertyConfiguration p17 = new PropertyConfiguration("http://dbpedia.org/resource/Frank_Sinatra","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Kiel");
        PropertyConfiguration p18 = new PropertyConfiguration("http://rdf.freebase.com/ns/m.07j9cs","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Hoboken,_New_Jersey");
        PropertyConfiguration p19 = new PropertyConfiguration("http://dbpedia.org/resource/Frank_Sinatra","http://dbpedia.org/ontology/birthPlace","http://rdf.freebase.com/ns/m.03spz");
        PropertyConfiguration p20 = new PropertyConfiguration("http://dbpedia.org/resource/Gerald_Ford","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Hoboken,_New_Jersey");
        PropertyConfiguration p21 = new PropertyConfiguration("http://dbpedia.org/resource/Dwight_D._Eisenhower","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Denison,_Texas");
        PropertyConfiguration p22 = new PropertyConfiguration("http://dbpedia.org/resource/Dwight_D._Eisenhower","http://dbpedia.org/ontology/birthPlace","http://rdf.freebase.com/ns/m.0gp66n");
        PropertyConfiguration p23 = new PropertyConfiguration("http://dbpedia.org/resource/Dwight_D._Eisenhower","http://dbpedia.org/ontology/birthPlace","http://rdf.freebase.com/ns/m.04swd");
        PropertyConfiguration p24 = new PropertyConfiguration("http://rdf.freebase.com/ns/m.02wk4d","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Denison,_Texas");
        PropertyConfiguration p25 = new PropertyConfiguration("http://dbpedia.org/resource/Dwight_D._Eisenhower","http://dbpedia.org/ontology/birthPlace","http://rdf.freebase.com/ns/m.0r6ff");
        PropertyConfiguration p26 = new PropertyConfiguration("http://dbpedia.org/resource/Kanye_West","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Atlanta");
        PropertyConfiguration p27 = new PropertyConfiguration("http://dbpedia.org/resource/Kanye_West","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Cairo");
        PropertyConfiguration p28 = new PropertyConfiguration("http://dbpedia.org/resource/Kanye_West","http://dbpedia.org/ontology/birthPlace","http://rdf.freebase.com/ns/m.01z645");
        PropertyConfiguration p29 = new PropertyConfiguration("http://dbpedia.org/resource/Kanye_West","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Los_Angeles");
        PropertyConfiguration p30 = new PropertyConfiguration("http://dbpedia.org/resource/Kanye_West","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Houston");
        PropertyConfiguration p31 = new PropertyConfiguration("http://dbpedia.org/resource/BeyoncÃ©_Knowles","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Houston");
        PropertyConfiguration p32 = new PropertyConfiguration("http://dbpedia.org/resource/BeyoncÃ©_Knowles","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Cambridge,_Massachusetts");
        PropertyConfiguration p33 = new PropertyConfiguration("http://dbpedia.org/resource/Hubert_Humphrey","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Houston");
        PropertyConfiguration p34 = new PropertyConfiguration("http://dbpedia.org/resource/BeyoncÃ©_Knowles","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Chennai");
        PropertyConfiguration p35 = new PropertyConfiguration("http://dbpedia.org/resource/BeyoncÃ©_Knowles","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Oxford");
        PropertyConfiguration p36 = new PropertyConfiguration("http://dbpedia.org/resource/Harry_S._Truman","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Lamar,_Missouri");
        PropertyConfiguration p37 = new PropertyConfiguration("http://dbpedia.org/resource/Harry_S._Truman","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Beverly_Hills,_California");
        PropertyConfiguration p38 = new PropertyConfiguration("http://rdf.freebase.com/ns/m.0qs4h8r","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Lamar,_Missouri");
        PropertyConfiguration p39 = new PropertyConfiguration("http://dbpedia.org/resource/Gerald_Wallace","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Lamar,_Missouri");
        PropertyConfiguration p40 = new PropertyConfiguration("http://rdf.freebase.com/ns/m.030xfx","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Lamar,_Missouri");
        PropertyConfiguration p41 = new PropertyConfiguration("http://dbpedia.org/resource/Lady_Gaga","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/New_York_City");
        PropertyConfiguration p42 = new PropertyConfiguration("http://rdf.freebase.com/ns/m.02wc72n","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/New_York_City");
        PropertyConfiguration p43 = new PropertyConfiguration("http://dbpedia.org/resource/Saddam_Hussein","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/New_York_City");
        PropertyConfiguration p44 = new PropertyConfiguration("http://dbpedia.org/resource/Saddam_Hussein","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/New_York_City");
        PropertyConfiguration p45 = new PropertyConfiguration("http://rdf.freebase.com/ns/m.08623t","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/New_York_City");
        PropertyConfiguration p46 = new PropertyConfiguration("http://dbpedia.org/resource/Hillary_Rodham_Clinton","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Chicago");
        PropertyConfiguration p47 = new PropertyConfiguration("http://rdf.freebase.com/ns/m.0j11784","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Chicago");
        PropertyConfiguration p48 = new PropertyConfiguration("http://dbpedia.org/resource/Raymond_Felton","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Chicago");
        PropertyConfiguration p49 = new PropertyConfiguration("http://dbpedia.org/resource/Hillary_Rodham_Clinton","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Los_Angeles");
        PropertyConfiguration p50 = new PropertyConfiguration("http://dbpedia.org/resource/Hillary_Rodham_Clinton","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Houston");
        PropertyConfiguration p51 = new PropertyConfiguration("http://dbpedia.org/resource/Prince_(musician)","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Minneapolis");
        PropertyConfiguration p52 = new PropertyConfiguration("http://dbpedia.org/resource/Prince_(musician)","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Springfield,_Missouri");
        PropertyConfiguration p53 = new PropertyConfiguration("http://dbpedia.org/resource/Prince_(musician)","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Waverly,_Minnesota");
        PropertyConfiguration p54 = new PropertyConfiguration("http://rdf.freebase.com/ns/m.0qs4h8r","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Minneapolis");
        PropertyConfiguration p55 = new PropertyConfiguration("http://dbpedia.org/resource/Andrei_Kirilenko","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Minneapolis");
        PropertyConfiguration p56 = new PropertyConfiguration("http://dbpedia.org/resource/Johnny_Cash","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Kingsland,_Arkansas");
        PropertyConfiguration p57 = new PropertyConfiguration("http://dbpedia.org/resource/Johnny_Cash","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Los_Angeles");
        PropertyConfiguration p58 = new PropertyConfiguration("http://dbpedia.org/resource/Johnny_Cash","http://dbpedia.org/ontology/birthPlace","http://rdf.freebase.com/ns/m.0fw2y");
        PropertyConfiguration p59 = new PropertyConfiguration("http://dbpedia.org/resource/Gottfried_Wilhelm_Leibniz","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Kingsland,_Arkansas");
        PropertyConfiguration p60 = new PropertyConfiguration("http://rdf.freebase.com/ns/m.06y9c2","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Kingsland,_Arkansas");
        PropertyConfiguration p61 = new PropertyConfiguration("http://dbpedia.org/resource/Serena_Williams","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Saginaw,_Michigan");
        PropertyConfiguration p62 = new PropertyConfiguration("http://dbpedia.org/resource/Serena_Williams","http://dbpedia.org/ontology/birthPlace","http://rdf.freebase.com/ns/m.02_286");
        PropertyConfiguration p63 = new PropertyConfiguration("http://dbpedia.org/resource/James_Buchanan","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Saginaw,_Michigan");
        PropertyConfiguration p64 = new PropertyConfiguration("http://dbpedia.org/resource/Serena_Williams","http://dbpedia.org/ontology/birthPlace","http://rdf.freebase.com/ns/m.01jr6");
        PropertyConfiguration p65 = new PropertyConfiguration("http://dbpedia.org/resource/Serena_Williams","http://dbpedia.org/ontology/birthPlace","http://rdf.freebase.com/ns/m.0154j");
        PropertyConfiguration p66 = new PropertyConfiguration("http://dbpedia.org/resource/Daniel_Nestor","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Belgrade");
        PropertyConfiguration p67 = new PropertyConfiguration("http://dbpedia.org/resource/J._R._Smith","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Belgrade");
        PropertyConfiguration p68 = new PropertyConfiguration("http://rdf.freebase.com/ns/m.0202l6","http://dbpedia.org/ontology/birthPlace","http://dbpedia.org/resource/Belgrade");

        cacheEvaluation.add(p1);
        cacheEvaluation.add(p2);
        cacheEvaluation.add(p3);
        cacheEvaluation.add(p4);
        cacheEvaluation.add(p5);
        cacheEvaluation.add(p6);
        cacheEvaluation.add(p7);
        cacheEvaluation.add(p8);
        cacheEvaluation.add(p9);
        cacheEvaluation.add(p10);
        cacheEvaluation.add(p11);
        cacheEvaluation.add(p12);
        cacheEvaluation.add(p13);
        cacheEvaluation.add(p14);
        cacheEvaluation.add(p15);
        cacheEvaluation.add(p16);
        cacheEvaluation.add(p17);
        cacheEvaluation.add(p18);
        cacheEvaluation.add(p19);
        cacheEvaluation.add(p20);
        cacheEvaluation.add(p21);
        cacheEvaluation.add(p22);
        cacheEvaluation.add(p23);
        cacheEvaluation.add(p24);
        cacheEvaluation.add(p25);
        cacheEvaluation.add(p26);
        cacheEvaluation.add(p27);
        cacheEvaluation.add(p28);
        cacheEvaluation.add(p29);
        cacheEvaluation.add(p30);
        cacheEvaluation.add(p31);
        cacheEvaluation.add(p32);
        cacheEvaluation.add(p33);
        cacheEvaluation.add(p34);
        cacheEvaluation.add(p35);
        cacheEvaluation.add(p36);
        cacheEvaluation.add(p37);
        cacheEvaluation.add(p38);
        cacheEvaluation.add(p39);
        cacheEvaluation.add(p40);
        cacheEvaluation.add(p41);
        cacheEvaluation.add(p42);
        cacheEvaluation.add(p43);
        cacheEvaluation.add(p44);
        cacheEvaluation.add(p45);
        cacheEvaluation.add(p46);
        cacheEvaluation.add(p47);
        cacheEvaluation.add(p48);
        cacheEvaluation.add(p49);
        cacheEvaluation.add(p50);
        cacheEvaluation.add(p51);
        cacheEvaluation.add(p52);
        cacheEvaluation.add(p53);
        cacheEvaluation.add(p54);
        cacheEvaluation.add(p55);
        cacheEvaluation.add(p56);
        cacheEvaluation.add(p57);
        cacheEvaluation.add(p58);
        cacheEvaluation.add(p59);
        cacheEvaluation.add(p60);
        cacheEvaluation.add(p61);
        cacheEvaluation.add(p62);
        cacheEvaluation.add(p63);
        cacheEvaluation.add(p64);
        cacheEvaluation.add(p65);
        cacheEvaluation.add(p66);
        cacheEvaluation.add(p67);
        cacheEvaluation.add(p68);

    }

    /**
     *
     * @param m1 the first model
     * @param m2 the second model
     * @param sURI the subject URI of the selected subject
     * @param oURI the object URI of the selected object
     * @return Model
     */
    private static DefactoModel mixModels(Model m1, Model m2, String sURI, String oURI, int index) throws Exception {

        LOGGER.info(":: starting the jena model generation");

        Model m = ModelFactory.createDefaultModel();

        //Map<String, String> pref = m1.getNsPrefixMap();

        m.setNsPrefix("fbase", "http://rdf.freebase.com/ns");
        m.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        m.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
        m.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        m.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        m.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");

        RDFNode auxObject = null;
        Property auxPredicate = null;

        DefactoModel finalModel;


        if (getWhichPartFromRandom.equals("O")) {
            //O from M2
            StmtIterator listIter2 = m2.listStatements();
            while (listIter2.hasNext()) {
                Statement stmt2 = listIter2.next();
                if (stmt2.getSubject().getURI().equals(oURI)) {
                    m.add(stmt2);
                    auxObject = stmt2.getSubject();
                }
            }
            //S and P updated from M1
            StmtIterator listIter1 = m1.listStatements();
            while (listIter1.hasNext()) {
                Statement stmt = listIter1.next();
                //S
                if (stmt.getSubject().getURI().equals(sURI)) {
                    m.add(stmt);
                    if (stmt.getObject().isResource() && stmt.getObject().asResource().getURI().matches("^.*__[0-9]*$")) {
                        auxPredicate = stmt.getPredicate(); //just to naming and save the file correctly
                    }
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
                if (stmt.getSubject().getURI().equals(oURI) || stmt.getSubject().getURI().matches("^.*__[0-9]*$")) {
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
                if (stmt2.getSubject().getURI().equals(sURI)){
                    if (stmt2.getObject().isResource() && stmt2.getObject().asResource().getURI().matches("^.*__[0-9]*$")) {
                        m.add(stmt2.getSubject(), auxPredicate, auxObject);
                    }else{
                        m.add(stmt2);
                    }
                }
            }
        }

        newRandomFileName = "C:\\DNE5\\github\\DeFacto\\data\\factbench\\v1_2013\\test\\unknown\\out_" + auxPredicate.getLocalName() + "_" + index + ".ttl";
        FileWriter out = new FileWriter(newRandomFileName);
        m.write(out, "TTL");

        LOGGER.info(":: New file model has been generated: " + newRandomFileName);

        //setting DeFacto Model
        finalModel = new DefactoModel(m, "Unknown " + index, false, Arrays.asList("en", "de", "fr"));

        return finalModel;

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
    private static DefactoModel mixModelsUp(DefactoModel original, DefactoModel random, int index) throws Exception{


        String subjectURI = original.getSubjectUri();
        String objectURI = original.getObjectUri();
        newRandomFileName= "";

        //DefactoModel finalModel = (DefactoModel) original.clone();
        DefactoModel finalModel;

        if (original == null || random ==null){
            throw new Exception("null error at model´s level (original / random)");
        }

        LOGGER.info("Default Model: [" + original.getSubjectLabel("en") + "] [" + original.getPredicate() + "] [" + original.getObjectLabel("en") + "]");
        LOGGER.info("Random Model: [" + random.getSubjectLabel("en") + "] [" + random.getPredicate() + "] [" + random.getObjectLabel("en") + "]");

        if (resourceToBeChanged.equals("S") && getWhichPartFromRandom.equals("S")){
            LOGGER.info("S1: Swapping the subject from [" + original.getSubjectLabel("en") + "] to [" + random.getSubject().getLabel("en") + "]");
            subjectURI = random.getSubjectUri();
            //finalModel.setSubject(random.getSubject());
        }
        else if (resourceToBeChanged.equals("O") && (getWhichPartFromRandom.equals("S"))){
            LOGGER.info("S2: Swapping the object from [" + original.getObjectLabel("en") + "] to [" + random.getSubject().getLabel("en") + "]");
            objectURI = random.getSubjectUri();
            //finalModel.setObject(random.getSubject());
        }
        else if (resourceToBeChanged.equals("S") && getWhichPartFromRandom.equals("O")){
            LOGGER.info("S3: Swapping the subject from [" + original.getSubjectLabel("en") + "] to [" + random.getObject().getLabel("en") + "]");
            subjectURI = random.getObjectUri();
            //finalModel.setSubject(random.getObject());
        }
        else if (resourceToBeChanged.equals("O") && getWhichPartFromRandom.equals("O")){
            LOGGER.info("S4: Swapping the object from [" + original.getObjectLabel("en") + "] to [" + random.getObject().getLabel("en") + "]");
            objectURI = random.getObjectUri();
            //finalModel.setObject(random.getObject());
        }
        else{
            throw new Exception("Strange...it should not happen :/ getWhichPart = " + getWhichPartFromRandom);
        }


        finalModel = mixModels(original.model, random.model, subjectURI, objectURI, index);

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
        return finalModel;
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

}
