import os
import sys
import urllib

import urlparse
from bs4 import BeautifulSoup

import util.querying.cache_db
from util.definitions import CONST_SEPARATOR


class DataExtractor:
    def __init__(self, subject, predicate, object):
        reload(sys)
        sys.setdefaultencoding('utf8')
        self.s = subject
        self.p = predicate
        self.o = object
        # derived
        self.triple = self.s + CONST_SEPARATOR + self.p + CONST_SEPARATOR + self.o
        # computed
        self.s_initial = 0
        self.s_final_o = 0
        self.s_final_po = 0
        self.so = 0
        self.spo = 0
        self.w_s_initial_30 = []
        self.w_s_final = []

    def query_s(self, top=30):
        result, total = util.querying.cache_db.cache_search_engine(self.s, self.triple, "S")
        self.s_initial = total
        # getting all the links in the results to find s_final
        if len(result) > 0:
            for r in result[0:top - 1]:
                self.w_s_initial_30.append(r.url)
                soup = BeautifulSoup(r.body, 'html.parser')
                links = soup.find_all("a")
                for link in links:
                    url = link.get('href')
                    # to get absolute URLs from relative URLs
                    absurl = urlparse.urljoin(r.url, url)
                    url1 = urlparse.urlparse(absurl)
                    url2 = urlparse.urlparse(r.url[0])
                    # we just need to get internal links
                    if url1.netloc == url2.netloc:
                        self.w_s_final.append(absurl)

            self.w_s_final = set(self.w_s_final)
            for link in self.w_s_final:
                html = 0
                try:
                    html = urllib.urlopen(link).read()
                except Exception as e:
                    print('Couldnt get webpage')
                    continue
                text = self.text_from_html(html)
                if text.find(self.o) != -1:
                    self.s_final_o = self.s_final_o + 1
                    en_verbalisations = open(
                        os.path.join(os.path.dirname(__file__), "verbalisations/" + self.p + ".english"))
                    # ger_verbalisations =  open(os.path.join(os.path.dirname(__file__),"verbalisations/" + self.p + ".german"))
                    verbalisations = en_verbalisations.readlines()  # + ger_verbalisations.readlines()
                    for v in verbalisations:
                        v = v.replace("%S", "")
                        v = v.replace("%O", "")
                        v = v.strip()[1:][:-1]
                        if text.find(v) != -1:
                            self.s_final_po = self.s_final_po + 1
                            break

    def query_so(self):
        query = '"' + self.s + '" + "' + self.o + '"'
        # caching and retrieving
        result, total = util.querying.cache_db.cache_search_engine(query, self.triple, "SO")
        self.so = total

    def query_spo(self):
        en_verbalisations = open(os.path.join(os.path.dirname(__file__), "verbalisations/" + self.p + ".english"))
        # ger_verbalisations = open(os.path.join(os.path.dirname(__file__), "verbalisations/" + self.p + ".german"))
        # verbalisations = en_verbalisations.readlines() + ger_verbalisations.readlines()
        verbalisations = en_verbalisations.readlines()
        total_all = 0
        for v in verbalisations:
            v = v.replace("%S", '"' + self.s + '" +')
            v = v.replace("%O", '+ "' + self.o + '"')
            v = v.strip()
            result, total = util.querying.cache_db.cache_search_engine(v, self.triple, "SPO")
            total_all += total
        self.spo = total_all