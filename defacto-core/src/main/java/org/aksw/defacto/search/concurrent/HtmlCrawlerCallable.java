package org.aksw.defacto.search.concurrent;

import java.util.concurrent.Callable;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.util.CrawlUtil;
import org.aksw.defacto.util.JsoupCrawlUtil;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * 
 */
public class HtmlCrawlerCallable implements Callable<WebSite> {

    private CrawlUtil crawlUtil = new JsoupCrawlUtil();
    private WebSite website;

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
        
        return this.website;
    }
}