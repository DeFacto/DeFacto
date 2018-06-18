/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.defacto.wordnet;

import java.util.HashSet;
import java.util.Set;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;

/**
 *
 * @author ngonga
 */
public class WordNetExpansion {

    WordNetDatabase database;

    public WordNetExpansion(String dictionary) {
        System.setProperty("wordnet.database.dir", dictionary);
        database = WordNetDatabase.getFileInstance();        
    }
    
    /** Expands a single keyword by retrieving all the elements of all its synsets
     * 
     * @param keyword Input token
     * @return All elements of all synsets of keyword
     */
    private Set<String> getSynset(String keyword)
    {
        Set<String> result = new HashSet<String>();
        Synset[] synsets = database.getSynsets(keyword);
        for (int i = 0; i < synsets.length; i++) {
            String[] s = synsets[i].getWordForms();
            for (int j = 0; j < s.length; j++) {
                result.add(s[j]);
            }
        }
        return result;
    }
    
    /** Expand a string by chunking it into tokens and expanding each of the
     * tokens using WordNet
     * @param keywords Input string
     * @return  Set of tokens after Wordnet expansion
     */
    private Set<String> expand(String keywords)
    {
        String[] split = keywords.split(" ");
        Set<String> result = new HashSet<String>();
        Set<String> buffer;
        for(int i=0; i<split.length; i++)
        {
            // no need to expand prepositions and the like
            if(split[i].length() > 2)
            {
                buffer = getSynset(split[i]);
                for(String s: buffer)
                    result.add(s);
            }
            else
            {
                result.add(split[i]);
            }
        }
        return result;
    }
    
    /** Computes the jaccard similarity of two strings after carrying out a WordNet
     * expansion of each of the tokens of the input string
     * @param s1 First input string
     * @param s2 Second input string
     * @return Similarity value between 0 and 1.
     */
    public double getExpandedJaccardSimilarity(String s1, String s2)
    {
        Set<String> tokens1 = expand(s1);
        Set<String> tokens2 = expand(s2);
        
        Set<String> intersection = new HashSet(tokens1);
        intersection.retainAll(tokens2);
        Set<String> union = new HashSet(tokens1);
        union.addAll(tokens2);
        
        return ((double)intersection.size())/((double)union.size());
    }
    
    public static void main(String args[])
    {
        WordNetExpansion wne = new WordNetExpansion("resources/wordnet/dict");
        String token = "couch";
        System.out.println(wne.getSynset(token));
        String token2 = "table";
        System.out.println(wne.getSynset(token2));
        System.out.println(wne.getExpandedJaccardSimilarity(token, token2));
        
    }
}
