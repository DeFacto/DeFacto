# -*- coding: utf-8 -*-
"""
@author: JohannesLoevenich
"""
import ComplexProof as cp
import editdistance as dist
class SubjectObjectSimilarityFeature:
    SubjectSimilarity = 0
    ObjectSimilarity = 0
    proof = cp.ComplexProof()
    def __init__(self,proof):
        self.proof = proof
        self.model = proof.get_model()
    def subject_levenstein_similarity(self):
        lang = self.proof.get_language()
        modelSub = self.model.get_subject_label(lang)
        modelObj = self.model.get_object_label(lang)
        proofSub = self.proof.get_subject()
        if dist.levenstein(modelSub,proofSub) > dist.levenstein(modelObj,proofSub):
            self.subject_levenstein_similarity = dist.levenstein(modelSub,proofSub)
        else:
            self.subject_levenstein_similarity = dist.levenstein(modelObj,proofSub)
        
    def object_levenstein_similarity(self,lang):
        lang = self.proof.get_language()
        modelSub = self.model.get_subject_label(lang)
        modelObj = self.model.get_object_label(lang)
        proofObj = self.proof.get_object()
        if dist.levenstein(modelSub,proofObj) > dist.levenstein(modelObj,proofObj):
            self.object_levenstein_similarity = dist.levenstein(modelSub,proofObj)
        else:
            self.object_levenstein_similarity = dist.levenstein(modelObj,proofObj)