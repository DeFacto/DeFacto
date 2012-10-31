package org.aksw.defacto.search.fact;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.defacto.DefactoModel;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.apache.log4j.Logger;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class DefaultFactSearcher implements FactSearcher {

    private Map<String, Set<String>> urisToLabels;
    private Logger logger = Logger.getLogger(DefaultFactSearcher.class);
    
    private static DefaultFactSearcher INSTANCE;

    /**
     * 
     */
    private DefaultFactSearcher() {

        logger.info("Starting to load surface forms!");
        initializeSurfaceForms();
        logger.info("Finished to load surface forms!");
    }
    
    /**
     * this needs to be synchronized in order to avoid calling this
     * method from every crawl thread
     * 
     * @return
     */
    public static synchronized FactSearcher getInstance() {
        
        if ( DefaultFactSearcher.INSTANCE == null )
            DefaultFactSearcher.INSTANCE = new DefaultFactSearcher();
        
        return DefaultFactSearcher.INSTANCE;
    }
    
    @Override
    public void generateProofs(Evidence evidence, WebSite website, DefactoModel model, Pattern pattern) {

        String subjectUri   = model.getSubjectUri();
        String objectUri    = model.getObjectUri();
        
        Set<String> subjectLabels  = urisToLabels.get(subjectUri.replace("http://dbpedia.org/resource/", ""));
        Set<String> objectLabels   = urisToLabels.get(objectUri.replace("http://dbpedia.org/resource/", ""));
        
        // fallback on the labels provided by the input, no surface forms from dbpedia
        if ( subjectLabels == null ) subjectLabels = new HashSet<String>(Arrays.asList(model.getSubjectLabel()));
        if ( objectLabels == null ) objectLabels = new HashSet<String>(Arrays.asList(model.getObjectLabel()));
        
        // this walks threw all occurrences of the nlr of the pattern in the text
        for ( int index = website.getText().indexOf(pattern.naturalLanguageRepresentationWithoutVariables) ; 
                index >= 0 ; 
                index = website.getText().indexOf(pattern.naturalLanguageRepresentationWithoutVariables, index + 1)) {
            
            int middleOfPatternIndex = index + (pattern.naturalLanguageRepresentationWithoutVariables.length() / 2);
            
            int leftBoundary = middleOfPatternIndex - 200;
            int rightBoundary = middleOfPatternIndex + 200;
            
            // make sure we get the start and the end correct
            if ( website.getText().length() > rightBoundary && leftBoundary > 0 ) {
                
                String phrase = website.getText().substring(leftBoundary, rightBoundary);
                
                // take the smaller set, so if we dont find an entry for this we dont need to do that for the longer set again
                for ( String firstLabel : subjectLabels.size() >= objectLabels.size() ? objectLabels : subjectLabels) {
                    
                    if ( !phrase.contains(firstLabel) ) continue; // no need to look for other label
                    else {
                        
                        for ( String secondLabel : subjectLabels.size() >= objectLabels.size() ? subjectLabels : objectLabels) {
                            
                            if ( phrase.contains(secondLabel) ) {
                                
//                                evidence.addStructuredProof(new Proof(phrase, website, pattern));
                                System.out.println("Found phrase! Yippiejahey: " + phrase);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 
     */
    private void initializeSurfaceForms() {
        
        this.urisToLabels = new HashMap<String,Set<String>>(); 
        
        try {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("resources/cache/labels/en_uri_surface_form.tsv"))));
            String line;
            while ((line = reader.readLine()) != null) {
                
                String[] lineParts = line.split("\t");
                this.urisToLabels.put(lineParts[0], new HashSet<String>(Arrays.asList(Arrays.copyOfRange(lineParts, 1, lineParts.length))));
            }
            reader.close();
        }
        catch (FileNotFoundException e) {
            
            System.out.println("Install the surface form file to resources/cache/labels/en_uri_surface_form.tsv!");
            e.printStackTrace();
        }
        catch (IOException e) {

            e.printStackTrace();
        }
    }
}
