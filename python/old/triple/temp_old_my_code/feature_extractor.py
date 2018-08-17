import re
import geograpy
import requests
from bs4.element import Comment
import urllib
from bs4 import BeautifulSoup
from urlparse import urlparse
import os
import httplib
import urlparse
import sys
from difflib import SequenceMatcher
from util import definitions
import datefinder
from dateutil.parser import parse
from microsofttranslator import Translator

class FeatureExtractor:
    def __init__(self, id, subject, predicate, object, data_subject, data_triple):
        reload(sys)
        sys.setdefaultencoding('utf8')
        self.id = id
        self.s = subject
        self.p = predicate
        self.o = object
        self.data_s = data_subject
        self.data_spo = data_triple
        if predicate == definitions.PREDICATE_ORG_FOUNDED_DATE:
            self.p_type_id = definitions.PREDICATE_ORG_FOUNDED_DATE_ID
        elif predicate == definitions.PREDICATE_ORG_PHONE_NUMBER:
            self.p_type_id = definitions.PREDICATE_ORG_PHONE_NUMBER_ID
        elif predicate == definitions.PREDICATE_ORG_DOMICILED_IN:
            self.p_type_id = definitions.PREDICATE_ORG_DOMICILED_IN_ID
        elif predicate == definitions.PREDICATE_ORG_WEBSITE_LINK:
            self.p_type_id = definitions.PREDICATE_ORG_WEBSITE_LINK_ID
        else:
            raise Exception('predicate not supported')
        self.top1 = []
        self.top2 = []
        self.top3 = []

        configs = open('/Users/esteves/Github/DeFacto3/querying/config.txt', "r+")
        key = ""
        # load Bing API key from config.txt
        configs = configs.readlines()
        for config in configs:
            config = config.split(" ")
            if config[0] == 'bing_api_key':
                key = config[1]
                break
        #translator = Translator('<Your Client ID>', '<Your Client Secret>')
        self.translator = Translator(key, key)

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

    def get_features(self):
        return (self.data_s.s_initial,
                self.data_spo.s_final_o,
                self.data_spo.s_final_po,
                self.data_spo.so,
                self.data_spo.spo)

    def object_similarity(self, top=3):
        # Assuming there is a variable holding all the urls for a given object
        urls = self.data_s.w_s_initial_30
        object_list = []
        phrase = ''
        # Get the html and then the text for the corresponding webpages
        for url in urls:
            try:
                if str(url).startswith('http://') or str(url).startswith('https://'):
                    r = requests.get(url)
                elif str(url).startswith('www'):
                    r = requests.get("http://" + url)
                else:
                    r = requests.get("http://www." + url)
                data = r.text
            except Exception as e:
                print('Couldnt get webpage')
                continue
            try:
                text = self.text_from_html(data)
                if self.translator.detect_language(text)!=u'en':
                    temp = self.translator.translate(text, 'en')
                    text = temp.text
            except Exception as e:
                raise e

            # Now catch the position of the predicate in the extracted website text
            en_verbalisations = open(os.path.join(os.path.dirname(__file__),"verbalisations/" + self.p + ".english"))
            #ger_verbalisations =  open(os.path.join(os.path.dirname(__file__),"verbalisations/" + self.p + ".german"))
            #verbalisations = en_verbalisations.readlines() + ger_verbalisations.readlines()
            verbalisations = en_verbalisations.readlines()
            words = [w.lower() for w in text.split(' ')]
            words_serial = " ".join(words)
            for v in verbalisations:
                v = v.replace("%S", "")
                v = v.replace("%O", "")
                v = v.strip()[1:][:-1]
                # Build a phrase in an area of 10 words before the verb and 10 words after the word
                if v in words_serial:
                    index = words_serial.index(v)
                    min=0
                    max=len(words_serial)
                    if (index-20) >= 0:
                        min=index-20
                    if (index+20) <= len(words_serial):
                        max=index+20
                    phrase = words_serial[min:max]
                    if self.p_type_id == definitions.PREDICATE_ORG_DOMICILED_IN_ID:
                        objects = self.extract_valid_countries(phrase)
                    if self.p_type_id == definitions.PREDICATE_ORG_FOUNDED_DATE_ID:
                        objects = self.extract_valid_dates(phrase)
                    if self.p_type_id == definitions.PREDICATE_ORG_PHONE_NUMBER_ID:
                        objects = self.extract_valid_phone_numbers(phrase)
                    # Check if object is contained in the object list. If not, add it. Otherwise increase the counter
                    for obj in objects:
                        if self.object_contained(obj, object_list):
                            continue
                        else:
                            object_list.append([obj, 1])
                    phrase = ''
        # Select the top3 features for the object
        topFeatures = self.get_top_features(object_list)
        self.top1 = topFeatures[0]
        self.top2 = topFeatures[1]
        self.top3 = topFeatures[2]

    def get_top_features(self,object_list):
        max = 0
        featCount = 0
        matches_list = []
        for i in range(0,3):
            for obj in object_list:
                if obj[1] > max:
                    max = obj[1]
            for obj in object_list:
                if obj[1] == max:
                    if featCount < 3:
                        matches_list.append(obj)
                        object_list.remove(obj)
                        featCount = featCount +1
            max = 0
        return matches_list

    def object_contained(self,object,object_list):
    #Check if object contained in object list
        for obj in object_list:
            for el in obj:
                if el is object:
                    obj[1] = obj[1] + 1
                    return True
        return False

    # Extract the valid countries in a phrase
    def extract_valid_countries(self,phrase):
        matches_list = []
        url = 'http://www.bbc.com/news/world-europe-26919928'
        places = geograpy.get_place_context(url=url)
        countries = places.countries
        words = phrase.split(' ')
        for word in words:
            if word in countries:
                matches_list.append(word)
        return matches_list

    # Extract the valid phone numbers in a phrase
    def extract_valid_phone_number(self,phrase):
        pattern = re.compile('(\d{3}[-\.\s]??\d{3}[-\.\s]??\d{4}|\(\d{3}\)\s*\d{3}[-\.\s]??\d{4}|\d{3}[-\.\s]??\d{4})')
        # finds all the matches of the regular expression and
        # returns a list containing them
        matches_list = pattern.findall(phrase)
        return matches_list

    # Extract the valid dates in phrase
    def extract_valid_dates(self,phrase):
        matches = datefinder.find_dates(phrase)
        ret=[]
        for m in matches:
            y = m.year
            #y = parse(str(m), fuzzy=True).year
            ret.append(y)
        return ret
        #date_reg_exp = re.compile('\d{4}[-/]\d{2}[-/]\d{2}')
        # finds all the matches of the regular expression and
        # returns a list containing them
        #matches_list=date_reg_exp.findall(phrase)


    def similar(self, a, b):
        return SequenceMatcher(None, a, b).ratio()