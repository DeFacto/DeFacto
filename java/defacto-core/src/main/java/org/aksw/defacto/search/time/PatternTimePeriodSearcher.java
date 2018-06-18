/**
 * 
 */
package org.aksw.defacto.search.time;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoTimePeriod;
import org.aksw.defacto.util.Frequency;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class PatternTimePeriodSearcher {

	public static Set<Pattern> timePatterns = new LinkedHashSet<>();
	public static Frequency patFreq = new Frequency();
	
	static {
		
		timePatterns.add(Pattern.compile("-LRB-\\s*[0-9]{4}\\s*(-|--|–)\\s*[0-9]{4}\\s*-RRB-"));
		timePatterns.add(Pattern.compile("\\(\\s*[0-9]{4}\\s*(-|--|–)}\\s*[0-9]{4}\\s*\\)"));
		timePatterns.add(Pattern.compile("[0-9]{4}\\s*(-|--|–)\\s*[0-9]{4}"));
		timePatterns.add(Pattern.compile("[0-9]{4}\\s*(/|-|--|–)\\s*[0-9]{4}"));
		timePatterns.add(Pattern.compile("[0-9]{4}\\s*(/|-|--|–)\\s*[0-9]{1,2}\\s*[A-z]*\\s*[0-9]{4}")); // - 20 November 
		
		timePatterns.add(Pattern.compile("[bB]etween [0-9]{4} and [0-9]{4}"));
		timePatterns.add(Pattern.compile("[Ff]rom [0-9]{4} to [0-9]{4}"));
		timePatterns.add(Pattern.compile("[0-9]{4} to [0-9]{4}"));
		timePatterns.add(Pattern.compile("[Ff]rom [0-9]{4} through [0-9]{4}"));
		timePatterns.add(Pattern.compile("[Ff]rom [0-9]{4} until [0-9]{4}"));
		timePatterns.add(Pattern.compile("[Ff]rom [0-9]{4}\\s*(-|--|–)\\s*[0-9]{4}"));
		timePatterns.add(Pattern.compile("[bB]etween the years [0-9]{4} and [0-9]{4}"));
		
		timePatterns.add(Pattern.compile("[dD]e [0-9]{4} à [0-9]{4}"));
		timePatterns.add(Pattern.compile("[0-9]{4} à [0-9]{4}"));
		timePatterns.add(Pattern.compile("[dD]e [0-9]{4}\\s*(-|--|–)\\s*[0-9]{4}"));
		timePatterns.add(Pattern.compile("[dD]ans les années [0-9]{4} et [0-9]{4}"));
		timePatterns.add(Pattern.compile("[eE]ntre [0-9]{4} et [0-9]{4}"));
		timePatterns.add(Pattern.compile("[aA]nnées [0-9]{4} et [0-9]{4}"));
		timePatterns.add(Pattern.compile("[dD]urant la période [0-9]{4} - [0-9]{4}"));
		timePatterns.add(Pattern.compile("[eE]n [0-9]{4} et [0-9]{4}"));
		timePatterns.add(Pattern.compile("[eE]ntre les années [0-9]{4} et [0-9]{4}"));
		timePatterns.add(Pattern.compile("[dD]urant les années [0-9]{4} et [0-9]{4}"));
		timePatterns.add(Pattern.compile("[dD]e [0-9]{4} jusqu'en [0-9]{4}"));
		timePatterns.add(Pattern.compile("[dD]e [0-9]{4} et à celui de [0-9]{4}"));
		timePatterns.add(Pattern.compile("[eE]n [0-9]{4} et se termina en [0-9]{4}"));
		
		timePatterns.add(Pattern.compile("[vV]on [0-9]{4} bis [0-9]{4}"));
		timePatterns.add(Pattern.compile("[vV]on [0-9]{4}\\s*(-|--|–)\\s*[0-9]{4}"));
		timePatterns.add(Pattern.compile("[zZ]wischen [0-9]{4} und [0-9]{4}"));
		timePatterns.add(Pattern.compile("[zZ]wischen den Jahren [0-9]{4} und [0-9]{4}"));
		timePatterns.add(Pattern.compile("[iI]n den Jahren [0-9]{4} bis [0-9]{4}"));
		timePatterns.add(Pattern.compile("[0-9]{4} bis [0-9]{4}"));
		timePatterns.add(Pattern.compile("[0-9]{4} bis einschließlich [0-9]{4}"));
		timePatterns.add(Pattern.compile("[aA]us den Jahren [0-9]{4} und [0-9]{4}"));
	}
	
	/**
	 * 
	 * @param evidence
	 * @return 
	 */
	public static DefactoTimePeriod findTimePeriod(Evidence evidence) {
		
		Set<String> sentences = new HashSet<>();
		for ( ComplexProof proof : evidence.getComplexProofs() ) {
			
			switch ( Defacto.DEFACTO_CONFIG.getStringSetting("settings", "context-size") ) {
			
				case "tiny": {
					
					sentences.add(proof.getTinyContext().trim());
					break;
				}
				case "small": {
									
					sentences.add(proof.getSmallContext().trim());
					break;
				}
				case "medium": {
					
					sentences.add(proof.getMediumContext().trim());
					break;
				}
				case "large": {
					
					sentences.add(proof.getLargeContext().trim());
					break;
				}
				default: throw new RuntimeException("Context size not allowed: " + Defacto.DEFACTO_CONFIG.getStringSetting("settings", "context-size")); 
			}
		}
		
		return findTimePeriod(timePatterns, sentences);
	}
	
	/**
	 * 
	 * @param patterns
	 * @param sentences
	 * @return
	 */
	public static DefactoTimePeriod findTimePeriod(Set<Pattern> patterns, Set<String> sentences) {
		
		Frequency firstFreq = new Frequency();
		Frequency secondFreq = new Frequency();
		Frequency bothFreq = new Frequency();
		
		for ( String sentence : sentences ) {
			
			for ( Pattern pat : patterns ) {
				
				Matcher matcher = pat.matcher(sentence);
				while (matcher.find()) {
					
					patFreq.addValue(pat.pattern());
					
					String both = matcher.group();
					Matcher yearMatcher = Pattern.compile("[0-9]{4}").matcher(both);
					List<String> matches = new ArrayList<>();
					
					// get first and second year
					while (yearMatcher.find()) {
						
						String match = yearMatcher.group();
						matches.add(match);
					}
					if ( matches.size() == 2) {
						
						Integer first = Integer.valueOf(matches.get(0));
						Integer second = Integer.valueOf(matches.get(1));
						
						if ( first <= 2013 && first > 1800 && second <= 2013 && second > 1800) {
							
							firstFreq.addValue(matches.get(0));
							secondFreq.addValue(matches.get(1));
							bothFreq.addValue(matches.get(0) + " " + matches.get(1));
						}
					}
					else System.err.println("YEAR MATCHES WENT WRONG: " + matches);
				}
			}
		}
		List<Entry<Comparable<?>, Long>> first = firstFreq.sortByValue();
		List<Entry<Comparable<?>, Long>> second = secondFreq.sortByValue();
		List<Entry<Comparable<?>, Long>> both = bothFreq.sortByValue();
		
		if ( first.isEmpty() || second.isEmpty() ) return null;
		
		if ( Defacto.DEFACTO_CONFIG.getStringSetting("settings", "periodSearchMethod").equals("frequency") )
			return new DefactoTimePeriod(Integer.valueOf((String) first.get(0).getKey()), Integer.valueOf((String)second.get(0).getKey()));
		
		else if ( Defacto.DEFACTO_CONFIG.getStringSetting("settings", "periodSearchMethod").equals("pattern") ) {
			
//			for (Entry<Comparable<?>, Long> entry : both) System.out.println(entry.getKey() +": " + entry.getValue());
			
			return new DefactoTimePeriod(Integer.valueOf(((String) both.get(0).getKey()).split(" ")[0]), Integer.valueOf(((String) both.get(0).getKey()).split(" ")[1]));
		}
		
		else 
			return new DefactoTimePeriod(0,0);
	}
}
