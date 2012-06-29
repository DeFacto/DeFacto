package org.aksw.boa;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 2/12/12
 * Time: 3:32 PM
 * Holds a result acquired through searching BOA index.
 */
public class BoaSearchResult implements Comparable<BoaSearchResult>{
    String NL_Represntation;
    String URI;
    double confidence;

    public String getNL_Represntation() {
        return NL_Represntation;
    }

    public String getURI() {
        return URI;
    }

    public double getConfidence() {
        return confidence;
    }

    public BoaSearchResult(String NL_Represntation, String URI, double confidence) {
        this.NL_Represntation = NL_Represntation;
        this.URI = URI;
        this.confidence = confidence;
    }
    
    public int compareTo(BoaSearchResult boaSearchResult){

        //We should sort the results descendingly according to the confidence, so we use and negative sign in front of
        //the comparison result
        return -Double.compare(this.confidence, boaSearchResult.confidence);
    }
}
