import whois
import csv
import time
from datetime import date

from sklearn.externals import joblib

from trustworthiness.features_core import FeaturesCore

if __name__ == '__main__':
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
        #print_variables('stackoverflow.com

        fc = FeaturesCore()

        features=[]
        with open(path, 'r') as f:
            for row in csv.reader(f, delimiter='"', skipinitialspace=True):
                if len(row)>=1:
                    i+=1
                    print(i, row[0])
                    features.append([i, fc.get_whois_features(row[0])])
                time.sleep(0.5)
        #print(features)
    except Exception as e:
        print(e)
    finally:
        name = 'factbench_2012_' + process + '_label_' + time.strftime("%Y%m%d%H%M%S") + '_' + str(i) + '_website_features.pkl'
        joblib.dump(features, out + name)
