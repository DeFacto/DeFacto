package org.aksw.defacto.evidence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.defacto.DefactoModel;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.ml.feature.AbstractFeature;
import org.aksw.defacto.topic.frequency.Word;
import org.aksw.defacto.util.VectorUtil;
import org.apache.commons.lang3.ArrayUtils;

import weka.core.Instance;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class Evidence {

    private DefactoModel model;
    private Map<Pattern,List<WebSite>> webSites         = new LinkedHashMap<Pattern,List<WebSite>>();
    private List<Word> topicTerms                       = new ArrayList<Word>();
//    private Map<Pattern,Double[][]> similarityMatricies = new LinkedHashMap<Pattern,Double[][]>();
    private Double[][] similarityMatrix                 = null;
    
    private Instance features;
    private Long totalHitCount;
    private String subjectLabel;
    private String objectLabel;
    private double deFactoScore;
    
    private Set<ComplexProof> complexProofs;
    private List<Pattern> boaPatterns;
    
    /**
     * 
     * @param model
     * @param totalHitCount
     * @param subjectLabel
     * @param objectLabel
     */
    public Evidence(DefactoModel model, Long totalHitCount, String subjectLabel, String objectLabel) {

        this.model              = model;
        this.totalHitCount      = totalHitCount;
        this.subjectLabel       = subjectLabel;
        this.objectLabel        = objectLabel;
        this.complexProofs      = new HashSet<ComplexProof>();
    }

    public Evidence(DefactoModel model) {

        this.model              = model;
        this.totalHitCount      = 0L;
        this.complexProofs      = new HashSet<ComplexProof>();
        
        // TODO
        // get the label of subject and object from the model
    }
    
    /**
     * @return the factFeatures
     */
    public Instance getFeatures() {
    
        if ( features == null ) {

            this.features = new Instance(AbstractFeature.provenance.numAttributes());
            this.features.setDataset(AbstractFeature.provenance);
            this.features.setValue(AbstractFeature.CLASS, String.valueOf(model.isCorrect()));
        }
        
        return features;
    }

    /**
     * 
     * @param site
     */
    public void addWebSite(Pattern pattern, WebSite site) {

        if ( this.webSites.containsKey(pattern) ) this.webSites.get(pattern).add(site);
        else this.webSites.put(pattern, new ArrayList<WebSite>(Arrays.asList(site)));
    }
    
    /**
     * 
     * @param site
     */
    public void addWebSites(Pattern pattern, List<WebSite> sites) {

        this.webSites.put(pattern, sites);
    }

    /**
     * 
     * @return
     */
    public Map<Pattern,List<WebSite>> getWebSites() {

        return this.webSites;
    }

    /**
     * @return the topicTerms
     */
    public List<Word> getTopicTerms() {

        return topicTerms;
    }

    /**
     * @param topicTerms the topicTerms to set
     */
    public void setTopicTerms(List<Word> topicTerms) {

        this.topicTerms = topicTerms;
    }
    
    /**
     * 
     */
    public void calculateSimilarityMatrix() {

        // This could would need to be used if we want to merge all the search results in the a single ball
        List<WebSite> allWebsites = new ArrayList<WebSite>();
        for ( List<WebSite> entry : this.webSites.values() ) allWebsites.addAll(entry);
        
        this.similarityMatrix = new Double[allWebsites.size()][allWebsites.size()];
        
        for ( int i = 0 ; i < allWebsites.size() ; i++ ) {
            similarityMatrix[i][i] = 1D; // i is always similar to itself

            for ( int j = i + 1 ; j < allWebsites.size() ; j++){
                
                double similarity = VectorUtil.calculateSimilarity(
                        ArrayUtils.toPrimitive(allWebsites.get(i).getTopicTerms().toArray(new Integer[allWebsites.get(i).getTopicTerms().size()])),
                        ArrayUtils.toPrimitive(allWebsites.get(j).getTopicTerms().toArray(new Integer[allWebsites.get(j).getTopicTerms().size()])));

                similarityMatrix[i][j] = similarityMatrix[j][i] = similarity;
            }
        }
        
//        for ( Map.Entry<Pattern, List<WebSite>> entry : this.webSites.entrySet() ) {
//
//            List<WebSite> websites = entry.getValue();
//            
//            Double[][] similarityMatrix = new Double[websites.size()][websites.size()];
//            
//            for ( int i = 0 ; i < websites.size() ; i++ ) {
//                similarityMatrix[i][i] = 1D; // i is always similar to itself
//
//                for ( int j = i + 1 ; j < websites.size() ; j++){
//                    
//                    double similarity = VectorUtil.calculateSimilarity(
//                            ArrayUtils.toPrimitive(websites.get(i).getTopicTerms().toArray(new Integer[websites.get(i).getTopicTerms().size()])),
//                            ArrayUtils.toPrimitive(websites.get(j).getTopicTerms().toArray(new Integer[websites.get(j).getTopicTerms().size()])));
//
//                    similarityMatrix[i][j] = similarityMatrix[j][i] = similarity;
//                }
//            }
//            this.similarityMatricies.put(entry.getKey(), similarityMatrix);
//        }
    }

    /**
     * 
     * @return
     */
    public String getSubjectLabel() {

        return this.subjectLabel;
    }

    /**
     * 
     * @return
     */
    public String getObjectLabel() {

        return this.objectLabel;
    }

    /**
     * 
     */
    public void setTopicTermVectorForWebsites() {

        for ( List<WebSite> websitesForPattern : this.webSites.values() )
            for ( WebSite website : websitesForPattern ) 
                website.setTopicTerms(this.topicTerms);
    }

    
    /**
     * @return the totalHitCount
     */
    public Long getTotalHitCount() {
    
        return totalHitCount;
    }
    
//    /**
//     * @return the similarityMatricies
//     */
//    public Map<Pattern, Double[][]> getSimilarityMatricies() {
//    
//        return similarityMatricies;
//    }

    /**
     * 
     * @param score
     */
    public void setDeFactoScore(double score) {

        this.deFactoScore = score;
    }

    /**
     * 
     * @return
     */
    public Double getDeFactoScore() {

        return this.deFactoScore;
    }

    /**
     * 
     * @param proof
     * @return
     */
	public synchronized boolean addComplexProof(ComplexProof proof) {
		return complexProofs.add(proof);
	}

	/**
	 * 
	 * @return
	 */
	public Set<ComplexProof> getComplexProofs() {
		return complexProofs;
	}
	
	/**
	 * 
	 * @param website
	 * @param pattern
	 * @return
	 */
	public List<ComplexProof> getComplexProofs(WebSite website, Pattern pattern) {
		List<ComplexProof> proofs = new LinkedList<ComplexProof>();
		for(ComplexProof proof : complexProofs) {
			if ( proof.getPattern().equals(pattern) && proof.getWebSite().equals(website) ) {
				proofs.add(proof);
			}
		}
		return proofs;
	}

    /**
     * @return the model
     */
    public DefactoModel getModel() {
    
        return model;
    }

    public Double[][] getSimilarityMatrix() {

        return this.similarityMatrix;
    }

    public List<ComplexProof> getComplexProofs(WebSite website) {
        
        List<ComplexProof> proofs = new ArrayList<ComplexProof>();
        for ( ComplexProof proof : this.complexProofs )
            if ( proof.getWebSite().equals(website)) proofs.add(proof);
        return proofs;
    }
    
    public List<WebSite> getAllWebSites(){
        
        List<WebSite> websites = new ArrayList<WebSite>();
        for ( List<WebSite> websiteList : this.webSites.values() ) websites.addAll(websiteList);
        return websites;
    }

    public void setBoaPatterns(List<Pattern> naturalLanguageRepresentations) {

        this.boaPatterns = naturalLanguageRepresentations;
    }

    
    /**
     * @return the boaPatterns
     */
    public List<Pattern> getBoaPatterns() {
    
        return boaPatterns;
    }
}
