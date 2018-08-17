# -*- coding: utf-8 -*-
"""
@author: JohannesLoevenich
"""

from AbstractFactFeature import AbstractFactFeature


class EndOfSentenceFeature(AbstractFactFeature): 
    end_of_sentence_dot = 0
    end_of_sentence_exclamation_mark = 0
    end_of_sentence_question_mark = 0
    
    def set_end_of_sentence_values(self,dot,exclamation,question):
        self.end_of_sentence_dot = dot
        self.end_of_sentence_exclamation_mark = exclamation
        self.end_of_sentence_question_mark = question
    
    def set_by_text(self,text):
        self.end_of_sentence_dot = text.count('.')
        self.end_of_sentence_exclamation_mark = text.count('!')
        self.end_of_sentence_question_mark = text.count('?')



