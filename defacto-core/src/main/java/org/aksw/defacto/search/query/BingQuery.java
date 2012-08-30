package org.aksw.defacto.search.query;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class BingQuery implements Query {

    @Override
    public String generateQuery(MetaQuery query) {

        String subject  = query.getSubjectLabel().replace("&", "and");
        String property = normalizePredicate(query.getPropertyLabel().substring(0, query.getPropertyLabel().length() - 3).substring(3).trim());
        String object   = query.getObjectLabel().replace("&", "and");
        String queryString = "";
        
        // standard search engine query
        if ( query.getTopicTerms().isEmpty() ) {
            
            // since we don't get any results if we use the near operator we skip it 
            // if ( query.getPropertyLabel().equals("??? NONE ???") ) queryString = String.format("\"%s\" near:%s \"%s\"", subject, 15, object);
            if ( query.getPropertyLabel().equals("??? NONE ???") ) queryString = String.format("\"%s\" AND \"%s\"", subject, object);
            else queryString = String.format("\"%s\" AND \"%s\" AND \"%s\"", subject, property, object);
        }
        else {
            
            // this query is going to be exectued from the topic majority web feature
            queryString = String.format("\"%s\" AND \"%s\" AND \"%s\"", subject, property, object);
            
            // add the first 3 topic terms to the query
            for ( int i = 0 ; i < 3 && i < query.getTopicTerms().size() ; i++)
                // use the norelax option here because bing only includes first 4 terms as must contain
                queryString += " AND norelax:\"" + query.getTopicTerms().get(i).getWord() + "\"";
        }
        return queryString;
    }

    @Override
    public String normalizePredicate(String propertyLabel) {

        return propertyLabel.replaceAll(",", "").replace("`", "").replace(" 's", "'s").replaceAll(" +", " ").replaceAll("'[^s]", "").replaceAll("&", "and").trim();
    }
    
    public static void main(String[] args) {

        MetaQuery query1 = new MetaQuery("Mount Eccles National Park|-|??? NONE ???|-|Texas");
        MetaQuery query2 = new MetaQuery("Mount Eccles National Park|-|?D? is a stupid ?R?|-|Texas");
        MetaQuery query3 = new MetaQuery("Mount Eccles National Park|-|?D? 's is a , ,, , '' stupid ?R?|-|Texas");
        
        BingQuery bq = new BingQuery();
        bq.generateQuery(query1);
        bq.generateQuery(query2);
        bq.generateQuery(query3);
    }
}
