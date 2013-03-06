package org.aksw.defacto.search.cache;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.util.Version;

/**
 * This class is used to make sure that the text to be indexed is converted 
 * into lowercase, where the actual case can still be recovered. Also we 
 * preserve all stop words and punctuation character to be able to make 
 * excat matches!
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public final class LowerCaseWhitespaceAnalyzer extends Analyzer {

	private Version version;
	
	public LowerCaseWhitespaceAnalyzer(Version version) {
		
		this.version = version;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		
		Tokenizer source = new WhitespaceTokenizer(version, reader);
	    TokenStream filter = new LowerCaseFilter(version, source);
		return new TokenStreamComponents(source, filter);
	}
}
