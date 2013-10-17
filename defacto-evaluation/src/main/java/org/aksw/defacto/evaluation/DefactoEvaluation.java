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
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.reader.DefactoModelReader;
import org.aksw.defacto.search.time.TimePeriodSearcher;
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
		
		double correctFacts = 0D;
		double correctFrom	= 0D;
		double correctTo	= 0D;
		
		List<String> languages = Arrays.asList("de", "fr", "en");
		String trainDirectory = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") 
				+ Defacto.DEFACTO_CONFIG.getStringSetting("eval", "train-directory");
		
//		for ( String path : Arrays.asList("award", "birth","death","foundationPlace","nbateam","publicationDate","spouse","starring","subsidiary","leader") ) {
			
		List<DefactoModel> models = new ArrayList<>();//
		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/", true, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/spouse", true, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/nbateam", true, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "correct/leader", true, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/range", false, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix", false, languages));
		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/property", false, languages));
		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/random", false, languages));
		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/domainrange", false, languages));
		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/domain", false, languages));
		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/mix/range", false, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/date/", false, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/range/", false, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/domainrange/spouse", false, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/domainrange/starring", false, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/domainrange/subsidiary", false, languages));
//		models.addAll(DefactoModelReader.readModels(trainDirectory + "wrong/property/", false, languages));
		Collections.shuffle(models);
		
		LOGGER.info("Starting Defacto for " + models.size() + " facts.");
		
		int i = 0, count = 0, empty=0;
		for ( DefactoModel model : models ){
			
//			if ( !model.getName().equals("train/wrong/mix/domain/award/award_00028.ttl") ) continue;
			
			LOGGER.info("Validating fact ("+i+++"): " + model);
			Evidence evidence = Defacto.checkFact(model, TIME_DISTRIBUTION_ONLY.NO);
			
//			if ( evidence.defactoTimePeriod == null ) System.out.println("NULLNULLNULLNULL");
//			
//			if ( evidence.defactoTimePeriod.from.equals(0) && evidence.defactoTimePeriod.to.equals(0)) empty++;
//			if (evidence.defactoTimePeriod.equals(model.getTimePeriod())) count++;
//			System.out.println("TIMEPERIOD: " + evidence.defactoTimePeriod + " " + model.getTimePeriod() + "  -->  " + evidence.defactoTimePeriod.equals(model.getTimePeriod()) + " Total: " + count +"/" + i  + "  Empty:" + empty);
			
//			for ( Entry<Comparable<?>, Long> sortByValue : TimePeriodSearcher.patFreq.sortByValue()) {
//				
//				System.out.println(sortByValue.getKey() + ": " + sortByValue.getValue());
//			}
			
			// this takes pretty long 
//			if ( i % 5 == 0 ) System.exit(0);
//			if ( i % 10 == 0 ) 
				Defacto.writeTrainingFiles();
		}
//		for ( Entry<Comparable<?>, Long> sortByValue : TimePeriodSearcher.patFreq.sortByValue()) {
//			
//			System.out.println(sortByValue.getKey() + ": " + sortByValue.getValue());
//		}
		
		// be sure to write the final files
		Defacto.writeTrainingFiles();
	}
}
