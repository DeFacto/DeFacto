from multiprocessing.dummy import Pool

import pandas as pd
import multiprocessing
import tldextract
import json

import time

from sklearn.externals import joblib

from coffeeandnoodles.core.util import get_open_pagerank
from config import DeFactoConfig
from defacto.definitions import DATASET_MICROSOFT_PATH, DATASET_3C_SITES_PATH, OUTPUT_FOLDER

config = DeFactoConfig()

class PreProcessing:
    def __init__(self, param):
        self.param = param

    def get_domains_from_dataset(self):

        try:
            config.logger.info('reading dataset: ' + self.param['DATASET'])
            df = pd.read_csv(self.param['PATH'], delimiter=self.param['DELIMITER'], header=0)
            config.logger.info('creating job args...')

            domains = []
            for index, row in df.iterrows():
                url = row[self.param['URL_COL_INDEX']]
                try:
                    o = tldextract.extract(url)
                    domains.append('%s.%s' % (o.domain, o.suffix))
                except Exception as e:
                    continue

            print('dataset: ', self.param['DATASET'])
            print('tot domains: ',  len(domains))
            print('tot set domains: ', len(set(domains)))

            return domains

        except Exception as e:
            raise e


def export_open_pagerank_data(parameters):

    try:

        API_QUERY_LIMIT = 100  # the API returns up to 100 websites, and there is a query limit.

        job_args = []
        domains = []
        for p in parameters:
            proc = PreProcessing(p)
            d = proc.get_domains_from_dataset()
            domains.extend(d)

        domains = list(set(domains))
        print('----')
        print('tot set final domains: ', len(domains))

        while len(domains) > 0:
            _min = min(len(domains), API_QUERY_LIMIT)
            toadd = domains[0:_min]
            job_args.append(toadd)
            domains = domains[_min:len(domains)]

        config.logger.info('job args created, starting multi thread proc: ' + str(len(domains)))
        with Pool(processes=multiprocessing.cpu_count()) as pool:
            asyncres = pool.map(get_open_pagerank, job_args)

        i = 1
        import os
        path = OUTPUT_FOLDER + 'open_pagerank/'
        if not os.path.exists(path):
            os.makedirs(path)

        for file in asyncres:
            if file is not None:
                with open(path + 'dump_' + str(i) + '.json', 'w') as outfile:
                    json.dump(file, outfile)
                    i += 1

        config.logger.info('done!')

    except Exception as e:
        config.logger.error(repr(e))


if __name__ == '__main__':

    parameters = [
        {'DATASET': 'microsoft', 'PATH': DATASET_MICROSOFT_PATH, 'URL_COL_INDEX': 3, 'URL_COL_NAME': 'url', 'DELIMITER': '\t'},
        {'DATASET': 'c3', 'PATH': DATASET_3C_SITES_PATH, 'URL_COL_INDEX': 2, 'URL_COL_NAME': 'document_url', 'DELIMITER': ','}
    ]

    export_open_pagerank_data(parameters)



