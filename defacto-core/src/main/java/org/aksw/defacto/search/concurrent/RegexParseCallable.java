/**
 * 
 */
package org.aksw.defacto.search.concurrent;

import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.nlp.ner.RegexTagger;
import org.aksw.defacto.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.defacto.util.NlpUtil;
import org.apache.log4j.Logger;

import edu.stanford.nlp.util.StringUtils;

/**
 * @author gerb
 *
 */
public class RegexParseCallable implements Callable<List<ComplexProof>>, ParseCallable {

	private List<ComplexProof> proofs;
	private Logger logger = Logger.getLogger(RegexParseCallable.class);
	private RegexTagger dateTagger = new RegexTagger();

	public RegexParseCallable(List<ComplexProof> proofsSublist) {

		this.proofs = proofsSublist;
	}

	@Override
	public List<ComplexProof> call() throws Exception {
		
		for ( ComplexProof proof : this.proofs ) {
			
			String merged = StringUtils.join(NlpUtil.mergeConsecutiveNerTags(dateTagger.getAnnotatedSentences(proof.getLargeContext())), "-=-");
			proof.setTaggedLargeContext(merged);
			
			merged = StringUtils.join(NlpUtil.mergeConsecutiveNerTags(dateTagger.getAnnotatedSentences(proof.getMediumContext())), "-=-");
			proof.setTaggedMediumContext(merged);
			
			merged = StringUtils.join(NlpUtil.mergeConsecutiveNerTags(dateTagger.getAnnotatedSentences(proof.getSmallContext())), "-=-");
			proof.setTaggedSmallContext(merged);
			
			merged = StringUtils.join(NlpUtil.mergeConsecutiveNerTags(dateTagger.getAnnotatedSentences(proof.getTinyContext())), "-=-");
			System.out.println(proof.getTinyContext());
			proof.setTaggedTinyContext(merged);
		}
		
		return this.proofs;
	}
}
