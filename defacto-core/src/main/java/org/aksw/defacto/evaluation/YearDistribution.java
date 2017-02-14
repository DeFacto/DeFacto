/**
 * 
 */
package org.aksw.defacto.evaluation;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.search.engine.bing.AzureBingSearchEngine;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.util.BufferedFileWriter;
import org.aksw.defacto.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.defacto.util.Encoder.Encoding;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class YearDistribution {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		Defacto.init();
		
		List<String> nppWinners = FileUtils.readLines(new File(YearDistribution.class.getResource("/eval/nobel_peace_prize.tsv").getFile()), "UTF-8");
		
		Map<Integer,Long> yearToNumber = new HashMap<Integer,Long>();
		
		for ( String winner : nppWinners ) {
			
			String[] parts = winner.split("\t");
			String person = parts[0];
			String npp = parts[1];
			Integer year = Integer.valueOf(parts[2]);

			MetaQuery query0 = new MetaQuery(String.format("%s|-|%s|-|%s", person, "??? NONE ???", npp), new Pattern("??? NONE ???"));
			
			AzureBingSearchEngine engine = new AzureBingSearchEngine();
			
			Long count = engine.query(query0, null).getTotalHitCount();
			
			if ( yearToNumber.containsKey(year) ) yearToNumber.put(year, yearToNumber.get(year) + count);
			else yearToNumber.put(year, count);
			
	        System.out.println(year + "\t" + engine.query(query0, null).getTotalHitCount() + "\t" + person);
		}
		
		BufferedFileWriter w = new BufferedFileWriter(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "yearDistribution.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);		
		
		for ( Map.Entry<Integer, Long> entry : yearToNumber.entrySet() ) {
			
			w.write(entry.getKey() + "\t" + entry.getValue());
		}
		
		w.close();
	}
}
