package org.aksw.defacto.model;


/**
 * Created by dnes on 05/01/16.
 */
public class PropertyConfiguration {

    private String subjectClass;
    private String predicateUri;
    private String objectClass;
    private boolean isFunctional;
    private String resourceToBeChangedForRubbish;

    public PropertyConfiguration(String predicate, String domainClass, String rangeClass, boolean functional, String resourceToBeChangedForRubbish){
        this.subjectClass = domainClass;
        this.predicateUri = predicate;
        this.objectClass = rangeClass;
        this.isFunctional = functional;
        this.resourceToBeChangedForRubbish = resourceToBeChangedForRubbish;
    }

    public String getSubjectClass(){
        return this.subjectClass;
    }

    public String getObjectClass(){
        return this.objectClass;
    }

    public boolean isFunctionalProperty() {
        return this.isFunctional;
    }

    public String getResourceToBeChangedForRubbish(){
        return this.resourceToBeChangedForRubbish;
    }

}
