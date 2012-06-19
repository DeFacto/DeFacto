import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.provenance.util.ModelUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;



public class ModelEvaluation {

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {

        List<File> modelFiles = new ArrayList<File>(Arrays.asList(new File("resources/training/data/true").listFiles()));
        
        List<File> pickedModels = new ArrayList<File>();
        Set<Integer> randoms = new HashSet<Integer>();
        
        for ( int i = 0; i < 1000 ; i++ ) {
            
            if ( pickedModels.size() == 150) break;

            Integer random = (int)(Math.random() * ((modelFiles.size()) + 1)) - 1;
            if ( !randoms.contains(random) && !modelFiles.get(random).isHidden()) {
                
                randoms.add(random);
                pickedModels.add(modelFiles.get(random));
            }
        }
        Collections.sort(pickedModels);
        
        for ( File file : pickedModels ) {
            Model model = ModelFactory.createDefaultModel();
            model.read(new FileReader(file), "", "TTL");
            
            System.out.println( ","+ file.getName() +","+ ModelUtil.getFact(model).toString().replace("[", "").replace("]", "") );
        }
    }
}
