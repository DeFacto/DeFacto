package org.aksw.defacto.search.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by esteves on 02.09.15.
 */
public class SolrWikiQuery implements Query {

    private static final Logger logger = LoggerFactory.getLogger(SolrWikiQuery.class);

    @Override
    public String generateQuery(MetaQuery query) {
       //implement later the wildcard and keyword similarity!

        if ( query.getPropertyLabel().startsWith("?D?") )
            return String.format("+\"%s\" +\"%s\" +\"%s\"", query.getSubjectLabel(), query.getPropertyLabel(), query.getObjectLabel());
        else
            return String.format("+\"%s\" +\"%s\" +\"%s\"", query.getObjectLabel(), query.getPropertyLabel(), query.getSubjectLabel());

    }

    @Override
    public String normalizePredicate(String propertyLabel) {
        logger.info(propertyLabel);
        return propertyLabel.replaceAll(",", "").replace("`", "").replace(" 's", "'s").replace("?R?", "").replace("?D?", "").replaceAll(" +", " ").replaceAll("'[^s]", "").replaceAll("&", "and").trim();
    }

}
