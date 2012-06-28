package org.aksw.provenance.search.concurrent;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.aksw.provenance.Constants;
import org.aksw.provenance.boa.Pattern;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.evidence.WebSite;
import org.aksw.provenance.util.CrawlUtil;
import org.aksw.provenance.util.JsoupCrawlUtil;
import org.jsoup.Jsoup;

import com.hp.hpl.jena.rdf.model.Model;

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
            website.setText(this.crawlUtil.readPage(website.getUrl(), Constants.WEB_SEARCH_TIMEOUT_MILLISECONDS));
        
        return this.website;
    }
}