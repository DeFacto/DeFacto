/**
 * 
 */
package org.aksw.defacto.search.cache;

import java.util.List;

import org.aksw.defacto.cache.Cache;
import org.aksw.defacto.search.result.SearchResult;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class LuceneSearchResultCache implements Cache<SearchResult> {

    @Override
    public boolean contains(String identifier) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public SearchResult getEntry(String identifier) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchResult removeEntryByPrimaryKey(String primaryKey) {

        throw new RuntimeException("not yet implemented");    
    }

    @Override
    public boolean updateEntry(SearchResult object) {

        throw new RuntimeException("not yet implemented");    
    }

    @Override
    public List<SearchResult> addAll(List<SearchResult> listToAdd) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchResult add(SearchResult entry) {

        // TODO Auto-generated method stub
        return null;
    }

}
