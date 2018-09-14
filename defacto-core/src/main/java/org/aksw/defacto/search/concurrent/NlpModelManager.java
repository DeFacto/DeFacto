package org.aksw.defacto.search.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NlpModelManager {

	private BlockingQueue<StanfordNLPNamedEntityRecognition> models = new LinkedBlockingQueue<StanfordNLPNamedEntityRecognition>(
			Defacto.DEFACTO_CONFIG.getIntegerSetting("extract", "NUMBER_NLP_STANFORD_MODELS"));
	
	private static NlpModelManager INSTANCE;
	private static final Logger LOGGER = LoggerFactory.getLogger(NlpModelManager.class);
	
	/**
	 * 
	 */
	private NlpModelManager(){
		
		for ( int i = 0; i < Defacto.DEFACTO_CONFIG.getIntegerSetting("extract", "NUMBER_NLP_STANFORD_MODELS"); i++) {

			models.add(new StanfordNLPNamedEntityRecognition());
			LOGGER.info("Created " + (i + 1) + " StanfordNLP NER model!");
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static synchronized NlpModelManager getInstance(){
		
		if ( INSTANCE == null ) INSTANCE = new NlpModelManager();
		return INSTANCE;
	}
	
	public synchronized StanfordNLPNamedEntityRecognition getNlpModel(){
		
		try {
			
			LOGGER.info("Deploying NLP model! Models-Size: " + this.models.size());
			StanfordNLPNamedEntityRecognition ner = this.models.take();
			LOGGER.info("Finished Deploying NLP model! Models-Size: " + this.models.size());
			return ner;
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public synchronized void releaseModel(StanfordNLPNamedEntityRecognition ner) {
		
		try {
			
			LOGGER.info("Releasing NLP model! Models-Size: " + this.models.size());
			this.models.put(ner);
			LOGGER.info("Finished Releasing NLP model! Models-Size: " + this.models.size());
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
