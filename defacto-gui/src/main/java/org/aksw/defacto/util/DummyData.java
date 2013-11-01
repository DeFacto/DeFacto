/**
 * 
 */
package org.aksw.defacto.util;

import java.util.Arrays;
import java.util.Collections;

import org.aksw.commons.collections.Pair;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.topic.frequency.Word;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
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
	
	public static Triple getDummyTriple(){
		return new Triple(
				NodeFactory.createURI("http://dbpedia.org/resource/Albert_Einstein"), 
        		NodeFactory.createURI("http://dbpedia.org/ontology/birthPlace"), 
        		NodeFactory.createURI("http://dbpedia.org/resource/Ulm"));
	}
	
	public static DefactoModel getDummyModel() {
		Model model = ModelFactory.createDefaultModel();
		Resource albert = model.createResource("http://dbpedia.org/resource/Albert_Einstein");
		albert.addProperty(RDFS.label, "Albert Einstein");
		Resource ulm = model.createResource("http://dbpedia.org/resource/Ulm");
		ulm.addProperty(RDFS.label, "Ulm");
		Property property = model.createProperty("http://dbpedia.org/ontology/birthPlace");
		property.addProperty(RDFS.label, "birth place");
		albert.addProperty(property, ulm);
		DefactoModel defactoModel = new DefactoModel(model, "ballack", true, Arrays.asList("en"));
		return defactoModel;
	}
	
	public static Pair<DefactoModel, Evidence> createDummyData(int size){
		String language = "en";
		DefactoModel model = getDummyModel();
		
        //dummy evidence
    	String subjectLabel = "Albert Einstein";
    	String propertyLabel = "birth place";
    	String objectLabel = "Ulm";
    	Pattern pattern = new Pattern("actor born in ", "en");
    	Evidence evidence = new Evidence(model, 20l, Collections.singleton(pattern));
        
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
    		
    		ComplexProof proof = new ComplexProof(null, subjectLabel, objectLabel, "dedicated to Albert Einstein, who was born in Ulm but left", "dedicated to Albert Einstein, who was born in Ulm but left", webSite);
    		proof.setTinyContext("dedicated to Albert Einstein, who was born in Ulm but left");
    		evidence.addComplexProof(proof);
    		proof = new ComplexProof(null, subjectLabel, objectLabel, "An important trade town for centuries, Ulm was also the birthplace of Albert Einstein. ", "An important trade town for centuries, Ulm was also the birthplace of Albert Einstein. ", webSite);
    		proof.setTinyContext("An important trade town for centuries, Ulm was also the birthplace of Albert Einstein. ");
    		evidence.addComplexProof(proof);
    		proof = new ComplexProof(null, subjectLabel, objectLabel, "the old imperial town of Ulm, the birth place of Albert Einstein.", "the old imperial town of Ulm, the birth place of Albert Einstein.", webSite);
    		proof.setTinyContext("the old imperial town of Ulm, the birth place of Albert Einstein.");
    		evidence.addComplexProof(proof);
    	}
    	
    	return new Pair<DefactoModel, Evidence>(model, evidence);
    }
}
