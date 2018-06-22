from multiprocessing.dummy import Pool

import pandas as pd
import multiprocessing
import tldextract
import json

import time

from sklearn.externals import joblib

from coffeeandnoodles.core.util import get_open_pagerank
from config import DeFactoConfig


config = DeFactoConfig()


config.logger.info('reading MS dataset...')
df = pd.read_csv(config.dataset_microsoft_webcred, delimiter='\t', header=0)
config.logger.info('creating job args...')
job_args = []
domains=[]
for index, row in df.iterrows():
    url = str(row[3])
    try:
        o = tldextract.extract(url)
        domains.append('%s.%s' % (o.domain, o.suffix))
    except Exception as e:
        continue

print(len(domains))
print(len(set(domains)))

# the API returns up to 100 websites, and there is a query limit.
# so we're optimizing this here...
domains=list(set(domains))
while len(domains)>0:
    _min=min(len(domains), 100)
    toadd=domains[0:_min]
    job_args.append(toadd)
    domains=domains[_min:len(domains)]


config.logger.info('job args created, starting multi thread proc')
with Pool(processes=multiprocessing.cpu_count()) as pool:
    asyncres=pool.map(get_open_pagerank, job_args)

i=1
for file in asyncres:
    with open(config.root_dir_data + 'pagerank/dump_' + str(i) + '.json', 'w') as outfile:
        json.dump(file, outfile)
        i+=1


config.logger.info('done!')

