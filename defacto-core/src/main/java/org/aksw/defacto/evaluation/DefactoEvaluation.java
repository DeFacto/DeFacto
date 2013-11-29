/**
 * 
 */
package org.aksw.defacto.evaluation;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.Defacto.TIME_DISTRIBUTION_ONLY;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.reader.DefactoModelReader;
import org.aksw.defacto.search.time.PatternTimePeriodSearcher;
import org.apache.commons.lang3.ArrayUtils;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefactoEvaluation {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefactoEvaluation.class);
	static Long duration = 0L;
	static Long number = 1L;
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		Defacto.init();
		for ( String trainOrTestAndSet : args) {
			String[] split = trainOrTestAndSet.split("-");
			if ( split[0].equals("train") || split[0].equals("test") ) generateArffFiles(split[1], split[0]);
		}
	}

	private static void generateArffFiles(String set, String testOrTrain) throws FileNotFoundException {
		
		List<String> languages = Arrays.asList("en");
		String trainDirectory = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") 
				+ Defacto.DEFACTO_CONFIG.getStringSetting("eval", testOrTrain + "-directory");
		
		List<DefactoModel> models = new ArrayList<>();//
		
		// mix contains date properties which will have there own evaluation
		if ( !set.equals("mix") && !set.equals("correct") )
			models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/"+ set, false, languages));
		else if ( set.equals("correct") ) {
			
			models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/", true, languages));
		}
		else {
			models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/domain", false, languages));
			models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/property", false, languages));
			models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/range", false, languages));
			models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/domainrange", false, languages));
			models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/random", false, languages));
		}
		
		// just to make preamtive tests in weka possible (same true false distribution) 
		Collections.shuffle(models, new Random(100));
		LOGGER.info("Starting Defacto for " + models.size() + " facts for set: " + set);
		System.out.println("Starting Defacto for " + models.size() + " facts for set: " + set);
		
		for ( int i = 0; i < models.size() ; i++ ) {
			
			long start = System.currentTimeMillis();
			LOGGER.info("Validating fact ("+ (i + 1) +"): " + models.get(i));
			System.out.print(String.format("Validation-Set: %s\tTask: %04d of %04d", set, i+1, models.size()));
			Defacto.checkFact(models.get(i), TIME_DISTRIBUTION_ONLY.NO);
			if ( i % 10 == 0 ) Defacto.writeFactTrainingFiles(Defacto.DEFACTO_CONFIG.getStringSetting("fact", "FACT_TRAINING_DATA_FILENAME") + testOrTrain + "/" + set + ".arff");
			Defacto.writeEvidenceTrainingFiles(Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_TRAINING_DATA_FILENAME") + testOrTrain + "/" + set + ".arff");
			long duration = System.currentTimeMillis() - start;
			System.out.println(" Time: " + duration + " Avg: " + DefactoEvaluation.duration / number);
			
			DefactoEvaluation.duration += duration;
			DefactoEvaluation.number++;
		}
		// write the last ones
		Defacto.writeFactTrainingFiles(Defacto.DEFACTO_CONFIG.getStringSetting("fact", "FACT_TRAINING_DATA_FILENAME") + testOrTrain + "/" + set + ".arff");
		
		// reset the index thingy
		AbstractEvidenceFeature.createInstances();
	}
}
