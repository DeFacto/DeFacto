from multiprocessing.dummy import Pool

import pandas as pd
import multiprocessing

import time

from sklearn.externals import joblib

from config import DeFactoConfig
from src.coffeeandnoodles.core.web.scrap.scrap import WebScrap
from src.core.classifiers.credibility.util import get_html_file_path, get_features_web, save_url_body
from src.core.feature_extractor import FeatureExtractor

config = DeFactoConfig()

config.logger.info('reading MS dataset...')
df = pd.read_csv(config.dataset_microsoft_webcred, delimiter='\t', header=0)
config.logger.info('creating job args...')
job_args = []
for index, row in df.iterrows():
    url = str(row[3])
    path = str(get_html_file_path(url))
    if path is not None:
        fe = FeatureExtractor(url, local_file_path=path)
    else:
        fe = FeatureExtractor(url)
    job_args.append(fe)

config.logger.info('job args created, starting multi thread proc')
with Pool(processes=multiprocessing.cpu_count()) as pool:
    pool.map(save_url_body, job_args)

config.logger.info('done!')