package org.aksw.defacto.search.concurrent;

import java.util.concurrent.Callable;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.util.CrawlUtil;
import org.aksw.defacto.util.JsoupCrawlUtil;
import org.aksw.defacto.util.PageRank;
import org.apache.log4j.Logger;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class HtmlCrawlerCallable implements Callable<WebSite> {

    private CrawlUtil crawlUtil = new JsoupCrawlUtil();
    private WebSite website;
    
    private Logger logger = Logger.getLogger(HtmlCrawlerCallable.class);
    
    /**
     * 
     * @param url
     */
    public HtmlCrawlerCallable(WebSite site) {

        this.website = site;
    }

    @Override
    public WebSite call() throws Exception {
        
        // we do only want to start the crawling if we haven't it done already
        if ( this.website.getText().isEmpty() && !this.website.isCached() )
            website.setText(this.crawlUtil.readPage(website.getUrl(), Defacto.DEFACTO_CONFIG.getIntegerSetting("crawl", "WEB_SEARCH_TIMEOUT_MILLISECONDS")));
     
        // every web site is spawned with a page rank of 11
        if ( Defacto.DEFACTO_CONFIG.getBooleanSetting("crawl", "getPageRank") && 
        		website.getPageRank() == Defacto.DEFACTO_CONFIG.getIntegerSetting("evidence", "UNASSIGNED_PAGE_RANK") && !this.website.isCached() ) {
            
            logger.info("Getting page rank for: " + website.getUrl());
            website.setPageRank(PageRank.getInstance().getPageRank(website.getUrl()));
        }
        
        return this.website;
    }
}