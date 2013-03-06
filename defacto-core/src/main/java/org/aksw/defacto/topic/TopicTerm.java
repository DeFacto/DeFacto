/**
 * 
 */
package org.aksw.defacto.topic;

import java.util.List;
import java.util.Set;

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

	public String label;
	public int occurrence;
	public List<Word> relatedTopics;
}
