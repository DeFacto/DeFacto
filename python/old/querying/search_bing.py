import logging
import requests

def bing_api5(query, key, top=50 ,market='en-us', safe='Moderate'):
    # https://msdn.microsoft.com/en-us/library/dn760794(v=bsynd.50).aspx
    try:
        txts = None
        imgs = None
        url = 'https://api.cognitive.microsoft.com/bing/v5.0/search'
        # query string parameters
        payload = {'q': query, 'mkt': market, 'count': top, 'offset': 0, 'safesearch': safe}
        # custom headers
        headers = {'Ocp-Apim-Subscription-Key': key}
        # make GET request
        r = requests.get(url, params=payload, headers=headers)
        # get JSON response
        try:
            if r.status_code != 200:
                raise Exception (':: problem when querying Bing! Status code = ' + str(r.status_code))
            txts = r.json().get('webPages', {}).get('value', {})
            imgs = r.json().get('images', {}).get('value', {})

        except Exception as e:
            logging.error(':: error on retrieving search results: ', e)

        return query, txts, imgs, r.json()
    except Exception as e:
        print (':: an error has occurred: ', e)
        return query, None

def query_bing(query, key, top=50, market='en-us', safe='Moderate', version='5.0'):
    try:
        if version == '5.0':
            return bing_api5(query, key, top, market, safe)
        else:
            raise ValueError('bing version %s not implemented' % version)
    except Exception as e:
        print (':: an error has occurred: ', e)

