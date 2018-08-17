# -*- coding: utf-8 -*-
"""
@author: JohannesLoevenich
"""
from skbio.core.alignment import StripedSmithWaterman
import ComplexProof as cp

# Pagetitle Feature works on proof level
class PageTitleFeature: 
    SubjectSmithWatermanDistance = 0
    ObjectSmithWatermanDistance = 0
    proof = cp.ComplexProof()
    def __init__(self,proof):
        self.proof = proof
        self.model = proof.get_model()
    def compute_subject_smith_waterman(self):
        similarityScore = 0
        pagetitle = self.proof.get_website().get_title()
        query = StripedSmithWaterman(pagetitle)
        targetsequences = self.model.get_subject_labels()
        for targetsequence in targetsequences:
            alignement = query(targetsequence)
            if alignement.score() > similarityScore:
                similarityScore = alignement.score()
                highestAlignment = alignement
        self.SubjectSmithWatermanDistance = highestAlignment
        self.alignment = alignement
    def compute_object_smith_waterman(self):
        similarityScore = 0
        pagetitle = self.proof.get_website().get_title()
        query = StripedSmithWaterman(pagetitle)
        targetsequences = self.model.get_object_labels()
        for targetsequence in targetsequences:
            alignement = query(targetsequence)
            if alignement.score() > similarityScore:
                similarityScore = alignement.score()
                highestAlignment = alignement
        self.ObjectSmithWatermanDistance = highestAlignment
        self.alignment = alignement
            
            
        