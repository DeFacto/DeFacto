import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.util.Frequency;
import org.aksw.defacto.util.SparqlUtil;
import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;



public class ModelEvaluation {

    /**
     * @param args
     * @throws IOException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        List<String> playerUris = FileUtils.readLines(new File("/Users/gerb/Development/workspaces/experimental/temporal/gold_standard.csv"), "UTF-8");
        Frequency freq  = new Frequency();
        System.out.println("player\tcountry");
        for ( String playerUri : playerUris ) {
        	
        	String query = String.format("SELECT distinct ?country where { " +
        			"{ <%s> <http://dbpedia.org/ontology/birthPlace> ?country . " +  
        			" ?country rdf:type <http://dbpedia.org/ontology/Country> } UNION { <%s> <http://dbpedia.org/ontology/birthPlace> ?place . ?place <http://dbpedia.org/ontology/isPartOf> ?country . ?country rdf:type <http://dbpedia.org/ontology/Country> } } ", playerUri, playerUri);
        	
        	ResultSet results = SparqlUtil.executeSelectQuery("http://dbpedia.org/sparql", "http://dbpedia.org", query);
        	
        	String country = "";
        	while ( results.hasNext() ) {
        		
        		QuerySolution s = results.next();
        		freq.addValue(s.get("country") + "");
        		country = s.get("country") + "";
        	}
        	System.out.println(playerUri + "\t" + country);
        	
        	Thread.sleep(200);
        }
        for  ( Entry<Comparable<?>, Long> entry : freq.sortByValue()) {
    		
    		System.out.println(entry.getKey() + ": " + entry.getValue());
    	}
//        List<File> modelFiles = new ArrayList<File>(Arrays.asList(new File("resources/training/data/true").listFiles()));
        
//        List<File> pickedModels = new ArrayList<File>();
//        Set<Integer> randoms = new HashSet<Integer>();
//        
//        for ( int i = 0; i < 1000 ; i++ ) {
//            
////            if ( pickedModels.size() == 150) break;
//
//            Integer random = (int)(Math.random() * ((modelFiles.size()) + 1)) - 1;
//            if ( !randoms.contains(random) && !modelFiles.get(random).isHidden()) {
//                
//                randoms.add(random);
//                pickedModels.add(modelFiles.get(random));
//            }
//        }
//        Collections.shuffle(modelFiles);
//        
//        Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(new File("defacto.ini")));
//        
//        for ( File file : modelFiles ) {
//            
//            if ( !filenames.contains(file.getName() )) {
//
//                Model model = ModelFactory.createDefaultModel();
//                model.read(new FileReader(file), "", "TTL");
//                DefactoModel defactoModel = new DefactoModel(model, "name", false);
//                
//                System.out.println( "\t \t"+ file.getName() +"\t"+ defactoModel.getFact().toString().replace("[", "").replace("]", "").replace(",", "").replace(" ", "\t") );
//            }
//        }
        
        
        
        
    }
}
