package org.aksw.defacto.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefactoTimePeriod {

	public Integer from;
	public Integer to;
	private Pattern pattern = Pattern.compile("[0-9]{4}");
	
	public DefactoTimePeriod(String from, String to) {
		
		Matcher matcher = pattern.matcher(from);
	    while (matcher.find()) {
	    	this.from = Integer.valueOf(matcher.group());
	    }
	    matcher = pattern.matcher(to);
	    while (matcher.find()) {
	    	this.to = Integer.valueOf(matcher.group());
	    }
	}

	public DefactoTimePeriod(int from, int to) {
		
		this.from = from;
		this.to = to;
	}

	public Integer getFrom() {
		return this.from;
	}

	public Integer getTo() {
		// TODO Auto-generated method stub
		return this.to;
	}

	public boolean isTimePoint() {
		return this.to.equals(this.from);
	}

}
