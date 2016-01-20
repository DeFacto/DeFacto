package org.aksw.defacto.evaluation;

import org.aksw.defacto.evidence.Evidence;

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
    private int header;

    private Integer df_num_cp;
    private Integer df_num_tt;
    private Long df_num_hc;
    private Double df_score;

    public int getHeader() {
        return header;
    }

    public Double getOverallScore() {
        return this.df_score;
    }
    public Long getTotalHitCount(){
        return this.df_num_hc;
    }
    public Integer getTotalTopicTerms() {
        return this.df_num_tt;
    }
    public Integer getTotalComplexProofs() {
        return this.df_num_cp;
    }

    public String getNewModelFileName() {
        return newModelFileName;
    }

    public String getRandomSourceModelFileName() {
        return randomSourceModelFileName;
    }

    public String getRandomPropertyLabel() {
        return randomPropertyLabel;
    }

    public String getType() {
        return type;
    }

    public String getSourceModelFileName() {
        return sourceModelFileName;
    }

    public String getObjectLabel() {
        return objectLabel;
    }

    public String getPredicateLabel() {
        return predicateLabel;
    }

    public String getSubjectLabel() {
        return subjectLabel;
    }

    public MetaEvaluationCache(String sURI, String pURI, String oURI, Integer df_num_cp,  Integer df_num_tt, Long df_num_hc, Double df_score){
        this.subjectUri = sURI;
        this.predicateUri = pURI;
        this.objectUri = oURI;
        this.df_num_cp = df_num_cp;
        this.df_num_tt = df_num_tt;
        this.df_num_hc = df_num_hc;
        this.df_score = df_score;

    }

    public MetaEvaluationCache(String sURI, String pURI, String oURI){
        this.subjectUri = sURI;
        this.predicateUri = pURI;
        this.objectUri = oURI;
    }

    public MetaEvaluationCache(Integer df_num_cp,  Integer df_num_tt, Long df_num_hc, Double df_score, String sourceModelFileName, String subjectUri, String subjectLabel, String predicateUri, String predicateLabel,
                               String objectUri, String objectLabel, String type,
                               String randomPropertyLabel, String randomSourceModelFileName, String newModelFileName, int header){
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
        this.df_num_cp = df_num_cp;
        this.df_num_tt = df_num_tt;
        this.df_num_hc = df_num_hc;
        this.df_score = df_score;
        this.header = header;
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
