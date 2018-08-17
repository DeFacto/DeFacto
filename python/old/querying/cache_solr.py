from search_bing import query_bing
import json
import urllib
import pysolr
from bs4 import BeautifulSoup
import os
import httplib
import urlparse

from util.definitions import CONST_SEPARATOR
from util.sqlite_helper import TripeScorerDB, SQLiteHelper


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

def cache_search_engine(query, triple, codification='SPO', return_values = True):
    try:
        temptriple = triple.split(CONST_SEPARATOR)
        subject = temptriple[0]
        predicate = temptriple[1]
        object = temptriple[2]
        language = 'en'
        total_hit = 0

        solr = pysolr.Solr('http://52.173.249.140:8983/solr/webpages/')
        params = ['query:"' + query + '"', 'triple:"' + triple + '"']
        r = solr.search(params)

        if len(r) < 1:
            print "Searching the web ..."
            dir = os.path.dirname(__file__)
            filepath = os.path.join(dir, "config.txt")
            configs = open(filepath, "r+")
            key = ""
            # load Bing API key from config.txt
            configs = configs.readlines()
            for config in configs:
                config = config.split(" ")
                if config[0] == 'bing_api_key':
                    key = config[1]
                    break
            query, txts, imgs, response = query_bing(query, key, version='5.0')
            #saving original json data to disk
            with SQLiteHelper() as sqlcon:
                ts = TripeScorerDB(sqlcon)
                try:
                    total_hit = response.json().get('webPages', {}).get('totalEstimatedMatches', {})
                except Exception as e:
                    print 'no results'
                idmetaquery = ts.save_metaquery(subject, predicate, object, language, query,
                                                codification,
                                                total_hit)
                with open("../data/bing_data/metaquery_" + str(idmetaquery) + ".json", 'w') as outfile:
                    json.dump(response, outfile)
            rank = 1
            for result in txts:
                ini = result['url'].index('&r=') + 3
                end = result['url'].index('&p=')
                result['url'] = result['url'][ini:end]
                result['url'] = urllib.unquote(result['url'])
                # removing useless about and displayUrl attributes
                if 'displayUrl' in result.keys():
                    result.pop('displayUrl')
                if 'about' in result.keys():
                    result.pop('about')
                if 'id' in result.keys():
                    result.pop('id')
                # add rank attribute
                result['rank'] = rank
                # adding deeplinks to list of results
                if 'deepLinks' in result.keys():
                    txts = txts + result['deepLinks']
                    result.pop('deepLinks')

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
                temp_json = open(str(rank) + ".json", "w+")
                temp_json.write(json.dumps(result, indent=4, sort_keys=True))
                #cache it
                cmd = 'curl -H "Content-Type:application/json" "http://52.173.249.140:8983/solr/webpages/update/json/docs?commit=true" --data-binary @' + str(
                    rank) + '.json'
                temp_file = open("curl", "w+")
                temp_file.write("#!/bin/sh\n" + cmd)
                os.system("sh curl")
                rank = rank + 1
                if return_values is True:
                    r = solr.search(params)
        else:
            print "Cool, this triple / query (%s %s %s / %s) has been cached : " % \
                  (subject, predicate, object, query)
            if return_values is True:
                with SQLiteHelper() as sqlcon:
                    t = TripeScorerDB(sqlcon)
                    m = t.get_triple_by_spolang(subject, predicate, object, language)
                    if m is not None:
                        total_hit = m.total_hit_count
                    else:
                        total_hit = -1

        return r, total_hit
    except Exception as e:
        raise e