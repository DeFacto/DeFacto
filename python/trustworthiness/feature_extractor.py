import multiprocessing
import sys
from datetime import date
import nltk
from bs4 import BeautifulSoup
from keras.preprocessing.sequence import pad_sequences
from sklearn.externals import joblib
from textstat.textstat import textstat
from sumy.parsers.html import HtmlParser
from sumy.parsers.plaintext import PlaintextParser
from sumy.nlp.tokenizers import Tokenizer
from sumy.summarizers.lsa import LsaSummarizer as Summarizer
from sumy.nlp.stemmers import Stemmer
from sumy.utils import get_stop_words
from singleton_decorator import singleton
from urllib.request import urlopen
from keras.datasets import imdb
from keras.models import load_model
from keras.preprocessing import sequence
import lxml.html
import json
import numpy as np
import socket
from multiprocessing.dummy import Pool
import pandas as pd
from tldextract import tldextract
from pathlib import Path
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer
from coffeeandnoodles.core.util import get_md5_from_string
from coffeeandnoodles.core.web.microsoft_azure.microsoft_azure_helper import MicrosoftAzurePlatform
from coffeeandnoodles.core.web.scrap.scrap import WebScrap
from config import DeFactoConfig
import whois
from urllib.parse import urlparse
import os
import warnings
from defacto.definitions import DEFACTO_LEXICON_GI_PATH, BENCHMARK_FILE_NAME_TEMPLATE, \
    DATASET_3C_SCORES_PATH, DATASET_3C_SITES_PATH, MAX_WEBSITES_PROCESS, SOCIAL_NETWORK_NAMES, \
    OUTPUT_FOLDER, TIMEOUT_MS, DATASET_MICROSOFT_PATH
from dev.web.credibility.feature_extractor import FeatureExtractor
from trustworthiness.util import get_html_file_path, get_features_web_microsoft, get_features_web_3c
from trustworthiness.topic_utils import TopicTerms

from trustworthiness import features_core

with warnings.catch_warnings():
    warnings.filterwarnings("ignore",category=FutureWarning)

__author__ = "Diego Esteves"
__copyright__ = "Copyright 2018, DeFacto Project"
__credits__ = ["Diego Esteves", "Aniketh Reddy", "Piyush Chawla"]
__license__ = "Apache"
__version__ = "0.0.1"
__maintainer__ = "Diego Esteves"
__email__ = "diegoesteves@gmail.com"
__status__ = "Dev"

config = DeFactoConfig()
bing = MicrosoftAzurePlatform(config.translation_secret)


def likert2bin(likert):

    assert likert>=1 and likert <=5

    if likert in (1, 2, 3):
        return 0
    elif likert in (4, 5):
        return 1
    else:
        raise Exception('error y')

def likert2tri(likert):

    assert likert>=1 and likert <=5

    if likert in (1, 2):
        return 0
    elif likert == 3:
        return 1
    elif likert in (4, 5):
        return 2
    else:
        raise Exception('error y')

