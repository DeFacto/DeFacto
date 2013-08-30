/**
 * 
 */
package org.aksw.defacto.evaluation.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.model.DefactoResource;
import org.aksw.defacto.reader.DefactoModelReader;
import org.apache.commons.io.FileUtils;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class TestAndTrainSetCreator {

	private static Map<String, List<DefactoResource>> subjectsForRelation = new HashMap<String, List<DefactoResource>>();
	private static Map<String, List<DefactoResource>> objectsForRelation = new HashMap<String, List<DefactoResource>>();
	
	private static Map<String,List<String>> relationToFact = new HashMap<String,List<String>>();
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		Defacto.init();
		List<String> relations = Arrays.asList("birth", "death", "spouse", "foundationPlace", "award", "publicationDate", "nbateam", "leader", "subsidiary", "starring");
		
		for ( String relation : relations) {
			
			// first we need to generate postitive examples
			generatePositiveExample(relation);
			
			// create wrong domain set
			createDomainSet("train", relation);
			createDomainSet("test", relation);
			
			// create wrong range set
			createRangeSet("train");
			createRangeSet("test");
			
			// create wrong domain/range set
			createDomainRangeSet("train");
			createDomainRangeSet("test");
					
			// create wrong property set
			createPropertySet("train");
			createPropertySet("test");
			
			// create wrong random set
			createRandomSet("train");
			createRandomSet("test");
			
			// create wrong datae set
			createDateSet("train");
			createDateSet("test");
		}
	}

	/**
	 * 
	 * @param testOrTrain
	 * @param relation
	 * @throws IOException 
	 */
	private static void createDomainSet(String testOrTrain, String relation) throws IOException {

		List<DefactoModel> models = DefactoModelReader.readModels(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
										"benchmark/" + testOrTrain + "/correct/" + relation + "/");

		for ( DefactoModel model : models ) {
			
			model.setSubject(getRandomSubject(relation, model));
			
			String path = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
					"benchmark/" + testOrTrain + "/wrong/domain/" + relation + "/";
			
			model.write(path, model.getName());
		}
	}

	private static void createRangeSet(String testOrTrain) {
		
	}

	private static void createDomainRangeSet(String testOrTrain) {
		
	}

	private static void createPropertySet(String testOrTrain) {
		
	}

	private static void createRandomSet(String testOrTrain) {
		
	}

	private static void createDateSet(String testOrTrain) {
		
	}
	
	private static DefactoResource getRandomSubject(String relation, DefactoModel model) {
		
		DefactoResource newSubject = subjectsForRelation.get(relation).get(new Random().nextInt(subjectsForRelation.get(relation).size()));
		while ( relationToFact.get(relation).contains(newSubject.getUri() + " " + model.getPropertyUri() + " " + model.getObjectUri()) ) {
			
			newSubject = subjectsForRelation.get(relation).get(new Random().nextInt(subjectsForRelation.get(relation).size()));
			System.out.println("Picked already contained subject");
		}
		
		return newSubject;
	}

	/**
	 * 
	 * @param relation
	 * @throws IOException
	 */
	private static void generatePositiveExample(String relation) throws IOException {
		
		File[] files = new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "eval/correct/"+relation+"/").listFiles();
		System.out.println("Relation " + relation + " with " + files.length + " model files.");
		
		int train = 0, test = 0;
		for (File file : files) {
			int number = Integer.valueOf(file.getName().replace(relation +"_", "").replace(".ttl", ""));
			
			if ( file.getName().endsWith(".ttl") ) {
				
				if ( file.getName().equals("birth_00082.ttl") ) continue;
				
				// train
				if ( train < 75 && number % 2 == 0 ) {
					
					FileUtils.copyFile(file, new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
							"benchmark/train/correct/"+relation+"/" + file.getName()));
					train++;
				}
				// test
				else if ( test < 75 && number % 2 == 1 ) {
					
					FileUtils.copyFile(file, new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
							"benchmark/test/correct/"+relation+"/" + file.getName()));
					test++;
				}
			}
		}
		relationToFact.put(relation, new ArrayList<String>());
		subjectsForRelation.put(relation, new ArrayList<DefactoResource>());
		
		// let's load all the facts
		
		for ( DefactoModel model : DefactoModelReader.readModels(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
				"benchmark/test/correct/" + relation + "/")) {
			
			relationToFact.get(relation).add(model.getSubjectUri() + " " + model.getPropertyUri() + " " + model.getObjectUri());
			subjectsForRelation.get(relation).add(model.getSubject());
		}
		
		for ( DefactoModel model : DefactoModelReader.readModels(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
				"benchmark/train/correct/" + relation + "/")) {
			
			relationToFact.get(relation).add(model.getSubjectUri() + " " + model.getPropertyUri() + " " + model.getObjectUri());
			subjectsForRelation.get(relation).add(model.getSubject());
		}
	}
}
