# -*- coding: utf-8 -*-
"""
@author: JohannesLoevenich

We need to expand this later. Just step by step.
ToDo: Extraction of all features by extract_all_features() method
"""
import NameFeature as nf
import EndOfSentenceFeature as eof

class ComplexProof:
	score = 0
	smallContext = ''
	taggedSmallContext = ''
	mediumContext = ''
	taggedMediumContext = ''
	largeContext = ''
	taggedLargeContext = ''
	#body of the html webpage
	tinyContext = ''
	taggedTinyContext = ''
	def __init__(self,model,firstLabel,secondLabel,occurrence,normalizedOccurence,site):
		self.model = model
		self.firstLabel = firstLabel
		self.secondLabel = secondLabel
		self.proofPhrase = occurrence;
		self.normalizedProofPhrase = normalizedOccurence
		self.website = site
		#Extract EndOfSentenceFeature
	def extract_end_of_sentence_feature(self):
		return eof.EndOfSentenceFeature(self.tinyContext)
		#Extract name feature
	def extract_name_feature(self):
		feature = nf.NameFeature()
		feature.set_values(self.firstLabel,self.proofPhrase,self.secondLabel,self.tinyContext,self.model,self.get_language)
		return feature
	def get_language(self):
		#just en,de
		return self.website.get_language()
	def get_model(self):
		return self.model
	def get_subject(self):
		return self.firstLabel
	def get_object(self):
		return self.secondLabel
	def get_proofPhrase(self):
		return self.proofPhrase
	def get_normalizedProofPhrase(self):
		return self.normalizedProofPhrase
	def get_website(self): 
		return self.website
	 # set methods
	def set_score(self,score):
		self.score = score
	def set_smallContext(self,smallContext):
		self.smallContext = smallContext
	def set_taggedSmallContext(self,taggedSmallContext):
		self.taggedSmallContext = taggedSmallContext
	def set_mediumContext(self,mediumContext):
		self.mediumContext = mediumContext
	def set_taggedMediumContext(self,taggedMediumContext):
		self.taggedMediumContext = taggedMediumContext
	def set_tinyContext(self,tinyContext):
		self.tinyContext = tinyContext
	def set_taggedTinyContext(self,taggedTinyContext):
		self.taggedtinyContext = taggedtinyContext
	 # get methods
	def get_score(self,score):
		return self.score
	def get_smallContext(self,smallContext):
		return self.smallContext 
	def get_taggedSmallContext(self,taggedSmallContext):
		return self.taggedSmallContext
	def get_mediumContext(self,mediumContext):
		return self.mediumContext
	def get_taggedMediumContext(self,taggedMediumContext):
		return self.taggedMediumContext
	def get_tinyContext(self,tinyContext):
		return self.tinyContext
	def get_taggedTinyContext(self,taggedTinyContext):
		return self.taggedtinyContext
