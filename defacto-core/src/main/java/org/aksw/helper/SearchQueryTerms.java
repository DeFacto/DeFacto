package org.aksw.helper;

import org.aksw.boa.BoaSearchResult;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA. User: Mohamed Morsey Date: 3/11/12 Time: 3:02 PM
 * Contains search terms, which are the subject label, object label, and the
 * various NL representations obtained from BOA
 */
public class SearchQueryTerms {

    private String subjectLabel, objectLabel;
    private ArrayList<BoaSearchResult> BoaPredicate_Representations;

    public SearchQueryTerms(String subjectLabel, String objectLabel, ArrayList<BoaSearchResult> boaPredicate_Representations) {

        this.subjectLabel = subjectLabel;
        this.objectLabel = objectLabel;
        BoaPredicate_Representations = boaPredicate_Representations;
    }

    public String getSubjectLabel() {

        return subjectLabel;
    }

    public String getObjectLabel() {

        return objectLabel;
    }

    public ArrayList<BoaSearchResult> getBoaPredicate_Representations() {

        return BoaPredicate_Representations;
    }
}
