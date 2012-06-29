package org.aksw.defacto.search.query;


public class GoogleQuery implements Query {

    @Override
    public String generateQuery(MetaQuery query) {

        // pattern starts with subject/domain so we need to put the subject label first
        if ( query.getPropertyLabel().startsWith("?D?") )
            return String.format("+\"%s\" +\"%s\" +\"%s\"", query.getSubjectLabel(), query.getPropertyLabel(), query.getObjectLabel());
        else
            return String.format("+\"%s\" +\"%s\" +\"%s\"", query.getObjectLabel(), query.getPropertyLabel(), query.getSubjectLabel());
    }

    @Override
    public String normalizePredicate(String propertyLabel) {

        // TODO Auto-generated method stub
        return null;
    }
}
