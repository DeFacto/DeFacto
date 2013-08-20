package org.aksw.defacto.ml.score;

import java.io.File;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.AbstractFeature;
import org.aksw.defacto.ml.feature.fact.FactScorer;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class EvidenceScorer {

    private Logger logger = Logger.getLogger(EvidenceScorer.class);
    
    private String pathToClassifier     = new File(FactScorer.class.getResource("/classifier/evidence/" + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE") + ".model").getFile()).getAbsolutePath();
//    private String pathToEvaluation     = "resources/classifier/evidence/" + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE") + ".eval.model";
//    private String pathToTrainingData   = Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_TRAINING_DATA_FILENAME");
    
    private Classifier classifier;
    
    /**
     * 
     */
    public EvidenceScorer() {
        
        if ( new File(pathToClassifier).exists() && !Defacto.DEFACTO_CONFIG.getBooleanSetting("evidence", "OVERWRITE_EVIDENCE_TRAINING_FILE") ) {
            
            logger.info("Loading machine learning model: " + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE"));
            this.classifier = this.loadClassifier();
        }
        else {
        	
        	throw new RuntimeException("No classifier at: " + pathToClassifier);
//            
//            logger.info("Train classifier: " + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE"));
//            this.classifier = this.trainClassifier();
        }
    }
    
//    /**
//     * 
//     * @return
//     */
//    private Classifier trainClassifier() {
//
//        String errorMessage = "Could not train classifier: " + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE") + " from: " + pathToClassifier + " with the training file: " + this.pathToTrainingData;
//        
//        try {
//
//            Classifier classifier;
//            if ( Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE").equals("LINEAR_REGRESSION_CLASSIFIER") ) 
//                classifier = new LinearRegression();
//            else if ( Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE").equals("MULTILAYER_PERCEPTRON") )
//                classifier = new MultilayerPerceptron();
//            else if ( Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE").equals("SMO") ) {
//                
//                classifier = new SMO();
//                ((SMO) classifier).setBuildLogisticModels(true);
//            }
//            else
//                classifier = new NaiveBayes(); // fallback
//
//            // train
//            Instances inst = new Instances(new BufferedReader(new FileReader(pathToTrainingData)));
//            Remove remove = new Remove();                         // new instance of filter to remove model name  (SMO cant handle strings)
//            remove.setOptions(new String[] {"-R","1"});           // set options, remove first attribute
//            remove.setInputFormat(inst);                          // inform filter about dataset **AFTER** setting options
//            Instances filteredInst = Filter.useFilter(inst, remove);                // appply filter
//            filteredInst.setClassIndex(filteredInst.numAttributes() - 1);         // class index is the last on in the list
//            classifier.buildClassifier(filteredInst);
//
//            // eval
////            Evaluation evaluation = new Evaluation(inst);
////            evaluation.crossValidateModel(classifier, inst, 2, new Random(1));
//            
//            // write eval
//            FileOutputStream fos = new FileOutputStream(pathToEvaluation);
////            fos.write(evaluation.toSummaryString().getBytes());
//            fos.flush();
//            fos.close();
//            
//            // serialize model
//            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(pathToClassifier));
//            oos.writeObject(classifier);
//            oos.flush();
//            oos.close();
//            
//            return classifier;
//        }
//        catch (FileNotFoundException e) {
//            
//            throw new RuntimeException(errorMessage, e);
//        }
//        catch (IOException e) {
//            
//            throw new RuntimeException(errorMessage, e);
//        }
//        catch (Exception e) {
//            
//            throw new RuntimeException(errorMessage, e);
//        }
//    }

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

            // this gives us the probability distribution for an input triple
            // [0.33, 0.66] means it's 33% likely to be true and 66% likely to be false
            // we are only interested in the true value
            evidence.setDeFactoScore(this.classifier.distributionForInstance(newInstance)[0]);
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }
}
