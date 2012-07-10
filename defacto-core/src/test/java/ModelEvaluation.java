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
import java.util.Set;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.DefactoModel;
import org.aksw.defacto.config.DefactoConfig;
import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;



public class ModelEvaluation {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {

        List<String> filenames = FileUtils.readLines(new File("resources/properties/evaluated_properties.txt"), "UTF-8");
        List<File> modelFiles = new ArrayList<File>(Arrays.asList(new File("resources/training/data/true").listFiles()));
        
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
        Collections.shuffle(modelFiles);
        
        Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(new File("defacto.ini")));
        
        for ( File file : modelFiles ) {
            
            if ( !filenames.contains(file.getName() )) {

                Model model = ModelFactory.createDefaultModel();
                model.read(new FileReader(file), "", "TTL");
                DefactoModel defactoModel = new DefactoModel(model, "name", false);
                
                System.out.println( "\t \t"+ file.getName() +"\t"+ defactoModel.getFact().toString().replace("[", "").replace("]", "").replace(",", "").replace(" ", "\t") );
            }
        }
    }
}
