import json
from urllib.parse import unquote

import pysolr
from bs4 import BeautifulSoup
import os


from util.definitions import CONST_SEPARATOR, SOLR_URL, JSON_FILES_CACHE_PATH
from util.metaquery import MetaQuery
from util.querying.search_bing import query_bing
from util.sqlite_helper import TripeScorerDB, SQLiteHelper
from util.webpage import WebPage

def get_websites_bing(query, idmetaquery, idtriple):
    print("Searching the web ...")
    websites = []
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
    try:
        with open(JSON_FILES_CACHE_PATH + "triple_" + str(idtriple) +
                          "_meta_" + str(idmetaquery) + ".json", 'w') as outfile:
            json.dump(response, outfile)
    except Exception as e:
        raise e

    try:
        total_hit = response['webPages']['totalEstimatedMatches']
        print('results = ' + str(total_hit))
    except Exception as e:
        total_hit = 0
        print('results = 0')

    rank = 1
    for result in txts:
        ini = result['url'].index('&r=') + 3
        end = result['url'].index('&p=')
        result['url'] = result['url'][ini:end]
        result['url'] = unquote(result['url'])

        #try:
        #    fileo = urllib2.urlopen(result['url'], timeout=10)
        #    html = fileo.read()
        #    fileo.close()
        #except Exception as e:
        #    print('Couldnt get webpage')
        #    rank = rank + 1
        #    continue

        #soup = BeautifulSoup(html, 'xml')
        result['body'] = ''# soup.prettify()
        result['content'] = ''#soup.get_text()

        w = WebPage(result['url'], result['name'], result['snippet'],
                    result['content'], result['body'], rank, idmetaquery)
        websites.append(w)
        rank = rank + 1

    return websites, total_hit

def cache_search_engine(query, triple, codification, return_values = True):
    try:
        temptriple = triple.split(CONST_SEPARATOR)
        subject = temptriple[0]
        predicate = temptriple[1]
        object = temptriple[2]
        language = 'en'

        with SQLiteHelper() as sqlcon:
            t = TripeScorerDB(sqlcon)

            triple = t.save_triple(subject, predicate, object, language)
            cached, metacached = t.metaquery_cached(triple.id, query, codification)
            if cached is False:
                meta = MetaQuery(triple.id, query, codification)

                metaid = t.save_metaquery(meta.id_triple, meta.querystr, meta.codification)
                websites, total_hit = get_websites_bing(meta.querystr, metaid, meta.id_triple)
                t.update_total_hits_count_query(metaid, len(websites), total_hit)

                for w in websites:
                    t.save_result_web(w.id_metaquery, w.url, w.title, w.snippet, w.html,
                                      w.body, w.search_rank)
                t.commit()
            else:
                total_hit = cached[0][1]
                metaid = cached[0][0]

            results = t.get_websites_by_metaquery_id(metaid)

        return results, total_hit

    except Exception as e:
        raise e
