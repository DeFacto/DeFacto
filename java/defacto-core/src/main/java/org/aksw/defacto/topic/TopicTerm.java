/**
 * 
 */
package org.aksw.defacto.topic;

import java.util.List;

import org.aksw.defacto.topic.frequency.Word;

/**
 * @author gerb
 *
 */
public class TopicTerm {

	public TopicTerm(String identifier) {
		this.label = identifier;
	}
	
	public TopicTerm(String subjectLabel, List<Word> topics) {
		this.label = subjectLabel;
		this.relatedTopics = topics;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		TopicTerm other = (TopicTerm) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String term = "Label=" + this.label + ": ";
		for ( Word w : this.relatedTopics ) term += w.getWord() + "("+w.getFrequency()+") ";
		return term.trim();
	}

	public String label;
	public int occurrence;
	public List<Word> relatedTopics;
}
