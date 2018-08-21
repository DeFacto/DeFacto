import multiprocessing
from bs4 import BeautifulSoup
from sklearn.externals import joblib
import numpy as np
from multiprocessing.dummy import Pool
import pandas as pd
from pathlib import Path
from coffeeandnoodles.core.util import get_md5_from_string
from coffeeandnoodles.core.web.microsoft_azure.microsoft_azure_helper import MicrosoftAzurePlatform
from config import DeFactoConfig
import os
import warnings
from defacto.definitions import BENCHMARK_FILE_NAME_TEMPLATE, \
    DATASET_3C_SCORES_PATH, DATASET_3C_SITES_PATH, MAX_WEBSITES_PROCESS, \
    OUTPUT_FOLDER, DATASET_MICROSOFT_PATH, ENC_WEB_DOMAIN, BEST_PAD_ALGORITHM, BEST_PAD_WINDOW, BEST_PAD_EXPERIMENT_TYPE
from trustworthiness.features_core import FeaturesCore
from trustworthiness.util import get_html_file_path, get_features_web_microsoft, get_features_web_c3

with warnings.catch_warnings():
    warnings.filterwarnings("ignore",category=FutureWarning)

__author__ = "Diego Esteves"
__copyright__ = "Copyright 2018, DeFacto Project"
__credits__ = ["Diego Esteves", "Aniketh Reddy", "Piyush Chawla"]
__license__ = "Apache"
__version__ = "1.0"
__maintainer__ = "Diego Esteves"
__email__ = "diegoesteves@gmail.com"
__status__ = "Dev"

config = DeFactoConfig()
bing = MicrosoftAzurePlatform(config.translation_secret)

