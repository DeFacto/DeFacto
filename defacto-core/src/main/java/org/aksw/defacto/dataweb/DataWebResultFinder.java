package org.aksw.defacto.dataweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.aksw.defacto.util.LabeledTriple;
import org.apache.log4j.Logger;
import org.nnsoft.sameas4j.DefaultSameAsServiceFactory;
import org.nnsoft.sameas4j.Equivalence;
import org.nnsoft.sameas4j.SameAsService;
import org.nnsoft.sameas4j.SameAsServiceException;
import org.nnsoft.sameas4j.cache.InMemoryCache;

import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * 
 * Searches the data web for triples, which are similar to an input triple.
 * Currently, this is backed by sameAs.org
 * 
 * @author Jens Lehmann
 * TODO make  this a feature and move it to the correct package
 * TODO delete InformationFinder package
 */
public class DataWebResultFinder {

	private static Logger logger = Logger.getLogger(DataWebResultFinder.class);

	private int numberOfTriples = 0;
	private int numberOfRelevantTriples = 0;
	Set<String> relevantTriples = new TreeSet<String>();
	Set<String> distinctResources = new TreeSet<String>();
	private int labelCalls = 0;

	private static Set<String> notAllowedProperties = new HashSet<String>();
	static {

		notAllowedProperties.add("http://www.w3.org/2002/07/owl#sameAs");
		notAllowedProperties
				.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		notAllowedProperties.add("http://www.w3.org/2002/07/owl#sameAs");
		notAllowedProperties.add("http://www.w3.org/2002/07/owl#sameAs");
	}

	private Map<String, String> labelCache = new TreeMap<String, String>();

	public Map<LabeledTriple, Double> findSimilarTriples(LabeledTriple ltriple,
			double threshold) throws SameAsServiceException {

		// call sameAs.org
		SameAsService service = DefaultSameAsServiceFactory.createNew();
		service.setCache(new InMemoryCache());
		Equivalence equivalence = service.getDuplicates(URI.create(ltriple
				.getSubjectURI()));
		logger.info(ltriple.getSubjectURI() + " is same as " + equivalence);

		long startTime = System.currentTimeMillis();
		// retrieve Linked Data from all sources
		// List<LabeledTriple> ltriples = new LinkedList<LabeledTriple>();
		// switched to set, because for some reason we need to filter duplicates
		// for hash URIs
		Set<LabeledTriple> ltriples = new TreeSet<LabeledTriple>();
		for (URI uri : equivalence) {
			if (!uri.toString().startsWith("http://www.econbiz.de/")) // not
																		// able
																		// to
																		// provide
																		// proper
																		// rdf
				ltriples.addAll(readLinkedDataLabeled(uri.toString()));
		}
		long duration = System.currentTimeMillis() - startTime;
		System.out.println("number of all triples: " + numberOfTriples);
		System.out.println("number of relevant triples: "
				+ numberOfRelevantTriples);
		System.out.println("number of distinct relevant triples: "
				+ relevantTriples.size());
		System.out.println("number of distinct resources: "
				+ distinctResources.size());
		System.out.println("Linked Data label calls: " + labelCalls);
		System.out.println("time spend for retrieving resources: " + duration
				+ " ms");

		// score all triples, which we have obtained
		Map<LabeledTriple, Double> simTriples = new HashMap<LabeledTriple, Double>();
		for (LabeledTriple t : ltriples) {
			simTriples.put(t, score(ltriple, t));
		}
		return simTriples;
	}

	public List<LabeledTriple> readLinkedDataLabeled(String uri) {
		logger.info("Reading all statements from " + uri + ".");
		String subjectLabel = getLabel(uri);
		List<LabeledTriple> ltriples = new LinkedList<LabeledTriple>();
		// retrieve all statements from the URI
		Model m = readLinkedData(uri);
		if (m == null) {
			return ltriples;
		}
		numberOfTriples += m.size();
		System.out.println("returned triples: " + m.size());
		StmtIterator it = m.listStatements();
		int i = 0;
		while (it.hasNext()) {
			Statement st = it.next();
			// // currently, we are only looking at object properties, so we
			// // assume only object properties can be similar;
			if (st.getSubject().isURIResource()
					&& st.getObject().isURIResource()
					&& st.getSubject().getURI().toString().equals(uri)
					&& !st.getPredicate().getURI().startsWith(RDF.getURI())
					&& !st.getPredicate().getURI().startsWith(RDFS.getURI())
					&& !st.getPredicate().getURI().startsWith(OWL.getURI())
					&& !st.getPredicate().getURI()
							.startsWith("http://www.w3.org/2004/02/skos/core")
					&& !st.getPredicate().getURI()
							.startsWith("http://www.w3.org/2008/05/skos-xl")
					&& !st.getPredicate().getURI()
							.startsWith("http://purl.org/dc/terms/subject")
					&& !st.getPredicate()
							.getURI()
							.startsWith(
									"http://www.geonames.org/ontology#wikipediaArticle")
					&& !st.getPredicate()
							.getURI()
							.startsWith(
									"http://dbpedia.org/ontology/wikiPageExternalLink")
			// !st.getObject().asResource().getURI().startsWith("http://www.econbiz.de/")
			// &&
			// !st.getSubject().getURI().startsWith("http://www.econbiz.de/"))
			) {
				i++;
				System.out.println(st.getSubject().getNameSpace() + "+++"
						+ st.getPredicate().toString() + "+++"
						+ st.getObject().toString());
				// quick hack
				boolean test = relevantTriples.add(st.getSubject()
						.getNameSpace()
						+ "+++"
						+ st.getPredicate().toString()
						+ "+++" + st.getObject().toString());
				if (test) {
					distinctResources.add(st.getSubject().getURI().toString());
					distinctResources.add(st.getObject().asNode().getURI()
							.toString());
					distinctResources
							.add(st.getPredicate().getURI().toString());
				}
				numberOfRelevantTriples++;
				// System.out.println(st);
				String predicateLabel = getLabel(st.getPredicate().getURI());
				String objectLabel = getLabel(st.getObject().asResource()
						.getURI());
				LabeledTriple lt = new LabeledTriple(st.getSubject().getURI(),
						subjectLabel, st.getPredicate().getURI(),
						predicateLabel, st.getObject().asResource().getURI(),
						objectLabel);
				ltriples.add(lt);
			}
		}
		System.out.println("possibly relevant triples: " + i);
		return ltriples;
	}

