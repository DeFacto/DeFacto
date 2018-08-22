import collections
import os
from pathlib import Path

import numpy as np
import pdfkit as pdfkit
from sklearn.metrics import mean_absolute_error, mean_squared_error, confusion_matrix, classification_report, \
    accuracy_score
from tldextract import tldextract
from sklearn.externals import joblib

from coffeeandnoodles.core.util import get_md5_from_string
from config import DeFactoConfig
from defacto.definitions import DATASET_3C_SITES_PATH, DATASET_MICROSOFT_PATH_PAGES_MISSING, \
    DATASET_MICROSOFT_PATH_PAGES_CACHED, ENC_WEB_DOMAIN, ENC_WEB_DOMAIN_SUFFIX, DATASET_MICROSOFT_PATH, OUTPUT_FOLDER

import re

from trustworthiness.feature_extractor import get_html2sec_features

config = DeFactoConfig()


def filterTerm(word):
    if word is not None:
        temp = word.lower()
        return re.sub(r"[^A-Za-z]+", '', temp)
    else:
        return ''

def print_report_regression(clf_name, predictions, y_test, targets):
    print('MAE', mean_absolute_error(y_test, predictions))
    print('RMSE', np.math.sqrt(mean_squared_error(y_test, predictions)))
    print("-----------------------------------------------------------------------")


def print_report(clf_name, predictions, y_test, targets):
    print("Classifier: ", clf_name)
    print(confusion_matrix(y_test, predictions))
    print("accuracy: ", accuracy_score(y_test, predictions))
    print(classification_report(y_test, predictions, target_names=targets))
    # print(":: recall: ", recall_score(y_test, predictions, average='weighted'))
    # print(":: precision: ", precision_score(y_test, predictions, average='weighted'))
    # print(":: f1: ", f1_score(y_test, predictions, average='weighted'))
    print("-----------------------------------------------------------------------")

def verify_and_create_experiment_folders(out_exp_folder, dataset):
    try:
        path = OUTPUT_FOLDER + out_exp_folder + dataset + '/'
        if not os.path.exists(path):
            os.makedirs(path)


        folders_text = ['benchmark/text/2-classes/cls/', 'benchmark/text/3-classes/cls/', 'benchmark/text/5-classes/cls/',
                      'benchmark/text/2-classes/log/', 'benchmark/text/3-classes/log/', 'benchmark/text/5-classes/log/',
                      'benchmark/text/2-classes/graph/', 'benchmark/text/3-classes/graph/', 'benchmark/text/5-classes/graph/']

        folders_html = ['benchmark/html/2-classes/cls/', 'benchmark/html/3-classes/cls/', 'benchmark/html/5-classes/cls/',
                      'benchmark/html/2-classes/log/', 'benchmark/html/3-classes/log/', 'benchmark/html/5-classes/log/',
                      'benchmark/html/2-classes/graph/', 'benchmark/html/3-classes/graph/', 'benchmark/html/5-classes/graph/']

        folders_text_html = ['benchmark/text_html/2-classes/cls/', 'benchmark/text_html/3-classes/cls/', 'benchmark/text_html/5-classes/cls/',
                      'benchmark/text_html/2-classes/log/', 'benchmark/text_html/3-classes/log/', 'benchmark/text_html/5-classes/log/',
                      'benchmark/text_html/2-classes/graph/', 'benchmark/text_html/3-classes/graph/', 'benchmark/text_html/5-classes/graph/']

        subfolders = ['features/text/', 'features/error/', 'features/html/', 'features/text_html/', 'features/html2seq/']

        subfolders.extend(folders_text)
        subfolders.extend(folders_html)
        subfolders.extend(folders_text_html)

        for subfolder in subfolders:
            if not os.path.exists(path + subfolder):
                os.makedirs(path + subfolder)

        config.logger.info('experiment sub-folders created successfully: ' + path)

    except Exception as e:
        raise e

