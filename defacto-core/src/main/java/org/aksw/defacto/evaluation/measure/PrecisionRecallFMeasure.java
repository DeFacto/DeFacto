package org.aksw.defacto.evaluation.measure;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 
 */
public class PrecisionRecallFMeasure {
	
	public Double precision;
	public Double recall;
	public Double fmeasure;
	
	public PrecisionRecallFMeasure(Set<Integer> relevantYears, Set<Integer> retrievedYears) {
		
		Set<Integer> intersection = new HashSet<>(relevantYears); // use the copy constructor
		intersection.retainAll(retrievedYears);
		
		if ( retrievedYears.size() != 0 ) precision = (double) intersection.size() / (double) retrievedYears.size();
		else precision = Double.NaN;
		
		if ( relevantYears.size() != 0 ) recall = (double) intersection.size() / (double) relevantYears.size();
		else recall = 0D;
		
		if ( (precision + recall) == 0 ) fmeasure = 0D;
		else {
			
			if ( Double.isNaN(precision) ) fmeasure = 0D;
			else fmeasure = (2*precision*recall) / (precision + recall);
		}
	}

	@Override
	public String toString(){
		
		return String.format(Locale.ENGLISH, "P: %.3f, R: %.3f, F: %.3f", precision, recall, fmeasure);
	}
}
