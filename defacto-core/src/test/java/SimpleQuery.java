import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.tdb.TDBFactory;


public class SimpleQuery extends JFrame
{
	static JTextField textfield = new JTextField(20);
    static JTextArea  textarea = new JTextArea(30,100);
	private static SimpleQuery query;
	
    public SimpleQuery(){
    	
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Name Game");
        this.setLocation(500,400);
        this.setSize(200,300);

        JPanel panel = new JPanel();
        panel.add(textarea);

        JButton button = new JButton("Search");
        button.addActionListener(new ActionListener() {
        	
        	String defaultQuery = 
    				"PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
    				"PREFIX dbr: <http://dbpedia.org/resource/> \n" +
    				"PREFIX yago: <http://dbpedia.org/class/yago/> \n" +
    				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
    				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
    				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n\n" +
    				
    				"SELECT * \n" +
    				"FROM <http://dbpedia.org> \n" +
    				"WHERE { \n" +
    					"\t?person dbo:termPeriod ?timePeriod . \n" + 
    					"\t?timePeriod dbo:office ?office . \n" + 
    					"\t?timePeriod dbo:activeYearsStartDate ?from . \n" + 
    					"\t?timePeriod dbo:activeYearsEndDate ?to . \n" +
    					"\tFILTER (regex(?office, \"^Prime Minister of.*\")) \n" +
    				"}";
        	 
            public void actionPerformed(ActionEvent e) {
                
            	Query query = null;
            	
            	try {
            		
            		if ( textarea.getText() == null || textarea.getText().isEmpty() ) {
                		
                		query = QueryFactory.create(defaultQuery, Syntax.syntaxARQ);
                	}
                	else {
                		
                		query = QueryFactory.create(textarea.getText(), Syntax.syntaxARQ);
                	}
            	}
            	catch (QueryParseException qpe) {
            		
            		System.err.println(qpe.getMessage());
            		System.exit(0);
            	}
            	
//            	SimpleQuery.query.setVisible(false);
            	
            	Dataset dataset = TDBFactory.createDataset("/Users/gerb/Development/workspaces/experimental/dbpedia/store");
        		dataset.begin(ReadWrite.READ);
            	
            	QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
				ResultSet execSelect = queryExecution.execSelect();
				
				ResultSetFormatter.out(System.out, execSelect );
            }
        });    
        panel.add(button);

        this.add(panel);
        this.pack();
        this.setVisible(true);
    }
    
    public static void main( String[] args) {

    	try {
    		query = new SimpleQuery();	
    	}
    	catch (Exception e) { /* DANGER */}
        
    }
}