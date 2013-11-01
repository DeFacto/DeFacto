/**
 * 
 */
package org.aksw.defacto.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.defacto.model.DefactoModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefactoModelReader {

	/**
	 * Creates a new DefactoModel. The model uses the filename of the
	 * provided file as name and is initialized as TRUE. The model 
	 * supports 'en', 'fr' and 'de'. If the file is not found a 
	 * FileNotFoundExcpetion is thrown.
	 * 
	 * @param pathToModel - the absolute path to the file
	 * @return a new DefactoModel(true, de/fr/en)
	 * @throws FileNotFoundException
	 */
	public static final DefactoModel readModel(String pathToModel) throws FileNotFoundException {
		
		return readModel(pathToModel, true, new ArrayList<String>(Arrays.asList("en", "de", "fr")));
	}
	
	/**
	 * Creates a new DefactoModel. The model uses the filename of the
	 * provided file as name and is initialized as <code>isCorrect</code>. The model 
	 * supports 'en', 'fr' and 'de'. If the file is not found a 
	 * FileNotFoundExcpetion is thrown.
	 * 
	 * @param pathToModel - the absolute path to the file
	 * @param isCorrect - true if the fact is true, false otherwise
	 * @return a new DefactoModel(true, de/fr/en)
	 * @throws FileNotFoundException
	 */
	public static final DefactoModel readModel(String pathToModel, Boolean isCorrect) throws FileNotFoundException {
		
		return readModel(pathToModel, isCorrect, new ArrayList<String>(Arrays.asList("en", "de", "fr")));
	}
	
	/**
	 * Creates a new DefactoModel. The model uses the filename of the
	 * provided file as name and is initialized as <code>isCorrect</code>. The model 
	 * supports the provided languages (e.g. 'en', 'fr' and 'de'). If the file is not found a 
	 * FileNotFoundExcpetion is thrown.
	 * 
	 * @param pathToModel - the absolute path to the file
	 * @param isCorrect - true if the fact is true, false otherwise
	 * @param languages - a list of iso code languages
	 * @return a new DefactoModel(true, de/fr/en)
	 * @throws FileNotFoundException
	 */
	public static final DefactoModel readModel(String pathToModel, Boolean isCorrect, List<String> languages) throws FileNotFoundException {
		
		Model model = ModelFactory.createDefaultModel();
        model.read(new FileReader(pathToModel), "", "TTL");
		
        String absolutePath = new File(pathToModel).getAbsolutePath();
        absolutePath = absolutePath.replace("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/FactBench/v1/", "");
        
		return new DefactoModel(model, absolutePath, isCorrect, languages);
	}
	
	/**
	 * Reads a list of DefactoModels (ttl files) from a given directory. 
	 * All returned models are true and support the languages
	 * 'de', 'fr' and 'en. 
	 * 
	 * @param pathToModels
	 * @return a list of defacto models (true, de/fr/en)
	 * @throws FileNotFoundException
	 */
	public static final List<DefactoModel> readModels(String pathToModels) throws FileNotFoundException {

		List<DefactoModel> models = new ArrayList<DefactoModel>();
		
		for ( File file : new File(pathToModels).listFiles()) {
			
			if ( file.getName().endsWith(".ttl") ) {
				
				models.add(readModel(file.getAbsolutePath()));
			}
		}
		
		return models;
	}
	
	/**
	 * Reads a list of DefactoModels (ttl files) from a given directory. 
	 * All returned models support the languages 'de', 'fr' and 'en. 
	 * 
	 * @param pathToModels
	 * @param isCorrect - true if the model is correct, false otherwise
	 * @return a list of defacto models (true, de/fr/en)
	 * @throws FileNotFoundException
	 */
	public static final List<DefactoModel> readModels(String pathToModels, Boolean isCorrect) throws FileNotFoundException {

		List<DefactoModel> models = new ArrayList<DefactoModel>();
		
		for ( File file : new File(pathToModels).listFiles()) {
			
			if ( file.getName().endsWith(".ttl") ) {
				
				models.add(readModel(file.getAbsolutePath()));
			}
		}
		
		return models;
	}
	
	/**
	 * Reads a list of DefactoModels (ttl files) from a given directory. 
	 * 
	 * @param pathToModels
	 * @param isCorrect - true if the model is correct, false otherwise
	 * @param languages - a list of iso code languages (e.g.: 'de', 'fr', 'en')
	 * @return a list of defacto models (true, de/fr/en)
	 * @throws FileNotFoundException
	 */
	public static final List<DefactoModel> readModels(String pathToModels, Boolean isCorrect, List<String> languages) throws FileNotFoundException {

		List<DefactoModel> models = new ArrayList<DefactoModel>();
		
		for ( File file : FileUtils.listFiles(new File(pathToModels), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
			if ( file.getName().endsWith(".ttl") ) {
				
				models.add(readModel(file.getAbsolutePath(), isCorrect, languages));
			}
		}
		
//		for ( File file : new File(pathToModels).listFiles()) {
//			
//			
//		}
		
		return models;
	}
}
