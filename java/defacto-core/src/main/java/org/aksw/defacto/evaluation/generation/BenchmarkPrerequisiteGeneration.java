/**
 * 
 */
package org.aksw.defacto.evaluation.generation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.util.BufferedFileReader;
import org.aksw.defacto.util.BufferedFileWriter;
import org.aksw.defacto.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.defacto.util.Encoder.Encoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.namespace.SKOS;
import org.semanticweb.yars.nx.namespace.XSD;
import org.semanticweb.yars.nx.parser.NxParser;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class BenchmarkPrerequisiteGeneration {

	public static Map<String,Set<String>> freebaseTodbpedia = new HashMap<String,Set<String>>();
	
	public static Set<String> SUPPORTED_LANGUAGES = new HashSet<String>(Arrays.asList("/lang/fr", "/lang/en", "/lang/de"));
	
	public static Map<String,SurfaceFormResource> resources = new HashMap<String,SurfaceFormResource>();
	
	public static Map<String, Set<String>> FR_SURFACE_FORMS = new HashMap<String, Set<String>>();
	public static Map<String, Set<String>> DE_SURFACE_FORMS = new HashMap<String, Set<String>>();
	public static Map<String, Set<String>> EN_SURFACE_FORMS = new HashMap<String, Set<String>>();
	
	public static Map<String,Integer> uriWithoutPrefix2InboundLinksNumber = new HashMap<String,Integer>();
	public static Map<String,Integer> uriWithoutPrefix2OutboundLinksNumber = new HashMap<String,Integer>();
	
	private static int SURFACE_FORM_MIN_LENGTH = 4 ;
	
	public BenchmarkPrerequisiteGeneration() throws JSONException, IOException {
		
		System.out.print("Start loading freebase-dbpedia mapping ... ");
		loadFreebaseToDbpediaMapping();
		System.out.println("DONE!");
		
		System.out.print("Start loading dbpedia surface forms ... ");
		loadDbpediaSurfaceForms();
		System.out.println("DONE!");
		
		System.out.print("Start loading interlanguage links ... ");
		loadDbpediaInterlanguageLinks();
		System.out.println("DONE!");
	}
	
	public static void main(String[] args) throws JSONException, IOException {
		
		Defacto.init();
		new BenchmarkPrerequisiteGeneration();
	}

	/**
	 * 
	 * @param model
	 */
	public static void setPrefixes(Model model) {
		
		model.setNsPrefix("fbase", Constants.FREEBASE_RESOURCE_NAMESPACE);
//		model.setNsPrefix("dbr", Constants.DBPEDIA_RESOURCE_NAMESPACE);
		model.setNsPrefix("dbo", Constants.DBPEDIA_ONTOLOGY_NAMESPACE);
		model.setNsPrefix("owl", OWL.NS);
		model.setNsPrefix("rdfs", RDFS.getURI());
		model.setNsPrefix("xsd", XSD.NS);
		model.setNsPrefix("skos", SKOS.NS);
//		model.setNsPrefix("de-dbr", "http://de.dbpedia.org/resource/");
//		model.setNsPrefix("fr-dbr", "http://fr.dbpedia.org/resource/");		
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public static void loadUriScores() throws IOException{
		
		if ( new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/inbound_links_en.tsv").exists() ) {
			
			System.out.println("Reading inbound links!");
			BufferedFileReader reader = new BufferedFileReader(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/inbound_links_en.tsv", Encoding.UTF_8);
			String line;
			while ( (line = reader.readLine()) != null ) {
				
				String[] parts = line.split("\t");
				uriWithoutPrefix2InboundLinksNumber.put(parts[0], Integer.valueOf(parts[1]));
			}
			reader.close();
		}
		else {

			System.out.println("Generating inbound links!");
			for ( File file : new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/pagelinks/").listFiles(new FilenameFilter() { 
				public boolean accept(File dir, String filename) { return filename.endsWith(".ttl"); }} )) {

				NxParser nxp = new NxParser(new FileInputStream(file));
				System.out.println("Processing file: " + file.getName());
				
				while (nxp.hasNext()) {
		        	
		        	Node[] next = nxp.next();
		        	
		        	String object  = next[2].toString().replace(Constants.DBPEDIA_RESOURCE_NAMESPACE, "");
		        	
		        	if ( !uriWithoutPrefix2InboundLinksNumber.containsKey(object) )  uriWithoutPrefix2InboundLinksNumber.put(object, 1);
		        	else uriWithoutPrefix2InboundLinksNumber.put(object, uriWithoutPrefix2InboundLinksNumber.get(object) + 1);
		        }
			}
			BufferedFileWriter writer = new BufferedFileWriter(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/inbound_links_en.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
			for ( Map.Entry<String, Integer> entry : uriWithoutPrefix2InboundLinksNumber.entrySet() ){

				writer.write(entry.getKey() + "\t" + entry.getValue());
			}
			writer.close();

		}
		
		if ( new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/outbound_links_en.tsv").exists() ) {
			
			System.out.println("Reading outbound links!");
			BufferedFileReader reader = new BufferedFileReader(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/outbound_links_en.tsv", Encoding.UTF_8);
			String line;
			while ( (line = reader.readLine()) != null ) {
				
				String[] parts = line.split("\t");
				uriWithoutPrefix2OutboundLinksNumber.put(parts[0], Integer.valueOf(parts[1]));
			}
			reader.close();
		}
		else {

			System.out.println("Generating outbound links!");
			for ( File file : new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/pagelinks/").listFiles(new FilenameFilter() { 
				public boolean accept(File dir, String filename) { return filename.endsWith(".ttl"); }} )) {

				NxParser nxp = new NxParser(new FileInputStream(file));
				System.out.println("Processing file: " + file.getName());
				
				while (nxp.hasNext()) {
		        	
		        	Node[] next = nxp.next();
		        	
		        	String subject  = next[0].toString().replace(Constants.DBPEDIA_RESOURCE_NAMESPACE, "");
		        	
		        	if ( !uriWithoutPrefix2OutboundLinksNumber.containsKey(subject) )  uriWithoutPrefix2OutboundLinksNumber.put(subject, 1);
		        	else uriWithoutPrefix2OutboundLinksNumber.put(subject, uriWithoutPrefix2OutboundLinksNumber.get(subject) + 1);
		        }
			}
			BufferedFileWriter writer = new BufferedFileWriter(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/outbound_links_en.tsv", Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
			for ( Map.Entry<String, Integer> entry : uriWithoutPrefix2OutboundLinksNumber.entrySet() ){

				writer.write(entry.getKey() + "\t" + entry.getValue());
			}
			writer.close();

		}
	}

	/**
	 * 
	 * @param model
	 * @param bnode
	 * @param property
	 * @param date
	 */
	public static void addDates(Model model, Resource bnode, Property property, String date) {
		
		// year only
		if ( date.matches("^[0-9]{4}$") ) {
			
			model.add(bnode, property, date, XSDDatatype.XSDgYear);
		}
		// year month day
		else if ( date.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}$") ) {
			
			model.add(bnode, property, date, XSDDatatype.XSDdate);
		}
		// year month
		else if ( date.matches("^[0-9]{4}-[0-9]{2}$") ) {
			
			model.add(bnode, property, date, XSDDatatype.XSDgYearMonth);
		}
		else if ( date.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}T00:00:00.*") ) {
			
			model.add(bnode, property, date, XSDDatatype.XSDgYear);
		}
		// not supported
		else {

			System.err.println("Date not valid: " + date);
		}
	}

	/**
	 * 
	 * @param model
	 * @param first
	 */
	public static void addOwlSameAs(Model model, Resource resource) {
		
		Set<String> urisWithoutPrefix = freebaseTodbpedia.get(resource.getURI().replace(Constants.FREEBASE_RESOURCE_NAMESPACE, ""));
		
		// no mapping -> no owl:sameAs
		if ( urisWithoutPrefix != null ) {
			
			Set<SurfaceFormResource> sfrs = new HashSet<BenchmarkPrerequisiteGeneration.SurfaceFormResource>();
			for ( String uri : urisWithoutPrefix ) {
				if ( resources.containsKey(uri) ) sfrs.add(resources.get(uri));
			}
			
			if ( sfrs.size() > 0)  {
				
				for (SurfaceFormResource sfr : sfrs) {

					Resource r = model.createResource(Constants.DBPEDIA_RESOURCE_NAMESPACE + sfr.enId);
					if ( !resource.equals(r) )model.add(resource, OWL.sameAs, r);
					model.add(resource, OWL.sameAs, model.createResource(Constants.DE_DBPEDIA_RESOURCE_NAMESPACE + sfr.deId));
					model.add(resource, OWL.sameAs, model.createResource(Constants.FR_DBPEDIA_RESOURCE_NAMESPACE + sfr.frId));
					
					for ( String label : sfr.enLabel ) 
						model.add(resource, model.createProperty(SKOS.NS + "altLabel"), label, "en");
					for ( String label : sfr.deLabel ) 
						model.add(resource, model.createProperty(SKOS.NS + "altLabel"), label, "de");
					for ( String label : sfr.frLabel )
						model.add(resource, model.createProperty(SKOS.NS + "altLabel"), label, "fr");
				}
			}
			// no interlanguage links available, so we use the english surface forms as backup
			else {
				
				for ( String uri : urisWithoutPrefix ) {

					Resource r = model.createResource(Constants.DBPEDIA_RESOURCE_NAMESPACE + uri);
					model.add(resource, OWL.sameAs, r);
					
					if ( EN_SURFACE_FORMS.containsKey(uri) ) {
						
						for ( String label : EN_SURFACE_FORMS.get(uri)) model.add(r, model.createProperty(SKOS.NS + "altLabel"), label, "en");
					}
					else {
						
						System.out.println("No surface form for: <" + uri + "> found");
					}
				}
			}
		}
		else {
			
			if ( !resource.getURI().startsWith(Constants.FREEBASE_RESOURCE_NAMESPACE) ) {
				
				String enUri = resource.getURI();
				String enUriNoPrefix = enUri.replace(Constants.DBPEDIA_RESOURCE_NAMESPACE, "");
				
				SurfaceFormResource sfr = resources.get(enUriNoPrefix);
				
				if ( sfr != null ) {
					
					Resource r = model.createResource(Constants.DBPEDIA_RESOURCE_NAMESPACE + sfr.enId);
					if ( !resource.equals(r) )model.add(resource, OWL.sameAs, r);
					if ( sfr.deId != null && !sfr.deId.isEmpty() ) model.add(resource, OWL.sameAs, model.createResource(Constants.DE_DBPEDIA_RESOURCE_NAMESPACE + sfr.deId));
					if ( sfr.frId != null && !sfr.frId.isEmpty() ) model.add(resource, OWL.sameAs, model.createResource(Constants.FR_DBPEDIA_RESOURCE_NAMESPACE + sfr.frId));
					
					for ( String label : sfr.enLabel ) 
						model.add(resource, model.createProperty(SKOS.NS + "altLabel"), label, "en");
					for ( String label : sfr.deLabel ) 
						model.add(resource, model.createProperty(SKOS.NS + "altLabel"), label, "de");
					for ( String label : sfr.frLabel )
						model.add(resource, model.createProperty(SKOS.NS + "altLabel"), label, "fr");
				}
			}
		}
	}

	/**
	 * 
	 * @param model
	 * @param resource
	 * @param names
	 * @throws JSONException
	 */
	public static void addNames(Model model, Resource resource, JSONArray names) throws JSONException {
		
		for (int i = 0; i < names.length(); i++) {
			
			JSONObject language = names.getJSONObject(i);
			
			if ( SUPPORTED_LANGUAGES.contains(language.get("lang")) ) {
				
				model.add(resource, RDFS.label, language.get("value").toString(), language.get("lang").toString().replace("/lang/", ""));
			}
		}
	}
	
	/**
	 * 
	 * @param model
	 * @param resource
	 * @param labels
	 */
	public static void addNames(Model model, Resource resource, Map<String,String> labels){
		
		String fallback = labels.get("en");
		
		for (Map.Entry<String, String> entry : labels.entrySet()) {
			
			String label = entry.getValue();
			String lang	 = entry.getKey();
			
			model.add(resource, RDFS.label, label != null && !label.isEmpty() ? label : fallback , lang);
		}
	}
	
	/**
	 * 
	 * @param model
	 * @param resource
	 * @param names
	 * @throws JSONException
	 */
	public static void addAliases(Model model, Resource resource, JSONArray names) throws JSONException {
		
		for (int i = 0; i < names.length(); i++) {
			
			JSONObject language = names.getJSONObject(i);
			
			if ( SUPPORTED_LANGUAGES.contains(language.get("lang")) ) {
				
				model.add(resource, model.createProperty(SKOS.NS + "altLabel"), language.get("value").toString(), language.get("lang").toString().replace("/lang/", ""));
			}
		}
	}
	
	private void loadDbpediaInterlanguageLinks() throws FileNotFoundException {

		NxParser nxp = new NxParser(new FileInputStream(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/interlanguage_links_en.ttl"));
        while (nxp.hasNext()) {
        	
        	Node[] next = nxp.next();
        	
//        	if ( i++ % 10000 == 0 ) System.out.println("Line " + i);
        	
        	String enUriWithoutPrefix	= next[0].toString().replace(Constants.DBPEDIA_RESOURCE_NAMESPACE, "");
        	String langUriWithPrefix	= next[2].toString();
        	
        	SurfaceFormResource sfr = !resources.containsKey(enUriWithoutPrefix) ? null : resources.get(enUriWithoutPrefix);
        	
        	if ( langUriWithPrefix.startsWith("http://de.") ) {
    			
        		if ( sfr == null)
        			sfr = new SurfaceFormResource(enUriWithoutPrefix, "", "");
        		
    			Set<String> forms = DE_SURFACE_FORMS.get(langUriWithPrefix.replace(Constants.DE_DBPEDIA_RESOURCE_NAMESPACE, ""));
    			sfr.deLabel = forms != null ? forms : new HashSet<String>();
    			sfr.deId = langUriWithPrefix.replace(Constants.DE_DBPEDIA_RESOURCE_NAMESPACE, "");
    		}
    		else if ( langUriWithPrefix.startsWith("http://fr.") ) {
    			
    			if ( sfr == null)
    				sfr = new SurfaceFormResource(enUriWithoutPrefix, "", "");
    			
    			Set<String> forms = FR_SURFACE_FORMS.get(langUriWithPrefix.replace(Constants.FR_DBPEDIA_RESOURCE_NAMESPACE, ""));
    			sfr.frLabel = forms != null ? forms : new HashSet<String>();
    			sfr.frId = langUriWithPrefix.replace(Constants.FR_DBPEDIA_RESOURCE_NAMESPACE, "");
    		}
    			
			if ( sfr == null ) {
				
				sfr = new SurfaceFormResource(enUriWithoutPrefix, "", "");
				Set<String> forms = EN_SURFACE_FORMS.get(enUriWithoutPrefix);
	        	sfr.enLabel = forms != null ? forms : new HashSet<String>();
			}
        	
        	if ( !resources.containsKey(enUriWithoutPrefix) ) resources.put(enUriWithoutPrefix, sfr);
        }
	}

	/**
	 * @throws FileNotFoundException 
	 * 
	 */
	private void loadFreebaseToDbpediaMapping() throws FileNotFoundException {

		NxParser nxp = new NxParser(new FileInputStream(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "freebase/freebase_links.nt"));
		
        while (nxp.hasNext()) {
        	
        	Node[] next = nxp.next();
        	
        	String dbpediaSubject	= next[0].toString().replace(Constants.DBPEDIA_RESOURCE_NAMESPACE, "");
        	String freebaseObject	= next[2].toString().replace(Constants.FREEBASE_RESOURCE_NAMESPACE, "");
        	
        	if ( !freebaseTodbpedia.containsKey(freebaseObject) ) freebaseTodbpedia.put(freebaseObject, new HashSet<String>());
        	freebaseTodbpedia.get(freebaseObject).add(dbpediaSubject);
        }
	}
	
	private void loadDbpediaSurfaceForms() throws IOException {

		loadDbpediaSurfaceFormsForLanguage("de", DE_SURFACE_FORMS);
		loadDbpediaSurfaceFormsForLanguage("fr", FR_SURFACE_FORMS);
		loadDbpediaSurfaceFormsForLanguage("en", EN_SURFACE_FORMS);
	}

	private void loadDbpediaSurfaceFormsForLanguage(String language, Map<String, Set<String>> surfaceForms) throws IOException {
		
		LineIterator it = FileUtils.lineIterator(new File(Defacto.DEFACTO_CONFIG.getStringSetting("eval", "data-directory") + "dbpedia/"+language+"_surface_forms.tsv"), "UTF-8");

		String uri = "http://"+language+".dbpedia.org/resource/";
		if ( uri.startsWith("http://en.dbpedia") ) uri = "http://dbpedia.org/resource/";
		
		while (it.hasNext()) {

			String[] lineParts = it.nextLine().split("\t");
			String[] surfaceFormsPart = Arrays.copyOfRange(lineParts, 1, lineParts.length);
			surfaceForms.put(lineParts[0].replace(uri, ""), new HashSet<String>());
			
			for (String surfaceForm : surfaceFormsPart) 
				if (surfaceForm.length() >= SURFACE_FORM_MIN_LENGTH && !lineParts[0].equals("http://dbpedia.org/resource/List_of_") ) 
					surfaceForms.get(lineParts[0].replace(uri, "")).add(surfaceForm);
		}
	}	
	
	private class SurfaceFormResource {
		
		public SurfaceFormResource(String enUriWithoutPrefix, String deUriWithoutPrefix, String frUriWithoutPrefix) {
			this.enId = enUriWithoutPrefix;
			this.frId = frUriWithoutPrefix;
			this.deId = deUriWithoutPrefix;
		}
		public String enId;
		public String frId;
		public String deId;
		
		public Set<String> enLabel = new HashSet<String>();
		public Set<String> deLabel = new HashSet<String>();
		public Set<String> frLabel = new HashSet<String>();
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			
			String output = "EN-ID: " + enId + " DE-ID: " + deId+ " FR-ID: " + frId;;
//			for (Map.Entry<String, List<SurfaceFormResource>> entry : languageToSurfaceForms.entrySet()) {
//				
//				output += "\t" + entry.getKey() + "\n";
//				
//				for (SurfaceFormResource s : entry.getValue()) {
//					
//					output += "\t\t" + s + "\n";
//				}
//			}
			
			return output;
		}
	}
}

