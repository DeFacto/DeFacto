package org.aksw.defacto.evidence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.model.DefactoTimePeriod;
import org.aksw.defacto.search.time.TimePeriodSearcher;
import org.aksw.defacto.topic.frequency.Word;
import org.aksw.defacto.util.VectorUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.github.gerbsen.math.Frequency;

import weka.core.Instance;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class Evidence {

    private DefactoModel model;
    private Map<Pattern,List<WebSite>> webSites         = new LinkedHashMap<Pattern,List<WebSite>>();
    private Map<String,List<Word>> topicTerms           = new HashMap<String,List<Word>>();
//    private Map<Pattern,Double[][]> similarityMatricies = new LinkedHashMap<Pattern,Double[][]>();
    public Double[][] similarityMatrix                 = null;
    
    public Map<String,Long> tinyContextYearOccurrences = new LinkedHashMap<String, Long>();
    public Map<String,Long> smallContextYearOccurrences = new LinkedHashMap<String, Long>();
    public Map<String,Long> mediumContextYearOccurrences = new LinkedHashMap<String, Long>();
    public Map<String,Long> largeContextYearOccurrences = new LinkedHashMap<String, Long>();
    
    private Instance features;
    private Long totalHitCount;
    private double deFactoScore;
    
    private Set<ComplexProof> complexProofs;
    private Map<String,List<Pattern>> boaPatterns = new HashMap<String,List<Pattern>>();
	public List<Match> dates = new ArrayList<Match>();
	public DefactoTimePeriod defactoTimePeriod;
	public TimePeriodSearcher tsSearcher = new TimePeriodSearcher();
	
    
    /**
     * 
     * @param model
     * @param totalHitCount
     * @param set 
     * @param subjectLabel
     * @param objectLabel
     */
    public Evidence(DefactoModel model, Long totalHitCount, Set<Pattern> set) {

        this.model              = model;
        this.totalHitCount      = totalHitCount;
        this.complexProofs      = new HashSet<ComplexProof>();
        
        boaPatterns.put("de", new ArrayList<Pattern>());
        boaPatterns.put("fr", new ArrayList<Pattern>());
        boaPatterns.put("en", new ArrayList<Pattern>());
        
        for ( Pattern p : set) boaPatterns.get(p.language).add(p);
    }

    public Evidence(DefactoModel model) {

        this.model              = model;
        this.totalHitCount      = 0L;
        this.complexProofs      = new HashSet<ComplexProof>();
    }
    
    /**
     * @return the factFeatures
     */
    public Instance getFeatures() {
    
        if ( features == null ) {

            this.features = new Instance(AbstractEvidenceFeature.provenance.numAttributes());
            this.features.setDataset(AbstractEvidenceFeature.provenance);
            this.features.setValue(AbstractEvidenceFeature.CLASS, String.valueOf(model.isCorrect()));
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
    public Map<String,List<Word>> getTopicTerms() {

        return topicTerms;
    }

    /**
     * @param topicTerms the topicTerms to set
     */
    public void setTopicTerms(String language, List<Word> topicTerms) {

        this.topicTerms.put(language, topicTerms);
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
     */
    public void setTopicTermVectorForWebsites(String language) {

        for ( List<WebSite> websitesForPattern : this.webSites.values() )
            for ( WebSite website : websitesForPattern ) 
                website.setTopicTerms(language, this.topicTerms.get(language));
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
//	public List<ComplexProof> getComplexProofs(WebSite website, Pattern pattern) {
//		List<ComplexProof> proofs = new LinkedList<ComplexProof>();
//		for(ComplexProof proof : complexProofs) {
//			if ( proof.getPattern().equals(pattern) && proof.getWebSite().equals(website) ) {
//				proofs.add(proof);
//			}
//		}
//		return proofs;
//	}

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
        boolean returnWebsitesWithNoProof = Defacto.DEFACTO_CONFIG.getBooleanSetting("evidence", "DISPLAY_WEBSITES_WITH_NO_PROOF");

        List<WebSite> websites = new ArrayList<WebSite>();
        for ( List<WebSite> websiteList : this.webSites.values() ){
            for(WebSite website:websiteList){
                if(this.getComplexProofs(website).size() > 0 )
                    websites.add(website);
                else if(returnWebsitesWithNoProof)
                    websites.add(website);
//            websites.addAll(websiteList);
            }
        }
        return websites;
    }

    public void setBoaPatterns(String language, List<Pattern> naturalLanguageRepresentations) {

        this.boaPatterns.put(language,naturalLanguageRepresentations);
    }

    
    /**
     * @return the boaPatterns
     */
    public List<Pattern> getBoaPatterns() {
    
    	List<Pattern> patterns = new ArrayList<Pattern>();
    	if ( this.boaPatterns.get("en") != null ) patterns.addAll(this.boaPatterns.get("en") );
    	if ( this.boaPatterns.get("de") != null ) patterns.addAll(this.boaPatterns.get("de") );
    	if ( this.boaPatterns.get("fr") != null ) patterns.addAll(this.boaPatterns.get("fr") );
        return patterns;
    }

	public void addDate(String match, int distance) {
		
		this.dates.add(new Match(match, distance));
	}

	/**
	 * 
	 */
	public void calculateDefactoTimePeriod() {
		
		// from and to are equal
		if ( this.model.getTimePeriod().isTimePoint() ) {
			
			Long occurrences = Long.MIN_VALUE;
			String occurrence = "";
			
			for ( Entry<String, Long> entry : this.smallContextYearOccurrences.entrySet()) {

				if ( entry.getValue() > occurrences ) {
					
					occurrences = entry.getValue();
					occurrence = entry.getKey();
				}
			}
			this.defactoTimePeriod = new DefactoTimePeriod(occurrence, occurrence);
		}
		// time periods spans multiple years
		else {
			
			// this needs to be implemented
			this.defactoTimePeriod = TimePeriodSearcher.findTimePeriod(this);
			if ( this.defactoTimePeriod == null ) {
				
				Frequency freq = new Frequency();
				
				for ( Entry<String, Long> entry : this.mediumContextYearOccurrences.entrySet()) {
					for (int i = 0; i < entry.getValue(); i++ ) {
						freq.addValue(entry.getKey());
					}
				}
				
				int first = 0;
				int second = 0;
				
				System.out.println(this.model.getTimePeriod());
				DescriptiveStatistics stats = new DescriptiveStatistics();
				for ( Entry<Comparable<?>, Long> entry : freq.sortByValue() ) {
					
//					for (int i = 0; i < entry.getValue(); i++) stats.addValue(Integer.valueOf((String)entry.getKey()));
//					System.out.println(entry.getKey() + ": " + entry.getValue());
					
					if (first == 0) {
						first = Integer.valueOf((String)entry.getKey());
						continue;
					}
					if (second == 0) {
						second = Integer.valueOf((String)entry.getKey());
						break;
					}
				}
//				System.out.println(stats);
//				first = (int) stats.getPercentile(50);
//				second =  (int) stats.getPercentile(50);
				this.defactoTimePeriod = new DefactoTimePeriod(Math.min(first, second), Math.max(first, second));
			}
				
			if ( this.defactoTimePeriod == null ) this.defactoTimePeriod = new DefactoTimePeriod(0,0);
		}
		
//		return new DefactoTimePeriod("0","0");
	}
}