def get_html2sec_features(exp_folder, ds_folder):
    tags_set = []
    sentences = []
    y = []
    y2 = []
    tot_files = 0

    features = None
    from sklearn import preprocessing
    le = preprocessing.LabelEncoder()

    config.logger.info('get_html2sec_features()')

    try:
        my_file = Path(OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq.pkl')
        path_html2seq = OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq/'

        if not os.path.exists(path_html2seq):
            os.makedirs(path_html2seq)

        if not my_file.exists():
            print('file not found: ' + OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq.pkl')
            print('start process (HTML2seq)')
            for file_html in os.listdir(OUTPUT_FOLDER + exp_folder + ds_folder + '/html'):

                html2seq_feature_file = file_html.replace('.txt', '.pkl')
                check = Path(path_html2seq + html2seq_feature_file)
                tot_files += 1
                if check.exists():
                    print('processing file ' + str(tot_files) + ' - cached')
                    tags = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + html2seq_feature_file)
                else:
                    print('processing file ' + str(tot_files) + ' - not cached')
                    #features_file = file_html.replace('dataset_visual_features_', 'dataset_features_')
                    features_file = file_html.replace('.txt', '.pkl')

                    path_feature = OUTPUT_FOLDER + exp_folder + ds_folder + 'text/'
                    path_html = OUTPUT_FOLDER + exp_folder + ds_folder + 'html/'

                    features = joblib.load(path_feature + features_file)

                    tags = []
                    #if file.startswith('microsoft_dataset_visual_features_') and file.endswith('.txt'):
                    soup = BeautifulSoup(open(path_html + file_html), "html.parser")
                    html = soup.prettify()
                    for line in html.split('\n'):
                        if isinstance(line, str) and len(line.strip()) > 0:
                            if (line.strip()[0] == '<') and (line.strip()[0:2] != '<!'):
                                if len(line.split()) > 1:
                                    tags.append(line.split()[0] + '>')
                                else:
                                    tags.append(line.split()[0])
                            elif (line.strip()[0:2] == '</' and line.strip()[0:2] != '<!'):
                                tags.append(line.split()[0])

                    # dump html2seq features
                    joblib.dump(tags, path_html2seq + html2seq_feature_file)

                if len(tags) > 0:
                    sentences.append(tags)
                    tags_set.extend(tags)
                    tags_set = list(set(tags_set))

                    # getting y
                    try:
                        y.append(int(features['likert']))
                        y2.append(likert2bin(int(features['likert'])))
                    except: # have to rename these fields later, to have the same interface (microsoft and c3 datasets)
                        try:
                            y.append(int(features['likert_mode']))
                            y2.append(likert2bin(int(features['likert_mode'])))
                        except:
                            print('should not happen!!!')
                            raise
                else:
                    print('no tags...')

            print('tot files: ', tot_files)
            print('dictionary size: ', len(tags_set))
            print('dictionary: ', tags_set)

            le.fit(tags_set)

            X = [le.transform(s) for s in sentences]
            print(len(X))
            print(len(y))

            features = (X, y, y2)

            joblib.dump(features, OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq.pkl')
            joblib.dump(le, OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq_enc.pkl')
        else:
            print('file found: ' + OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq.pkl')
            print('loading dump (HTML2seq)')
            features = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq.pkl')
            le = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq_enc.pkl')

        return (features, le)

    except Exception as e:
        config.logger.error(repr(e))
        raise

def get_text_features(exp_folder, ds_folder, html2seq = False, best_pad=0, best_cls='', exp_type_combined='bin'):
    try:
        assert (exp_folder is not None and exp_folder != '')
        assert (ds_folder is not None and ds_folder != '')
        config.logger.info('get_text_features()')

        XX = []
        y = []
        y2 = []
        encoder = joblib.load(config.enc_domain)

        if html2seq is True:
            le = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq_enc.pkl')
            # load best classifier
            file = BENCHMARK_FILE_NAME_TEMPLATE % (best_cls.lower(), best_pad, exp_type_combined)
            config.logger.debug('loading model: ' + file)
            clf_html2seq = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'models/html2seq/' + file)

        for file in os.listdir(OUTPUT_FOLDER + exp_folder + ds_folder):
            if file.endswith('_text_features.pkl'):
                config.logger.info('features file found: ' + file)
                features = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + file)
                config.logger.debug('extracting features')
                for d in features:
                    feat = d.get('features')
                    if feat is None:
                        raise Exception('error in the feature extraction! No features extracted...')
                    # feat[2] = encoder.transform([feat[2]])
                    try:
                        feat[3] = encoder.transform([feat[3]])[0]
                    except Exception as e:
                        print('encoder transformation error! ', repr(e))
                        feat[3] = encoder.transform('com')
                    del feat[2]

                    if html2seq is True:
                        hash = get_md5_from_string(d.get('url'))
                        file_name = ds_folder.replace('/','') + '_dataset_features_%s.pkl' % (hash)
                        x=joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq/' + file_name)
                        if best_pad <= len(x):
                            x2 = le.transform(x[0:best_pad])
                            klass = clf_html2seq.predict([x2])[0]
                        else:
                            x2 = le.transform(x)
                            klass = clf_html2seq.predict([np.pad(x2, (0, best_pad-len(x2)), 'constant')])[0]

                        feat.extend([klass])
                        XX.append(feat)
                    else:
                        XX.append(feat)

                    try:
                        y.append(int(d.get('likert')))
                        y2.append(likert2bin(int(d.get('likert'))))
                    except:  # have to rename these fields later, to have the same interface (microsoft and c3 datasets)
                        y.append(int(d.get('likert_mode')))
                        y2.append(likert2bin(int(d.get('likert_mode'))))


        if len(XX) == 0:
            raise Exception('processed full file not found for this folder! ' + OUTPUT_FOLDER + exp_folder + ds_folder)

        config.logger.info('OK')
        return XX, y, y2


    except Exception as e:
        config.logger.error(repr(e))
        raise

def __export_features_multi_proc_microsoft(exp_folder, ds_folder, export_html_tags, force):

    assert (exp_folder is not None and exp_folder != '')
    assert (ds_folder is not None and ds_folder != '')

    df = pd.read_csv(DATASET_MICROSOFT_PATH, delimiter='\t', header=0)
    config.logger.info('creating job args for: ' + DATASET_MICROSOFT_PATH)
    job_args = []
    tot_proc = 0
    err = 0
    for index, row in df.iterrows():
        url = str(row[3])
        urlencoded = get_md5_from_string(url)
        name = 'microsoft_dataset_features_' + urlencoded + '.pkl'
        folder = OUTPUT_FOLDER + exp_folder + ds_folder
        my_file = Path(folder + 'text/' + name)
        my_file_err = Path(folder + 'error/' + name)
        if (not my_file.exists() and not my_file_err.exists()) or force is True:
            topic = row[0]
            query = row[1]
            rank = int(row[2])
            likert = int(row[4])
            path = str(get_html_file_path(url))
            if path is not None:
                fe = FeatureExtractor(url, local_file_path=path)
            else:
                fe = FeatureExtractor(url)
            if fe.error is False:
                job_args.append((fe, topic, query, rank, url, likert, folder, name, export_html_tags)) # -> multiple arguments
                tot_proc += 1
                if tot_proc > MAX_WEBSITES_PROCESS - 1:
                    config.logger.warn('max number of websites reached: ' + str(MAX_WEBSITES_PROCESS))
                    break
            else:
                err += 1

            if index % 100 ==0:
                config.logger.info('processing job args ' + str(index))
            # extractors.append(fe) # -> single argument

    config.logger.info('%d job args created (out of %s): starting multi thread' % (len(job_args), len(df)))
    config.logger.info('apart from the jobs, weve got %d errors' % (err))
    config.logger.info(str(multiprocessing.cpu_count()) + ' CPUs available')
    with Pool(processes=multiprocessing.cpu_count()) as pool:
        asyncres = pool.starmap(get_features_web_microsoft, job_args)
        #asyncres = pool.map(get_features_web, extractors)

    config.logger.info('feature extraction done! saving...')
    #name = 'microsoft_dataset_' + time.strftime("%Y%m%d%H%M%S") + '_features.pkl'
    name = 'microsoft_dataset_' + str(len(job_args)) + '_text_features.pkl'
    joblib.dump(asyncres, OUTPUT_FOLDER + exp_folder + name)
    config.logger.info('done! file: ' + name)

def __export_features_multi_proc_3c(exp_folder, ds_folder, export_html_tags, force):
    assert (exp_folder is not None and exp_folder != '')
    # get the parameters
    config.logger.info('reading 3C dataset...')
    df_sites = pd.read_csv(DATASET_3C_SITES_PATH, na_values=0, delimiter=',', usecols=['document_id', 'document_url'])
    df_scores = pd.read_csv(DATASET_3C_SCORES_PATH, na_values=0, delimiter=';', usecols=['average(documentevaluation_credibility)',
                                                                            'mode(documentevaluation_credibility)',
                                                                            'document_id'])

    df_sites.set_index('document_id', inplace=True)
    #df_scores.set_index('document_id', inplace=True)

    config.logger.info('creating job args...')
    job_args = []
    err = 0
    tot_proc = 0

    for doc_index, row in df_sites.iterrows():
        url = str(row[0])
        url_id = doc_index
        urlencoded = get_md5_from_string(url)
        name = '3c_dataset_features_' + urlencoded + '.pkl'
        folder = OUTPUT_FOLDER + exp_folder + ds_folder
        my_file = Path(folder + 'text/' + name)
        my_file_err = Path(folder + 'error/' + name)
        if not my_file.exists() and not my_file_err.exists():
            temp = df_scores['document_id'].isin([url_id])
            likert_mode = df_scores.loc[temp, 'mode(documentevaluation_credibility)']
            likert_avg = df_scores.loc[temp, 'average(documentevaluation_credibility)']

            #likert_mode = df_scores.loc[url_id]['mode(documentevaluation_credibility)']
            #likert_avg = df_scores.loc[url_id]['average(documentevaluation_credibility)']
            fe = FeatureExtractor(url)
            if fe.error is False:
                job_args.append((fe, url, likert_mode, likert_avg, folder, name, export_html_tags))  # -> multiple arguments
                tot_proc += 1
                if tot_proc > MAX_WEBSITES_PROCESS - 1:
                    config.logger.warn('max number of websites reached: ' + str(MAX_WEBSITES_PROCESS))
                    break
            else:
                err += 1
            if tot_proc % 100 == 0:
                config.logger.info('processing job args ' + str(tot_proc))

    config.logger.info('%d job args created (out of %s): starting multi thread' % (len(job_args), len(df_sites)))
    config.logger.info('apart from the jobs, weve got %d errors' % (err))
    config.logger.info(str(multiprocessing.cpu_count()) + ' CPUs available')
    with Pool(processes=multiprocessing.cpu_count()) as pool:
        asyncres = pool.starmap(get_features_web_3c, job_args)

    config.logger.info('feature extraction done! saving...')
    name = '3c_dataset_' + str(len(job_args)) + '_text_features.pkl'
    joblib.dump(asyncres, config.dir_output + exp_folder + name)
    config.logger.info('done! file: ' + name)

def export_features_multithread(out_exp_folder, dataset, export_html_tags = True, force=False):

    try:
        if dataset == 'microsoft':
            __export_features_multi_proc_microsoft(out_exp_folder, dataset + '/', export_html_tags, force)
        elif dataset == '3c':
            __export_features_multi_proc_3c(out_exp_folder, dataset + '/', export_html_tags, force)
        else:
            raise('script not implemented: ' + dataset)
    except:
        raise


if __name__ == '__main__':

    if 1==2:

        '''
            manually example of features extracted from a given URL
        '''

        fe = FeaturesCore('https://www.amazon.com/Aristocats-Phil-Harris/dp/B00A29IQPK')
        print(fe.get_final_feature_vector())

    else:

        '''
            automatically extracts all features from a given dataset (currently microsoft or 3c)
            and saves the files locally, one per example (URL). 
            Since it implements multithread, in order to have a final features file, 
            one needs to call the method: read_feat_files_and_merge()
        '''

        export_features_multithread('exp003/', 'microsoft', export_html_tags=True, force=True)

        export_features_multithread('exp003/', '3c', export_html_tags=True, force=True)





