package org.aksw.defacto.search.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.apache.log4j.Logger;

public class NlpModelManager {

	private BlockingQueue<StanfordNLPNamedEntityRecognition> models = new LinkedBlockingQueue<StanfordNLPNamedEntityRecognition>(
			Defacto.DEFACTO_CONFIG.getIntegerSetting("extract", "NUMBER_NLP_STANFORD_MODELS"));
	
	private static NlpModelManager INSTANCE;
	private Logger logger = Logger.getLogger(NlpModelManager.class);
	
	/**
	 * 
	 */
	private NlpModelManager(){
		
		for ( int i = 0; i < Defacto.DEFACTO_CONFIG.getIntegerSetting("extract", "NUMBER_NLP_STANFORD_MODELS"); i++) {

			models.add(new StanfordNLPNamedEntityRecognition());
			logger.info("Created " + (i + 1) + " StanfordNLP NER model!");
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
			
			this.logger.info("Deploying NLP model! Models-Size: " + this.models.size());
			StanfordNLPNamedEntityRecognition ner = this.models.take();
			this.logger.info("Finished Deploying NLP model! Models-Size: " + this.models.size());
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
			
			this.logger.info("Releasing NLP model! Models-Size: " + this.models.size());
			this.models.put(ner);
			this.logger.info("Finished Releasing NLP model! Models-Size: " + this.models.size());
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
