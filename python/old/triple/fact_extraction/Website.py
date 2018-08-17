# -*- coding: utf-8 -*-
"""
@author: JohannesLoevenich
"""
import nltk
from nltk.tokenize import word_tokenize as wt

class Website: 
    pagerank= 0
    score = 0
    ComplexProofs = []
    def __init__(self,url,text,title,language):
        self.url = url
        self.text = text 
        self.tilte = title
        self.language = language
        
    def set_values(self,url,text,title,pagerank,score,language):
        self.url = url
        self.text = text 
        self.tilte = title
        self.score = score
        self.pagerank = pagerank
        self.language = language
        
    def tokenized_content(self):
        return wt.word_tokenize(self.text)
    
    def tokenized_title(self):
        return wt.word_tokenize(self.title)
    
    def tagged_content(self):
        tag = wt.word_tokenize(self.text)
        return nltk.pos_tag(tag)
    
    def tagged_title(self):
        tag = wt.word_tokenize(self.title)
        return nltk.pos_tag(tag)
    
    def add_complex_proof(self,proof):
        self.ComplexProofs.append(proof)
    
    def set_complex_proofs(self,proofs):
        self.ComplexProofs = proofs
    
    def set_score(self,score):
        self.score = score
    
    def set_pagerank(self,pagerank):
        self.pagerank = pagerank
    
    def set_url(self,url):
        self.url = url
    
    def set_text(self,text):
        self.text = text
    
    def set_title(self,title): 
        self.title = title
    
    def set_language(self,language): 
        self.language = language
        
    def get_score(self,score):
        return self.score
    
    def get_complex_proofs(self):
        return self.complexProofs
    
    def get_pagerank(self,pagerank):
        return self.pagerank
    
    def get_url(self,url):
        return self.url
    
    def get_text(self,text):
        return self.text
    
    def get_title(self,title): 
        return self.title
    
    def get_language(self,language): 
        return self.language

