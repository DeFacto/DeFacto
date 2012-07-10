package org.aksw.defacto.search.concurrent;

import java.util.concurrent.Callable;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.DefactoModel;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.fact.SubjectObjectFactSearcher;
import org.aksw.defacto.util.PageRank;
import org.apache.log4j.Logger;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class WebSiteScoreCallable implements Callable<WebSite> {

    private Logger logger = Logger.getLogger(WebSiteScoreCallable.class);
    
    private DefactoModel model;
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
    public WebSiteScoreCallable(WebSite website, Evidence evidence, DefactoModel model) {

        this.website  = website;
        this.model    = model;
        this.evidence = evidence;
    }

    @Override
    public WebSite call() {
        
        SubjectObjectFactSearcher.getInstance().generateProofs(evidence, website, model, pattern);
        
        // every web site is spawned with a page rank of 11
        if ( website.getPageRank() == Defacto.DEFACTO_CONFIG.getIntegerSetting("evidence", "UNASSIGNED_PAGE_RANK") ) {
            
            logger.info("Getting page rank for: " + website.getUrl());
            website.setPageRank(PageRank.getPageRank(website.getUrl()));
        }
        
        return website;
    }
}
