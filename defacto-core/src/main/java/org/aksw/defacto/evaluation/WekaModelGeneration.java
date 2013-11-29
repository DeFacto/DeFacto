/**
 * 
 */
package org.aksw.defacto.evaluation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * @author Daniel Gerber <daniel.gerber@deinestadtsuchtdich.de>
 * 
 */
public class WekaModelGeneration {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// test();
//		startEval();
		startMLEval();
	}

	private static void test() throws Exception {

		Instances train = new Instances(new BufferedReader(new FileReader("/Users/gerb/Development/workspaces/experimental/property_train.arff")));
		Instances test = new Instances(new BufferedReader(new FileReader("/Users/gerb/Development/workspaces/experimental/property_test.arff")));
		train.setClassIndex(15);
		test.setClassIndex(15);

		Classifier cls = new J48();
		cls.buildClassifier(train);
		Evaluation eval = new Evaluation(train);
		eval.evaluateModel(cls, test);

		System.out.println(eval.toSummaryString());
		System.out.println(eval.weightedFMeasure());
	}

	private static void startEval() throws Exception {
		
		List<Classifier> classifier = new ArrayList<>();
		classifier.add(new J48());
		classifier.add(new SimpleLogistic());
		classifier.add(new NaiveBayes());
		classifier.add(new SMO());
		
		int j = 0;
		for ( String[] sets : Arrays.asList(new String[]{"domain", "range"}, new String[]{"domainRange", "property"}, new String[]{"random", "mix"})) {
			
			String finalOutput = "";
			if ( j++ > 1 ) finalOutput = "\\rule{0pt}{3ex}";
			finalOutput = "~ & \\multicolumn{6}{c}{"+StringUtils.capitalize(sets[0])+"} & \\phantom{a} & \\multicolumn{6}{c}{"+StringUtils.capitalize(sets[1])+"}\\\\\n"; 
			finalOutput += "~ & C & P & R & F$_1$ & AUC 	& RMSE 	& & C & P 		& R 		& F$_1$ & AUC		& RMSE \\\\\n";
			finalOutput += "\\cmidrule{2-7} \\cmidrule{9-14}\\\\\n";
			
			for (Classifier cls : classifier) {
				
				List<String> output = new ArrayList<>();
				output.add(cls.getClass().getSimpleName());
				
				for (int i = 0; i < sets.length ; i++) {
					
					String set = sets[i];
					
					DataSource trainDataset = new DataSource("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/machinelearning/eval/evidence/v5/train/"+set.toLowerCase()+".arff");
					DataSource testDataset = new DataSource("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/machinelearning/eval/evidence/v5/test/"+set.toLowerCase()+".arff");
					
					Instances train = trainDataset.getDataSet();
					Instances test = testDataset.getDataSet();
					
					Remove removeTrain = new Remove(); 
				    removeTrain.setAttributeIndices("1");
				    removeTrain.setInvertSelection(false);
				    removeTrain.setInputFormat(train);
				    train = Filter.useFilter(train, removeTrain);
				    
				    Remove removeTest = new Remove(); 
				    removeTest.setAttributeIndices("1");
				    removeTest.setInvertSelection(false);
				    removeTest.setInputFormat(test);
				    test = Filter.useFilter(test, removeTest);
					
				    train.setClassIndex(15);
					test.setClassIndex(15);
					
					cls.buildClassifier(train);
					Evaluation eval = new Evaluation(train);
					eval.evaluateModel(cls, test);
					
					output.add(String.format(Locale.ENGLISH, "%.1f\\%%", eval.pctCorrect()));
					output.add(String.format(Locale.ENGLISH, "%.3f", eval.weightedPrecision()));
					output.add(String.format(Locale.ENGLISH, "%.3f", eval.weightedRecall()));
					output.add(String.format(Locale.ENGLISH, "%.3f", eval.weightedFMeasure()));
					output.add(String.format(Locale.ENGLISH, "%.3f", eval.weightedAreaUnderROC()));
					output.add(String.format(Locale.ENGLISH, "%.3f", eval.rootMeanSquaredError()));
					if ( i == 0 ) output.add(" ");
				}
				String join = StringUtils.join(output, "\t&\t");
				
				finalOutput += join + "\t\\\\\n";
			}
			
			System.out.println(finalOutput + "\\\\");
		}
	}
		
		
		private static void startMLEval() throws Exception {
			
			List<Classifier> classifier = new ArrayList<>();
			classifier.add(new J48());
			classifier.add(new SimpleLogistic());
			classifier.add(new NaiveBayes());
			classifier.add(new SMO());
			
			for (Classifier cls : classifier) {
				
				List<String> output = new ArrayList<>();
				output.add(cls.getClass().getSimpleName());
				
				DataSource trainDataset = new DataSource("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/machinelearning/eval/evidence/v6/train/mix.arff");
				DataSource testDataset = new DataSource("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/machinelearning/eval/evidence/v6/test/mix.arff");
				
				Instances train = trainDataset.getDataSet();
				Instances test = testDataset.getDataSet();
				
				Remove removeTrain = new Remove(); 
			    removeTrain.setAttributeIndices("1");
			    removeTrain.setInvertSelection(false);
			    removeTrain.setInputFormat(train);
			    train = Filter.useFilter(train, removeTrain);
			    
			    Remove removeTest = new Remove(); 
			    removeTest.setAttributeIndices("1");
			    removeTest.setInvertSelection(false);
			    removeTest.setInputFormat(test);
			    test = Filter.useFilter(test, removeTest);
				
			    train.setClassIndex(15);
				test.setClassIndex(15);
				
				cls.buildClassifier(train);
				Evaluation eval = new Evaluation(train);
				eval.evaluateModel(cls, test);
				
				for ( double[] arr : eval.confusionMatrix() )
					System.out.println(Arrays.toString(arr));
				
				output.add(String.format(Locale.ENGLISH, "%.1f\\%%", eval.pctCorrect()));
				output.add(String.format(Locale.ENGLISH, "%.3f", eval.weightedPrecision()));
				output.add(String.format(Locale.ENGLISH, "%.3f", eval.weightedRecall()));
				output.add(String.format(Locale.ENGLISH, "%.3f", eval.weightedFMeasure()));
				output.add(String.format(Locale.ENGLISH, "%.3f", eval.weightedAreaUnderROC()));
				output.add(String.format(Locale.ENGLISH, "%.3f", eval.rootMeanSquaredError()));
				String join = StringUtils.join(output, "\t&\t");
				
				System.out.println(join);
			}
	}
}
