/**
 * 
 */
package org.aksw.defacto.comp;

import java.util.Comparator;

import org.aksw.defacto.evaluation.configuration.Configuration;

/**
 * @author Daniel Gerber <daniel.gerber@deinestadtsuchtdich.de>
 *
 */
public class FMeasureComparator implements Comparator<Configuration>{

	public int compare(Configuration o1, Configuration o2) {
		return o2.fMeasure.compareTo(o1.fMeasure);
	}
}
