package org.aksw.defacto.search.time;

import java.util.Map;
import java.util.Map.Entry;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoTimePeriod;
import org.aksw.defacto.util.Frequency;

public class OccurrenceTimePeriodSearcher implements DefactoTimePeriodSearcher {

	@Override
	public DefactoTimePeriod getTimePoint(Evidence evidence) {
		
		Map.Entry<String, Long> maxEntry = null;

		for (Map.Entry<String, Long> entry : evidence.getPreferedContext().entrySet())
		    if ( maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0 )
		    	if ( Integer.valueOf(entry.getKey()) <= 2013 )
		    		maxEntry = entry;
		
		return maxEntry == null ? null : new DefactoTimePeriod(maxEntry.getKey(), maxEntry.getKey());
	}

	@Override
	public DefactoTimePeriod getTimePeriod(Evidence evidence) {
		
		DefactoTimePeriod defactoTimePeriod = PatternTimePeriodSearcher.findTimePeriod(evidence);
		if ( defactoTimePeriod == null ) {
			
			Frequency freq = new Frequency();
			for ( Entry<String, Long> entry : evidence.getPreferedContext().entrySet())
				for (int i = 0; i < entry.getValue(); i++ )
					if ( Integer.valueOf(entry.getKey()) <= 2013) 
						freq.addValue(entry.getKey());
			
			int first = 0;
			int second = 0;
			
			// find the first and second most occurring year values
			for ( Entry<Comparable<?>, Long> entry : freq.sortByValue() ) {
				
				if (first == 0) {
					
					first = Integer.valueOf((String) entry.getKey());
					continue;
				}
				if (second == 0) {
					
					second = Integer.valueOf((String) entry.getKey());
					break;
				}
			}
			
			defactoTimePeriod = new DefactoTimePeriod(Math.min(first, second), Math.max(first, second));
		}
		
		return defactoTimePeriod;
	}
}
