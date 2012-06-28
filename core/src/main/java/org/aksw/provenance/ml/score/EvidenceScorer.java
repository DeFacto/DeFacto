package org.aksw.provenance.ml.score;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.concurrent.Callable;

import org.aksw.provenance.Constants;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.ml.feature.AbstractFeature;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.RemoveType;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class EvidenceScorer {

    private Logger logger = Logger.getLogger(EvidenceScorer.class);
    
    private String pathToClassifier     = "resources/classifier/" + Constants.CLASSIFIER_FUNCTION + ".model";
    private String pathToEvaluation     = "resources/classifier/" + Constants.CLASSIFIER_FUNCTION + ".eval.model";
    private String pathToTrainingData   = "resources/training/" + Constants.TRAINING_DATA_FILENAME;
    
    private Classifier classifier;
    
    /**
     * 
     */
    public EvidenceScorer() {
        
        if ( new File(pathToClassifier).exists() && !Constants.OVERRIDE_CURRENT_MODEL ) {
            
            logger.info("Loading machine learning model: " + Constants.CLASSIFIER_FUNCTION);
            this.classifier = this.loadClassifier();
        }
        else {
            
            logger.info("Train classifier: " + Constants.CLASSIFIER_FUNCTION);
            this.classifier = this.trainClassifier();
        }
    }
    
    /**
     * 
     * @return
     */
    private Classifier trainClassifier() {

        String errorMessage = "Could not train classifier: " + Constants.CLASSIFIER_FUNCTION + " from: " + pathToClassifier + " with the training file: " + this.pathToTrainingData;
        
        try {

            Classifier classifier;
            if ( Constants.CLASSIFIER_FUNCTION.equals(Constants.LINEAR_REGRESSION_CLASSIFIER) ) 
                classifier = new LinearRegression();
            else if ( Constants.CLASSIFIER_FUNCTION.equals((Constants.MULTILAYER_PERCEPTRON)) )
                classifier = new MultilayerPerceptron();
            else if ( Constants.CLASSIFIER_FUNCTION.equals((Constants.SMO)) ) {
                
                classifier = new SMO();
                ((SMO) classifier).setBuildLogisticModels(true);
            }
            else
                classifier = new NaiveBayes(); // fallback

            // train
            Instances inst = new Instances(new BufferedReader(new FileReader(pathToTrainingData)));
            Remove remove = new Remove();                         // new instance of filter to remove model name  (SMO cant handle strings)
            remove.setOptions(new String[] {"-R","1"});           // set options, remove first attribute
            remove.setInputFormat(inst);                          // inform filter about dataset **AFTER** setting options
            Instances filteredInst = Filter.useFilter(inst, remove);                // appply filter
            filteredInst.setClassIndex(filteredInst.numAttributes() - 1);         // class index is the last on in the list
            classifier.buildClassifier(filteredInst);

            // eval
//            Evaluation evaluation = new Evaluation(inst);
//            evaluation.crossValidateModel(classifier, inst, 2, new Random(1));
            
            // write eval
            FileOutputStream fos = new FileOutputStream(pathToEvaluation);
//            fos.write(evaluation.toSummaryString().getBytes());
            fos.flush();
            fos.close();
            
            // serialize model
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(pathToClassifier));
            oos.writeObject(classifier);
            oos.flush();
            oos.close();
            
            return classifier;
        }
        catch (FileNotFoundException e) {
            
            throw new RuntimeException(errorMessage, e);
        }
        catch (IOException e) {
            
            throw new RuntimeException(errorMessage, e);
        }
        catch (Exception e) {
            
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * 
     * @return
     */
    private Classifier loadClassifier() {

        try {
            
            return (Classifier) weka.core.SerializationHelper.read(pathToClassifier);
        }
        catch (Exception e) {

            throw new RuntimeException("Could not load classifier from: " + pathToClassifier, e);
        }
    }

    /**
     * 
     * @param evidence
     * @return
     */
    public void scoreEvidence(Evidence evidence) {

        try {
            
            Instances withoutName = new Instances(AbstractFeature.provenance);
            withoutName.setClassIndex(withoutName.numAttributes() - 1);
            withoutName.deleteStringAttributes();
            
            Instance newInstance = new Instance(evidence.getFeatures());
            newInstance.deleteAttributeAt(1);
            newInstance.setDataset(withoutName);

            evidence.setDeFactoScore(this.classifier.classifyInstance(newInstance));
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }
}
