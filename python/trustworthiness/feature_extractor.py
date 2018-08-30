import collections
import json
import multiprocessing
import pickle

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
    OUTPUT_FOLDER, DATASET_MICROSOFT_PATH, BEST_PAD_ALGORITHM, MICROSOFT_BEST_MODEL_2_KLASS, \
    BEST_PAD_EXPERIMENT_TYPE, MICROSOFT_BEST_K
from trustworthiness.benchmark_utils import verify_and_create_experiment_folders, get_best_html2seq_model_by_exp_type, \
    get_best_html2seq_model
from trustworthiness.features_core import FeaturesCore
from trustworthiness.util import get_html_file_path

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

def get_html2seq(extractor):
    try:
        tags = []
        tags2seq = [0]
        html = extractor.webscrap.soup.prettify()
        for line in html.split('\n'):
            if isinstance(line, str) and len(line.strip()) > 0:
                if (line.strip()[0] == '<') and (line.strip()[0:2] != '<!'):
                    if len(line.split()) > 1:
                        tags.append(line.split()[0] + '>')
                    else:
                        tags.append(line.split()[0])
                elif (line.strip()[0:2] == '</' and line.strip()[0:2] != '<!'):
                    tags.append(line.split()[0])

        for t in tags:
            try:
                x = extractor.encoders.html2seq.transform([t])
                tags2seq.append(x[0])
            except ValueError as ve:
                config.logger.error(repr(ve))
                continue
            except:
                raise

        return tags2seq

    except Exception as e:
        config.logger.error(repr(e))
        raise

def get_features_web(id, extractor, topic, query, rank, url, likert, likert_mode, likert_avg, folder, name, export_html_tags):

    try:
        config.logger.info('processing file ' + str(id))
        if extractor.webscrap is None:
            extractor.call_web_scrap()

        if not extractor.error:

            #config.logger.debug('process starts for : ' + extractor.url)

            data = collections.defaultdict(dict)
            data['topic'] = topic
            data['query'] = query
            data['rank'] = rank
            data['url'] = url
            data['hash'] = get_md5_from_string(url)
            data['likert'] = likert # Microsoft dataset
            data['likert_mode'] = likert_mode # C3 dataset
            data['likert_avg'] = likert_avg # C3 dataset
            data['html2seq'] = []

            err, out = extractor.get_final_feature_vector()
            config.logger.debug('total of features function errors: ' + str(err))

            # text/
            data['features'] = out
            extractor.tot_feat_extraction_errors = err

            # save html?
            html_error = False
            if export_html_tags:
                with open(folder + 'html/' + name.replace('.pkl', '.txt'), "w") as file:
                    content = str(extractor.webscrap.soup)
                    if content is not None:
                        file.write(content)
                    else:
                        html_error = True

            if html_error is False:
                try:
                    data['html2seq'] = get_html2seq(extractor)
                    joblib.dump(data, folder + 'ok/' + name)
                    config.logger.debug('OK: ' + extractor.url)
                    return data
                except:
                    Path(folder + 'error/' + name).touch()
                    config.logger.error('Err: ' + extractor.url)
            else:
                Path(folder + 'error/' + name).touch()
                config.logger.error('Err: ' + extractor.url)
        else:
            Path(folder + 'error/' + name).touch()
            config.logger.error('extractor error: ' + extractor.url)

    except Exception as e:
        Path(folder + 'error/' + name).touch()
        config.logger.error('exception: ' + extractor.url + ' - ' + repr(e))


