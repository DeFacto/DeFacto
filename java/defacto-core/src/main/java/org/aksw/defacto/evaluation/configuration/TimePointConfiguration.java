package org.aksw.defacto.evaluation.configuration;

import java.util.List;

import org.aksw.defacto.model.DefactoModel;

public class TimePointConfiguration extends Configuration {

	public String periodSearchMethod;
	
	public TimePointConfiguration(String name, List<String> relationNames) {
		super(name, relationNames);
	}

	public String toString(){
		
		return String.format("%s\t%s\t%s\t%s\t%s\t%.5f\t%.5f\t%.5f\t%.5f\t%s\t%s", 
				name, language, normalizer, periodSearchMethod, context, precision, recall, fMeasure, mrrAverage, isPossible, correct);
	}
}
