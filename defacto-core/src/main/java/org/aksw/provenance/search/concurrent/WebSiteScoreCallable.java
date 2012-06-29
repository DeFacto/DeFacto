package org.aksw.provenance.search.concurrent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.aksw.provenance.Constants;
import org.aksw.provenance.boa.BoaPatternSearcher;
import org.aksw.provenance.boa.Pattern;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.evidence.PossibleProof;
import org.aksw.provenance.evidence.Proof;
import org.aksw.provenance.evidence.WebSite;
import org.aksw.provenance.search.cache.SearchResultCache;
import org.aksw.provenance.search.fact.DefaultFactSearcher;
import org.aksw.provenance.search.fact.SubjectObjectFactSearcher;
import org.aksw.provenance.search.result.SearchResult;
import org.aksw.provenance.topic.TopicTermExtractor;
import org.aksw.provenance.util.ModelUtil;
import org.aksw.provenance.util.PageRank;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class WebSiteScoreCallable implements Callable<WebSite> {

    private Logger logger = Logger.getLogger(WebSiteScoreCallable.class);
    
    private Model model;
    private Pattern pattern;
    private WebSite website;
    private Evidence evidence;
    
    /**
     * 
     * @param website
     * @param evidence
     * @param model
     * @param patterns 
     */
    public WebSiteScoreCallable(WebSite website, Evidence evidence, Model model) {

        this.website  = website;
        this.model    = model;
        this.evidence = evidence;
    }

    @Override
    public WebSite call() {
        
        SubjectObjectFactSearcher.getInstance().generateProofs(evidence, website, model, pattern);
        
        // every web site is spawned with a page rank of 11
        if ( website.getPageRank() == Constants.UNASSIGNED_PAGE_RANK ) {
            
            logger.info("Getting page rank for: " + website.getUrl());
            website.setPageRank(PageRank.getPageRank(website.getUrl()));
        }
        
        return website;
    }
    
//    private double scoreSite() {
//
//        double score = 0;
//        
//        // boa pattern found -> +++
//        for ( Proof proof : this.evidence.getStructuredProofs(this.website) ) score += 1D;
//        
//        for ( Map.Entry<PossibleProof,Integer> possibleProof : evidence.getPossibleProofs(this.website).entrySet() ) {
//            
//            if (possibleProof.getKey().getPhrase().trim().isEmpty()) return 0.3;
//            else {
//                
//                int distance = possibleProof.getKey().getPhrase().trim().split(" ").length;
//                if ( distance == 0 ) score += 0.3;
//                else if ( distance > 0 && distance <= 5 ) score += 0.2;
//                else score += 0.1;
//            }
//        }
//        
//        return score;
//    }
}
