/**
 * 
 */
package org.defacto.evaluation;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

import org.aksw.defacto.model.DefactoModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class DefactoModelTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws FileNotFoundException {
		
		Model model = ModelFactory.createDefaultModel();
        model.read(new FileReader(new File(DefactoModel.class.getResource("/eval/spouse_0.ttl").getFile())), "", "TTL");
    	
        DefactoModel defactoModel = new DefactoModel(model, "test", true, Arrays.asList("en"));
        
    	System.out.println(defactoModel);
    	System.out.println("S    : " + defactoModel.getSubjectLabels());
    	System.out.println("S_en : " + defactoModel.getSubjectLabel("en"));
    	System.out.println("SA   : " + defactoModel.getSubjectAltLabels());
    	System.out.println("SA_en: " + defactoModel.getSubjectAltLabels("en"));
    	System.out.println("O    : " + defactoModel.getObjectLabels());
    	System.out.println("O_en : " + defactoModel.getObjectLabel("en"));
    	System.out.println("OA   : " + defactoModel.getObjectAltLabels());
    	System.out.println("OA_en: " + defactoModel.getObjectAltLabels("en"));
    	
    	model = ModelFactory.createDefaultModel();
        model.read(new FileReader(new File(DefactoModel.class.getResource("/eval/award_0.ttl").getFile())), "", "TTL");
    	
        defactoModel = new DefactoModel(model, "test", true, Arrays.asList("en"));
        
    	System.out.println(defactoModel);
    	System.out.println("S    : " + defactoModel.getSubjectLabels());
    	System.out.println("S_en : " + defactoModel.getSubjectLabel("en"));
    	System.out.println("SA   : " + defactoModel.getSubjectAltLabels());
    	System.out.println("SA_en: " + defactoModel.getSubjectAltLabels("en"));
    	System.out.println("O    : " + defactoModel.getObjectLabels());
    	System.out.println("O_en : " + defactoModel.getObjectLabel("en"));
    	System.out.println("OA   : " + defactoModel.getObjectAltLabels());
    	System.out.println("OA_en: " + defactoModel.getObjectAltLabels("en"));
	}
}
