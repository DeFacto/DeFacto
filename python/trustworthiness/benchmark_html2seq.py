from keras.preprocessing.sequence import pad_sequences
from sklearn.decomposition import PCA, TruncatedSVD
from sklearn.exceptions import UndefinedMetricWarning
from sklearn.externals import joblib
import os
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer, CountVectorizer
from sklearn.model_selection import train_test_split
import plotly.plotly as py
from sklearn.preprocessing import StandardScaler, MinMaxScaler

from trustworthiness.benchmark_utils import train_test_export_save_per_exp_type, export_chart_scatter
from trustworthiness.benchmark_utils import append_annotation_style
from config import DeFactoConfig
from defacto.definitions import OUTPUT_FOLDER, HEADER_CLASSIFICATION, CONFIGS_HIGH_DIMEN_CLASSIFICATION, EXP_5_CLASSES_LABEL, \
    EXP_3_CLASSES_LABEL, \
    EXP_2_CLASSES_LABEL, TEST_SIZE, RANDOM_STATE, CONFIGS_HIGH_DIMEN_REGRESSION, ENC_TAGS, HEADER_REGRESSION
from trustworthiness.feature_extractor import likert2bin, likert2tri
import numpy as np
import math
import plotly.graph_objs as go
import dill as pickle # this is to import lambda functions
import warnings

from trustworthiness.util import get_logger

warnings.filterwarnings("ignore", category=UndefinedMetricWarning)

