/**
 * 
 */
package org.aksw.defacto.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.defacto.model.DefactoModel;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Lorenz Buehmann
 *
 */
public class FactBenchExamplesLoader {
	
	private static File examplesFolder = new File(FactBenchExamplesLoader.class.getClassLoader().getResource("examples").getPath());
	private static Set<FactBenchExample> examples;
	
	public static Set<FactBenchExample> loadExamples(){
		if(examples == null){
			Set<FactBenchExample> examples = new TreeSet<FactBenchExample>();
			try {
				for (File propertyFolder : examplesFolder.listFiles()) {
					for (File file : propertyFolder.listFiles()) {
						Model model = ModelFactory.createDefaultModel();
						model.read(new FileInputStream(file), null, "TURTLE");
						
						QueryExecution qe = QueryExecutionFactory.create(
								"SELECT * WHERE {"
								+ "?s ?p ?o. "
								+ "?s <http://www.w3.org/2000/01/rdf-schema#label> ?ls. "
								+ "?o <http://www.w3.org/2000/01/rdf-schema#label> ?lo. "
								+ "FILTER(STRSTARTS(STR(?p), 'http://dbpedia.org/ontology/')) "
								+ "FILTER(LANGMATCHES(LANG(?ls),'en'))"
								+ "FILTER(LANGMATCHES(LANG(?lo),'en'))"
								+ "}"
								, model);
						ResultSet rs = qe.execSelect();
						if(rs.hasNext()){
							QuerySolution qs = rs.next();
							
							Resource subject = qs.getResource("s");
							Resource object = qs.getResource("o");
							Resource predicate = qs.getResource("p");
							
							String subjectLabel = rs.next().getLiteral("ls").getLexicalForm();
							String objectLabel = rs.next().getLiteral("lo").getLexicalForm();
							
							String fact = subjectLabel + ", " + propertyFolder.getName() + ", "  + objectLabel;
							examples.add(new FactBenchExample(
									Triple.create(subject.asNode(), predicate.asNode(), object.asNode()), 
									fact, 
									new DefactoModel(model, fact, true, Lists.newArrayList("en", "fr", "de"))));
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return examples;
	}
}
