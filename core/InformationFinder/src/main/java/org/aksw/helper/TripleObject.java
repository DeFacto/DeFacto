package org.aksw.helper;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 2/12/12
 * Time: 6:33 AM
 * Represents an object of a triple, as the object is different from the subject and predicate, as it may be
 * a URI or a literal (typed or untyped)
 */
public class TripleObject {

    private boolean isURI;
    private String object;

    public String getObject() {
        return object;
    }

    public boolean isURI() {
        return isURI;
    }

    public TripleObject(String object, boolean URI) {
        isURI = URI;
        this.object = object;
    }
}
