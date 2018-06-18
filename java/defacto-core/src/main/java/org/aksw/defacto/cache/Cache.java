/**
 * 
 */
package org.aksw.defacto.cache;

import java.util.List;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public interface Cache<T> {

    /**
     * 
     * @return
     */
    public boolean contains(String identifier);
    
    /**
     * 
     * @param id
     * @param clazz
     * @return
     */
    public T getEntry(String identifier);
    
    /**
     * 
     * @param id
     * @param clazz
     * @return
     */
    public T removeEntryByPrimaryKey(String primaryKey);
    
    /**
     * 
     * @param id
     * @param clazz
     * @return
     */
    public boolean updateEntry(T object);

    /**
     * 
     * @param listToAdd
     * @return
     */
    public List<T> addAll(List<T> listToAdd);
    
    /**
     * 
     * @param listToAdd
     * @return
     */
    public T add(T entry);
}
