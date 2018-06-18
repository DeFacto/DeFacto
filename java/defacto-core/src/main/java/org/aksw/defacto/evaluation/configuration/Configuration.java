package org.aksw.defacto.evaluation.configuration;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import org.aksw.defacto.model.DefactoModel;

public class Configuration {

	static DecimalFormat df = new DecimalFormat("#0.#");
	public Double precision;
	public Double recall;
	public Double fMeasure;
	public String normalizer;
	public String context;
	public String language;
	public String isPossible;
	public int correct;
	public Double mrrAverage;
	public List<String> relationNames;
	public String name;
	public List<DefactoModel> models;
	public String periodSearchMethod;
	public int correctStart;
	public int correctEnd;
	
	public Configuration(String name, List<String> relationNames) {
		
		this.name = name;
		this.relationNames = relationNames;
	}

	public String toString(){
		
		return String.format(Locale.ENGLISH, "%s\t%s\t%s\t%s\t%s\t%.1f\t&\t%.1f\t&\t%.1f\t&\t%.1f\t&\t%s\t&\t%.1f\t%s", 
				name, language, periodSearchMethod, normalizer, context, 
				format(precision), format(recall), format(fMeasure), format(mrrAverage), isPossible.split("/")[0], format(correct / (double)Integer.valueOf(isPossible.split("/")[0])), correct);
	}
	
	private Double format(Double value) {
		// TODO Auto-generated method stub
		return Double.valueOf(df.format(value * 100).replace(",", "."));
	}

	public static void main(String[] args) {
		System.out.println(df.format(0.1145343234234 * 100).replace(",", "."));
	}
}
