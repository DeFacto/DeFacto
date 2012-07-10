package org.aksw.defacto.search.fact;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.DefactoModel;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Jens Lehmann
 */
public class SubjectObjectFactSearcher implements FactSearcher {

    private Map<String, Set<String>> urisToLabels = new HashMap<String,Set<String>>(); 
    private Logger logger = Logger.getLogger(SubjectObjectFactSearcher.class);
    private static final Set<String> stopwords = new HashSet<String>(Arrays.asList("the", "of", "and"));
    
    private static SubjectObjectFactSearcher INSTANCE;
    
    private static final java.util.regex.Pattern ROUND_BRACKETS     = java.util.regex.Pattern.compile("\\(.+?\\)");
    private static final java.util.regex.Pattern SQUARED_BRACKETS   = java.util.regex.Pattern.compile("\\[.+?\\]");
    private static final java.util.regex.Pattern TRASH              = java.util.regex.Pattern.compile("[^\\p{L}\\p{N}.?!' ]");
    private static final java.util.regex.Pattern WHITESPACES        = java.util.regex.Pattern.compile("\\n");

    /**
     * 
     */
    private SubjectObjectFactSearcher() {

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
    public static synchronized SubjectObjectFactSearcher getInstance() {
        
        if ( SubjectObjectFactSearcher.INSTANCE == null )
            SubjectObjectFactSearcher.INSTANCE = new SubjectObjectFactSearcher();
        
        return SubjectObjectFactSearcher.INSTANCE;
    }
    
    @Override
    public void generateProofs(Evidence evidence, WebSite website, DefactoModel model, Pattern pattern) {

        String subjectUri   = model.getSubjectUri();
        String objectUri    = model.getObjectUri();
        String websiteText  = website.getText().toLowerCase();
        
        Set<String> subjectLabels  = urisToLabels.get(subjectUri.replace("http://dbpedia.org/resource/", ""));
        Set<String> objectLabels   = urisToLabels.get(objectUri.replace("http://dbpedia.org/resource/", ""));
        
        // fallback on the labels provided by the input, no surface forms from dbpedia
        if ( subjectLabels == null ) subjectLabels = new HashSet<String>(Arrays.asList(model.getSubjectLabel()));
        if ( objectLabels == null ) objectLabels = new HashSet<String>(Arrays.asList(model.getObjectLabel()));
        
        for ( String subjectLabel : subjectLabels ) { subjectLabel = subjectLabel.toLowerCase(); // save some time
            for ( String objectLabel : objectLabels ) { objectLabel = objectLabel.toLowerCase(); // same here

                String[] subjectObjectMatches = StringUtils.substringsBetween(websiteText, " " + subjectLabel, objectLabel + " ");
                String[] objectSubjectMatches = StringUtils.substringsBetween(websiteText, " " + objectLabel, subjectLabel + " ");
                
                // we need to check for both directions
                List<String> subjectObjectOccurrences = new ArrayList<String>();
                if (subjectObjectMatches != null) subjectObjectOccurrences.addAll(Arrays.asList(subjectObjectMatches));
                List<String> objectSubjectOccurrences = new ArrayList<String>();
                if (objectSubjectMatches != null) objectSubjectOccurrences.addAll(Arrays.asList(objectSubjectMatches));
                
                // combine the list to make processing a little easier
                Set<String> surfaceForms = new HashSet<String>(subjectLabels);
                surfaceForms.addAll(objectLabels);
                
                // direction: subject property object
                createProofsForEvidence(evidence, subjectObjectOccurrences, subjectLabel, objectLabel, websiteText, website, surfaceForms);
                // direction: object property subject 
                createProofsForEvidence(evidence, objectSubjectOccurrences, objectLabel, subjectLabel, websiteText, website, surfaceForms);
            }
        }
    }
    
    
    /**
     * 
     * @param evidence
     * @param matches
     * @param patternToSearch
     * @param firstLabel
     * @param secondLabel
     * @param site
     */
    private void createProofsForEvidence(Evidence evidence, List<String> matches, String firstLabel, String secondLabel, String websiteTextLowerCase, WebSite site, Set<String> surfaceForms) {
        
        for ( String occurrence : matches ) {
            
            // it makes no sense to look at longer strings 
            if ( occurrence.split(" ").length < Defacto.DEFACTO_CONFIG.getIntegerSetting("extract", "NUMBER_OF_TOKENS_BETWEEN_ENTITIES") ) {
                
                ComplexProof proof = null;
                String normalizedOccurrence = this.normalizeOccurrence(occurrence, surfaceForms);
                
                // first we check if we can find a boa pattern inside the mathing string
                for (Pattern boaPattern : evidence.getBoaPatterns()) { // go through all patterns and look if a non empty normalized pattern string is inside the match
                    if ( occurrence.contains(boaPattern.normalize()) && !boaPattern.normalize().trim().isEmpty() ) {
                        
                        proof = new ComplexProof(evidence.getModel(), firstLabel, secondLabel, occurrence, normalizedOccurrence, site, boaPattern);
                        break;
                    }
                }
                // no boa pattern was found
                if ( proof == null ) proof = new ComplexProof(evidence.getModel(), firstLabel, secondLabel, occurrence, normalizedOccurrence, site);
                // we need to do this for both proofs
                proof.setContext(this.getLeftAndRightContext(site.getText(), websiteTextLowerCase, firstLabel + occurrence + secondLabel));
                
                evidence.addComplexProof(proof);
            }
        }
    }
    
    /**
     * this method removes all thrash from the found pattern
     *  - everything between "(" and ")"
     *  - everything which is not a character or a number
     *  - leading and trailing white-spaces
     * 
     * @param occurrence
     * @param surfaceForms
     * @return0000
     */
    private String normalizeOccurrence(String occurrence, Set<String> surfaceForms) {

        // hopefully gain some performance improvements through using compiled patterns
        String normalizedOccurrence = ROUND_BRACKETS.matcher(occurrence).replaceAll("");
        normalizedOccurrence = SQUARED_BRACKETS.matcher(normalizedOccurrence).replaceAll("");
        normalizedOccurrence = TRASH.matcher(normalizedOccurrence).replaceAll(" ").trim();
        normalizedOccurrence = WHITESPACES.matcher(normalizedOccurrence).replaceAll(" ");
        
//        String normalizedOccurrence = occurrence.replaceAll("\\(.+?\\)", "").replaceAll("\\[.+?\\]", "").replaceAll("[^\\p{L}\\p{N}.?!' ]", " ").trim().replaceAll("\\n\\r", " ");
        
        for ( String label : surfaceForms ) { label = label.toLowerCase();
            for (String part : label.split(" ") ) {
                
                if ( !stopwords.contains(part) ) {

                    if (normalizedOccurrence.startsWith(part)) {
                        
                        normalizedOccurrence = StringUtils.replaceOnce(part, normalizedOccurrence, "");
                        logger.debug("Removed: ^" + part);
                    }
                    if (normalizedOccurrence.endsWith(part)) {
                        
                        normalizedOccurrence = normalizedOccurrence.replaceAll(part + "$", "");
                        logger.debug("Removed: " + part + "$");
                    }
                }
            }
        }
        
        return normalizedOccurrence.trim();
    }

    /**
     * 
     */
    private void initializeSurfaceForms() {
        
        try {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("resources/cache/labels/surface_forms_en.tsv"))));
            String line;
            while ((line = reader.readLine()) != null) {
                
                // TODO remove only for debugging!!
//                if( !line.startsWith("Mother%27s_Finest") || !line.startsWith("Another_Mother_Further") ) continue;
                
                String[] lineParts = line.split("\t");
                Set<String> surfaceForms = new HashSet<String>();
                for ( String label : Arrays.asList(Arrays.copyOfRange(lineParts, 1, lineParts.length)) ) if ( label.length() > 3 ) surfaceForms.add(label);
                this.urisToLabels.put(lineParts[0], surfaceForms);
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
    
    private String getLeftAndRightContext(String normalCase, String lowerCase, String match) {
        
        int leftIndex = lowerCase.indexOf(match);
        int rightIndex = leftIndex + match.length();
        
        // avoid index out of bounds
        if ( leftIndex - 30 >= 0 ) leftIndex -= 30;
        else leftIndex = 0;
        
        if ( rightIndex + 30 > lowerCase.length() ) rightIndex = lowerCase.length() - 1;
        else rightIndex += 30;
        
        return normalCase.substring(leftIndex, rightIndex);
    }
}
