import collections
from pathlib import Path

import numpy as np
import pdfkit as pdfkit
from sklearn.metrics import mean_absolute_error, mean_squared_error, confusion_matrix, classification_report, \
    accuracy_score
from tldextract import tldextract

from coffeeandnoodles.core.web.scrap.scrap import WebScrap
from sklearn.externals import joblib

from coffeeandnoodles.core.util import get_md5_from_string
from config import DeFactoConfig
from defacto.definitions import DATASET_3C_SITES_PATH, DATASET_MICROSOFT_PATH_PAGES_MISSING, \
    DATASET_MICROSOFT_PATH_PAGES_CACHED

config = DeFactoConfig()


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
    le = preprocessing.LabelEncoder()

    domains = []

    df_sites = pd.read_csv(DATASET_3C_SITES_PATH, na_values=0, delimiter=',', usecols=['document_url'])
    for index, row in df_sites.iterrows():
        url = str(row[0])
        print(index, url)
        try:
            o = tldextract.extract(url)
            if o.suffix is not None:
                domains.append(str(o.suffix).lower())
        except:
            continue

    # appending upper level domains, from http://data.iana.org/TLD/tlds-alpha-by-domain.txt
    # Version 2018040300, Last Updated Tue Apr  3 07:07:01 2018 UTC
    df = pd.read_csv(config.datasets + 'data/iana/org/TLD/tlds-alpha-by-domain.txt', sep=" ", header=None)
    for index, row in df.iterrows():
        print(index, row[0])
        domains.append(str(row[0]).lower())

    df = pd.read_csv(config.dataset_microsoft_webcred, delimiter='\t', header=0)
    for index, row in df.iterrows():
        url = str(row[3])
        print(index, url)
        path = get_html_file_path(url)
        web = WebScrap(url, 15, 'lxml', path)
        domains.append(web.get_suffix())





    le.fit(domains)
    joblib.dump(le, config.enc_domain)
    print(le.classes_)

def get_features_web_c3(extractor, url, likert_mode, likert_avg, folder, name, export_html_tags):

    try:

        if extractor.webscrap is None:
            extractor.call_web_scrap()

        if not extractor.error:

            config.logger.info('process starts for : ' + extractor.url)

            data = collections.defaultdict(dict)
            data['url'] = url
            data['likert_mode'] = likert_mode
            data['likert_avg'] = likert_avg

            out = extractor.get_final_feature_vector()

            # text/
            data['features'] = out
            config.logger.info('features extracted - OK: ' + extractor.url)
            joblib.dump(data, folder + 'text/' + name)

            # html/
            if export_html_tags:
                with open(folder + 'html/' + name.replace('.pkl', '.txt'), "w") as file:
                    file.write(str(extractor.get_sequence_html()))

            return data

        else:
            Path(folder + 'error/' + name).touch()

    except:
        config.logger.error('features extraction - ERROR: ' + extractor.url)
        raise

def get_features_web_microsoft(extractor, topic, query, rank, url, likert, folder, name, export_html_tags):

    try:

        if extractor.webscrap is None:
            extractor.call_web_scrap()

        if not extractor.error:

            config.logger.info('process starts for : ' + extractor.url)

            data = collections.defaultdict(dict)
            data['topic'] = topic
            data['query'] = query
            data['rank'] = rank
            data['url'] = url
            data['likert'] = likert

            out = extractor.get_final_feature_vector()

            # text/
            data['features'] = out
            config.logger.info('features extracted - OK: ' + extractor.url)
            joblib.dump(data, folder + 'text/' + name)

            # html/
            if export_html_tags:
                with open(folder + 'html/' + name.replace('.pkl', '.txt'), "w") as file:
                    file.write(str(extractor.get_sequence_html()))

            return data

        else:

            Path(folder + 'error/' + name).touch()

    except:
        config.logger.error('features extraction - ERROR: ' + extractor.url)
        raise

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