def benchmark_html_sequence(X_train_sca, X_test_sca, y5_train, y5_test, y3_train, y3_test, y2_train, y2_test, exp_folder, ds_folder, pads, n_grams):

    file_log_reg = None
    file_log_cla = None

    try:
        config.logger.info('benchmark_html_sequence()')
        subfolder = 'html2seq/'
        path = OUTPUT_FOLDER + exp_folder + ds_folder + 'benchmark/' + subfolder
        file_log_reg = open(path + 'results_regression.txt', "w")
        file_log_cla = open(path + 'results_classification.txt', "w")
        file_log_reg.write(HEADER_REGRESSION)
        file_log_cla.write(HEADER_CLASSIFICATION)

        mlpr_5, svr_5 = [], []
        nb_2, bnb_2, rf_2, rid_2 = [], [], [], []
        nb_3, bnb_3, rf_3, rid_3 = [], [], [], []


        for maxpad in pads:
            config.logger.info('pad = ' + str(maxpad))

            X_train_sca_pad = np.array([t[0:maxpad] for t in X_train_sca])
            X_test_sca_pad = np.array([t[0:maxpad] for t in X_test_sca])

            config.logger.info('vectorizing...')
            # works by adjusting the preprocessor and tokenizer via lambda functions. But I got problems to pickle this and use later...
            # --> tfidf = TfidfVectorizer(preprocessor=lambda x: str(x), tokenizer=lambda x: str(x), ngram_range=(1, n_grams))
            # so, by now I'll just get a workaround and convert the numbers to str before....
            tfidf = TfidfVectorizer(ngram_range=(1, N_GRAMS))
            X_train_s = [str(x) for x in X_train_sca_pad]
            X_test_s = [str(x) for x in X_test_sca_pad]

            X_train_vec_pad = tfidf.fit_transform(X_train_s)
            X_test_vec_pad = tfidf.transform(X_test_s)
            joblib.dump(tfidf, path + 'tfidf.vectorizer.' + str(maxpad) + '.pkl')
            config.logger.info('TF-IDF ok.')

            config.logger.info('regression benchmark')

            '''
            -------------------------------------------------------------------------------------------------------------------
            REGRESSION
            -------------------------------------------------------------------------------------------------------------------
            '''
            # ================
            # svm.SVR
            # ================
            cls, params, search_method = CONFIGS_HIGH_DIMEN_REGRESSION[0]
            mlpr_5, _ = train_test_export_save_per_exp_type(cls, 'linsvr', params, search_method, X_train_vec_pad, X_test_vec_pad, y5_train,
                                                            y5_test, EXP_5_CLASSES_LABEL, maxpad, mlpr_5, file_log_reg, subfolder, exp_folder, ds_folder)
            file_log_reg.flush()
            # ================
            # Ridge
            # ================
            cls, params, search_method = CONFIGS_HIGH_DIMEN_REGRESSION[1]
            svr_5, _ = train_test_export_save_per_exp_type(cls, 'ridge', params, search_method, X_train_vec_pad, X_test_vec_pad, y5_train,
                                                           y5_test, EXP_5_CLASSES_LABEL, maxpad, svr_5, file_log_reg, subfolder, exp_folder, ds_folder)
            file_log_reg.flush()

            '''
            -------------------------------------------------------------------------------------------------------------------
            CLASSIFICATION
            -------------------------------------------------------------------------------------------------------------------
            '''
            config.logger.info('classification benchmark')

            # ================
            # NB
            # ================
            cls, params, search_method = CONFIGS_HIGH_DIMEN_CLASSIFICATION[0]
            nb_3, _ = train_test_export_save_per_exp_type(cls, 'nb', params, search_method, X_train_vec_pad, X_test_vec_pad, y3_train, y3_test,
                                                          EXP_3_CLASSES_LABEL, maxpad, nb_3, file_log_cla, subfolder, exp_folder, ds_folder)
            nb_2, _ = train_test_export_save_per_exp_type(cls, 'nb', params, search_method, X_train_vec_pad, X_test_vec_pad, y2_train, y2_test,
                                                          EXP_2_CLASSES_LABEL, maxpad, nb_2, file_log_cla, subfolder, exp_folder, ds_folder)

            file_log_cla.flush()
            # ================
            # BernoulliNB
            # ================
            cls, params, search_method = CONFIGS_HIGH_DIMEN_CLASSIFICATION[1]
            bnb_3, _ = train_test_export_save_per_exp_type(cls, 'bnb', params, search_method, X_train_vec_pad, X_test_vec_pad, y3_train, y3_test,
                                                           EXP_3_CLASSES_LABEL, maxpad, bnb_3, file_log_cla, subfolder, exp_folder, ds_folder)
            bnb_2, _ = train_test_export_save_per_exp_type(cls, 'bnb', params, search_method, X_train_vec_pad, X_test_vec_pad, y2_train, y2_test,
                                                           EXP_2_CLASSES_LABEL, maxpad, bnb_2, file_log_cla, subfolder, exp_folder, ds_folder)

            file_log_cla.flush()
            # ================
            # RidgeClassifier
            # ================
            cls, params, search_method = CONFIGS_HIGH_DIMEN_CLASSIFICATION[2]
            rid_3, _ = train_test_export_save_per_exp_type(cls, 'ridge', params, search_method, X_train_vec_pad, X_test_vec_pad, y3_train, y3_test,
                                                           EXP_3_CLASSES_LABEL, maxpad, rid_3, file_log_cla, subfolder, exp_folder, ds_folder)
            rid_2, _ = train_test_export_save_per_exp_type(cls, 'ridge', params, search_method, X_train_vec_pad, X_test_vec_pad, y2_train, y2_test,
                                                           EXP_2_CLASSES_LABEL, maxpad, rid_2, file_log_cla, subfolder, exp_folder, ds_folder)

            file_log_cla.flush()

            # ================
            # RF
            # ================
            cls, params, search_method = CONFIGS_HIGH_DIMEN_CLASSIFICATION[3]
            rf_3, _ = train_test_export_save_per_exp_type(cls, 'rf', params, search_method, X_train_sca_pad, X_test_sca_pad, y3_train, y3_test,
                                                           EXP_3_CLASSES_LABEL, maxpad, rf_3, file_log_cla, subfolder, exp_folder, ds_folder)
            rf_2, _ = train_test_export_save_per_exp_type(cls, 'rf', params, search_method, X_train_sca_pad, X_test_sca_pad, y2_train, y2_test,
                                                           EXP_2_CLASSES_LABEL, maxpad, rf_2, file_log_cla, subfolder, exp_folder, ds_folder)

            file_log_cla.flush()

        file_log_reg.close()
        file_log_cla.close()
        config.logger.info('exporting graphs (classification)')
        title = 'HTML2Seq: performance varying window size'
        x_title = 'Padding window size (log scale)'
        y_title = 'F1-measure (average)'
        export_chart_scatter(pads, ['NB', 'BNB', 'Ridge', 'RF'],
                             [np.array(nb_3)[:, 2], np.array(bnb_3)[:, 2], np.array(rf_3)[:, 2], np.array(rid_3)[:, 2]],
                             [np.array(nb_2)[:, 2], np.array(bnb_2)[:, 2], np.array(rf_2)[:, 2], np.array(rid_2)[:, 2]],
                             'benchmark_html2seq', exp_folder, ds_folder, title, x_title, y_title)

        # ================
        # K-means
        # ================
        # svd = TruncatedSVD(n_components=100, n_iter=7, random_state=42)
        # X_tr_pca = svd.fit_transform(X_train_sca)
        # X_te_pca = svd.transform(X_test_sca)
        # pca = PCA()
        # X_tr_pca = pca.fit_transform(X_train_sca)
        # X_te_pca = pca.transform(X_test_sca)

    except Exception as e:
        config.logger.error(repr(e))
        if file_log_reg is not None and file_log_reg.closed == False:
            file_log_reg.close()
        if file_log_cla is not None and file_log_cla.closed == False:
            file_log_cla.close()
        raise


