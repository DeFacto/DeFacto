package org.aksw.defacto.evidence;

import org.aksw.defacto.boa.Pattern;

/**
 * A proof for a particular fact.
 * 
 * @author Jens Lehmann
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class Proof {

	private String phrase;
	private WebSite webSite;
	private Pattern pattern;
	private String subject;
	private String object;
	
	/**
	 * @param phrase
	 * @param webSite
	 * @param pattern
	 */
	public Proof(String phrase, WebSite webSite, Pattern pattern) {
		super();
		this.phrase = phrase;
		this.webSite = webSite;
		this.pattern = pattern;
	}
	
	/**
	 * 
	 * @param subject
	 * @param object
	 * @param phrase
	 * @param webSite
	 * @param pattern
	 */
	public Proof(String subject, String object, String phrase, WebSite webSite, Pattern pattern) {
        super();
        this.subject    = subject;
        this.object    = object;
        this.phrase     = phrase;
        this.webSite    = webSite;
        this.pattern    = pattern;
    }
	
	/**
	 * 
	 * @return
	 */
	public String getSubject() {
        return subject;
    }
	
	/**
	 * 
	 * @return
	 */
	public String getObject() {
        return object;
    }
	
	/**
	 * 
	 * @return
	 */
	public String getPhrase() {
		return phrase;
	}
	
	/**
	 * 
	 * @return
	 */
	public WebSite getWebSite() {
		return webSite;
	}
	
	/**
	 * 
	 * @return
	 */
	public Pattern getPattern() {
		return pattern;
	}
	
	/**
	 * 
	 */
	public String toString() {
		return phrase;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        result = prime * result + ((phrase == null) ? 0 : phrase.hashCode());
        result = prime * result + ((webSite == null) ? 0 : webSite.hashCode());
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
        Proof other = (Proof) obj;
        if (pattern == null) {
            if (other.pattern != null)
                return false;
        }
        else
            if (!pattern.equals(other.pattern))
                return false;
        if (phrase == null) {
            if (other.phrase != null)
                return false;
        }
        else
            if (!phrase.equals(other.phrase))
                return false;
        if (webSite == null) {
            if (other.webSite != null)
                return false;
        }
        else
            if (!webSite.equals(other.webSite))
                return false;
        return true;
    }
	
}
