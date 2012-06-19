package org.aksw.provenance;


public class Constants {

    /**
     * how many boa patterns should be used to search: N * NUMBER_OF_BOA_PATTERNS search results will be generated
     */
    public static final int NUMBER_OF_BOA_PATTERNS = 10;
    
    /**
     * this is used to get the labels of the resources from the training models
     */
    public static final Object RESOURCE_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";

    /**
     * the bing api key :)
     */
    public static final String BING_API_KEY = "08A2FCFA4D5D60D053ACE5422EE9495E1E5795DD";

    /**
     * number of search results return be the search engine
     */
    public static final int NUMBER_OF_SEARCH_RESULTS = 20;

    /**
     * crawl thread gets terminated after this time
     */
    public static final int WEB_SEARCH_TIMEOUT_MILLISECONDS = 10000;

    /**
     * One thread for every BOA pattern
     */
    public static final int NUMBER_OF_SEARCH_THREADS = NUMBER_OF_BOA_PATTERNS;

    /**
     * write the training examples in this file
     */
    public static String TRAINING_DATA_FILENAME = "defacto_evidence.arff";
    
    /**
     * write the training examples in this file
     */
    public static final String FACT_TRAINING_DATA_FILENAME = "defacto_fact.arff";

    /**
     * learning method
     */
    public static final String LINEAR_REGRESSION_CLASSIFIER = "LINEAR_REGRESSION_CLASSIFIER";
    
    /**
     * 
     */
    public static final String MULTILAYER_PERCEPTRON = "MULTILAYER_PERCEPTRON";
    
    /**
     * 
     */
    public static final String SMO = "SMO";
    
    /**
     * Set this value to an available classifier like Constants.LINEAR_REGRESSION_CLASSIFIER
     */
    public static final String CLASSIFIER_FUNCTION = SMO;

    /**
     * only patterns which have a pattern score higher than this will be used to search for fact in text
     */
    public static final String PATTERN_SCORE_THRESHOLD = "0.5";

    /**
     * between the extracted entity and the pattern may be this distance in tokens
     */
    public static final String CONTEXT_LOOK_AHEAD_THRESHOLD = "3";

    /**
     * active this flag to rewrite the training data
     */
    public static final boolean WRITE_TRAINING_FILE = true;

    /**
     * onyl this much results will be returned from wikipedia query: "barack obama michelle obama" returns 860 results
     */
    public static final Integer MAX_WIKIPEDIA_RESULTS = 10;

    /**
     * use only the n most frequent topic terms from wikipedia pages
     */
    public static final int TOPIC_TERMS_MAX_SIZE = 10;

    /**
     * one topic term needs to be in more than this pags
     */
    public static final Integer TOPIC_TERM_PAGE_THRESHOLD = 1;

    /**
     * all pages with a pagerank below this we be discarded
     */
    public static final int MINIMUM_WEBSITE_PAGERANK = 0;

    /**
     * set this flag if you want to override the current trained model
     */
    public static final boolean OVERRIDE_CURRENT_MODEL = true;

    /**
     * see the paper Nakamura et. al. 2007 for details 
     */
    public static final Double WEBSITE_SIMILARITY_THRESHOLD = 0.8;

    /**
     * the amount of time which lies between a google page rank request
     */
    public static final long GOOGLE_WAIT_TIME = 2000;
    
    /**
     * 
     */
    public static final int UNASSIGNED_PAGE_RANK = 11;

    /**
     * 
     */
    public static final int NUMBER_OF_TOKENS_BETWEEN_ENTITIES = 20;

    /**
     * 
     */
    public static final boolean WRITE_FACT_TRAINING_FILE = false;

    /**
     * 
     */
    public static final double CONFIRMATION_THRESHOLD = 0.5;
}
