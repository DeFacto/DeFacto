/**
 * 
 */
package org.aksw.defacto.evaluation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.Defacto.TIME_DISTRIBUTION_ONLY;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.ml.feature.evidence.DummyEvidenceScorer;
import org.aksw.defacto.ml.feature.evidence.EvidenceScorer;
import org.aksw.defacto.ml.feature.evidence.Scorer;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.reader.DefactoModelReader;
import org.aksw.defacto.util.Frequency;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Daniel Gerber <daniel.gerber@deinestadtsuchtdich.de>
 *
 */
public class EvidenceEvaluation {
	
	private static Classifier classifier = null; 
	private static Instances trainingInstances = null;
	private static Instances backupInstances = null;
	private static Map<Integer,Integer> correctYearMap = new TreeMap<>();
	private static Map<Integer,Integer> wrongYearMap = new TreeMap<>();
	
	public static void main(String[] args) throws Exception {
		
		Defacto.init();
		loadClassifier();
		initYearMap();
		Map<String,Integer> relationToCorrectCount = new TreeMap<>();
		trainingInstances = new Instances(new BufferedReader(new FileReader(DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "ARFF_EVIDENCE_TRAINING_DATA_FILENAME"))));
		backupInstances = new Instances(new BufferedReader(new FileReader(DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "ARFF_EVIDENCE_TRAINING_DATA_FILENAME"))));
		trainingInstances.setClassIndex(16);
		trainingInstances.deleteAttributeAt(0);
		
		System.out.println(String.format("Got %s instances!", 	trainingInstances.numInstances()));
		System.out.println(String.format("Got %s attributes!", 	trainingInstances.numAttributes()));
		System.out.println(String.format("Got %s classes!", 	trainingInstances.numClasses()));
		int sum = 0;
		
		Frequency freq = new Frequency();
		
		for ( int i = 0 ; i < trainingInstances.numInstances() ; i++) {
			
			Instance instance = trainingInstances.instance(i);
			String relation = backupInstances.instance(i).attribute(0).value(i);
			relation = relation.replaceAll("(train|test)/correct/", "").replaceAll("/.*", "");
//			System.out.println("REL: " +  relation);
			
			freq.addValue(classifier.distributionForInstance(instance)[0]);
			
			DefactoModel model = DefactoModelReader.readModel("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/FactBench/v1/" + backupInstances.instance(i).attribute(0).value(i));
			String averageYear = ((model.timePeriod.to + model.timePeriod.from) / 2) + "";
			averageYear = averageYear.substring(0, 3) + "0";
			if ( Integer.valueOf(averageYear) < 1900 ) averageYear = "1890";
			
			if ( classifier.distributionForInstance(instance)[0] > 0.5D ) {
				
				sum++;
				if ( !relationToCorrectCount.containsKey(relation) ) relationToCorrectCount.put(relation, 1);
				else relationToCorrectCount.put(relation, relationToCorrectCount.get(relation) + 1);
				
				correctYearMap.put(Integer.valueOf(averageYear), correctYearMap.get(Integer.valueOf(averageYear)) + 1);
			}
			else {
				
				wrongYearMap.put(Integer.valueOf(averageYear), wrongYearMap.get(Integer.valueOf(averageYear)) + 1);
//				System.out.println(model);
//				System.out.println(backupInstances.instance(i).attribute(0).value(i));
			}
		}
		
		for ( int i = 1890; i <= 2010 ; i = i+10 ) {

			Integer correct = correctYearMap.get(i);
			Integer wrong = wrongYearMap.get(i);
			Double ratio = (((wrong * 100) / (double)(wrong + correct)) * (wrong + correct)) / 3300 ;
			String year = i + "";
			if ( i == 1890 ) year = "< 1890";
			
			System.out.println(year + "\t&\t" + correct + "\t&\t" + wrong + "\t&\t" + String.format(Locale.ENGLISH, "%.1f", ratio)+ "\\\\");
		}
		
//		for ( Entry<Comparable<?>, Long> sortByValue : freq.sortByValue() ) {
//			
//			System.out.println(sortByValue.getKey() + "\t" +sortByValue.getValue());
//		}
		
//		System.out.println("All: " + sum);
//		for (Map.Entry<String, Integer> entry : relationToCorrectCount.entrySet() ){
//			
//			System.out.println(entry.getKey() + "\t" + entry.getValue());
//		}
	}

	private static void initYearMap() {
		
		for ( int i = 1890; i <= 2010 ; i = i+10 ) {
			
			correctYearMap.put(i, 0);
			wrongYearMap.put(i, 0);
		}
	}

	public static void evaluate(String trainOrTest) throws FileNotFoundException {
		
		Scorer scorer = new DummyEvidenceScorer();
		Map<String,Integer> relationToCorrectCount = new HashMap<>();
		
		String trainDirectory = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") 
				+ Defacto.DEFACTO_CONFIG.getStringSetting("eval", trainOrTest + "-directory");
		
		List<DefactoModel> models = DefactoModelReader.readModels(trainDirectory + "correct/", true, Arrays.asList("en", "de", "fr"));
		Collections.shuffle(models, new Random(100));
		
		for ( DefactoModel model : models) {
			
			Evidence evidence = Defacto.checkFact(model, TIME_DISTRIBUTION_ONLY.NO);
			scorer.scoreEvidence(evidence);
			
			if ( evidence.getDeFactoScore() >= 0.5 ) {
				
				if ( !relationToCorrectCount.containsKey(model.getPropertyUri()) ) relationToCorrectCount.put(model.getPropertyUri(), 1);
				else relationToCorrectCount.put(model.getPropertyUri(), relationToCorrectCount.get(model.getPropertyUri()) + 1);
			}
			
			for (Map.Entry<String, Integer> entry : relationToCorrectCount.entrySet() ){
				
				System.out.println(entry.getKey() + ": " + entry.getValue());
			}
		}
		
		for (Map.Entry<String, Integer> entry : relationToCorrectCount.entrySet() ){
			
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}

	private static void loadClassifier() {

        try {
            
            classifier = (Classifier) weka.core.SerializationHelper.read(
            		DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE"));
        }
        catch (Exception e) {

            throw new RuntimeException("Could not load classifier from: " + 
            		DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE"), e);
        }
    }
}
