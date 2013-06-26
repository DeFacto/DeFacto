/**
 * 
 */
package org.aksw.defacto.search.concurrent;

import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.defacto.util.NlpUtil;
import org.apache.log4j.Logger;

import edu.stanford.nlp.util.StringUtils;

/**
 * @author gerb
 *
 */
public class ParseCallable implements Callable<List<ComplexProof>> {

	private List<ComplexProof> proofs;
	private StanfordNLPNamedEntityRecognition nerTagger = null;
	private Logger logger = Logger.getLogger(ParseCallable.class);

	public ParseCallable(List<ComplexProof> proofsSublist) {

		this.proofs = proofsSublist;
	}

	@Override
	public List<ComplexProof> call() throws Exception {
		
		this.nerTagger = NlpModelManager.getInstance().getNlpModel();
		
		for ( ComplexProof proof : this.proofs ) {
			
//			String merged = StringUtils.join(NlpUtil.mergeConsecutiveNerTags(nerTagger.getAnnotatedSentences(proof.getLargeContext())), "-=-");
//			proof.setTaggedLargeContext(merged);
//			
//			merged = StringUtils.join(NlpUtil.mergeConsecutiveNerTags(nerTagger.getAnnotatedSentences(proof.getMediumContext())), "-=-");
//			proof.setTaggedMediumContext(merged);
//			
//			merged = StringUtils.join(NlpUtil.mergeConsecutiveNerTags(nerTagger.getAnnotatedSentences(proof.getSmallContext())), "-=-");
//			proof.setTaggedSmallContext(merged);
		}
		
		NlpModelManager.getInstance().releaseModel(this.nerTagger);
		
		return this.proofs;
	}
}
