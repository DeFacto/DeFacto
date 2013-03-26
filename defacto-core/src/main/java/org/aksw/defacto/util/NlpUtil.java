/**
 * 
 */
package org.aksw.defacto.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gerb
 *
 */
public class NlpUtil {

	public static List<String> mergeConsecutiveNerTags(String nerTaggedSentence) {

        List<String> tokens = new ArrayList<String>();
        String lastToken = "";
        String lastTag = "";
        String currentTag = "";
        String newToken = "";
        
        for (String currentToken : nerTaggedSentence.split(" ")) {

            currentTag = currentToken.substring(currentToken.lastIndexOf("_") + 1);

            // we need to check for the previous token's tag
            if (!currentToken.endsWith("_OTHER")) {

                // we need to merge the cell
                if (currentTag.equals(lastTag)) {

                    newToken = lastToken.substring(0, lastToken.lastIndexOf("_")) + " " + currentToken;
                    tokens.set(tokens.size() - 1, newToken);
                }
                // different tag found so just add it
                else
                    tokens.add(currentToken);
            }
            else {

                // add the current token
                tokens.add(currentToken);
            }
            // update for next iteration
            lastToken = tokens.get(tokens.size() - 1);
            lastTag = currentTag;
        }
        return tokens;
    }
	
    /**
     * 
     * @param mergedTaggedSentence
     * @return
     */
	public static List<String> getDateEntities(List<String> mergedTaggedSentence) {

		List<String> entities = new ArrayList<String>();
		for (String entity : mergedTaggedSentence) {

			if (entity.endsWith("_DATE"))
				entities.add(entity.replace("_DATE", ""));
		}
		
		return entities;
	}
}
