package org.aksw.defacto.evaluation;

/**
 * Created by Diego on 1/19/2016.
 */
public class MetaEvaluationCache {

    private String subjectUri;
    private String subjectLabel;

    private String predicateUri;
    private String predicateLabel;

    private String objectUri;
    private String objectLabel;

    private String sourceModelFileName;
    private String type; //S = swapped, O =original
    private String randomPropertyLabel;
    private String randomSourceModelFileName;
    private String newModelFileName;

    private double overallScore;

    public MetaEvaluationCache(String sURI, String pURI, String oURI, double overallScore){
        this.subjectUri = sURI;
        this.predicateUri = pURI;
        this.objectUri = oURI;
        this.overallScore = overallScore;
    }

    public MetaEvaluationCache(String subjectUri, String subjectLabel, String predicateUri, String predicateLabel,
                               String objectUri, String objectLabel, String sourceModelFileName, String type,
                               String randomPropertyLabel, String randomSourceModelFileName, String newModelFileName,
                               double overallScore){
        this.subjectUri = subjectUri;
        this.subjectLabel = subjectLabel;
        this.predicateUri = predicateUri;
        this.predicateLabel = predicateLabel;
        this.objectUri = objectUri;
        this.objectLabel = objectLabel;
        this.sourceModelFileName = sourceModelFileName;
        this.type = type;
        this.randomPropertyLabel = randomPropertyLabel;
        this.randomSourceModelFileName = randomSourceModelFileName;
        this.newModelFileName = newModelFileName;
        this.overallScore = overallScore;
    }

    public String getObjectURI(){
        return this.objectUri;
    }

    public String getSubjectURI(){
        return this.subjectUri;
    }
    public String getPredicateURI(){
        return this.predicateUri;
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (!(other instanceof MetaEvaluationCache))return false;
        MetaEvaluationCache p = (MetaEvaluationCache) other;
        if ((p.getSubjectURI().equals(this.subjectUri)) &&
                (p.getPredicateURI().equals(this.predicateUri)) &&
                (p.getObjectURI().equals(this.objectUri))){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subjectUri == null) ? 0 : subjectUri.hashCode());
        result = prime * result + ((predicateUri == null) ? 0 : predicateUri.hashCode());
        result = prime * result + ((objectUri == null) ? 0 : objectUri.hashCode());
        return result;
    }
}
