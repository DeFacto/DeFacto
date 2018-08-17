import logging
import requests

def translate_api(text, key, target_language, source_language='', content_type = "text/plain", category = 'general'):
    try:
        url = 'https://api.microsofttranslator.com/V2/Http.svc/Translate'
        # query parameters
        payload = ""
        if source_language!="":
            payload = {'appid': "", 'text': text, 'from': source_language, 'to': target_language, 'contentType': content_type, 'category': category}
        else:
            payload = {'appid': "", 'text': text, 'to': target_language, 'contentType': content_type, 'category': category}
        # custom headers
        headers = {'Ocp-Apim-Subscription-Key': key}
        # make GET request
        print payload
        print headers
        r = requests.get(url, params=payload, headers=headers)
        # get JSON response
        try:
            if r.status_code != 200:
                raise Exception (':: problem when querying Microsoft Translate Status code = ' + str(r.status_code))
            else:
                print r
        except Exception as e:
            print e
        return r
    except Exception as e:
        print (':: an error has occurred: ', e)
        return None