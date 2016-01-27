package org.aksw.defacto.search.fact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Jens Lehmann
 * @author Diego Esteves <esteves@informatik.uni-leipzig.de>
 */
public class SubjectObjectFactSearcher implements FactSearcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubjectObjectFactSearcher.class);
    public static org.apache.log4j.Logger LOGDEV    = org.apache.log4j.Logger.getLogger("developer");
    private static final Set<String> stopwords = new HashSet<String>(Arrays.asList("the", "of", "and"));
    
    private static SubjectObjectFactSearcher INSTANCE;
    
    private static final java.util.regex.Pattern ROUND_BRACKETS     = java.util.regex.Pattern.compile("\\(.+?\\)");
    private static final java.util.regex.Pattern SQUARED_BRACKETS   = java.util.regex.Pattern.compile("\\[.+?\\]");
    private static final java.util.regex.Pattern TRASH              = java.util.regex.Pattern.compile("[^\\p{L}\\p{N}.?!' ]");
    private static final java.util.regex.Pattern WHITESPACES        = java.util.regex.Pattern.compile("\\n");

    /**
     * 
     */
    public SubjectObjectFactSearcher() {
    }
    
    /**
     * this needs to be synchronized in order to avoid calling this
     * method from every crawl thread
     * 
     * @return
     */
