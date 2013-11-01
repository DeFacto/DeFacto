package org.aksw.defacto.search.time;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoTimePeriod;

public interface DefactoTimePeriodSearcher {

	DefactoTimePeriod getTimePoint(Evidence evidence);
	
	DefactoTimePeriod getTimePeriod(Evidence evidence);

}
