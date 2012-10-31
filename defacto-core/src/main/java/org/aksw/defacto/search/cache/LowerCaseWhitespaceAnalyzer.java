package org.aksw.defacto.search.cache;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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

	@Override
	public TokenStream tokenStream(String string, Reader reader) {

		return new LowerCaseFilter(Version.LUCENE_34, new WhitespaceTokenizer(Version.LUCENE_34, reader)); 
	}
	
	public static void main(String[] args) throws IOException {

		String str = "An easy way to write an analyzer for tokens bi-gram (or even tokens n-grams) with lucene";
		Analyzer analyzer = new LowerCaseWhitespaceAnalyzer();

		TokenStream stream = analyzer.tokenStream("content", new StringReader(str));
		CharTermAttribute termAttribute = stream.getAttribute(CharTermAttribute.class);

		System.out.print("Token: ");
		while (stream.incrementToken()) {
		    System.out.print(termAttribute.toString() + " | ");
		}
	}
}
