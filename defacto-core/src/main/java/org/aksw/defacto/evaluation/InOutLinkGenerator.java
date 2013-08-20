/**
 * 
 */
package org.aksw.defacto.evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.namespace.SKOS;
import org.semanticweb.yars.nx.namespace.XSD;
import org.semanticweb.yars.nx.parser.NxParser;

import com.github.gerbsen.encoding.Encoder.Encoding;
import com.github.gerbsen.file.BufferedFileReader;
import com.github.gerbsen.file.BufferedFileWriter;
import com.github.gerbsen.file.BufferedFileWriter.WRITER_WRITE_MODE;
import com.github.gerbsen.rdf.NtripleUtil;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

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

