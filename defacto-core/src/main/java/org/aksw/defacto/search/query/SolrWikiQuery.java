package org.aksw.defacto.search.query;

/**
 * Created by esteves on 02.09.15.
 */
public class SolrWikiQuery implements Query {

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
        System.out.println(propertyLabel);
        return propertyLabel.replaceAll(",", "").replace("`", "").replace(" 's", "'s").replace("?R?", "").replace("?D?", "").replaceAll(" +", " ").replaceAll("'[^s]", "").replaceAll("&", "and").trim();
    }

}
