package org.aksw.defacto.topic.frequency;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import org.apache.lucene.util.Version;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 3/9/12
 * Time: 11:59 AM
 * Helps in counting the frequency of words in a string and sort them descendingly with the frequency
 * copied from http://javabycode.blogspot.com/2010/12/word-frequency-counter.html
 */
public class WordFrequencyCounter {


    /**
     * 
     * @param inputWords
     * @return
     */
    
}