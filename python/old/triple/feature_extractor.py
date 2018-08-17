import querying.query
import pysolr
from bs4.element import Comment
import urllib
from bs4 import BeautifulSoup
from urlparse import urljoin
import os
import httplib
import urlparse
import sys

class FeatureExtractor:
    def __init__(self, subject, predicate, object):
        reload(sys)
        sys.setdefaultencoding('utf8')
        self.s = subject
        self.p = predicate
        self.o = object
        self.s_initial = 0
        self.s_final_o = 0
        self.s_final_po = 0
        self.so = 0
        self.spo = 0
        self.solr = pysolr.Solr('http://52.173.249.140:8983/solr/webpages/')
        #each triple is stored in the form: SUBJECT #~~~$ PREDICATE #~~~$ OBJECT
        self.triple = self.s + "#~~~$" + self.p + "#~~~$" + self.o

    def get_actual_url(self,url):
        try:
            parsed = urlparse.urlparse(url)
            h = httplib.HTTPConnection(parsed.netloc)
            h.request('HEAD', parsed.path)
            response = h.getresponse()
            if response.status / 100 == 3 and response.getheader('Location'):
                return response.getheader('Location')
            else:
                return url
        except Exception as e:
            return url

    def tag_visible(self,element):
        if element.parent.name in ['style', 'script', 'head', 'title', 'meta', '[document]']:
            return False
        if isinstance(element, Comment):
            return False
        return True

    def text_from_html(self,body):
        soup = BeautifulSoup(body, 'html.parser')
        texts = soup.findAll(text=True)
        visible_texts = filter(self.tag_visible, texts)
        return u" ".join(t.strip() for t in visible_texts)

    def query_s(self, idnum, top=5):
        query = self.s
        # triple = self.triple
        querying.query.query(query,query, idnum, top=top)
        result = self.solr.search('triple:"' + query + '"',rows=100)
        self.s_initial = len(result)
        # #getting all the links in the results to find s_final
        # all_links = []
        # for r in result:
        #     soup = BeautifulSoup(r['body'][0],'html.parser')
        #     links = soup.find_all("a")
        #     for link in links:
        #         url = link.get('href')
        #         #to get absolute URLs from relative URLs
        #         absurl = urljoin(r['url'][0],url)
        #         all_links.append(absurl)
        #
        # for link in all_links:
        #     html = 0
        #     try:
        #         html = urllib.urlopen(link).read()
        #     except Exception as e:
        #         print('Couldnt get webpage')
        #         continue
        #     text = self.text_from_html(html)
        #     if text.find(self.o) != -1:
        #         self.s_final_o = self.s_final_o + 1
        #         en_verbalisations = open(os.path.join(os.path.dirname(__file__),"verbalisations/" + self.p + ".english"))
        #         ger_verbalisations =  open(os.path.join(os.path.dirname(__file__),"verbalisations/" + self.p + ".german"))
        #         verbalisations = en_verbalisations.readlines() + ger_verbalisations.readlines()
        #         for v in verbalisations:
        #             v = v.replace("%S", "")
        #             v = v.replace("%O", "")
        #             v = v.strip()
        #             if text.find(v) != -1:
        #                 self.s_final_po = self.s_final_po + 1
        #                 break

    def query_so(self):
        # query = self.s + " AND " + self.o
        triple = self.s
        # querying.query.query(query,triple,top=5)
        result = self.solr.search('content:' + self.o + ' AND triple:"' + triple + '"',rows=100)
        self.so = len(result)

    # def query_spo(self):
    #     triple = "SPO: " + self.triple
    #     en_verbalisations = open(os.path.join(os.path.dirname(__file__), "verbalisations/" + self.p + ".english"))
    #     ger_verbalisations = open(os.path.join(os.path.dirname(__file__), "verbalisations/" + self.p + ".german"))
    #     verbalisations = en_verbalisations.readlines() + ger_verbalisations.readlines()
    #     for v in verbalisations:
    #         v=v.replace("%S",self.s)
    #         v=v.replace("%O",self.o)
    #         v=v.replace(" ", " AND ")
    #         v.strip()
    #         querying.query.query(v,triple)
    #     result = self.solr.search('triple:"' + triple + '"',rows=100)
    #     self.spo = len(result)

    def get_features(self):
        return (self.s_initial, self.so)