def get_html2sec_features(folder):
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
        my_file = Path(folder + 'features.html2seq.pkl')
        my_encoder = Path(folder + 'encoder.html2seq.pkl')
        path_html2seq = folder + 'html2seq/'
        path_html = folder + 'html/'
        path_text = folder + 'text/'

        if not my_file.exists():
            config.logger.warn('file not found: ' + my_file)
            config.logger.debug('start extraction process (HTML2seq)')
            for file_html in os.listdir(path_html):
                html2seq_feature_file = file_html.replace('.txt', '.pkl')
                check = Path(path_html2seq + html2seq_feature_file)
                tot_files += 1
                if check.exists():
                    config.logger.debug('tags extracted. ' + str(tot_files) + ' - cached')
                    tags = joblib.load(path_html2seq + html2seq_feature_file)
                else:
                    config.logger.debug('extracting tags. ' + str(tot_files) + ' - not cached')

                    # get corresponding label
                    text_data = joblib.load(path_text + file_html.replace('.txt', '.pkl'))

                    # get tags
                    tags = []
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
                    config.logger.debug('dumping single feature file...')
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
                    config.logger.debug('no tags...')

            config.logger.info('tot files: ', tot_files)
            config.logger.info('dictionary size: ', len(tags_set))
            config.logger.info('dictionary: ', tags_set)

            le.fit(tags_set)

            X_html = [le.transform(s) for s in sentences]
            config.logger.info(len(X_html))
            config.logger.info(len(y5))

            data = (X_html, y5, y3, y2)

            config.logger.debug('dumping full feature file...')
            joblib.dump(data, str(my_file))
            joblib.dump(le, str(my_encoder))
        else:
            config.logger.debug('full feature file found: ' + str(my_file))
            data = joblib.load(str(my_file))
            le = joblib.load(str(my_encoder))

        return (data, le)

    except Exception as e:
        config.logger.error(repr(e))
        raise

