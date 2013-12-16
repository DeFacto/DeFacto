/**
 * 
 */
package org.aksw.defacto.search.concurrent;

import java.util.concurrent.Callable;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.nlp.ner.RegexTagger;
import org.aksw.defacto.util.NlpUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author gerb
 *
 */
public class RegexParseCallable implements Callable<ComplexProof>, ParseCallable {

	private ComplexProof proof;
	private Logger logger = Logger.getLogger(RegexParseCallable.class);
	private RegexTagger dateTagger = new RegexTagger();

	public RegexParseCallable(ComplexProof proofsSublist) {

		this.proof = proofsSublist;
	}

	@Override
	public ComplexProof call() throws Exception {
		
//		for ( ComplexProof proof : this.proofs ) {
			
			String merged = StringUtils.join(NlpUtil.mergeConsecutiveNerTags(dateTagger.getAnnotatedSentences(proof.getLargeContext())), "-=-");
			proof.setTaggedLargeContext(merged);
			
			merged = StringUtils.join(NlpUtil.mergeConsecutiveNerTags(dateTagger.getAnnotatedSentences(proof.getMediumContext())), "-=-");
			proof.setTaggedMediumContext(merged);
			
			merged = StringUtils.join(NlpUtil.mergeConsecutiveNerTags(dateTagger.getAnnotatedSentences(proof.getSmallContext())), "-=-");
			proof.setTaggedSmallContext(merged);
			
			merged = StringUtils.join(NlpUtil.mergeConsecutiveNerTags(dateTagger.getAnnotatedSentences(proof.getTinyContext())), "-=-");
			proof.setTaggedTinyContext(merged);
//		}
		
		return this.proof;
	}
}
