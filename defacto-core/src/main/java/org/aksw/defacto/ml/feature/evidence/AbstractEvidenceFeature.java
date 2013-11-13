package org.aksw.defacto.ml.feature.evidence;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public abstract class AbstractEvidenceFeature implements EvidenceFeature {

    public static FastVector attributes;
    
    public static final Attribute PAGE_RANK_MAX                                = new Attribute("page_rank_max");
    public static final Attribute PAGE_RANK_SUM                                = new Attribute("page_rank_sum");
    public static final Attribute TOTAL_HIT_COUNT_FEATURE                      = new Attribute("total_hit_count");
    public static final Attribute TOPIC_COVERAGE_SUM                           = new Attribute("topic_coverage_sum");
    public static final Attribute TOPIC_COVERAGE_MAX                           = new Attribute("topic_coverage_max");
    public static final Attribute TOPIC_MAJORITY_WEB_SUM                       = new Attribute("topic_majority_web_sum");
    public static final Attribute TOPIC_MAJORITY_WEB_MAX                       = new Attribute("topic_majority_web_max");
    public static final Attribute TOPIC_MAJORITY_SEARCH_RESULT_SUM             = new Attribute("topic_majority_search_sum");
    public static final Attribute TOPIC_MAJORITY_SEARCH_RESULT_MAX             = new Attribute("topic_majority_search_max");
    public static final Attribute NUMBER_OF_PROOFS                             = new Attribute("number_of_proofs");
    public static final Attribute NUMBER_OF_CONFIRMING_PROOFS                  = new Attribute("number_of_confirming_sites");
//    public static final Attribute NUMBER_OF_POSSIBLY_CONFIRMING_FACTS          = new Attribute("number_of_possibly_confirming_facts");
//    public static final Attribute NUMBER_OF_POSSIBLY_CONFIRMING_SITES          = new Attribute("number_of_possibly_confirming_sites");
//    public static final Attribute MAX_NUMBER_OF_POSSIBLY_CONFIRMING_FACT       = new Attribute("max_number_of_possible_confirming_fact");
    public static final Attribute TOTAL_POSITIVES_EVIDENCE_SCORE               = new Attribute("total_positives_evidence_score");
    public static final Attribute TOTAL_NEGATIVES_EVIDENCE_SCORE               = new Attribute("total_negatives_evidence_score");
    public static final Attribute MODEL_NAME                                   = new Attribute("name_feature", (FastVector) null);
    public static final Attribute DOMAIN_RANGE_CHECK                           = new Attribute("domain_range_check");
    public static final Attribute GOODNESS 									   = new Attribute("goodness");
    
    public static Attribute CLASS                                              = new Attribute("clazz");
    public static Instances provenance;

    static {

    	createInstances();
    }
    
    public static void createInstances(){
    	
    	attributes = new FastVector();
    	attributes.addElement(MODEL_NAME);
        attributes.addElement(PAGE_RANK_MAX);
        attributes.addElement(PAGE_RANK_SUM);
        attributes.addElement(TOTAL_HIT_COUNT_FEATURE);
        attributes.addElement(TOPIC_COVERAGE_SUM);
        attributes.addElement(TOPIC_COVERAGE_MAX);
        attributes.addElement(TOPIC_MAJORITY_WEB_SUM);
        attributes.addElement(TOPIC_MAJORITY_WEB_MAX);
        attributes.addElement(TOPIC_MAJORITY_SEARCH_RESULT_SUM);
        attributes.addElement(TOPIC_MAJORITY_SEARCH_RESULT_MAX);
        attributes.addElement(NUMBER_OF_PROOFS);
        attributes.addElement(GOODNESS);
        attributes.addElement(NUMBER_OF_CONFIRMING_PROOFS);
//        attributes.addElement(NUMBER_OF_POSSIBLY_CONFIRMING_FACTS);
//        attributes.addElement(NUMBER_OF_POSSIBLY_CONFIRMING_SITES);
//        attributes.addElement(MAX_NUMBER_OF_POSSIBLY_CONFIRMING_FACT);
        attributes.addElement(TOTAL_POSITIVES_EVIDENCE_SCORE);
        attributes.addElement(TOTAL_NEGATIVES_EVIDENCE_SCORE);
        attributes.addElement(DOMAIN_RANGE_CHECK);
        FastVector clazz = new FastVector(2);
        clazz.addElement("true");
        clazz.addElement("false");
        CLASS = new Attribute("class", clazz);
        attributes.addElement(CLASS);
    	
    	provenance = new Instances("defacto", attributes, 0);
        provenance.setClass(CLASS);
    }
}
