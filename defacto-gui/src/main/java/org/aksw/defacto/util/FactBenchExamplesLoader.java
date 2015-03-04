/**
 * 
 */
package org.aksw.defacto.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.reader.DefactoModelReader;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author Lorenz Buehmann
 *
 */
public class FactBenchExamplesLoader {
	
	
	private static final Logger logger = Logger.getLogger(FactBenchExamplesLoader.class.getName());
	
	private static File examplesFolder = new File(FactBenchExamplesLoader.class.getClassLoader().getResource("examples").getPath());
	private static Set<FactBenchExample> examples;
	
	public static Set<FactBenchExample> loadExamples(){
		if(examples == null){
			logger.info("Loading FactBench examples...");
			examples = new TreeSet<FactBenchExample>();
			try {
            File propertyFolder2[] = examplesFolder.listFiles();
            if(propertyFolder2!=null && propertyFolder2.length>0) {
               for (int i = 0; i < propertyFolder2.length; i++) {
               }
            }else
            {
               logger.info("Error while trying open examples folder: " + examplesFolder.toString());
            }

				for (File propertyFolder : examplesFolder.listFiles()) {
					for (File file : propertyFolder.listFiles()) {
						DefactoModel model = DefactoModelReader.readModel(file.getPath());
						Statement st = model.getFact();
						String fact = model.getSubjectLabel("en") + ", " + propertyFolder.getName() + ", "  + model.getObjectLabel("en");
						examples.add(new FactBenchExample(
									Triple.create(st.getSubject().asNode(), st.getPredicate().asNode(), st.getObject().asNode()), 
									fact, 
									model));
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			logger.info("...done.");
		}
		return examples;
	}
}