if __name__ == '__main__':
    try:
        config = DeFactoConfig()


        # benchmarking the best window for HTML2seq
        # HTML sequence windows
        PADS = [25, 50, 100, 175, 250, 500, 1000, 1250, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400,
                2500, 2600, 2700, 2800, 2900, 3000, 3500, 4000, 4500, 5000, 6000, 7000, 8000, 9000, 10000]

        N_GRAMS = 3

        config.logger.info('html2seq feature benchmark')
        #(features_seq, y5, y3, y2), le = get_html2sec_features(exp, ds)

        #ds = 'microsoft/'
        ds = 'c3/'

        #K1 = '911'
        K1 = '2977'

        exp = 'exp010/'

        features_html2seq_file = OUTPUT_FOLDER + exp + ds + 'features/' + 'features.split.html2seq.' + K1 + '.pkl'

        X_html2seq = []
        y5_html2seq = []
        y3_html2seq = []
        y2_html2seq = []
        if ds == 'microsoft/':
            label_likert = 'likert'
        elif ds == 'c3/':
            label_likert = 'likert_mode'
        else:
            raise Exception('not supported! ' + ds)

        if (not os.path.exists(features_html2seq_file.replace('.pkl', '') + '.temp.X.pkl')) and \
            (not os.path.exists(features_html2seq_file.replace('.pkl', '') + '.temp.y.pkl')):
            config.logger.info('loading file...')
            X_html2seq_f = joblib.load(features_html2seq_file)
            config.logger.info('ok')
            for x in X_html2seq_f:
                #hash = x[0]
                y = x[1]
                y5_html2seq.append(y)
                y3_html2seq.append(likert2tri(int(y)))
                y2_html2seq.append(likert2bin(int(y)))
                x = np.array(x)
                html2seq = np.delete(x, np.s_[0:2], axis=0)
                X_html2seq.append(html2seq)

            joblib.dump(X_html2seq, features_html2seq_file.replace('.pkl', '') + '.temp.X.pkl')
            joblib.dump((y5_html2seq, y3_html2seq, y2_html2seq), features_html2seq_file.replace('.pkl', '') + '.temp.y.pkl')

        else:
            X_html2seq = joblib.load(features_html2seq_file.replace('.pkl', '') + '.temp.X.pkl')
            y5_html2seq, y3_html2seq, y2_html2seq = joblib.load(features_html2seq_file.replace('.pkl', '') + '.temp.y.pkl')

        ## getting the max sentence
        #maxsent = -1
        #for e in X_html2seq:
        #    maxsent = len(e) if len(e) > maxsent else maxsent
        #config.logger.debug('max_sent: ' + str(maxsent))

        # padding according to the max pad (saves resources)
        maxsent = PADS[len(PADS)-1]

        # padding the max sentence
        config.logger.debug('padding ' + str(maxsent))
        XX = pad_sequences(X_html2seq, maxlen=maxsent, dtype='int', padding='post', truncating='post', value=0)

        # splits
        X_train_5, X_test_5, y_train_5, y_test_5 = train_test_split(XX, y5_html2seq, test_size=TEST_SIZE, random_state=RANDOM_STATE)
        X_train_3, X_test_3, y_train_3, y_test_3 = train_test_split(XX, y3_html2seq, test_size=TEST_SIZE, random_state=RANDOM_STATE)
        X_train_2, X_test_2, y_train_2, y_test_2 = train_test_split(XX, y2_html2seq, test_size=TEST_SIZE, random_state=RANDOM_STATE)

        assert np.all(X_train_5 == X_train_3)
        assert np.all(X_test_5 == X_test_2)
        X_train = X_train_5
        X_test = X_test_5

        config.logger.info('scaling')
        scaler = MinMaxScaler(feature_range=(0, 1))
        X_train_scaled = scaler.fit_transform(X_train)
        X_test_scaled = scaler.transform(X_test)
        path = OUTPUT_FOLDER + exp + ds + 'benchmark/html2seq/'
        joblib.dump(scaler, path + 'min-max.scaler.pkl')
        config.logger.info('min-max ok.')


        benchmark_html_sequence(X_train_scaled, X_test_scaled, y_train_5, y_test_5, y_train_3, y_test_3, y_train_2, y_test_2, exp, ds, PADS, N_GRAMS)

    except:
        raise