def get_web_features(exp_folder, ds_folder, features_file_K, html2seq = False):
    try:
        assert (exp_folder is not None and exp_folder != '')
        assert (ds_folder is not None and ds_folder != '')
        config.logger.info('get_text_features()')

        #y2 = []
        #y3 = []
        y5 = []

        if html2seq is True:
            features_file = 'features.split.all+html2seq_pad.' + features_file_K + '.pkl'
            X = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'features/' + features_file)
            clf_html2seq, best_k = get_best_html2seq_model(ds_folder, exp_folder)
            path = OUTPUT_FOLDER + exp_folder + ds_folder + 'benchmark/html2seq/tfidf.vectorizer.' + best_k + '.pkl'
            vec = joblib.load(path)
        else:
            features_file = 'features.split.all.' + features_file_K + '.pkl'
            X = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'features/' + features_file)

        config.logger.debug('extracting features for: ' + features_file)

        #le = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'html2seq_enc.pkl')
        ## load best classifier
        #file = BENCHMARK_FILE_NAME_TEMPLATE % (BEST_PAD_ALGORITHM.lower(), MICROSOFT_BEST_MODEL_2_KLASS, BEST_PAD_EXPERIMENT_TYPE)
        #config.logger.debug('loading model: ' + file)
        #clf_html2seq = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'models/html2seq/' + file)

        for feat in X:
            if html2seq is True:

                #standard = feat[0]
                html2seq_pad = vec.transform([str(feat[1][1:int(best_k)])])
                pred_klass = clf_html2seq.predict(html2seq_pad)
                pred_prob = clf_html2seq.predict_proba(html2seq_pad)

                feat[0].extend(pred_klass)
                feat[0].extend(pred_prob[0])

            #y2.append(likert2bin(feat[1]))
            #y3.append(likert2tri(feat[1]))
            y5.append(feat[0][1])

        X = np.array(X)
        X_clean = []
        # excluding hash and y data
        for x in X[:,0]:
            X_clean.append(x[2:len(x)])
        #X_clean = np.delete(X, np.s_[0:2], axis=1)
        #config.logger.debug('OK -> ' + str(X_clean.shape))
        return X_clean, y5#, y3, y2


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
        id = 0
        cached= []
        for index, row in df.iterrows():
            id += 1
            url = str(row[3])
            urlencoded = get_md5_from_string(url)
            name = urlencoded + '.pkl'
            folder = OUTPUT_FOLDER + exp_folder + ds_folder + 'features/'
            my_file = Path(folder + 'ok/' + name)
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
                    job_args.append((id, fe, topic, query, rank, url, likert, 0, 0, folder, name, export_html_tags)) # -> multiple arguments
                    tot_proc += 1
                    if tot_proc > MAX_WEBSITES_PROCESS - 1:
                        config.logger.warn('max number of websites reached: ' + str(MAX_WEBSITES_PROCESS))
                        break
                else:
                    err += 1

                if index % 100 ==0:
                    config.logger.info('processing job args ' + str(index))
                # extractors.append(fe) # -> single argument
            elif my_file.exists():
                data = joblib.load(str(my_file))
                cached.append(data)

        config.logger.info('%d job args created (out of %s): starting multi thread' % (len(job_args), len(df)))
        config.logger.info('apart from the jobs, weve got %d errors' % (err))
        config.logger.info(str(multiprocessing.cpu_count()) + ' CPUs available')
        with Pool(processes=multiprocessing.cpu_count()) as pool:
            asyncres = pool.starmap(get_features_web, job_args)
            #asyncres = pool.map(get_features_web, extractors)

        if len(cached) > 0:
            asyncres.extend(cached)

        config.logger.info('feature extraction done! saving...')
        name = 'features.complex.all.' + str(len(job_args) + len(cached)) + '.pkl'
        joblib.dump(asyncres, OUTPUT_FOLDER + exp_folder + ds_folder + 'features/' + name)
        config.logger.info('done! file: ' + name + ' (' + OUTPUT_FOLDER + exp_folder + ds_folder + 'features/)' )

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
    tot = 0
    cached = []
    for doc_index, row in df_sites.iterrows():
        tot+=1
        url = str(row[0])
        url_id = doc_index
        urlencoded = get_md5_from_string(url)
        name = urlencoded + '.pkl'
        folder = OUTPUT_FOLDER + exp_folder + ds_folder + 'features/'
        my_file = Path(folder + 'ok/' + name)
        my_file_err = Path(folder + 'error/' + name)
        if (not my_file.exists() and not my_file_err.exists()) or force is True:
            temp = df_scores['document_id'].isin([url_id])
            likert_mode = df_scores.loc[temp, 'mode(documentevaluation_credibility)']
            likert_avg = df_scores.loc[temp, 'average(documentevaluation_credibility)']
            #likert_mode = df_scores.loc[url_id]['mode(documentevaluation_credibility)']
            #likert_avg = df_scores.loc[url_id]['average(documentevaluation_credibility)']
            fe = FeaturesCore(url)
            if fe.error is False:
                job_args.append((tot, fe, 0, 0, 0, url, 0, likert_mode, likert_avg, folder, name, export_html_tags))  # -> multiple arguments
                tot_proc += 1
                if tot_proc > MAX_WEBSITES_PROCESS - 1:
                    config.logger.warn('max number of websites reached: ' + str(MAX_WEBSITES_PROCESS))
                    break
            else:
                err += 1
            if tot_proc % 100 == 0:
                config.logger.info('processing job args ' + str(tot_proc))
        elif my_file.exists():
            data = joblib.load(str(my_file))
            cached.append(data)

    config.logger.info('%d job args created (out of %s): starting multi thread' % (len(job_args), len(df_sites)))
    config.logger.info('apart from the jobs, weve got %d errors' % (err))
    config.logger.info(str(multiprocessing.cpu_count()) + ' CPUs available')
    with Pool(processes=multiprocessing.cpu_count()) as pool:
        asyncres = pool.starmap(get_features_web, job_args)

    if len(cached) > 0:
        asyncres.extend(cached)

    config.logger.info('feature extraction done! saving...')
    name = 'features.complex.all.' + str(len(job_args) + len(cached)) + '.pkl'
    joblib.dump(asyncres, OUTPUT_FOLDER + exp_folder + ds_folder + 'features/' + name)
    config.logger.info('done! file: ' + name)

def export_features_multithread(out_exp_folder, dataset, export_html_tags = True, force=False):

    try:

        verify_and_create_experiment_folders(out_exp_folder, dataset)

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
        {'EXP_FOLDER': 'exp010/', 'DATASET': 'microsoft', 'EXPORT_HTML': True, 'REPROCESS': True},
        #{'EXP_FOLDER': 'exp010/', 'DATASET': 'c3', 'EXPORT_HTML': True, 'REPROCESS': False},
    ]

    for p in params:
        export_features_multithread(p['EXP_FOLDER'], p['DATASET'], export_html_tags=p['EXPORT_HTML'], force=p['REPROCESS'])