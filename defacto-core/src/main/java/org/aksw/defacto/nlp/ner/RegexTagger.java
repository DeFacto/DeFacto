/**
 * 
 */
package org.aksw.defacto.nlp.ner;

import org.aksw.defacto.Constants;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class RegexTagger {
	
	java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[12][0-9]{3}");

	public String getAnnotatedDateSentences(String textToTag) {
		
		Set<String> years = new HashSet<String>();
		Matcher matcher = pattern.matcher(textToTag);
	    while (matcher.find()) years.add(matcher.group());
	    
	    StringBuffer buffer = new StringBuffer();
	    for ( String part : textToTag.split(" ")) {
	    	
	    	if ( years.contains(part) ) buffer.append(part + Constants.NAMED_ENTITY_TAG_DELIMITER + NamedEntityTagNormalizer.NAMED_ENTITY_TAG_DATE + " ");
	    	else buffer.append(part + Constants.NAMED_ENTITY_TAG_DELIMITER + NamedEntityTagNormalizer.NAMED_ENTITY_TAG_OTHER + " ");
	    }
		return buffer.toString().trim();
	}
	
	
	public static void main(String[] args) {
		
		String test = "This is a year 1872 this is not a year 345 this is also not a year 3323 but his 1293 2013";
		RegexTagger t  = new RegexTagger();
		System.out.println(t.getAnnotatedDateSentences(test));
	}
}
