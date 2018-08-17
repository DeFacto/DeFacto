# -*- coding: utf-8 -*-
"""
@author: JohannesLoevenich
"""
class AbstractEvidenceFeature: 
    NumberOfProofs = 0
    NumberOfConfirmingProofs = 0
    TotalPositiveEvidenceScore = 0
    TotalNegativeEvidenceScore = 0
    
    def set_values(self,nProofs,nConfirmingProofs,positiveEvidenceScore,negativeEvidenceScore):
        self.NumberOfProofs = nProofs
        self.NumberIfConfirmingProofs = nConfirmingProofs
        self.TotalPositiveEvidenceScore = positiveEvidenceScore
        self.TotalNegativeEvidenceScore = negativeEvidenceScore
    def set_number_of_proofs(self,nProofs):
        self.NumberOfProofs = nProofs
    def set_number_of_confirming_proofs(self,nConfirmingProofs):
        self.NumberOfConfirmingProofs = nConfirmingProofs
    def set_positive_evidence_score(self,positiveEvidenceScore):
        self.TotalPositiveEvidenceScore = positiveEvidenceScore
    def set_negative_evidence_score(self,negativeEvidenceScore):
        self.TotalNegativeEvidenceScore = negativeEvidenceScore

    def get_number_of_proofs(self):
        return self.NumberOfProofs
    def get_number_of_confirming_proofs(self):
        return self.NumberOfConfirmingProofs
    def get_positive_evidence_score(self):
        return self.TotalPositiveEvidenceScore
    def get_negative_evidence_score(self):
        return self.TotalNegativeEvidenceScore
    