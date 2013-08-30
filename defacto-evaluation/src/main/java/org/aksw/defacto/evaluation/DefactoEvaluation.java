/**
 * 
 */
package org.aksw.defacto.evaluation;

import java.io.FileNotFoundException;
import java.util.Arrays;
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
		
		String trainDirectory = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") 
				+ Defacto.DEFACTO_CONFIG.getStringSetting("eval", "train-directory");
		
		List<String> languages = Arrays.asList("de", "fr", "en");
		List<DefactoModel> models = DefactoModelReader.readModels(trainDirectory, true, languages);
		
		Map<DefactoModel, Evidence> evidences = Defacto.checkFacts(models.subList(0, Math.min(10, models.size())));
		
		for ( Entry<DefactoModel, Evidence> entry : evidences.entrySet() ) {
			LOGGER.debug("Validating fact: " + entry.getKey());
		
			DefactoModel model = entry.getKey();
			Evidence evidence = entry.getValue();
		}
	}
}
