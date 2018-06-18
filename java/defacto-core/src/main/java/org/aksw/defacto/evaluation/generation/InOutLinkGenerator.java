/**
 * 
 */
package org.aksw.defacto.evaluation.generation;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.util.BufferedFileReader;
import org.aksw.defacto.util.BufferedFileWriter;
import org.aksw.defacto.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.defacto.util.Encoder.Encoding;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class InOutLinkGenerator {

	public static void main(String[] args) {
		
		Defacto.init();
		
		BufferedFileReader reader = new BufferedFileReader(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/links_inbound_en.tsv", Encoding.UTF_8);
		BufferedFileWriter writer = new BufferedFileWriter(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/links_inbound_en.ttl", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
		String line;
		while ( (line = reader.readLine()) != null ) {
			
			String[] parts = line.split("\t");
			
			writer.write(String.format("<%s> <http://dbpedia.org/ontology/numberOfInboundLinks> \"%s\"^^<http://www.w3.org/2001/XMLSchema#integer> .", 
					Constants.DBPEDIA_RESOURCE_NAMESPACE + parts[0], parts[1]));
		}
		reader.close();
		writer.close();
		
		reader = new BufferedFileReader(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/links_outbound_en.tsv", Encoding.UTF_8);
		writer = new BufferedFileWriter(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/links_outbound_en.ttl", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
		while ( (line = reader.readLine()) != null ) {
			
			String[] parts = line.split("\t");
			
			writer.write(String.format("<%s> <http://dbpedia.org/ontology/numberOfOutboundLinks> \"%s\"^^<http://www.w3.org/2001/XMLSchema#integer> .", 
					Constants.DBPEDIA_RESOURCE_NAMESPACE + parts[0], parts[1]));
		}
		reader.close();
		writer.close();
	}
}

