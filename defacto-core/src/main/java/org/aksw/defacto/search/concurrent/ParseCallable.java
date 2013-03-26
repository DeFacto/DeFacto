/**
 * 
 */
package org.aksw.defacto.search.concurrent;

import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.defacto.util.NlpUtil;

import edu.stanford.nlp.util.StringUtils;

/**
 * @author gerb
 *
 */
public class ParseCallable implements Callable<List<ComplexProof>> {

	private List<ComplexProof> proofs;
	private StanfordNLPNamedEntityRecognition nerTagger = null;

	public ParseCallable(List<ComplexProof> proofsSublist) {

		this.nerTagger = new StanfordNLPNamedEntityRecognition();
		this.proofs = proofsSublist;
	}

	@Override
	public List<ComplexProof> call() throws Exception {
		
		for ( ComplexProof proof : this.proofs ) {
			
			String merged = StringUtils.join(NlpUtil.mergeConsecutiveNerTags(nerTagger.getAnnotatedSentences(proof.getLongContext())), "-=-");
			proof.setTaggedLongContext(merged);
		}
		
		this.nerTagger = null; // set this null to save memory since the executor service is not shut down immediately 
		return this.proofs;
	}
}
