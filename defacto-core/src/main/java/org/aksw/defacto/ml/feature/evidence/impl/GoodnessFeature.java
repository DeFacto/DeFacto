/**
 * 
 */
package org.aksw.defacto.ml.feature.evidence.impl;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.sparql.metrics.DatabaseBackedSPARQLEndpointMetrics;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class GoodnessFeature extends AbstractEvidenceFeature {

    private static DatabaseBackedSPARQLEndpointMetrics metric = null;
	private static SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GoodnessFeature.class);
	static {
		
		try {
			Class.forName("com.mysql.jdbc.Driver");

            if (Defacto.DEFACTO_CONFIG == null)
                Defacto.init();

			String dbHost = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "DB_HOST");
			String dbPort = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "DB_PORT");
			String database = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "DB_NAME");
			String dbUser = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "DB_USER");
            String pw = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "DB_PWD");

            Connection conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database + "?" + "user=" + dbUser + "&password=" + pw);
			metric = new DatabaseBackedSPARQLEndpointMetrics(endpoint, "pmi-cache", conn);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    @Override
    public void extractFeature(Evidence evidence) {
    	
    	String subject = evidence.getModel().getDBpediaSubjectUri();
    	String object = evidence.getModel().getDBpediaObjectUri();
    	
    	double goodness = -1;
    	if ( subject != null && object != null  ) {
    		
    		goodness = metric.getGoodness(
    				new Individual(subject), new ObjectProperty(evidence.getModel().getPropertyUri()), new Individual(object));
    		
    		evidence.getFeatures().setValue(AbstractEvidenceFeature.GOODNESS, goodness);
    	}
    }
    
    
    public static void main(String[] args) {
		
    	ObjectProperty property = new ObjectProperty("http://dbpedia.org/ontology/author");
		Individual subject = new Individual("http://dbpedia.org/resource/The_Da_Vinci_Code");
		Individual object = new Individual("http://dbpedia.org/resource/Dan_Brown");

		LOGGER.info(String.valueOf(metric.getGoodness(subject, property, object)));
		LOGGER.info(String.valueOf(metric.getGoodness(subject, new ObjectProperty("http://dbpedia.org/ontology/writer"), object)));
	}
}