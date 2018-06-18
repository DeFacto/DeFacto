/**
 * 
 */
package org.defacto.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aksw.defacto.evaluation.measure.PrecisionRecallFMeasure;
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

//	@Test
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
	
	@Test
	public void testPRF()  {
		
		Set<Integer> relevantYears = new HashSet<Integer>(Arrays.asList(2000, 2001, 2002));
		Set<Integer> retrievedYears = new HashSet<Integer>(Arrays.asList(2000, 2001, 2002));
		
		PrecisionRecallFMeasure scorer = new PrecisionRecallFMeasure(relevantYears, retrievedYears);
		assertEquals("we found only correct years == 1", 1D, scorer.precision.doubleValue(), 0.0);
		assertEquals("we found all correct years == 1", 1D, scorer.recall.doubleValue(), 0.0);
		assertEquals("so f1 == 1",  1D, scorer.fmeasure.doubleValue(), 0.0);
		
		relevantYears = new HashSet<Integer>(Arrays.asList(2003, 2004, 2005));
		retrievedYears = new HashSet<Integer>(Arrays.asList(2000, 2001, 2002));
		
		scorer = new PrecisionRecallFMeasure(relevantYears, retrievedYears);
		assertEquals("we found no correct year == 0", 0D, scorer.precision.doubleValue(), 0.0);
		assertEquals("we found non correct years == 0", 0D, scorer.recall.doubleValue(), 0.0);
		assertEquals("we found non correct years == 0", 0D, scorer.fmeasure.doubleValue(), 0.0);
		
		relevantYears = new HashSet<Integer>(Arrays.asList(2003, 2004, 2005));
		retrievedYears = new HashSet<Integer>(Arrays.asList(2001, 2002, 2003, 2006));
		
		scorer = new PrecisionRecallFMeasure(relevantYears, retrievedYears);
		assertEquals("only 1 of 4 years is correct", 0.25D, scorer.precision.doubleValue(), 0.00001);
		assertEquals("we found 1 of 3 correct years", 0.333333D, scorer.recall.doubleValue(), 0.00001);
		assertEquals("so the fmeasure is not so good", 0.2857, scorer.fmeasure.doubleValue(), 0.0001);
		
		relevantYears = new HashSet<Integer>(Arrays.asList(2003, 2004, 2005));
		retrievedYears = new HashSet<Integer>();
		
		scorer = new PrecisionRecallFMeasure(relevantYears, retrievedYears);
		assertTrue(Double.isNaN(scorer.precision));
		assertEquals("we found 1 of 3 correct years", 0.0, scorer.recall.doubleValue(),0.0);
		assertEquals("so the fmeasure is not so good", 0.0, scorer.fmeasure.doubleValue(), 0.0);
	}
}
