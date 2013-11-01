/**
 * 
 */
package org.aksw.defacto.util;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.topic.frequency.Word;

import com.google.common.collect.Lists;

/**
 * @author Lorenz Buehmann
 *
 */
public class DummyData {
	public static Evidence createDummyEvidence(int size){
    	String subjectLabel = "Brad Pitt";
    	String objectLabel = "Berlin";
    	Evidence evidence = new Evidence(null, 20l, subjectLabel, objectLabel);
        
    	for(int i = 0; i < size; i++){
    		WebSite webSite = new WebSite(new MetaQuery(subjectLabel, "birth place", objectLabel, Lists.<Word>newArrayList()), "http://en.wikipedia.org/wiki/Brad_Pitt" + i);
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
    	
    	return evidence;
    }
}
