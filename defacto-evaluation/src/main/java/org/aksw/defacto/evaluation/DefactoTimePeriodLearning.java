/**
 * 
 */
package org.aksw.defacto.evaluation;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.Defacto.TIME_DISTRIBUTION_ONLY;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.model.DefactoTimePeriod;
import org.aksw.defacto.reader.DefactoModelReader;
import org.apache.solr.common.util.Hash;

import com.github.gerbsen.time.TimeUtil;

/**
 * @author Daniel Gerber <daniel.gerber@deinestadtsuchtdich.de>
 *
 */
public class DefactoTimePeriodLearning {

	public static void main(String[] args) throws FileNotFoundException {
		
//		Set<Integer> relevant = new HashSet<>();
//		relevant.add(4);
//		relevant.add(5);
//		relevant.add(6);
//		
//		Set<Integer> retrieved = new HashSet<>();
//		
//		System.out.println(new PrecisionRecallFMeasure(relevant, retrieved));
		
		
		startEvaluation();
	}
	
	private static PrecisionRecallFMeasure getResults(DefactoTimePeriod defactoTimePeriod, DefactoModel model) {
		
		Set<Integer> relevantYears = new LinkedHashSet<>();
		for ( int i = model.timePeriod.from ; i <= model.timePeriod.to ; i++) relevantYears.add(i);
		
		Set<Integer> retrievedYears = new LinkedHashSet<>();
		if ( !defactoTimePeriod.equals(DefactoTimePeriod.EMPTY_DEFACTO_TIME_PERIOD) )
			for ( int i = defactoTimePeriod.from ; i <= defactoTimePeriod.to ; i++) 
				retrievedYears.add(i);
		
		return new PrecisionRecallFMeasure(relevantYears, retrievedYears);
	}

	public static class PrecisionRecallFMeasure {
		
		public Double precision;
		public Double recall;
		public Double fmeasure;
		
		public PrecisionRecallFMeasure(Set<Integer> relevantYears, Set<Integer> retrievedYears) {
			
			Set<Integer> intersection = new HashSet<>(relevantYears); // use the copy constructor
			intersection.retainAll(retrievedYears);
			
			if ( retrievedYears.size() != 0 ) precision = (double) intersection.size() / (double) retrievedYears.size();
			else precision = Double.NaN;
			
			if ( relevantYears.size() != 0 ) recall = (double) intersection.size() / (double) relevantYears.size();
			else recall = 0D;
			
			if ( (precision + recall) == 0 ) fmeasure = 0D;
			else fmeasure = (2*precision*recall) / (precision + recall);
		}

		@Override
		public String toString(){
			
			return String.format(Locale.ENGLISH, "P: %.3f, R: %.3f, F: %.3f", precision, recall, fmeasure);
		}
	}
	
	static void updateProgress(double progressPercentage) {
		final int width = 100; // progress bar width in chars

		System.out.print("\r[");
		int i = 0;
		for (; i <= (int) (progressPercentage * width); i++) {
			System.out.print(".");
		}
		for (; i < width; i++) {
			System.out.print(" ");
		}
		System.out.print("]");
	}
	
	public static void startEvaluation() throws FileNotFoundException{
		
		Defacto.init();
		
		List<String> languages 		= Arrays.asList("de", "fr", "en");
		List<DefactoModel> models	= new ArrayList<>();
		String trainDirectory		= Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") 
										+ Defacto.DEFACTO_CONFIG.getStringSetting("eval", "train-directory");
		// TIME POINTS
		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/award", true, languages));
		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/birth", true, languages));
		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/death", true, languages));
		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/foundationPlace", true, languages));
		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/publicationDate", true, languages));
		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/starring", true, languages));
		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/subsidiary", true, languages));
		
		// TIME PERIODS
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/spouse", true, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/nbateam", true, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/leader", true, languages));
		
		learn(models);
		
//		System.out.println();
//		for ( Entry<Comparable<?>, Long> sortByValue : org.aksw.defacto.search.time.TimeUtil.allYearsAndTimePeriod.sortByValue()) {
//			
//			System.out.println(sortByValue.getKey()+  ": " + sortByValue.getValue());
//		}
//		
//		System.out.println();
//		for ( Entry<Comparable<?>, Long> sortByValue : org.aksw.defacto.search.time.TimeUtil.allYears.sortByValue()) {
//			
//			System.out.println(sortByValue.getKey()+  ": " + sortByValue.getValue());
//		}
	}
	
	public static void learn(List<DefactoModel> models){
		
		int precisionCounter = 0;
		
		Double macroPrecision	= 0D;
		Double macroRecall		= 0D;
		Double macroFmeasure	= 0D;
		
		for (int i = 0; i < models.size(); i++) {

			DefactoModel model = models.get(i); 
			DefactoTimePeriod defactoTimePeriod = Defacto.checkFact(model, TIME_DISTRIBUTION_ONLY.YES).defactoTimePeriod;
			
			PrecisionRecallFMeasure results = getResults(defactoTimePeriod, model);
			
			if ( !results.precision.isNaN() ) { 
				
				macroPrecision += results.precision;
				precisionCounter++;
			}
			macroRecall += results.recall;
			macroFmeasure = getFmeasure(macroPrecision / precisionCounter, macroRecall / (i+1));
			
			System.out.println(String.format(Locale.ENGLISH, "total --> P: %.5f, R: %.5f, F: %.5f", 
					macroPrecision / (precisionCounter), macroRecall / (i + 1), macroFmeasure) + 
					" current --> " + results);
		}
	}
	
	public static Double getFmeasure(Double precision, Double recall) {
		
		if ( precision + recall == 0) return 0D;
		return (2*precision*recall) / (precision + recall);
	}
}
