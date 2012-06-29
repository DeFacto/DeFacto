package org.aksw.defacto.search.result;

import java.util.List;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.query.MetaQuery;

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