def get_html_file_path(url):
    path = url.replace('http://', '')
    last = path.split('/')[-1]

    path_root = None
    if ('.html' not in last) and ('.htm' not in last) and ('.shtml' not in last):
        if path[-1] != '/':
            path = path + '/'
        path_root1 = Path(DATASET_MICROSOFT_PATH_PAGES_CACHED + path + 'index.html')
        path_root2 = Path(DATASET_MICROSOFT_PATH_PAGES_MISSING + path + 'index.html')
    else:
        path_root1 = Path(DATASET_MICROSOFT_PATH_PAGES_CACHED + path)
        path_root2 = Path(DATASET_MICROSOFT_PATH_PAGES_MISSING + path)

    if path_root1.exists():
        path_root = path_root1
    elif path_root2.exists():
        path_root = path_root2
    else:
        # sometimes the last part is not a folder, but the file itself without the ".html" , try it as a last attempt
        path_root3a = Path(DATASET_MICROSOFT_PATH_PAGES_CACHED + path.replace(last, '') + last + '.html')
        path_root3b = Path(DATASET_MICROSOFT_PATH_PAGES_CACHED + path.replace(last, '') + last + '.htm')
        path_root3c = Path(DATASET_MICROSOFT_PATH_PAGES_CACHED + path.replace(last, '') + last + '.shtml')
        if path_root3a.exists():
            path_root = path_root3a
        elif path_root3b.exists():
            path_root = path_root3b
        elif path_root3c.exists():
            path_root = path_root3c
        else:
            # url_broken.append(url)
            raise Exception(
                ':: this should not happen, double check core/web/credibility/fix_dataset_microsoft.py | url = ' + url)

    return path_root


def get_encoder_domain():

    import pandas as pd
    from sklearn import preprocessing
    le1 = preprocessing.LabelEncoder()
    le2 = preprocessing.LabelEncoder()

    domain_s = ['com']
    domain = []

    df_sites = pd.read_csv(DATASET_3C_SITES_PATH, na_values=0, delimiter=',', usecols=['document_url'])
    for index, row in df_sites.iterrows():
        url = str(row[0])
        print(index, url)
        try:
            o = tldextract.extract(url)
            if o.suffix is not None:
                domain_s.append(str(o.suffix).lower())
            if o.domain is not None:
                domain.append(str(o.domain).lower())
        except:
            continue

    # appending upper level domains, from http://data.iana.org/TLD/tlds-alpha-by-domain.txt
    # Version 2018040300, Last Updated Tue Apr  3 07:07:01 2018 UTC
    df = pd.read_csv(config.datasets + 'data/iana/org/TLD/tlds-alpha-by-domain.txt', sep=" ", header=None)
    for index, row in df.iterrows():
        print(index, row[0])
        domain.append(str(row[0]).lower())

    df = pd.read_csv(DATASET_MICROSOFT_PATH, delimiter='\t', header=0)
    for index, row in df.iterrows():
        url = str(row[3])
        print(index, url)
        try:
            o = tldextract.extract(url)
            if o.suffix is not None:
                domain_s.append(str(o.suffix).lower())
            if o.domain is not None:
                domain.append(str(o.domain).lower())
        except:
            continue


    le1.fit(domain)
    joblib.dump(le1, ENC_WEB_DOMAIN)
    print(le1.classes_)

    le2.fit(domain_s)
    joblib.dump(le2, ENC_WEB_DOMAIN_SUFFIX)
    print(le2.classes_)

def diff_month(d1, d2):
    return (d1.year - d2.year) * 12 + d1.month - d2.month

def save_url_body(extractor):
    try:
        config.logger.info('extracting features for: ' + extractor.url)
        hash = get_md5_from_string(extractor.local_file_path)
        text=extractor.webscrap.get_body()
        with open(config.root_dir_data + 'marseille/input/' + hash + '.txt', "w") as file:
            file.write(text)

    except Exception as e:
        config.logger.error(repr(e))
        raise



if __name__ == '__main__':
    get_encoder_domain()