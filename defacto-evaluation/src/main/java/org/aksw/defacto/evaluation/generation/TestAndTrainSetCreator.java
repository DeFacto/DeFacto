/**
 * 
 */
package org.aksw.defacto.evaluation.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.model.DefactoResource;
import org.aksw.defacto.model.DefactoTimePeriod;
import org.aksw.defacto.reader.DefactoModelReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class TestAndTrainSetCreator {

	private static Map<String, List<DefactoResource>> subjectsForRelation = new HashMap<String, List<DefactoResource>>();
	private static List<String> properties = new ArrayList<String>();
	static {

		properties.add("http://dbpedia.org/ontology/team");
		properties.add("http://dbpedia.org/ontology/subsidiary");
		properties.add("http://dbpedia.org/ontology/deathPlace");
		properties.add("http://dbpedia.org/ontology/author");
		properties.add("http://dbpedia.org/ontology/award");
		properties.add("http://dbpedia.org/ontology/foundationPlace");
		properties.add("http://dbpedia.org/ontology/birthPlace");
		properties.add("http://dbpedia.org/ontology/spouse");
		properties.add("http://dbpedia.org/ontology/starring");
		properties.add("http://dbpedia.org/ontology/office");
	}
	
	
	private static Map<String, List<DefactoResource>> objectsForRelation = new HashMap<String, List<DefactoResource>>();
	
	private static Map<String,List<String>> relationToFact = new HashMap<String,List<String>>();
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		Defacto.init();
		List<String> relations = Arrays.asList("birth", "death", "spouse", "foundationPlace", "award", "publicationDate", "nbateam", "leader", "subsidiary", "starring");
		
//		for ( String relation : relations) {
//			
//			// first we need to generate postitive examples
//			generatePositiveExample(relation);
//			
//			// create wrong domain set
//			createDomainSet("train", relation);
//			createDomainSet("test", relation);
//			
//			// create wrong range set
//			createRangeSet("train", relation);
//			createRangeSet("test", relation);
//			
//			// create wrong domain/range set
//			createDomainRangeSet("train", relation);
//			createDomainRangeSet("test", relation);
//					
//			// create wrong property set
//			createPropertySet("train", relation);
//			createPropertySet("test", relation);
//			
//			// create wrong random set
//			createRandomSet("train", relation);
//			createRandomSet("test", relation);
//			
//			// create wrong date set
//			createDateSet("train", relation);
//			createDateSet("test", relation);
//		}
		
		for ( String relation : relations ) {
			
			System.out.println(relation);
			// create wrong datae set
			createMixSet("train", relation);
			createMixSet("test", relation);
		}
	}

	private static void createMixSet(String testOrTrain, String relation) throws IOException {

		System.out.println("\t" + testOrTrain);
		
		List<String> wrongCases = Arrays.asList("domain", "range", "domainrange", "random", "property", "date");
		
		for ( String wrongCase : wrongCases ) {
			
			System.out.println("\t\t" + wrongCase);

			List<DefactoModel> models = DefactoModelReader.readModels(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
					"benchmark/" + testOrTrain + "/wrong/" + wrongCase + "/" + relation + "/");
			
			Collections.shuffle(models);
			
			models = models.subList(0, 13);
			int i = 0 ;
			for ( DefactoModel model : models ) {
				
//				System.out.println(i++);
			
				String path = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
						"benchmark/" + testOrTrain + "/wrong/mix/" + wrongCase +"/" + relation + "/";
				
//				System.out.println(path + model.getName());
				model.write(path, model.getName());
			}
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

	/**
	 * 
	 * @param testOrTrain
	 * @param relation
	 * @throws IOException
	 */
	private static void createRangeSet(String testOrTrain, String relation) throws IOException {
		
		List<DefactoModel> models = DefactoModelReader.readModels(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
				"benchmark/" + testOrTrain + "/correct/" + relation + "/");

		for ( DefactoModel model : models ) {
		
			model.setObject(getRandomObject(relation, model));
			
			String path = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
					"benchmark/" + testOrTrain + "/wrong/range/" + relation + "/";
			
			model.write(path, model.getName());
		}
	}

	private static void createDomainRangeSet(String testOrTrain, String relation) throws IOException {
		
		List<DefactoModel> models = DefactoModelReader.readModels(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
				"benchmark/" + testOrTrain + "/correct/" + relation + "/");

		for ( DefactoModel model : models ) {
		
			model.setSubject(getRandomSubject(relation, model));
			model.setObject(getRandomObject(relation, model));
			
			String path = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
					"benchmark/" + testOrTrain + "/wrong/domainrange/" + relation + "/";
			
			model.write(path, model.getName());
		}
	}

	private static void createPropertySet(String testOrTrain, String relation) throws IOException {
		
		List<DefactoModel> models = DefactoModelReader.readModels(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
				"benchmark/" + testOrTrain + "/correct/" + relation + "/");

		for ( DefactoModel model : models ) {
			
			model.setProperty(getRandomProperty(relation, model));
			
			String path = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
					"benchmark/" + testOrTrain + "/wrong/property/" + relation + "/";
			
			model.write(path, model.getName());
		}
	}

	private static void createRandomSet(String testOrTrain, String relation) throws IOException {
		
		List<DefactoModel> models = DefactoModelReader.readModels(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
				"benchmark/" + testOrTrain + "/correct/" + relation + "/");

		for ( DefactoModel model : models ) {
			
			model.setSubject(getRandomSubject(relation, model));
			model.setObject(getRandomObject(relation, model));
			model.setProperty(getRandomProperty(relation, model));
			
			String path = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
					"benchmark/" + testOrTrain + "/wrong/random/" + relation + "/";
			
			model.write(path, model.getName());
		}
	}

	private static void createDateSet(String testOrTrain, String relation) throws IOException {
		
		List<DefactoModel> models = DefactoModelReader.readModels(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
				"benchmark/" + testOrTrain + "/correct/" + relation + "/");

		for ( DefactoModel model : models ) {
			
			if ( model.timePeriod.isTimePoint() ) {
				
				int newYear = getNearbyYear(model.timePeriod.from);
				model.timePeriod = new DefactoTimePeriod(newYear, newYear);
			}
			else {
				
				int[] newInterval = getNearbyInterval(model.timePeriod.from, model.timePeriod.to);
				model.timePeriod = new DefactoTimePeriod(newInterval[0], newInterval[1]);
			}
			
			String path = Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
					"benchmark/" + testOrTrain + "/wrong/date/" + relation + "/";
			
			model.write(path, model.getName());
		}
	}
	
	private static int[] getNearbyInterval(int start, int end) {
		
		int[] timeperiod;
		do {
			
			int duration = end - start;
			
			// get the new start year, can be the old start year
			// but only if duration is different
			int nearbyStart;
			do {
				
				nearbyStart = (int)(start + new Random().nextGaussian() * 2D);
			}
			while (nearbyStart > 2013 || nearbyStart <= 0);
			
			// get the new duration, can be the old duration
			// but only if start year is different
			int newDuration;
			do {
				
				newDuration = (int)(duration + new Random().nextGaussian() * 5D);
			}
			while (newDuration > 2013 || newDuration <= 0);
			
			timeperiod = new int[]{nearbyStart, nearbyStart+newDuration};
		}
		while ( (timeperiod[0] == start && timeperiod[1] == end) || timeperiod[0] > 2013 || timeperiod[1] > 2013 );
		
		return timeperiod;
	}
	
	/**
	 * This method returns a random year for a gauss distribution
	 * with a mean of <code>year</code> and a variance of 5%.
	 * 
	 * @param year
	 * @return
	 */
	private static int getNearbyYear(Integer year) {
		
		int nearbyYear;
		do {
			
			nearbyYear = (int)(year + new Random().nextGaussian() * 5D);
		}
		while (nearbyYear == year || nearbyYear > 2013 || nearbyYear <= 0);
		return nearbyYear;
	}
	
	/**
	 * 
	 * @param relation
	 * @param model
	 * @return
	 */
	private static Property getRandomProperty(String relation, DefactoModel model) {
		
		String newPropertyUri = properties.get(new Random().nextInt(properties.size()));
		while ( relationToFact.get(relation).contains(model.getSubjectUri() + " " + newPropertyUri + " " + model.getObjectUri()) ) {
			
			newPropertyUri = properties.get(new Random().nextInt(properties.size()));
			System.out.println("Picked already contained property!");
		}
		
		return model.model.createProperty(newPropertyUri);
	}
	
	/**
	 * Returns a random object from the set of all objects
	 * of a given relation.
	 * 
	 * @param relation
	 * @param model
	 * @return
	 */
	private static DefactoResource getRandomObject(String relation, DefactoModel model) {
		
		DefactoResource newObject = objectsForRelation.get(relation).get(new Random().nextInt(objectsForRelation.get(relation).size()));
		while ( relationToFact.get(relation).contains(model.getSubjectUri() + " " + model.getPropertyUri() + " " + newObject.getUri()) ) {
			
			newObject = objectsForRelation.get(relation).get(new Random().nextInt(objectsForRelation.get(relation).size()));
			System.out.println("Picked already contained object");
		}
		
		return newObject;
	}
	
	/**
	 * Returns a random subject from the set of all subjects
	 * of a given relation.
	 * 
	 * @param relation
	 * @param model
	 * @return
	 */
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
		objectsForRelation.put(relation, new ArrayList<DefactoResource>());
		
		// let's load all the facts
		for ( DefactoModel model : DefactoModelReader.readModels(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
				"benchmark/test/correct/" + relation + "/")) {
			
			relationToFact.get(relation).add(model.getSubjectUri() + " " + model.getPropertyUri() + " " + model.getObjectUri());
			subjectsForRelation.get(relation).add(model.getSubject());
			objectsForRelation.get(relation).add(model.getObject());
		}
		for ( DefactoModel model : DefactoModelReader.readModels(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + 
				"benchmark/train/correct/" + relation + "/")) {
			
			relationToFact.get(relation).add(model.getSubjectUri() + " " + model.getPropertyUri() + " " + model.getObjectUri());
			subjectsForRelation.get(relation).add(model.getSubject());
			objectsForRelation.get(relation).add(model.getObject());
		}
	}
}
