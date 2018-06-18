package org.aksw.defacto.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefactoTimePeriod {

	public static final DefactoTimePeriod EMPTY_DEFACTO_TIME_PERIOD = new DefactoTimePeriod(0, 0);
	
	public Integer from = 0;
	public Integer to = 0;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefactoTimePeriod other = (DefactoTimePeriod) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DTP ["+from+"/"+to+"]";
	}
}
