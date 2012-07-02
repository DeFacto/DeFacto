package org.aksw.helper;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 7/1/12
 * Time: 9:07 PM
 * This class is need to pass subject, predicate, object, along with their labels from the MainForm to the SearchResultRepeater
 */
public class TripleComponent {

    private String subject, predicate, object, subjectLabel, predicateLabel, objectLabel;

    public TripleComponent(String subject, String predicate, String object, String subjectLabel, String predicateLabel, String objectLabel) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.subjectLabel = subjectLabel;
        this.predicateLabel = predicateLabel;
        this.objectLabel = objectLabel;
    }

    public String getSubject() {
        return subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }

    public String getSubjectLabel() {
        return subjectLabel;
    }

    public String getPredicateLabel() {
        return predicateLabel;
    }

    public String getObjectLabel() {
        return objectLabel;
    }
}
