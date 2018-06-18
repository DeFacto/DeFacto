import requests
import hashlib
import urllib.parse
from src.coffeeandnoodles.core.config import CoffeeAndNoodlesConfig

config = CoffeeAndNoodlesConfig()


def get_md5_from_string(value):
    hash_md5 = hashlib.md5()
    hash_md5.update(value.encode('utf-8'))
    return hash_md5.hexdigest()

def get_md5_from_file(fname):
    hash_md5 = hashlib.md5()
    with open(fname, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)
    return hash_md5.hexdigest()

def get_open_pagerank(domain):
    try:
        urlstr=''
        i=0
        for x in domain:
            urlstr+='domains'+urllib.parse.quote_plus('['+str(i)+']') + '=' + \
                    urllib.parse.quote_plus(x) + '&'
            i+=1
        headers = {'API-OPR': config.open_page_rank_api}
        r = requests.get('https://openpagerank.com/api/v1.0/getPageRank?' + urlstr[:-1], headers=headers)
        return r.json()
    except Exception as e:
        config.logger.error(repr(e))
        return None

#print(get_open_pagerank(['google.com', 'apple.com', 'unknowndomain.com']))