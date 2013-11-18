package org.aksw.defacto.evaluation;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.reader.DefactoModelReader;
import org.aksw.defacto.util.BufferedFileReader;
import org.aksw.defacto.util.BufferedFileWriter;
import org.aksw.defacto.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.defacto.util.Encoder.Encoding;
import org.aksw.defacto.util.Frequency;

import edu.stanford.nlp.util.StringUtils;

public class GoldStandardInspection {

	public static void main(String[] args) throws FileNotFoundException {
		
		Defacto.init();
		
		List<String> languages = Arrays.asList("de", "fr", "en");
		String trainDirectory = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") 
				+ Defacto.DEFACTO_CONFIG.getStringSetting("eval", "train-directory");
		String testDirectory = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "FactBench/v1/test/";
		
		Frequency yearsFreq = new Frequency();
		
		for ( String path : Arrays.asList("award", "birth","death","foundationPlace","nbateam","publicationDate","spouse","starring","subsidiary","leader") ) {
			
			List<DefactoModel> modelsTrain = new ArrayList<>();
			modelsTrain.addAll(DefactoModelReader.readModels(trainDirectory + "correct/" + path, true, languages));
			
			Set<String> subjectsTrain = new HashSet<>();
			Set<String> objectsTrain = new HashSet<>();
			int minYearTrain = Integer.MAX_VALUE;
			int maxYearTrain = Integer.MIN_VALUE;
			int averageYearTrain = 0;
			
			for ( DefactoModel model : modelsTrain) {
				
				subjectsTrain.add(model.getSubjectUri());
				objectsTrain.add(model.getObjectUri());
				
				minYearTrain = Math.min(minYearTrain, model.timePeriod.from);
				maxYearTrain = Math.max(maxYearTrain, model.timePeriod.to);
				
				if ( model.timePeriod.from.equals(model.timePeriod.to) ) yearsFreq.addValue(model.timePeriod.from);
				else {
					
					yearsFreq.addValue(model.timePeriod.from);
					yearsFreq.addValue(model.timePeriod.to);
				}
				
				if ( model.timePeriod.from.equals(model.timePeriod.to) ) averageYearTrain += model.timePeriod.from;
				else averageYearTrain += ((model.timePeriod.from + model.timePeriod.to) / 2);
			}
			
			List<DefactoModel> modelsTest = new ArrayList<>();
			modelsTest.addAll(DefactoModelReader.readModels(testDirectory + "correct/" + path, true, languages));
			
			Set<String> subjectsTest = new HashSet<>();
			Set<String> objectsTest = new HashSet<>();
			int minYearTest = Integer.MAX_VALUE;
			int maxYearTest = Integer.MIN_VALUE;
			int averageYearTest = 0;
			
			for ( DefactoModel model : modelsTest) {
				
				subjectsTest.add(model.getSubjectUri());
				objectsTest.add(model.getObjectUri());
				
				minYearTest = Math.min(minYearTest, model.timePeriod.from);
				maxYearTest = Math.max(maxYearTest, model.timePeriod.to);
				if ( model.timePeriod.from.equals(model.timePeriod.to) ) averageYearTest += model.timePeriod.from;
				else averageYearTest += ((model.timePeriod.from + model.timePeriod.to) / 2);
			}
			
			System.out.println(String.format("%s & %s/%s & %s/%s & point & %s/%s & %s/%s & %s/%s & \\\\", path, 
					subjectsTrain.size(), subjectsTest.size(), 
					objectsTrain.size(), objectsTest.size(), 
					minYearTrain, minYearTest, 
					maxYearTrain, maxYearTest, 
					averageYearTrain / modelsTrain.size(), averageYearTest / modelsTest.size()));
		}
		
		for ( Entry<Comparable<?>, Long> entry : yearsFreq.sortByValue()){
			
			System.out.println("tf.put("+entry.getKey() + ", " + entry.getValue()+ "L);");
		}
	}
	
	public static void inspectTimeInterval(String trainDirectory , List<String> languages) throws FileNotFoundException{
		
		for ( String path : Arrays.asList("award", "birth","death","foundationPlace","nbateam","publicationDate","spouse","starring","subsidiary","leader") ) {
			
			List<DefactoModel> models = new ArrayList<>();//
//			models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/" + path, true, languages));
			models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/", true, languages));
			
			Map<Integer, Map<Integer,Integer>> yearYearCount = new LinkedHashMap<Integer, Map<Integer,Integer>>(); 

			int startYear = 1900; 
			for ( int i = startYear; i < 2020; i++) {
				yearYearCount.put(i, new LinkedHashMap<Integer,Integer>());
				for ( int j = startYear; j < 2020; j++) {
					yearYearCount.get(i).put(j, 0);
				}
			}
			
			for ( DefactoModel model : models) {
				
				Integer from = model.timePeriod.from;
				Integer to   = model.timePeriod.to;
//				System.out.println(from + " "+ to);
				
				if ( from > 1900 && to > 1900)
					yearYearCount.get(from).put(to, yearYearCount.get(from).get(to) + 1);
			}
			
//			System.out.println("Relation: "+ path);
			
			
			int max = 0;
			List<String> data = new ArrayList<String>();
			for ( Map.Entry<Integer, Map<Integer,Integer>> entry : yearYearCount.entrySet()) {
				
				StringBuffer buffer = new StringBuffer();
				for ( Map.Entry<Integer,Integer> entryYear : entry.getValue().entrySet()){
					
//					if ( entryYear.getValue() > 0 ) {
						
						max = Math.max(max, entryYear.getValue());
						data.add("{x: "+ (entry.getKey() - startYear) * 10 +", y: "+ (entryYear.getKey()- startYear)* 10 +", count: "+entryYear.getValue()+"}");
						
						buffer.append(entryYear.getValue()+ "\t");
//					}
				}
				System.out.println(buffer);
			}
			BufferedFileWriter writer = new BufferedFileWriter("/Users/gerb/Development/workspaces/tex/AKSW_Papers/2013/JWS_Temporal_Multilingual_Defacto/statistics/heatmap/years_goldset_"+path+".js", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
			writer.write(String.format("var data = {max: %s, data: [", max));
			writer.write(StringUtils.join(data, ", \n"));
			writer.write("]}");
			writer.close();
		}
	}
}
