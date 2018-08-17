from bingapi import bing_api5
import json
import urllib
import pysolr
from bs4 import BeautifulSoup
import os
import httplib
import urlparse

from util.html_extractors import ExtractorBS
from util.querying import query_bing

metaquery, result_txts, result_imgs, jsonobject = query_bing('rio de janeiro',
                                                '<azure key>',
                                                market='en-US', version='5.0')


bs = ExtractorBS()

title , text, body = bs.get_title_and_text('http://www.globo.com')

print('title: ' + title.strip())
print('text: ' + text.strip())


def get_actual_url(url):
    try:
        parsed = urlparse.urlparse(url)
        h = httplib.HTTPConnection(parsed.netloc)
        h.request('HEAD', parsed.path)
        response = h.getresponse()
        if response.status/100 == 3 and response.getheader('Location'):
            return response.getheader('Location')
        else:
            return url
    except Exception as e:
        return url

def query(query, triple, idnum, top = 0):
    solr = pysolr.Solr('http://52.173.249.140:8983/solr/webpages/')
    dir = os.path.dirname(__file__)
    filepath = os.path.join(dir,"config.txt")
    configs = open(filepath, "r+")
    key = ""
    #load Bing API key from config.txt
    configs = configs.readlines()
    for config in configs:
        config = config.split(" ")
        if config[0] == 'bing_api_key':
            key = config[1]
            break
    query, txts, imgs, response = bing_api5(query, key, top=top)
    # results = {}
    # results['query'] = query
    # results['triple'] = triple
    # os.system("curl -H 'Content-Type: application/json' 'http://52.173.249.140:8983/solr/results/update/json/docs?commit=true' --data-binary '" + json.dumps(results) + "'")
    rank = 1

    for result in txts:
        ini = result['url'].index('&r=') + 3
        end = result['url'].index('&p=')
        result['url'] = result['url'][ini:end]
        result['url'] = urllib.unquote(result['url'])
        #removing useless about and displayUrl attributes
        if 'displayUrl' in result.keys():
            result.pop('displayUrl')
        if 'about' in result.keys():
            result.pop('about')
        if 'id' in result.keys():
            result.pop('id')
        #add rank attribute
        result['rank'] = rank
        #adding deeplinks to list of results
        if 'deepLinks' in result.keys():
            txts = txts + result['deepLinks']
            result.pop('deepLinks')
        #downloading the webpage and getting cleaned content using BeautifulSoup
        r = solr.search('url:"' + result['url'] + '"')
        if len(r)>0:
            print "Already explored this url : " + result['url']
            rank = rank + 1
            continue
        html = 0
        try:
            html = urllib.urlopen(result['url'])
        except Exception as e:
            print('Couldnt get webpage')
            rank = rank + 1
            continue
        soup = BeautifulSoup(html, 'html.parser')
        result['body'] = soup.prettify()
        result['content'] = soup.get_text()
        result['query'] = query
        result['triple'] = triple
        temp_json = open(str(rank) + ".json","w+")
        temp_json.write(json.dumps(result, indent=4, sort_keys=True))
        cmd = 'curl -H "Content-Type:application/json" "http://52.173.249.140:8983/solr/webpages/update/json/docs?commit=true" --data-binary @' + str(rank) + '.json'
        temp_file = open("curl","w+")
        temp_file.write("#!/bin/sh\n" + cmd)
        os.system("sh curl")
        rank = rank + 1

    temp_json = open(str(idnum) + ".json", "w+")
    temp_json.write(json.dumps(response, indent=4, sort_keys=True))