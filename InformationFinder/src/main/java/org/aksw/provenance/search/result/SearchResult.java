package org.aksw.provenance.search.result;

import java.util.List;

import org.aksw.provenance.boa.Pattern;
import org.aksw.provenance.evidence.WebSite;
import org.aksw.provenance.search.query.MetaQuery;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public interface SearchResult {

    public Long getTotalHitCount();
    
    public List<WebSite> getWebSites();
    
    public MetaQuery getQuery() ;

    public void setPattern(Pattern pattern);

    public Pattern getPattern();
}