//    public static synchronized SubjectObjectFactSearcher getInstance() {
//        
//        if ( SubjectObjectFactSearcher.INSTANCE == null )
//            SubjectObjectFactSearcher.INSTANCE = new SubjectObjectFactSearcher();
//        
//        return SubjectObjectFactSearcher.INSTANCE;
//    }
    
    @Override
    public void generateProofs(Evidence evidence, WebSite website, DefactoModel model, Pattern pattern) {

        LOGDEV.debug(" -> starting proof generation process");

        String websiteText  = website.getText().toLowerCase();
        
        Set<String> subjectLabels = new HashSet<String>();
        Set<String> objectLabels = new HashSet<String>();
        
        for ( String language : model.getLanguages() ) {
        	
        	subjectLabels.add(model.getSubjectLabelNoFallBack(language));
        	subjectLabels.addAll(model.getSubjectAltLabels(language));
        	
        	objectLabels.add(model.getObjectLabelNoFallBack(language));
        	objectLabels.addAll(model.getObjectAltLabels());
        }
        subjectLabels.removeAll(Collections.singleton(Constants.NO_LABEL));
        objectLabels.removeAll(Collections.singleton(Constants.NO_LABEL));

        toLowerCase(subjectLabels);
        toLowerCase(objectLabels);
        
        // combine the list to make processing a little easier
        Set<String> surfaceForms = new HashSet<String>(subjectLabels);
        surfaceForms.addAll(objectLabels);
        
        for ( String subjectLabel : subjectLabels ) { 
        	
        	// save some time
        	if ( !websiteText.contains(subjectLabel) ) continue;
        	
            for ( String objectLabel : objectLabels ) { 
            	
            	if ( !websiteText.contains(objectLabel) ) continue;
            	
            	LOGGER.debug("Search proof for: '" + subjectLabel + "' and '" + objectLabel + "'.");
            
            	if ( subjectLabel.equals(objectLabel) ) continue;
            	
                String[] subjectObjectMatches = StringUtils.substringsBetween(websiteText, " " + subjectLabel, objectLabel + " ");
                String[] objectSubjectMatches = StringUtils.substringsBetween(websiteText, " " + objectLabel, subjectLabel + " ");
                
                // we need to check for both directions
                List<String> subjectObjectOccurrences = new ArrayList<String>();
                if (subjectObjectMatches != null) for ( String s : subjectObjectMatches ) subjectObjectOccurrences.add(s);
                // asdjklajsd
                List<String> objectSubjectOccurrences = new ArrayList<String>();
                if (objectSubjectMatches != null) for ( String s : objectSubjectMatches) objectSubjectOccurrences.add(s);
                
                // direction: subject property object
                createProofsForEvidence(evidence, subjectObjectOccurrences, subjectLabel, objectLabel, websiteText, website, surfaceForms, pattern);
                // direction: object property subject 
                createProofsForEvidence(evidence, objectSubjectOccurrences, objectLabel, subjectLabel, websiteText, website, surfaceForms, pattern);
            }
        }
        LOGGER.debug("#sLabels: "+  subjectLabels.size() + " #oLabels:" + objectLabels.size() + " #Proofs: " + evidence.getComplexProofs().size() + " #lang: " + model.getLanguages().size());
    }
    
    public static void toLowerCase(Set<String> strings)
    {
        String[] stringsArray = strings.toArray(new String[0]);
        for (int i=0; i< stringsArray.length; ++i) {
            stringsArray[i] = stringsArray[i].toLowerCase();
        }
        strings.clear();
        strings.addAll(Arrays.asList(stringsArray));
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
    private void createProofsForEvidence(Evidence evidence, List<String> matches,
                                         String firstLabel, String secondLabel,
                                         String websiteTextLowerCase, WebSite site,
                                         Set<String> surfaceForms,
                                         Pattern pattern) {


        LOGDEV.debug(" -> creating proofs for evidence: #first label = " + firstLabel +
                " / #second label = " + secondLabel +
                " / #website url = " + site.getUrl() +
                " / #normalized pattern = " + pattern.getNormalized());

        for ( String occurrence : matches ) {

            LOGDEV.debug(" #match = " + occurrence.toString());
            
            // it makes no sense to look at longer strings 
            if ( occurrence.split(" ").length < Defacto.DEFACTO_CONFIG.getIntegerSetting("extract", "NUMBER_OF_TOKENS_BETWEEN_ENTITIES") ) {

                // first we check if we can find a boa pattern inside the mathing string
//                for (Pattern boaPattern : evidence.getBoaPatterns()) { // go through all patterns and look if a non empty normalized pattern string is inside the match
                	
//                	if ( !tinyContext.contains(boaPattern.normalize()) ) continue;
                	
//                	System.out.println(boaPattern.normalize());
//                	System.out.println(tinyContext);
//                	
                	// this can only be if the patterns contains only garbage
//                	if ( boaPattern.normalize().isEmpty() ) continue;

                String tinyContext = this.getLeftAndRightContext(site.getText(), websiteTextLowerCase, firstLabel + occurrence + secondLabel, 25);

                ComplexProof proof = new ComplexProof(evidence.getModel(), firstLabel, secondLabel, occurrence, normalizeOccurrence(tinyContext,surfaceForms), site, pattern);
                proof.setTinyContext(this.getLeftAndRightContext(site.getText(), websiteTextLowerCase, firstLabel + occurrence + secondLabel, 25));
                proof.setSmallContext(this.getLeftAndRightContext(site.getText(), websiteTextLowerCase, firstLabel + occurrence + secondLabel, 50));
                proof.setMediumContext(this.getLeftAndRightContext(site.getText(), websiteTextLowerCase, firstLabel + occurrence + secondLabel, 100));
                proof.setLargeContext(this.getLeftAndRightContext(site.getText(), websiteTextLowerCase, firstLabel + occurrence + secondLabel, 150));

                //proof.setTaggedTinyContext();

                //is there a pattern in between S1 and S2?
                proof.setHasPatternInBetween(occurrence.contains(pattern.getNormalized()));

                //continuar aqui, agora aplicando a regra de S ou O diferentes....
                String[] rule = pattern.NER.split(";");

                evidence.addComplexProof(proof);
//              }
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
                        LOGGER.debug("Removed: ^" + part);
                    }
                    if (normalizedOccurrence.endsWith(part)) {
                        
                        normalizedOccurrence = normalizedOccurrence.replaceAll(part + "$", "");
                        LOGGER.debug("Removed: " + part + "$");
                    }
                }
            }
        }
        
        return normalizedOccurrence.trim();
    }

    /**
     * 
     * @param normalCase
     * @param lowerCase
     * @param match
     * @param contextLength
     * @return
     */
    private String getLeftAndRightContext(String normalCase, String lowerCase, String match, int contextLength) {
        
        int leftIndex = lowerCase.indexOf(match);
        int rightIndex = leftIndex + match.length();
        
        // avoid index out of bounds
        if ( leftIndex - contextLength >= 0 ) leftIndex -= contextLength;
        else leftIndex = 0;
        
        if ( rightIndex + contextLength > lowerCase.length() ) rightIndex = lowerCase.length() - 1;
        else rightIndex += contextLength;
        
        return normalCase.substring(leftIndex, rightIndex);
    }
}
