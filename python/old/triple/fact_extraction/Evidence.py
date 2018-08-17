# -*- coding: utf-8 -*-
"""
@author: JohannesLoevenich
"""
class Evidence:
    total_hit_count = 0
    defacto_score= 0
    def __init__(self,websites,complexProofs,boaPatterns):
        self.websites = websites
        self.complexProofs = complexProofs
        self.boaPatterns = boaPatterns
    def get_complex_proofs(self):
        return self.complexProofs
    def get_complex_proofs(self,website):
        return website.get_complex_proofs()
    def get_websites(self): 
        return self.websites
    def get_patterns(self): 
        return self.boaPatterns

            
        