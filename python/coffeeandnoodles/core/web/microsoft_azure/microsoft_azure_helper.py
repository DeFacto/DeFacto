# -*- coding: utf8 -*-
import re
import requests
from xml.etree import ElementTree
import logging
import urllib
import requests
import json
#sess = requests.Session()
#adapter = requests.adapters.HTTPAdapter(max_retries = 20)
#sess.mount('http://', adapter)
import time

from config import DeFactoConfig
from defacto.definitions import BING_LANG_DISABLED


class MicrosoftAzurePlatform(object):
    def __init__(self, key):
        try:
            self.key = key
            self.host = 'api.microsofttranslator.com'
            self.path_translate = '/V2/Http.svc/Translate'
            self.detect_url = "https://api.microsofttranslator.com/V2/Http.svc/Detect"
            self.translate_url = "https://api.microsofttranslator.com/v2/Http.svc/Translate"
        except Exception as error:
            raise error

    def clean_text(self, text):
        if isinstance(text, str) == False:
            text = text.encode('ascii', 'ignore')
        return re.sub('\W+', ' ', text)

    def bing_translate_text(self, text, to):
        try:
            new = self.clean_text(text)
            params = 'to=' + to + '&text=' + new
            headers = {'Ocp-Apim-Subscription-Key': self.key}
            response = requests.get(self.translate_url, params=params, headers=headers)
            if response.status_code != 200:
                raise Exception(':: bing translation: ' + str(response.status_code) + ' - ' + str(response.text))
            translation = ElementTree.fromstring(response.text.encode('utf-8'))
            return translation.text
        except:
            raise

    def bing_detect_language(self, text):
        try:
            if BING_LANG_DISABLED == 1:
                return 'en'
            if len(text) > 500:
                text = text[0:500]
            text = self.clean_text(text)
            params = {'text': text}
            headers = {'Ocp-Apim-Subscription-Key': self.key}
            response = requests.get(self.detect_url, params=params, headers=headers)
            if response.status_code != 200:
                raise Exception(':: bing lang detection: ' + str(response.status_code) + ' - ' + str(response.text))
            translation = ElementTree.fromstring(response.text.encode('utf-8'))
            return translation.text
        except:
            raise

    def query_bing(self, query, key, top, market='en-us', safe='Moderate', version='v5'):
        if version == 'v5':
            return self.__bing_api5(query, key, top, market, safe)
        else:
            raise Exception('bing api version not implemented')

    def __bing_api5(self, query, key, top, market, safe):
        # https://msdn.microsoft.com/en-us/library/dn760794(v=bsynd.50).aspx
        try:
            txts = []
            imgs = []
            url = 'https://api.cognitive.microsoft.com/bing/v5.0/search'
            # query string parameters
            if top != 0:
                payload = {'q': query, 'mkt': market, 'count': top, 'offset': 0, 'safesearch': safe}
            else:
                payload = {'q': query, 'mkt': market, 'offset': 0, 'safesearch': safe}
            # custom headers
            headers = {'Ocp-Apim-Subscription-Key': key}
            # make GET request
            r = requests.get(url, params=payload, headers=headers)
            # get JSON response
            try:
                if r.status_code != 200:
                    raise Exception('problem when querying Bing! Status code = ' + r.status_code)
                txts = r.json().get('webPages', {}).get('value', {})
                imgs = r.json().get('images', {}).get('value', {})

            except Exception as e:
                logging.error('error on retrieving search results: ', e)

            return query, txts, imgs
        except Exception as e:
            print('an error has occurred: ', e)
            return query, [], []


if __name__ == "__main__":
    config = DeFactoConfig()
    azure = MicrosoftAzurePlatform(config.translation_secret)
    for i in range(10):
        print(azure.bing_detect_language('ola tomas tudo bem?') + ' - ' + str(i))
        print(azure.bing_translate_text('sim sim por aqui tudo bem e com voce? Hup &%5', 'en') + ' - ' + str(i))
        q, t, i = azure.query_bing('diego esteves', config.search_engine_key, config.search_engine_tot_resources)
        print(q, len(t), len(i))
        time.sleep(2)