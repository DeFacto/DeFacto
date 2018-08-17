import util
import pandas as pd
import numpy as np
import tldextract

dataset = pd.read_csv('/Users/diegoesteves/DropDrive/CloudStation/experiments_cache/web_credibility/datasets/NewsAggregatorDataset/newsCorpora.csv', sep='\t')
sources = {}

def star(f):
  return lambda args: f(*args)

for article in dataset.itertuples():
    url = article[3]
    ext = tldextract.extract(url)
    domain = ext.domain
    if domain not in sources:
        sources[domain] = 0
    sources[domain] = sources[domain] + 1
f = open('sources.txt',"w+")
for key, value in sorted(sources.iteritems(), key=star(lambda k,v: (v,k),reverse=True)):
    f.write(key + " " + str(value) + '\n')