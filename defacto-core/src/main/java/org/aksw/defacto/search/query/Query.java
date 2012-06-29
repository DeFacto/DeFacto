package org.aksw.defacto.search.query;


public interface Query {

    public String generateQuery(MetaQuery query);
    
    public String normalizePredicate(String propertyLabel);
}
