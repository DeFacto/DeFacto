import whois
import csv
import time
from datetime import date

from sklearn.externals import joblib


def get_whois_features(index, domain):
    data = []
    data.append(index)
    data.append(domain)
    _OK=1
    _NONE = -1
    _ERR = -2
    try:
        try:
            details = whois.whois(domain)
            if isinstance(details.expiration_date, list) == True: dt=details.expiration_date[0]
            else: dt=details.expiration_date
            if isinstance(details.creation_date, list) == True: dtc=details.creation_date[0]
            else: dtc=details.creation_date
            if dt is None: data.append(_NONE)
            else: data.append((dt.date()-date.today()).days)
            if dtc is None: data.append(_NONE)
            else: data.append((date.today()-dtc.date()).days)
            data.append(_NONE if details.name_servers is None else len(details.name_servers))
            data.append(_NONE if details.emails is None else len(details.emails))
            data.append(_NONE if details.name is None else _OK)
            data.append(_NONE if details.address is None else _OK)
            data.append(_NONE if details.city is None else _OK)
            data.append(_NONE if details.state is None else _OK)
            data.append(_NONE if details.zipcode is None else _OK)
            data.append(_NONE if details.country is None else _OK)
        except:
            raise
    except Exception as e:
        print(e)
        data.append([_ERR] * 10)

    return data


try:
    process = 'true'

    #true/test=25.808 domains
    #false/test=20.061 domains

    path='/Users/diegoesteves/Github/factchecking/WebTrustworthiness/python/data/datasets/data/factbench/2012/750_'+ \
         process + '_examples_factbench2012_testset_domains.txt'
    out='/Users/diegoesteves/Github/factchecking/WebTrustworthiness/python/data/datasets/data/factbench/2012/'
    i=0
    #print_variables('globo.com')
    #print_variables('google.com')
    #print_variables('stackoverflow.com')

    features=[]
    with open(path, 'r') as f:
        for row in csv.reader(f, delimiter='"', skipinitialspace=True):
            if len(row)>=1:
                i+=1
                print(i, row[0])
                features.append(get_whois_features(i, row[0]))
            time.sleep(0.5)
    #print(features)
except Exception as e:
    print(e)
finally:
    name = 'factbench_2012_' + process + '_label_' + time.strftime("%Y%m%d%H%M%S") + '_' + str(i) + '_website_features.pkl'
    joblib.dump(features, out + name)
