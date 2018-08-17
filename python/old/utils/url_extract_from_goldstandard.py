import csv
import pandas as pd
import urllib.parse

import logging

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logging.getLogger('requests').setLevel(logging.CRITICAL)
logger = logging.getLogger(__name__)

from tldextract import tldextract

liar = '/Users/diegoesteves/cache_files/fake_news/liar_liar_paints_on_fire/'
fake = '/Users/diegoesteves/cache_files/fake_news/kaggle_fake_news_challenge/'
get_full_document = "wget http://www.politifact.com//api/v/2/statement/[ID]/?format=json"

def get_domain_of_url(url):
    list = tldextract.extract(url)
    d = list.domain + '.' + list.suffix
    return d

def process_fake_dataset():
   df = pd.read_csv(fake + 'fake.csv', delimiter=',', encoding='utf-8')
   fake_header = ['uuid', 'ord_in_thread', 'author', 'published', 'title', 'text', 'language', 'crawled', 'site_url',
                  'country', 'domain_rank', 'thread_title', 'spam_score', 'main_img_url', 'replies_count',
                  'participants_count', 'likes', 'comments', 'shares', 'type']
   df.columns = fake_header
   print(len(df))
   domains, urls = [], []
   ffakeurls = open(fake + 'urls.txt', 'w')
   for index, row in df.iterrows():
       d = get_domain_of_url(row['site_url'])
       if d not in domains:
           domains.append(d)
           ffakeurls.write("%s\n" % d)
       if row['site_url'] not in urls:
           urls.append(row['site_url'])
   ffakeurls.close()
   print(len(domains))
   print(len(urls))
   print("--labels: ")
   print(list(df.ix[:, 1].unique()))
   print("--total of examples: ", len(df))
   # print(pd.value_counts(df['label'].values, sort=False))
   # print(pd.value_counts(df['language'].values, sort=False))

def process_liar_dataset():
    df = pd.read_csv(liar + 'train.tsv', delimiter='\t', encoding='utf-8', header=0)
    liar_header = ['id', 'label', 'statement', 'subjects', 'speaker', 'speaker_job', 'state_info',
                   'party_affiliation', 'hist', 'false_count', 'half_true_count', 'mostly_true_count',
                   'pants_on_fire_count', 'context']
    df.columns = liar_header
    print(len(df))
    domains, urls = [], []
    ffakeurls = open(liar + 'urls.txt', 'w')
    #for index, row in df.iterrows():


logging.info('processing the kaggle fake news dataset')
process_fake_dataset()

logging.info('processing the liar dataset')
# problem: only data from PolitiFact.com
process_liar_dataset()
