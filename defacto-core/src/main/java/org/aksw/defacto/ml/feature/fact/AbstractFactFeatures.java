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
    
    public static final Attribute SUBJECT                           = new Attribute("subject", (FastVector) null);
    public static final Attribute PHRASE                            = new Attribute("phrase", (FastVector) null);
    public static final Attribute OBJECT                            = new Attribute("object", (FastVector) null);
    public static final Attribute CONTEXT                           = new Attribute("context", (FastVector) null);
    public static final Attribute PROPERTY_NAME                     = new Attribute("property_name", (FastVector) null);
    public static final Attribute TRUE_FALSE_TYPE                   = new Attribute("true_false_type", (FastVector) null);
    
    public static final Attribute TOKEN_DISTANCE                    = new Attribute("token_distance");
    
    public static final Attribute BOA_BOOLEAN                       = new Attribute("boa_feature_boolean");
    public static final Attribute BOA_SCORE                         = new Attribute("boa_feature_score");
    
    public static final Attribute WORDNET_EXPANSION                 = new Attribute("wordnet_expansion");
    
    public static final Attribute TOTAL_OCCURRENCE                  = new Attribute("total_occurrence");
    
    public static final Attribute PAGE_TITLE_SUBJECT                = new Attribute("page_title_subject");
    public static final Attribute PAGE_TITLE_OBJECT                 = new Attribute("page_title_object");
    
    public static final Attribute END_OF_SENTENCE_DOT               = new Attribute("end_of_sentence_dot");
    public static final Attribute END_OF_SENTENCE_QUESTION_MARK     = new Attribute("end_of_sentence_question_mark");
    public static final Attribute END_OF_SENTENCE_EXCLAMATION_MARK  = new Attribute("end_of_sentence_exclamation_mark");

    public static final Attribute NUMBER_OF_NON_ALPHA_NUMERIC_CHARACTERS = new Attribute("number_of_non_alpha_numeric_characters");

	public static final Attribute POSSESSIVE_FEATURE				= new Attribute("possessive_feature");

    public static Attribute CLASS                                   = new Attribute("clazz");
    
    static {

        attributes.addElement(BOA_BOOLEAN);
        attributes.addElement(BOA_SCORE);
        attributes.addElement(PAGE_TITLE_SUBJECT);
        attributes.addElement(PAGE_TITLE_OBJECT);
        attributes.addElement(END_OF_SENTENCE_DOT);
        attributes.addElement(END_OF_SENTENCE_QUESTION_MARK);
        attributes.addElement(END_OF_SENTENCE_EXCLAMATION_MARK);
//        attributes.addElement(NUMBER_OF_NON_ALPHA_NUMERIC_CHARACTERS);
//        attributes.addElement(POSSESSIVE_FEATURE);
        attributes.addElement(TOKEN_DISTANCE);
        attributes.addElement(TOTAL_OCCURRENCE);
        attributes.addElement(WORDNET_EXPANSION);
        attributes.addElement(TRUE_FALSE_TYPE);
        attributes.addElement(PROPERTY_NAME);
        attributes.addElement(SUBJECT);
        attributes.addElement(PHRASE);
        attributes.addElement(OBJECT);
        attributes.addElement(CONTEXT);
        
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
}
