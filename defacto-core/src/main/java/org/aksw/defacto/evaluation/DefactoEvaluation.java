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

import org.aksw.defacto.Defacto;
import org.aksw.defacto.Defacto.TIME_DISTRIBUTION_ONLY;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.reader.DefactoModelReader;
import org.aksw.defacto.search.time.PatternTimePeriodSearcher;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefactoEvaluation {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefactoEvaluation.class);
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		Defacto.init();

//		generateArffFiles("property", args[0]);
		generateArffFiles("mix", args[0]);
		generateArffFiles("random", args[0]);
		generateArffFiles("domain", args[0]);
		generateArffFiles("range", args[0]);
		generateArffFiles("domainrange", args[0]);
	}

	private static void generateArffFiles(String set, String testOrTrain) throws FileNotFoundException {
		
		List<String> languages = Arrays.asList("de", "fr", "en");
		String trainDirectory = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") 
				+ Defacto.DEFACTO_CONFIG.getStringSetting("eval", testOrTrain + "-directory");
		
		List<DefactoModel> models = new ArrayList<>();//
		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/", true, languages));
		
		// mix contains date properties which will have there own evaluation
		if ( !set.equals("mix") )
			models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/"+ set, false, languages));
		else {
			
			models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/domain", false, languages));
			models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/property", false, languages));
			models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/range", false, languages));
			models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/domainrange", false, languages));
			models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/random", false, languages));
		}
		
		// just to make preamtive tests in weka possible (same true false distribution) 
		Collections.shuffle(models);
		LOGGER.info("Starting Defacto for " + models.size() + " facts for set: " + set);
		
		for ( int i = 0; i < models.size() ; i++ ) {
			
			long start = System.currentTimeMillis();
			LOGGER.info("Validating fact ("+ (i + 1) +"): " + models.get(i));
			System.out.print(String.format("Validation-Set: %s\tTask: %04d of %04d", set, i+1, models.size()));
			Evidence evidence = Defacto.checkFact(models.get(i), TIME_DISTRIBUTION_ONLY.YES);
			Defacto.writeEvidenceTrainingFiles(
					Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_TRAINING_DATA_FILENAME") + testOrTrain + "/" + set + ".arff");
			System.out.println(" Time: " + (System.currentTimeMillis() - start));
		}
		
		// reset the index thingy
		AbstractEvidenceFeature.createInstances();
	}
}
