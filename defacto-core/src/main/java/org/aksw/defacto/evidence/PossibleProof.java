/**
 * 
 */
package org.aksw.defacto.evidence;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PossibleProof {

    private String firstLabel;
    private String secondLabel;
    private String occurrence;
    private WebSite site;
    private String normalizedOccurrence;

    public PossibleProof(String firstLabel, String secondLabel, String occurrence, String normalizedOccurrence, WebSite site) {

        this.firstLabel = firstLabel;
        this.secondLabel = secondLabel;
        this.occurrence = occurrence;
        this.normalizedOccurrence =  normalizedOccurrence;
        this.site = site;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((normalizedOccurrence == null) ? 0 : normalizedOccurrence.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PossibleProof other = (PossibleProof) obj;
        if (normalizedOccurrence == null) {
            if (other.normalizedOccurrence != null)
                return false;
        }
        else
            if (!normalizedOccurrence.equals(other.normalizedOccurrence))
                return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return String.format("%s___ %s ___%s", firstLabel, normalizedOccurrence, secondLabel);
    }

    public WebSite getWebSite() {

        return this.site;
    }

    public String getSubject() {

        return this.firstLabel;
    }

    public String getPhrase() {

        return this.normalizedOccurrence;
    }

    public String getObject() {

        return this.secondLabel;
    }
}
