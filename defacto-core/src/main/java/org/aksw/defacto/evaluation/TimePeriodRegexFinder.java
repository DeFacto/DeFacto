/**
 * 
 */
package org.aksw.defacto.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class TimePeriodRegexFinder {

	private static final int MAXIMUM_MATCH_LENGTH = 6;
	
	static Analyzer analyzer         = new org.aksw.defacto.search.cache.LowerCaseWhitespaceAnalyzer(Version.LUCENE_34);
	static QueryParser parser        = new QueryParser(Version.LUCENE_34, "sentence", analyzer);
	

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ParseException {
		
//		String textToTag = "Bacon ipsum dolor 2004 sit amet bacon 1009 filet mignon jowl short 2013 ribs flank boudin corned "
//				+ "beef 2013 fatback 12 shank ground 900 round kielbasa. 2001 Salami short ribs ham 1910 frankfurter. "
//				+ "Prosciutto kielbasa filet mignon short loin salami hamburger turducken sausage tongue boudin short "
//				+ "ribs meatloaf ground round fatback. Boudin jowl pork belly chicken, shankle corned beef meatloaf.";
//		
//		for ( YearMatch matching : findYearMatches(textToTag) ) {
//			
//			System.out.println(matching.beforeMatch);
//			System.out.println(matching.startYear);
//			System.out.println(StringUtils.join(matching.match, " "));
//			System.out.println(matching.endYear);
//			System.out.println(matching.afterMatch);
//			System.out.println();
//		}
		
		getPatterns();
	}
	
	public static void getPatterns() throws IOException, ParseException{
		
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_34, new org.aksw.defacto.search.cache.LowerCaseWhitespaceAnalyzer(Version.LUCENE_34));
        indexWriterConfig.setOpenMode(OpenMode.APPEND);
        
        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(NIOFSDirectory.open(new File("/Users/gerb/Development/workspaces/experimental/boa/qa/en/index/corpus"))));
		
		Query q =  parser.parse("sentence:(1997) AND sentence:(2005)");

		Map<Integer,Set<String>> luceneDocIdsToPatterns = new HashMap<Integer,Set<String>>();
		
		// go through all sentences and surface form combinations 
		for ( ScoreDoc hit : indexSearcher.search(q, 1000).scoreDocs ) {
		
			String sentence     = indexSearcher.doc(hit.doc).get("sentence");
			List<String> currentMatches = findMatchedText(sentence, "1997", "2005");
			
			System.out.println(currentMatches);
		}
	}
	
    protected static List<String> findMatchedText(final String sentence, final String firstLabel, final String secondLabel){
        
        final String sentenceLowerCase    = sentence.toLowerCase();
        List<String> currentMatches = new ArrayList<String>();
        
        // subject comes first
        String[] match1 = StringUtils.substringsBetween(sentenceLowerCase, firstLabel, secondLabel);
        if (match1 != null) {

            for (int j = 0; j < match1.length; j++) 
                currentMatches.add("?D? " + match1[j].trim() + " ?R?");
        }
        // object comes first
        String[] match2 = StringUtils.substringsBetween(sentenceLowerCase, secondLabel, firstLabel);
        if (match2 != null) {

            for (int j = 0; j < match2.length; j++) 
                currentMatches.add("?R? " + match2[j].trim() + " ?D?");
        }

        return currentMatches;
    }
	
	public static List<YearMatch> findYearMatches(String textToTag){
		
		List<String> words = Arrays.asList(textToTag.split(" "));
		List<YearMatch> matches = new ArrayList<YearMatch>();
		
		YearMatch match = new YearMatch();
		
		for ( int i = 0; i < words.size() ; i++ ){
			
			String token = words.get(i);
			
			// we found a year
			if ( token.matches("[12][0-9]{3}") ) {
				
				// we found the start of a match
				if ( match.startYear == -1 ) {
					
					match.startYear = Integer.valueOf(token);
					if ( i - 3 >= 0 ) match.beforeMatch.add(words.get(i - 3));
					if ( i - 2 >= 0 ) match.beforeMatch.add(words.get(i - 2));
					if ( i - 1 >= 0 ) match.beforeMatch.add(words.get(i - 1));
				}
				// we found the end of a match
				else {
					
					match.endYear = Integer.valueOf(token);
					
					if ( i + 1 < words.size() ) match.afterMatch.add(words.get(i + 1));
					if ( i + 2 < words.size() ) match.afterMatch.add(words.get(i + 2));
					if ( i + 3 < words.size() ) match.afterMatch.add(words.get(i + 3));
					
					if ( match.size() <= MAXIMUM_MATCH_LENGTH ) matches.add(match);
						
					// make the new start the old end
					match = new YearMatch();
				}
			}
			// we found a normal token
			else {
				
				// so far no match
				if ( match.startYear == -1 ) {
					
					continue;
				}
				else {
					
					match.add(token);
				}
			}
		}
		return matches;
	}
	
	
	public static class YearMatch {
		
		public List<String> beforeMatch = new ArrayList<String>();
		public List<String> afterMatch = new ArrayList<String>();
		public List<String> match = new ArrayList<String>();
		public int startYear = -1;
		public int endYear = -1;
		public boolean isEmpty() {
			return match.isEmpty();
		}
		public int size() {
			return match.size();
		}
		public void add(String token) {
			
			match.add(token);
		}
	}
}
