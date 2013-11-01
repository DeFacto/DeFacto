package org.aksw.defacto.search.time;

import java.util.Map;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoTimePeriod;

public class GlobalTimePeriodSearcher implements DefactoTimePeriodSearcher {

	@Override
	public DefactoTimePeriod getTimePoint(Evidence evidence) {
		
		Map.Entry<String, Long> maxEntry = null;

		for (Map.Entry<String, Long> entry : evidence.smallContextYearOccurrences.entrySet()) {
			
			if (maxEntry == null) maxEntry = entry;
			else {
				
				Double entryPopularity = entry.getValue() * TimeUtil.getGlobalNormalizedPopularity(Integer.valueOf(entry.getKey()));
				Double maxEntryPopularity = maxEntry.getValue() * TimeUtil.getGlobalNormalizedPopularity(Integer.valueOf(maxEntry.getKey()));
				
				if ((entryPopularity).compareTo(maxEntryPopularity) > 0) maxEntry = entry;
			}
		}
		    
		
		return maxEntry == null ? null : new DefactoTimePeriod(maxEntry.getKey(), maxEntry.getKey());
	}

	@Override
	public DefactoTimePeriod getTimePeriod(Evidence evidence) {
		// TODO Auto-generated method stub
		return null;
	}
}
