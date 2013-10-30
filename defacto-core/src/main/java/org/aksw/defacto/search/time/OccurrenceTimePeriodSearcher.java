package org.aksw.defacto.search.time;

import java.util.Map;
import java.util.Map.Entry;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoTimePeriod;

public class OccurrenceTimePeriodSearcher implements DefactoTimePeriodSearcher {

	@Override
	public DefactoTimePeriod getTimePoint(Evidence evidence) {
		
		Map.Entry<String, Long> maxEntry = null;

		for (Map.Entry<String, Long> entry : evidence.smallContextYearOccurrences.entrySet())
		    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
		        maxEntry = entry;
		
		return maxEntry == null ? null : new DefactoTimePeriod(maxEntry.getKey(), maxEntry.getKey());
	}

	@Override
	public DefactoTimePeriod getTimePeriod(Evidence evidence) {
		// TODO Auto-generated method stub
		return null;
	}
}
