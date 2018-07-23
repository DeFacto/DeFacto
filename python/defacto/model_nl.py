from coffeeandnoodles.SolrUtils import SolrUtils
from defacto.core_util import get_topic_terms
from defacto.rel_extract import TripleExtraction_ClausIE
from defacto.wikipedia import WikiPediaUtils


class ModelNL(object):

    def __init__(self, claim, language='en'):
        try:
            self.claim = claim
            self.language = language
            self.label = None
            self.evidences = []
            self.triples = []
            self.__extract_triples()
            if len(self.triples) == 0:
                raise Exception('could not extract triples out of the claim!')
        except Exception as error:
            raise error


    def __extract_triples(self):
        try:
            print('extracting triples...')
            re1 = TripleExtraction_ClausIE()
            triples = re1.get_triples(self.claim)
            for t in triples:
                self.triples.append([t.subject, t.predicate, t.object])
        except:
            raise


if __name__ == '__main__':

    try:
        claim = 'Roman Atwood is a content creator.'

        defactoNL = ModelNL(claim=claim, language='en')
        print(defactoNL.triples)

        wiki_utils = WikiPediaUtils(tt_top=20)
        server = SolrUtils()

        pages_subject = []
        pages_object = []

        for triple in defactoNL.triples:
            pages_subject.append((triple.subject, wiki_utils.get_page_object(triple.subject)))
            pages_object.append((triple.object, wiki_utils.get_page_object(triple.object)))

        for (label, page) in pages_subject:
            if page.exists():
                tt, freq = get_topic_terms(page.text)
                server.add_document(label, tt, freq)


        server.commit()


    except Exception as e:
        print(e)