package org.aksw.defacto.search.time;

public class Score {
	
	public Double score;
	public Integer year;
	
	public Score(String year, double score) {
		
		this.year = Integer.valueOf(year);
		this.score = score;
	}
	
	public String toString(){
		
		return this.year + ": " + this.score;
	}
}
