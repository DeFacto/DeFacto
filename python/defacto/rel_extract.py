from pyclausie import ClausIE
from nltk import sent_tokenize
from defacto.definitions import CLAUSIE_PATH, STANFORD_CORE_MODEL_PATH, STANFORD_3_8_PATH
from nltk.parse.stanford import StanfordParser
from nltk.tree import ParentedTree, Tree
from nltk.stem import WordNetLemmatizer
from pycorenlp import *

def get_sentences_from_document(text):
    return sent_tokenize(text)


class TripleExtraction_StanfordOpenIE(object):

    def __init__(self):
        try:
            import os
            os.environ['CORENLP_HOME'] = STANFORD_3_8_PATH
            self.core_nlp_server = StanfordCoreNLP("http://localhost:9000/")
            self.properties={"annotators": "tokenize,ssplit,pos,depparse,lemma,natlog,openie",
                                                 "outputFormat": "json",
                                                 "openie.triple.strict": "true",
                                                 "openie.max_entailments_per_clause": "1",
                                                 "splitter.disable": "true"}

        except Exception as error:
            raise error

    def annotate(self, sentence):
        try:
            output = self.core_nlp_server.annotate(sentence, self.properties)
            result = [output["sentences"][0]["openie"] for item in output]
            out=[]
            for i in result:
                for rel in i:
                    relationSent = rel['subject'], rel['relation'], rel['object']
                    out.append(relationSent)
            return out
        except Exception as error:
            raise error



class TripleExtraction_Rusu(object):
    '''
    [1] Delia Rusu, Lorand Dali, Blaž Fortuna, Marko Grobelnik, Dunja Mladenić Triplet Extraction from Sentences
    http://ailab.ijs.si/dunja/SiKDD2007/Papers/Rusu_Trippels.pdf
    '''

    def __init__(self):
        try:
            import os
            os.environ['STANFORD_PARSER'] = STANFORD_CORE_MODEL_PATH
            os.environ['STANFORD_MODELS'] = STANFORD_CORE_MODEL_PATH

            self.parser = StanfordParser()


        except Exception as error:
            raise error

    def find_subject(self,t):
        for s in t.subtrees(lambda t: t.label() == 'NP'):
            for n in s.subtrees(lambda n: n.label().startswith('NN')):
                return (n[0], self.find_attrs(n))

    def find_predicate(self,t):
        v = None

        for s in t.subtrees(lambda t: t.label() == 'VP'):
            for n in s.subtrees(lambda n: n.label().startswith('VB')):
                v = n
            return (v[0], self.find_attrs(v))

    def find_object(self,t):
        for s in t.subtrees(lambda t: t.label() == 'VP'):
            for n in s.subtrees(lambda n: n.label() in ['NP', 'PP', 'ADJP']):
                if n.label() in ['NP', 'PP']:
                    for c in n.subtrees(lambda c: c.label().startswith('NN')):
                        return (c[0], self.find_attrs(c))
                else:
                    for c in n.subtrees(lambda c: c.label().startswith('JJ')):
                        return (c[0], self.find_attrs(c))

    def find_attrs(self, node):
        attrs = []
        p = node.parent()

        # Search siblings
        if node.label().startswith('JJ'):
            for s in p:
                if s.label() == 'RB':
                    attrs.append(s[0])

        elif node.label().startswith('NN'):
            for s in p:
                if s.label() in ['DT', 'PRP$', 'POS', 'JJ', 'CD', 'ADJP', 'QP', 'NP']:
                    attrs.append(' '.join(s.flatten()))

        elif node.label().startswith('VB'):
            for s in p:
                if s.label() == 'ADVP':
                    attrs.append(' '.join(s.flatten()))

        # Search uncles
        if node.label().startswith('JJ') or node.label().startswith('NN'):
            for s in p.parent():
                if s != p and s.label() == 'PP':
                    attrs.append(' '.join(s.flatten()))

        elif node.label().startswith('VB'):
            for s in p.parent():
                if s != p and s.label().startswith('VB'):
                    attrs.append(s[0])

        return attrs

    def get_triples(self, sentence):
        t = list(self.parser.raw_parse(sentence))[0]
        t = ParentedTree.convert(t)
        s = self.find_subject(t)
        p = self.find_predicate(t)
        o = self.find_object(t)
        return (s, p, o)

class TripleExtraction_ClausIE(object):

    def __init__(self):
        try:
            self.cl = ClausIE.get_instance(jar_filename=CLAUSIE_PATH)
        except Exception as error:
            raise error

    def get_triples(self, sentence):
        try:
            triples = self.cl.extract_triples([sentence])
            return triples
        except:
            raise

if __name__ == '__main__':

    try:
        sentence = 'John Cage is an American actor who lives in Florida'
        lemmatizer = WordNetLemmatizer()

        print('---------------------------------------------------------------------------------------------')
        re0 = TripleExtraction_StanfordOpenIE()
        triples = re0.annotate(sentence)
        for t in triples:
            print(t)
        print('---------------------------------------------------------------------------------------------')
        re1 = TripleExtraction_ClausIE()
        triples = re1.get_triples(sentence)
        for t in triples:
            print(t)
            #print(lemmatizer.lemmatize(t.predicate, pos='v'))
        print('---------------------------------------------------------------------------------------------')
        re2 = TripleExtraction_Rusu()
        triples = re2.get_triples(sentence)
        for t in triples:
            print(t)
        print('---------------------------------------------------------------------------------------------')

    except Exception as e:
        print(e)