package org.aksw.defacto.search.concurrent;

import java.util.concurrent.Callable;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.fact.SubjectObjectFactSearcher;
import org.apache.log4j.Logger;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class WebSiteScoreCallable implements Callable<WebSite> {

    private DefactoModel model;
    private Pattern pattern;
    private WebSite website;
    private Evidence evidence;
    SubjectObjectFactSearcher searcher = new SubjectObjectFactSearcher();
    
    /**
     * 
     * @param website
     * @param evidence
     * @param model
     * @param patterns 
     */
    public WebSiteScoreCallable(WebSite website, Evidence evidence, DefactoModel model, Pattern pattern) {

        this.website  = website;
        this.model    = model;
        this.evidence = evidence;
        this.pattern  = pattern;
    }

    @Override
    public WebSite call() {
        
    	searcher.generateProofs(evidence, website, model, pattern);
        return website;
    }
}
