/**
 * 
 */
package org.aksw.defacto.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.aksw.commons.collections.Pair;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.topic.frequency.Word;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Lorenz Buehmann
 *
 */
public class DummyData {
	
	public static Pair<DefactoModel, Evidence> createDummyData(int size){
		String language = "en";
		//dummy model
		Model model = ModelFactory.createDefaultModel();
        Resource albert = model.createResource("http://dbpedia.org/resource/Albert_Einstein");
        albert.addProperty(RDFS.label, "Albert Einstein");
        Resource ulm = model.createResource("http://dbpedia.org/resource/Ulm");
        ulm.addProperty(RDFS.label, "Ulm");
        Property property = model.createProperty("http://dbpedia.org/ontology/birthPlace");
        property.addProperty(RDFS.label, "birth place");
		albert.addProperty(property, ulm);
        DefactoModel defactoModel = new DefactoModel(model, "ballack", true, Arrays.asList(language));
		
        //dummy evidence
    	String subjectLabel = "Brad Pitt";
    	String propertyLabel = "birth place";
    	String objectLabel = "Berlin";
    	Pattern pattern = new Pattern("actor born in ", "en");
    	Evidence evidence = new Evidence(defactoModel, 20l, Collections.singleton(pattern));
        
    	for(int i = 0; i < size; i++){
    		WebSite webSite = new WebSite(new MetaQuery(subjectLabel, propertyLabel, objectLabel, language, Lists.<Word>newArrayList()), "http://en.wikipedia.org/wiki/Brad_Pitt" + i);
        	webSite.setTitle("Test page" + i);
        	webSite.setScore(0.63);
        	webSite.setPageRank(5);
        	webSite.setPageRankScore(0.73);
        	webSite.setTopicCoverageScore(0.2);
        	webSite.setTopicMajoritySearchFeature(0.44);
        	webSite.setTopicMajorityWebFeature(0.31);
    		evidence.addWebSite(new Pattern(), webSite);    
    		
    		ComplexProof proof = new ComplexProof(null, "Brad Pitt", "Berlin", "the actor Brad Pitt was born in Berlin in 1980", "the actor Brad Pitt was born in Berlin in 1980", webSite);
    		evidence.addComplexProof(proof);
    		proof = new ComplexProof(null, "Brad Pitt", "Berlin", "While Brad Pitt got stuck in to a gruelling day of promoting World War Z in Berlin, Angelina Jolie took", "While Brad Pitt got stuck in to a gruelling day of promoting World War Z in Berlin, Angelina Jolie took", webSite);
    		evidence.addComplexProof(proof);
    		proof = new ComplexProof(null, "Brad Pitt", "Berlin", "Brad Pitt let Angelina Jolie have an early night on her 38th birthday following a family meal out in Berlin, partying", "Brad Pitt let Angelina Jolie have an early night on her 38th birthday following a family meal out in Berlin, partying", webSite);
    		evidence.addComplexProof(proof);
    	}
    	
    	return new Pair<DefactoModel, Evidence>(defactoModel, evidence);
    }
}
