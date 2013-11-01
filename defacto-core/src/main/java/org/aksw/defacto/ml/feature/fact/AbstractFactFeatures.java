/**
 * 
 */
package org.aksw.defacto.ml.feature.fact;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class AbstractFactFeatures {

    public static FastVector attributes = new FastVector();
    
    public static final Attribute QGRAMS 									= new Attribute("qgrams");;
    public static final Attribute PHRASE                            		= new Attribute("phrase", (FastVector) null);
    public static final Attribute OBJECT                            		= new Attribute("object", (FastVector) null);
    public static final Attribute SUBJECT                           		= new Attribute("subject", (FastVector) null);
    public static final Attribute CONTEXT                           		= new Attribute("context", (FastVector) null);
    public static final Attribute CORRECT                   				= new Attribute("correct", (FastVector) null);
    public static final Attribute FILE_NAME                   				= new Attribute("filename", (FastVector) null);
    public static final Attribute DIGIT_COUNT 								= new Attribute("digit_count");
    public static final Attribute COMMA_COUNT 								= new Attribute("comma_count");
    public static final Attribute LEVENSHTEIN 								= new Attribute("levenshtein");
    public static final Attribute TOKEN_DISTANCE                    		= new Attribute("token_distance");
    public static final Attribute SMITH_WATERMAN 							= new Attribute("smith_waterman");;
    public static final Attribute CHARACTER_COUNT 							= new Attribute("character_count");;
    public static final Attribute QGRAMS_BOA_SCORE 							= new Attribute("qgrams_boa_score");;
    public static final Attribute TOTAL_OCCURRENCE                  		= new Attribute("total_occurrence");
    public static final Attribute WORDNET_EXPANSION                 		= new Attribute("wordnet_expansion");
    public static final Attribute PAGE_TITLE_OBJECT                 		= new Attribute("page_title_object");
    public static final Attribute PAGE_TITLE_SUBJECT                		= new Attribute("page_title_subject");
    public static final Attribute POSSESSIVE_FEATURE						= new Attribute("possessive_feature");
    public static final Attribute END_OF_SENTENCE_DOT               		= new Attribute("end_of_sentence_dot");
    public static final Attribute AVERAGE_TOKEN_LENGHT 						= new Attribute("average_token_length");
    public static final Attribute LEVENSHTEIN_BOA_SCORE 					= new Attribute("levenshtein_boa_score");
    public static final Attribute UPPERCASE_LETTER_COUNT 					= new Attribute("uppercase_letters_count");
    public static final Attribute SMITH_WATERMAN_BOA_SCORE                  = new Attribute("sw_boa_score");
    public static final Attribute END_OF_SENTENCE_QUESTION_MARK     		= new Attribute("end_of_sentence_question_mark");
    public static final Attribute END_OF_SENTENCE_EXCLAMATION_MARK  		= new Attribute("end_of_sentence_exclamation_mark");
    public static final Attribute NUMBER_OF_NON_ALPHA_NUMERIC_CHARACTERS 	= new Attribute("number_of_non_alpha_numeric_characters");

	public static final Attribute SUBJECT_SIMILARITY 						= new Attribute("subject_similarity");
	public static final Attribute OBJECT_SIMILARITY 						= new Attribute("object_similarity");

	public static final Attribute BOA_PATTERN_COUNT 						= new Attribute("boa_pattern_count");
	public static final Attribute BOA_PATTERN_NORMALIZED_COUNT 				= new Attribute("boa_pattern_norm_count");

    public static Attribute CLASS                                   		= new Attribute("clazz");
    public static Attribute LANGUAGE                   						= new Attribute("language");
    public static Attribute PROPERTY_NAME                     				= new Attribute("property_name");
    
    static {

    	attributes.addElement(BOA_PATTERN_NORMALIZED_COUNT);
    	attributes.addElement(BOA_PATTERN_COUNT);
    	attributes.addElement(QGRAMS);
    	attributes.addElement(QGRAMS_BOA_SCORE);
    	attributes.addElement(SMITH_WATERMAN_BOA_SCORE);
        attributes.addElement(SMITH_WATERMAN);
        attributes.addElement(LEVENSHTEIN);
        attributes.addElement(LEVENSHTEIN_BOA_SCORE);
        
        attributes.addElement(OBJECT_SIMILARITY);
        attributes.addElement(SUBJECT_SIMILARITY);

        attributes.addElement(COMMA_COUNT);
    	attributes.addElement(DIGIT_COUNT);
        attributes.addElement(AVERAGE_TOKEN_LENGHT);
    	attributes.addElement(UPPERCASE_LETTER_COUNT);
    	attributes.addElement(CHARACTER_COUNT);
    	attributes.addElement(END_OF_SENTENCE_DOT);
        attributes.addElement(END_OF_SENTENCE_QUESTION_MARK);
        attributes.addElement(END_OF_SENTENCE_EXCLAMATION_MARK);
        
        attributes.addElement(PAGE_TITLE_SUBJECT);
        attributes.addElement(PAGE_TITLE_OBJECT);
        
        
        attributes.addElement(NUMBER_OF_NON_ALPHA_NUMERIC_CHARACTERS);
//        attributes.addElement(POSSESSIVE_FEATURE);
        attributes.addElement(TOKEN_DISTANCE);
        attributes.addElement(TOTAL_OCCURRENCE);
        attributes.addElement(WORDNET_EXPANSION);
        attributes.addElement(FILE_NAME);
        attributes.addElement(SUBJECT);
        attributes.addElement(PHRASE);
        attributes.addElement(OBJECT);
        attributes.addElement(CONTEXT);
        
        FastVector languages = new FastVector(3);
        languages.addElement("de");
        languages.addElement("en");
        languages.addElement("fr");
        LANGUAGE = new Attribute("language", languages);
        attributes.addElement(LANGUAGE);
        
        FastVector propertyName = new FastVector(2);
        propertyName.addElement("team");
        propertyName.addElement("spouse");
        propertyName.addElement("foundationPlace");
        propertyName.addElement("author");
        propertyName.addElement("award");
        propertyName.addElement("subsidiary");
        propertyName.addElement("leaderName");
        propertyName.addElement("birthPlace");
        propertyName.addElement("deathPlace");
        propertyName.addElement("starring");
        PROPERTY_NAME = new Attribute("property_name", propertyName);
        attributes.addElement(PROPERTY_NAME);
        
        FastVector clazz = new FastVector(2);
        clazz.addElement("true");
        clazz.addElement("false");
        CLASS = new Attribute("class", clazz);
        attributes.addElement(CLASS);
    }
    
    public static Instances factFeatures  = new Instances("fact_confirmation", attributes, 0);
    
    static {
        
        factFeatures.setClass(CLASS);
    }
    
    public static void main(String[] args) {
    	System.out.println(AbstractFactFeatures.factFeatures.toString());
	}
}
