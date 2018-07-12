
class ModelNL(object):

    def __init__(self, claim, language='en'):
        try:
            self.claim = claim
            self.language = language
            self.label = None
            self.evidences = []
            self.triples = []
        except Exception as error:
            raise error
