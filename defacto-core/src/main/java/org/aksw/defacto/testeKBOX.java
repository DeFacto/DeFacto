package org.aksw.defacto;

import com.hp.hpl.jena.query.ResultSet;
//import org.aksw.kbox.kibe.KBox;
import org.dllearner.core.owl.KB;
import org.junit.BeforeClass;
//import org.aksw.kbox.kibe.tdb.TDB;

import java.io.File;
import java.net.URL;

/**
 * Created by esteves on 03.03.17.
 */
public class testeKBOX {

    @BeforeClass
    public static void setUp() throws Exception {
        File indexFile = File.createTempFile("knowledgebase","idx");
        URL[] filesToIndex = new URL[1];
        URL url = testeKBOX.class.getResource("/org/aksw/kbox/kibe/dbpedia_3.9.xml");
        filesToIndex[0] = url;
        //KBox.createIndex(indexFile, filesToIndex);
        //KBox.installKB(indexFile.toURI().toURL(), new URL("http://dbpedia39"));
        indexFile.deleteOnExit();

    }


    public static void main(String[] args) {



        try{
            //ResultSet rs =
                    //KBox.query("Select (count(distinct ?s) as ?n) where {?s ?p ?o}", new URL("http://dbpedia39"));
        }catch (Exception e){
            System.out.print(e);
        }

    }
}
