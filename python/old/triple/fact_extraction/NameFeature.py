# -*- coding: utf-8 -*-
"""
@author: JohannesLoevenich
"""
from AbstractFactFeature import AbstractFactFeature


class NameFeature(AbstractFactFeature): 
     def set_values(self,subject, phrase, object,context,file_name, language):
        self.subject = subject
        self.phrase = phrase
        self.object = object
        self.context = context
        self.file_name = file_name
        self.language = language
 
        
