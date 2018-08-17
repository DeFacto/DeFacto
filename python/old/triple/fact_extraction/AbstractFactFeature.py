# -*- coding: utf-8 -*-
"""
@author: JohannesLoevenich


"""

class AbstractFactFeature:
    # Properties for the name feature 
    subject = ''
    phrase = ''
    object = ''
    context = ''
    file_name = ''
    language = ''
    
    def set_name_values(self,subject, phrase, object,context,file_name, language):
        self.subject = subject
        self.phrase = phrase
        self.object = object
        self.context = context
        self.file_name = file_name
        self.language = language
    

    