def likert2bin(likert):

    assert (likert>=1 and likert <=5)

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
    y5 = []
    y3 = []
    y2 = []
    tot_files = 0
    text_data = None

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

                    text_data = joblib.load(path_feature + features_file)

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
                    y5.append(text_data[1])
                    y3.append(likert2tri(text_data[1]))
                    y2.append(likert2bin(text_data[1]))

                else:
                    print('no tags...')

            print('tot files: ', tot_files)
            print('dictionary size: ', len(tags_set))
            print('dictionary: ', tags_set)

            le.fit(tags_set)

            X_html = [le.transform(s) for s in sentences]
            print(len(X_html))
            print(len(y5))

            data = (X_html, y5, y3, y2)

            joblib.dump(data, OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq.pkl')
            joblib.dump(le, OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq_enc.pkl')
        else:
            print('file found: ' + OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq.pkl')
            print('loading dump (HTML2seq)')
            data = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq.pkl')
            le = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq_enc.pkl')

        return (data, le)

    except Exception as e:
        config.logger.error(repr(e))
        raise

def get_text_features(exp_folder, ds_folder, features_file, html2seq = False):
    try:
        assert (exp_folder is not None and exp_folder != '')
        assert (ds_folder is not None and ds_folder != '')
        config.logger.info('get_text_features()')

        y2 = []
        y3 = []
        y5 = []

        config.logger.debug('extracting features for: ' + features_file)
        X = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + features_file)

        if html2seq is True:
            le = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq_enc.pkl')
            # load best classifier
            file = BENCHMARK_FILE_NAME_TEMPLATE % (BEST_PAD_ALGORITHM.lower(), BEST_PAD_WINDOW, BEST_PAD_EXPERIMENT_TYPE)
            config.logger.debug('loading model: ' + file)
            clf_html2seq = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'models/html2seq/' + file)

        for feat in X:
            if html2seq is True:
                hash = get_md5_from_string(feat[0])
                file_name = ds_folder.replace('/','') + '_dataset_features_%s.pkl' % (hash)
                x = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq/' + file_name)
                if BEST_PAD_WINDOW <= len(x):
                    x2 = le.transform(x[0:BEST_PAD_WINDOW])
                    klass = clf_html2seq.predict([x2])[0]
                else:
                    x2 = le.transform(x)
                    klass = clf_html2seq.predict([np.pad(x2, (0, BEST_PAD_WINDOW - len(x2)), 'constant')])[0]
                feat.extend([klass])

            y2.append(likert2bin(feat[1]))
            y3.append(likert2tri(feat[1]))
            y5.append(feat[1])

        X = np.array(X)
        # excluding hash and y data
        X_clean = np.delete(X, np.s_[0:2], axis=1)
        config.logger.info('OK -> ' + str(X_clean.shape))
        return X_clean, y5, y3, y2


    except Exception as e:
        config.logger.error(repr(e))
        raise

def __export_features_multi_proc_microsoft(exp_folder, ds_folder, export_html_tags, force):

    assert (exp_folder is not None and exp_folder != '')
    assert (ds_folder is not None and ds_folder != '')

    try:

        df = pd.read_csv(DATASET_MICROSOFT_PATH, delimiter='\t', header=0)
        config.logger.info('creating job args for: ' + DATASET_MICROSOFT_PATH)
        job_args = []
        tot_proc = 0
        err = 0
        for index, row in df.iterrows():
            url = str(row[3])
            urlencoded = get_md5_from_string(url)
            name = urlencoded + '.pkl'
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
                    fe = FeaturesCore(url, local_file_path=path)
                else:
                    fe = FeaturesCore(url)
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

    except Exception as e:
        config.logger.error(repr(e))

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
        name = urlencoded + '.pkl'
        folder = OUTPUT_FOLDER + exp_folder + ds_folder
        my_file = Path(folder + 'text/' + name)
        my_file_err = Path(folder + 'error/' + name)
        if (not my_file.exists() and not my_file_err.exists()) or force is True:
            temp = df_scores['document_id'].isin([url_id])
            likert_mode = df_scores.loc[temp, 'mode(documentevaluation_credibility)']
            likert_avg = df_scores.loc[temp, 'average(documentevaluation_credibility)']
            #likert_mode = df_scores.loc[url_id]['mode(documentevaluation_credibility)']
            #likert_avg = df_scores.loc[url_id]['average(documentevaluation_credibility)']
            fe = FeaturesCore(url)
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
        asyncres = pool.starmap(get_features_web_c3, job_args)

    config.logger.info('feature extraction done! saving...')
    name = '3c_dataset_' + str(len(job_args)) + '_text_features.pkl'
    joblib.dump(asyncres, OUTPUT_FOLDER + exp_folder + name)
    config.logger.info('done! file: ' + name)

def __verify_and_create_experiment_folders(out_exp_folder, dataset):
    try:
        path = OUTPUT_FOLDER + out_exp_folder + dataset + '/'
        if not os.path.exists(path):
            os.makedirs(path)

        subfolders = ['error', 'graphs', 'html', 'text', 'models',
                      'models/text', 'models/html2seq', 'models/html',
                      'models/text/2-classes/', 'models/text/3-classes/', 'models/text/5-classes/']
        for subfolder in subfolders:
            if not os.path.exists(path + subfolder):
                os.makedirs(path + subfolder)

        config.logger.info('experiment sub-folders created successfully: ' + path)

    except Exception as e:
        raise e

def export_features_multithread(out_exp_folder, dataset, export_html_tags = True, force=False):

    try:
        __verify_and_create_experiment_folders(out_exp_folder, dataset)

        if dataset == 'microsoft':
            __export_features_multi_proc_microsoft(out_exp_folder, dataset + '/', export_html_tags, force)
        elif dataset == 'c3':
            __export_features_multi_proc_3c(out_exp_folder, dataset + '/', export_html_tags, force)
        else:
            raise('script not implemented: ' + dataset)
    except:
        raise


if __name__ == '__main__':

    '''
        automatically extracts all features from a given dataset (currently microsoft or 3c)
        and saves the files locally, one per example (URL)
    '''

    params = [
        {'EXP_FOLDER': 'exp004/', 'DATASET': 'microsoft', 'EXPORT_HTML': True, 'REPROCESS': True},
        {'EXP_FOLDER': 'exp004/', 'DATASET': 'c3', 'EXPORT_HTML': True, 'REPROCESS': True},
    ]

    for p in params:
        export_features_multithread(p['EXP_FOLDER'], p['DATASET'], export_html_tags=p['EXPORT_HTML'], force=p['REPROCESS'])