	// retrieves the label from a resource via Linked Data (TODO: that should be
	// cached,
	// otherwise we end up doing very many Linked Data calls)
	public String getLabel(String uri) {
		logger.info("Getting label of " + uri + ".");

		// check whether we already have the label in our cache
		String lc = labelCache.get(uri);
		if (lc != null) {
			logger.info("Cache hit.");
			return lc;
		}
		labelCalls++;
		Model m = readLinkedData(uri);
		if (m == null) {
			// use local name if URI cannot be dereferenced
			return ModelFactory.createDefaultModel().createResource(uri)
					.getLocalName();
		}
		Property prefLabel = m
				.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel");
		Statement st = m.getResource(uri).getProperty(prefLabel);
		if (st != null) {
			return st.getObject().toString();
		}
		st = m.getResource(uri).getProperty(RDFS.label);
		// System.out.println(st);
		if (st != null) {
			return st.getObject().toString();
		} else {
			// the fallback is to create the label from the URI name
			return m.createResource(uri).getLocalName();
		}
	}

	// TODO: because we get all kinds of errors in Linked Data, this method
	// needs to be extended
	// quite a bit - maybe even an own project or using any23 as heavy-weight
	// dependency
	public Model readLinkedData(String uri) {
		Model model = ModelFactory.createDefaultModel();

		// replaced Jena by own implementation to have timeouts
		URL url;
		InputStream in = null;
		try {
			url = new URL(uri);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Accept", "application/rdf+xml");
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(5000);
			in = conn.getInputStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		System.out.println(inputStreamToString(in));
		
		try {
			model.read(in, "RDF/XML");
			
		} catch (Exception e4) {
			e4.printStackTrace();
			// implement own content negotiation to get N3/Turtle as fallback
			try {
				URL url2 = new URL(uri);
				URLConnection conn2 = url2.openConnection();
				conn2.setRequestProperty("Accept", "text/n3");
				conn2.setConnectTimeout(3000);
				conn2.setReadTimeout(5000);
				InputStream in2 = conn2.getInputStream();
				try {
					model.read(in2, "N3");
				} catch (Exception e3) {
					e3.printStackTrace();
					return null;
				}
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			return null;
		}

		// JenaReader jr = new JenaReader();
		// jr.read(model, uri);
		// } catch(JenaException e) {
		// model = ModelFactory.createDefaultModel();

		// }
		return model;
	}

	public double score(LabeledTriple triple1, LabeledTriple triple2) {
		// take q-grams over subject, predicate and object
		QGramsDistance distance = new QGramsDistance();
		double sVal = distance.getSimilarity(triple1.getSubjectLabel(),
				triple2.getSubjectLabel());
		double pVal = distance.getSimilarity(triple1.getPredicateLabel(),
				triple2.getPredicateLabel());
		double oVal = distance.getSimilarity(triple1.getObjectLabel(),
				triple2.getObjectLabel());
		return (sVal + pVal + oVal) / 3;
	}

	public static void main(String args[]) throws SameAsServiceException {

		DataWebResultFinder finder = new DataWebResultFinder();

		// test triple
		LabeledTriple ltriple = new LabeledTriple(
				"http://dbpedia.org/resource/Berlin", "Berlin",
				"http://dbpedia.org/ontology/leader", "leader",
				"http://dbpedia.org/resource/Klaus_Wowereit", "Klaus Wowereit");

		Map<LabeledTriple, Double> similarTriples = finder.findSimilarTriples(
				ltriple, 0.0);

		// Ordering<LabeledTriple> valueComparator =
		// Ordering.natural().onResultOf(Functions.forMap(similarTriples));
		// Map<LabeledTriple,Double> sortedMap =
		// ImmutableSortedMap.copyOf(similarTriples, valueComparator);

		for (Entry<LabeledTriple, Double> entry : similarTriples.entrySet()) {
			if (entry.getValue() > 0.0) {
				System.out.println(entry.getValue() + " " + entry.getKey());
			}
		}

	}
	
    private String inputStreamToString(InputStream in) {
	    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
	    StringBuilder stringBuilder = new StringBuilder();
	    String line = null;
	
	    try {
			while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line + "\n");
			}
	    bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	    return stringBuilder.toString();
    }	

}
