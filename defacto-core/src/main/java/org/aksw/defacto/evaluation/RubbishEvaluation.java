package org.aksw.defacto.evaluation;

/**
 * Created by dnes on 04/01/16.
 */

import org.aksw.defacto.Defacto;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.model.PropertyConfiguration;
import org.aksw.defacto.model.PropertyConfigurationSingleton;
import org.aksw.defacto.reader.DefactoModelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A naive strategy to generate metadata for correct triples x changed ones
 */
public class RubbishEvaluation {

    private static final Logger LOGGER = LoggerFactory.getLogger(RubbishEvaluation.class);
    private static String getWhichPartFromRandom = "";
    private static String resourceToBeChanged;
    private static PrintWriter writer;
    private static int totalFilesProcessed = 0;
    private static int totalFilesToBeProcessed = 0;
    private static int totalFoldersToBeProcessed = 0;

    public static void main(String[] args) throws Exception {

        long startTimeOverallProcess = System.currentTimeMillis();

        Defacto.init();

        writer = new PrintWriter("rubbish3.txt", "UTF-8");
        writer.println("score;model;subject;predicate;object;model_type;random_source_folder;random_source_file");

        LOGGER.info("Starting the process");

        List<String> languages = Arrays.asList("en");
        //the list of true models
        List<DefactoModel> models = new ArrayList<>();
        //the aux models that will be used to generate different models (changing S) based on the same model
        List<DefactoModel> modelsRandom = new ArrayList<>();
        //the number of models to be computed
        int nrModelsToCompare = 4;
        //the number of models for each property (folder)
        int sizePropertyFolder = 0;

        String trainDirectory = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory")
                + Defacto.DEFACTO_CONFIG.getStringSetting("eval", "train-directory") + "correct/";

        LOGGER.info("Selecting folders which contain FP...");

        File files = new File(trainDirectory);
        //getting all folders (properties)
        ArrayList<File> folders = new ArrayList<>();
        for(File f : files.listFiles()){
            //this test must be performed only for FP
            if (f.isDirectory() && isFunctional(f.getName())){
                folders.add(f); totalFoldersToBeProcessed++;
                LOGGER.info("Folder '" + f.getName() + "' has been selected for evaluation");
            }
        }
        LOGGER.info("Total of folders to be processed: " + String.valueOf(totalFoldersToBeProcessed));

        LOGGER.info("------------------------------------------------------------------------------------------------------------");

        //start the process for each property (folder)
        for(File currentFolder : folders){

                models.clear();

                sizePropertyFolder = currentFolder.listFiles().length;
                LOGGER.info("Reading the folder : '" + currentFolder.getName() + "' which contains " + sizePropertyFolder + " models (files)");

                //add all the models which presents functional property then we can deal with exclusions
                models.addAll(DefactoModelReader.readModels(currentFolder.getAbsolutePath(), true, languages));

                LOGGER.info(models.size() + " models has been added!");

                //this folder will be used to collect new (random) resources
                File randomFolder = getRandomProperty(folders, currentFolder, true);

                //starting computation for each model
                for (int i=0; i<models.size(); i++){

                    totalFilesProcessed++;

                    LOGGER.info("Folder '" + randomFolder.getName() + "' has been selected as source for '" + currentFolder.getName() + "'");

                    //get inside the selected folder, N models
                    ArrayList<File> selectedFiles = getNRandomFiles(nrModelsToCompare, randomFolder);

                    //add all random selected models
                    for (int j=0; j<selectedFiles.size(); j++){
                        modelsRandom.add(DefactoModelReader.readModel(selectedFiles.get(j).getAbsolutePath()));
                        LOGGER.info("File " + selectedFiles.get(j).getAbsolutePath() + " has been selected as source for " + currentFolder.getName());
                    }

                    LOGGER.info("Main Model: Starting DeFacto for [" + models.get(i).getSubjectLabel("en") +
                            "] [" + models.get(i).getPredicate().getLocalName() + "] [" + models.get(i).getObjectLabel("en") + "]");

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

                    LOGGER.info("Starting to compute random models...");

                    //compute the scores for aux models
                    for ( int m = 0; m < modelsRandom.size() ; m++ ) {

                        DefactoModel tempModel = models.get(i);
                        tempModel.setName("temp" + m);

                        LOGGER.info("Random Model: [" + modelsRandom.get(m).getSubjectLabel("en") + "] [" + modelsRandom.get(m).getPredicate() + "] [" + modelsRandom.get(m).getObjectLabel("en") + "]");

                        if (resourceToBeChanged.equals("S") && getWhichPartFromRandom.equals("S")){
                            LOGGER.info("S1: Swapping the subject from [" + tempModel.getSubjectLabel("en") + "] to [" + modelsRandom.get(m).getSubject().getLabel("en") + "]");
                            tempModel.setSubject(modelsRandom.get(m).getSubject());
                        }
                        else if (resourceToBeChanged.equals("O") && (getWhichPartFromRandom.equals("S"))){
                            LOGGER.info("S2: Swapping the object from [" + tempModel.getObjectLabel("en") + "] to [" + modelsRandom.get(m).getSubject().getLabel("en") + "]");
                            tempModel.setObject(modelsRandom.get(m).getSubject());
                        }
                        else if (resourceToBeChanged.equals("S") && getWhichPartFromRandom.equals("O")){
                            LOGGER.info("S3: Swapping the subject from [" + tempModel.getSubjectLabel("en") + "] to [" + modelsRandom.get(m).getObject().getLabel("en") + "]");
                            tempModel.setSubject(modelsRandom.get(m).getObject());

                        }else if (resourceToBeChanged.equals("O") && getWhichPartFromRandom.equals("O")){
                            LOGGER.info("S4: Swapping the object from [" + tempModel.getObjectLabel("en") + "] to [" + modelsRandom.get(m).getObject().getLabel("en") + "]");
                            tempModel.setObject(modelsRandom.get(m).getObject());
                        }else{
                            throw new Exception("Strange...it should not happen :/ getWhichPart = " + getWhichPartFromRandom);
                        }

                        LOGGER.info("Random: Starting DeFacto for [" + tempModel.getSubjectLabel("en") +
                                     "] [" + tempModel.getPredicate().getLocalName() + "] [" + tempModel.getObjectLabel("en") + "]");
                        //compute the score for main model

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
                                        ";" + modelsRandom.get(m).name);

                        LOGGER.info("Changed Model '" + tempModel.getName() + "' has been processed");
                        writer.flush();
                    }

                    //clear the selected files
                    modelsRandom.clear();
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

        LOGGER.info("Done! " + totalFilesProcessed + " were processed");
        LOGGER.info("Processing Time: " + String.valueOf((elapsedTime / 1000) * 60) + " minutes");
    }

    private static boolean isFunctional(String propertyName) {
        return PropertyConfigurationSingleton.getInstance().getConfigurations().get(propertyName).isFunctionalProperty();
    }

    private static ArrayList<DefactoModel> getRandomModels(){

        ArrayList<DefactoModel> models = new ArrayList<>();

        return models;

    }

    /***
     * returns a random folder (property) of FactBench to be used as source to the new model generation process based on the structure of the provided model
      * @param current
     * @return
     */
    private static File getRandomProperty(ArrayList<File> folders, File current, boolean onlyFuncional) throws Exception{
        File randomFolder = current;
        int randomM;
        boolean allowed = false;
        getWhichPartFromRandom = "";
        ArrayList<Integer> selected = new ArrayList<>();
        PropertyConfiguration cCurrent, cRandom;

        if (folders.size() > 1) {

            cCurrent = PropertyConfigurationSingleton.getInstance().getConfigurations().get(current.getName());

            while ((randomFolder.equals(current)) && selected.size() < folders.size()) {

                Random rand = new Random();
                randomM = rand.nextInt(folders.size());
                randomFolder = folders.get(randomM);
                if (!selected.contains(randomM)) {
                    selected.add(randomM);
                }
                //check whether current and chosen share the same configuration
                if (!PropertyConfigurationSingleton.getInstance().getConfigurations().containsKey(randomFolder.getName()))
                    throw new Exception("Configuration has not been found -> " + randomFolder.getName());

                if (!PropertyConfigurationSingleton.getInstance().getConfigurations().containsKey(current.getName()))
                    throw new Exception("Configuration has not been found -> " + current.getName());


                if (!current.getName().equals(randomFolder.getName())) {
                    if (onlyFuncional && isFunctional(randomFolder.getName())){

                        cRandom = PropertyConfigurationSingleton.getInstance().getConfigurations().get(randomFolder.getName());

                        if (cCurrent.getResourceToBeChangedForRubbish().equals("S")){
                            if (cCurrent.getSubjectClass().equals(cRandom.getSubjectClass())) {
                                allowed = true;
                                getWhichPartFromRandom = "S";
                            } else if (cCurrent.getSubjectClass().equals(cRandom.getObjectClass())) {
                                allowed = true;
                                getWhichPartFromRandom = "O";
                            }
                        }else if (cCurrent.getResourceToBeChangedForRubbish().equals("O")) {
                            if (cCurrent.getObjectClass().equals(cRandom.getSubjectClass())) {
                                allowed = true;
                                getWhichPartFromRandom = "S";
                            } else if (cCurrent.getObjectClass().equals(cRandom.getObjectClass())) {
                                allowed = true;
                                getWhichPartFromRandom = "O";
                            }
                        }

                        //checking constraints

                        resourceToBeChanged = cCurrent.getResourceToBeChangedForRubbish();

                        if (!allowed) {
                            randomFolder = current;
                        }
                    }
                }
            }

            if (randomFolder.equals(current)) {
                //no further options have been selected, rely on the same folder, which is kind of senseless (although the last option for desired task)
                getWhichPartFromRandom = cCurrent.getResourceToBeChangedForRubbish();
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
