/**
 * 
 */
package org.aksw.defacto.search.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.nlp.ner.NamedEntityTagNormalizer;
import org.aksw.defacto.nlp.ner.StanfordNLPNamedEntityRecognition;

import edu.stanford.nlp.util.StringUtils;

/**
 * @author gerb
 *
 */
public class ParseCallable implements Callable<List<WebSite>> {

	private List<WebSite> websites;
	private StanfordNLPNamedEntityRecognition nerTagger = null;

	public ParseCallable(List<WebSite> websiteSublist) {
		
		this.websites = websiteSublist;
		this.nerTagger = new StanfordNLPNamedEntityRecognition();
	}

	@Override
	public List<WebSite> call() throws Exception {
		
		for ( WebSite site : this.websites ) {
			
			String merged = StringUtils.join(mergeTagsInSentences(nerTagger.getAnnotatedSentences(site.getText())), "-=-");
			site.setTaggedText(merged);
		}
		
		this.nerTagger = null; // set this null to save memory since the executor service is not shut down immediately 
		return this.websites;
	}
	
	public List<String> mergeTagsInSentences(String nerTaggedSentence) {

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
}
