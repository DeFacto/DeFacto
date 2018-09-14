package org.aksw.defacto.search.time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoTimePeriod;

public class GlobalTimePeriodSearcher implements DefactoTimePeriodSearcher {

	@Override
	public DefactoTimePeriod getTimePoint(Evidence evidence) {
		
		Map.Entry<String, Long> maxEntry = null;

		for (Map.Entry<String, Long> entry : evidence.getPreferedContext().entrySet()) {
			
			if (maxEntry == null) maxEntry = entry;
			else {
				
//				Double entryNorm = TimeUtil.getGlobalNormalizedPopularity(Integer.valueOf(entry.getKey()));
//				Double maxNorm = TimeUtil.getGlobalNormalizedPopularity(Integer.valueOf(maxEntry.getKey()));
				Double entryNorm = TimeUtil.getGlobalNormalizedRootPopularity(Integer.valueOf(entry.getKey()));
				Double maxNorm = TimeUtil.getGlobalNormalizedRootPopularity(Integer.valueOf(maxEntry.getKey()));
//				Double entryNorm = TimeUtil.getGlobalNormalizedLogPopularity(Integer.valueOf(entry.getKey()));
//				Double maxNorm = TimeUtil.getGlobalNormalizedLogPopularity(Integer.valueOf(maxEntry.getKey()));
				
				Double entryPopularity = entryNorm != 0 ? entry.getValue() / entryNorm : 0;
				Double maxEntryPopularity = maxNorm != 0 ? maxEntry.getValue() / maxNorm : 0;
				
				if ( entryPopularity > maxEntryPopularity ) maxEntry = entry;
			}
		}
		    
		
		return maxEntry == null ? null : new DefactoTimePeriod(maxEntry.getKey(), maxEntry.getKey());
	}

	@Override
	public DefactoTimePeriod getTimePeriod(Evidence evidence) {
		
		DefactoTimePeriod defactoTimePeriod = PatternTimePeriodSearcher.findTimePeriod(evidence);
		if ( defactoTimePeriod == null ) {
			
			List<Score> scores = new ArrayList<>();
			for ( Entry<String, Long> entry : evidence.getPreferedContext().entrySet()) {
				
//				Double entryNorm = TimeUtil.getGlobalNormalizedPopularity(Integer.valueOf(entry.getKey()));
				Double entryNorm = TimeUtil.getGlobalNormalizedRootPopularity(Integer.valueOf(entry.getKey()));
//				Double entryNorm = TimeUtil.getGlobalNormalizedLogPopularity(Integer.valueOf(entry.getKey()));
				scores.add(new Score(entry.getKey(), entryNorm != 0 ? entry.getValue() / entryNorm : 0));
			}
			
			Collections.sort(scores, new Comparator<Score>(){

				@Override
				public int compare(Score o1, Score o2) {
					return o2.score.compareTo(o1.score);
				}
			});
			
			// only one year found so just take this
			if ( scores.size() == 1) defactoTimePeriod = new DefactoTimePeriod(scores.get(0).year, scores.get(0).year);
			// no year found so take nothing
			else if ( scores.size() == 0 ) defactoTimePeriod = new DefactoTimePeriod(0, 0);
			// more then two years found, take first two and from oldest to newest
			else defactoTimePeriod = new DefactoTimePeriod(Math.min(scores.get(0).year, scores.get(1).year), Math.max(scores.get(0).year, scores.get(1).year));
		}
		
		return defactoTimePeriod;
	}
}
