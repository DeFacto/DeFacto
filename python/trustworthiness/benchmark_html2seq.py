from keras.preprocessing.sequence import pad_sequences
from sklearn.decomposition import PCA, TruncatedSVD
from sklearn.exceptions import UndefinedMetricWarning
from sklearn.externals import joblib
import os
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer, CountVectorizer
from sklearn.model_selection import train_test_split
import plotly.plotly as py
from sklearn.preprocessing import StandardScaler

from trustworthiness.benchmark_utils import train_test_export_save_per_exp_type, export_chart_scatter
from trustworthiness.benchmark_utils import append_annotation_style
from config import DeFactoConfig
from defacto.definitions import OUTPUT_FOLDER, HEADER, CONFIGS_HIGH_DIMEN_CLASSIFICATION, EXP_5_CLASSES_LABEL, \
    EXP_3_CLASSES_LABEL, \
    EXP_2_CLASSES_LABEL, TEST_SIZE, RANDOM_STATE, CONFIGS_HIGH_DIMEN_REGRESSION, ENC_TAGS, HEADER_REGRESSION
from trustworthiness.feature_extractor import likert2bin, likert2tri
import numpy as np
import math
import plotly.graph_objs as go

import warnings

from trustworthiness.util import get_logger

warnings.filterwarnings("ignore", category=UndefinedMetricWarning)

def benchmark_html_sequence(X, y5, y3, y2, exp_folder, ds_folder, random_state, test_size, pads):
    try:
        config.logger.info('benchmark_html_sequence()')
        maxsent = -1
        for e in X:
            maxsent = len(e) if len(e) > maxsent else maxsent
        config.logger.debug('max_sent: ' + str(maxsent))

        subfolder = 'html2seq/'
        path = OUTPUT_FOLDER + exp_folder + ds_folder + 'benchmark/' + subfolder
        #logger = get_logger('defacto.trust.benchmark.html2seq', path)

        scaler = StandardScaler()


        config.logger.info('regression benchmark...')
        with open(path + 'results_regression.txt', "w") as file_log:
            file_log.write(HEADER_REGRESSION)
            mlpr_5, svr_5 = [], []

            for maxpad in pads:
                config.logger.debug('padding ' + str(maxpad))
                XX = pad_sequences(X, maxlen=maxpad, dtype='int', padding='pre', truncating='pre', value=0)
                X_train_5, X_test_5, y_train_5, y_test_5 = train_test_split(XX, y5, test_size=test_size, random_state=random_state)
                X_train_3, X_test_3, y_train_3, y_test_3 = train_test_split(XX, y3, test_size=test_size, random_state=random_state)
                X_train_2, X_test_2, y_train_2, y_test_2 = train_test_split(XX, y2, test_size=test_size, random_state=random_state)

                assert np.all(X_train_5 == X_train_3)
                assert np.all(X_test_5 == X_test_2)
                X_train = X_train_5
                X_test = X_test_5

                #c = CountVectorizer(vocabulary=joblib.load(ENC_TAGS))
                #X_train = c.fit_transform(X_train)
                #X_test = c.transform(X_test)

                config.logger.info('vectorizing...')
                tfidf = TfidfVectorizer(preprocessor=lambda x: str(x), tokenizer=lambda x: str(x), ngram_range=(1, 3))
                X_train = tfidf.fit_transform(X_train)
                X_test = tfidf.transform(X_test)
                config.logger.info('TF-IDF ok.')


                #config.logger.debug('scaling..')
                #scaler.fit(X_train)
                #X_train = scaler.transform(X_train)
                #X_test = scaler.transform(X_test)

                '''
                -------------------------------------------------------------------------------------------------------------------
                REGRESSION
                -------------------------------------------------------------------------------------------------------------------
                '''
                # ================
                # svm.SVR
                # ================
                cls, params, search_method = CONFIGS_HIGH_DIMEN_REGRESSION[0]
                mlpr_5, _ = train_test_export_save_per_exp_type(cls, 'linsvr', params, search_method, X_train, X_test, y_train_5,
                                                              y_test_5, EXP_5_CLASSES_LABEL, maxpad, mlpr_5, file_log, subfolder, exp_folder, ds_folder)
                file_log.flush()
                # ================
                # SVR
                # ================
                cls, params, search_method = CONFIGS_HIGH_DIMEN_REGRESSION[1]
                svr_5, _ = train_test_export_save_per_exp_type(cls, 'ridge', params, search_method, X_train, X_test, y_train_5,
                                                              y_test_5, EXP_5_CLASSES_LABEL, maxpad, svr_5, file_log, subfolder, exp_folder, ds_folder)
                file_log.flush()

        config.logger.info('classification benchmark...')
        with open(path + 'results_classification.txt', "w") as file_log:
            file_log.write(HEADER)
            nb_2, bnb_2, svc_2, mlp_2 = [], [], [], []
            nb_3, bnb_3, svc_3, mlp_3 = [], [], [], []

            for maxpad in pads:
                config.logger.debug('padding ' + str(maxpad))
                XX = pad_sequences(X, maxlen=maxpad, dtype='int', padding='pre', truncating='pre', value=0)
                X_train_5, X_test_5, y_train_5, y_test_5 = train_test_split(XX, y5, test_size=test_size, random_state=random_state)
                X_train_3, X_test_3, y_train_3, y_test_3 = train_test_split(XX, y3, test_size=test_size, random_state=random_state)
                X_train_2, X_test_2, y_train_2, y_test_2 = train_test_split(XX, y2, test_size=test_size, random_state=random_state)

                assert np.all(X_train_5 == X_train_3)
                assert np.all(X_test_5 == X_test_2)
                X_train = X_train_5
                X_test = X_test_5

                config.logger.info('vectorizing...')
                tfidf = TfidfVectorizer(preprocessor=lambda x: str(x), tokenizer=lambda x: str(x), ngram_range=(1, 3))
                X_train = tfidf.fit_transform(X_train)
                X_test = tfidf.transform(X_test)
                config.logger.info('TF-IDF ok.')


                '''
                try CountVectorizer(ngram_range=(1, 3), min_df=0.1, max_df=0.8) later...
                '''

                '''
                -------------------------------------------------------------------------------------------------------------------
                CLASSIFICATION
                -------------------------------------------------------------------------------------------------------------------
                '''

                # ================
                # NB
                # ================
                cls, params, search_method = CONFIGS_HIGH_DIMEN_CLASSIFICATION[0]
                nb_3, _ = train_test_export_save_per_exp_type(cls, 'nb', params, search_method, X_train, X_test, y_train_3, y_test_3,
                                                              EXP_3_CLASSES_LABEL, maxpad, nb_3, file_log, subfolder, exp_folder, ds_folder)
                nb_2, _ = train_test_export_save_per_exp_type(cls, 'nb', params, search_method, X_train, X_test, y_train_2, y_test_2,
                                                              EXP_2_CLASSES_LABEL, maxpad, nb_2, file_log, subfolder, exp_folder, ds_folder)

                file_log.flush()
                # ================
                # BernoulliNB
                # ================
                cls, params, search_method = CONFIGS_HIGH_DIMEN_CLASSIFICATION[1]
                bnb_3, _ = train_test_export_save_per_exp_type(cls, 'bnb', params, search_method, X_train, X_test, y_train_3, y_test_3,
                                                               EXP_3_CLASSES_LABEL, maxpad, bnb_3, file_log, subfolder, exp_folder, ds_folder)
                bnb_2, _ = train_test_export_save_per_exp_type(cls, 'bnb', params, search_method, X_train, X_test, y_train_2, y_test_2,
                                                               EXP_2_CLASSES_LABEL, maxpad, bnb_2, file_log, subfolder, exp_folder, ds_folder)

                file_log.flush()
                # ================
                # SVC.svm
                # ================
                cls, params, search_method = CONFIGS_HIGH_DIMEN_CLASSIFICATION[2]
                mlp_3, _ = train_test_export_save_per_exp_type(cls, 'ridge', params, search_method, X_train, X_test, y_train_3, y_test_3,
                                                               EXP_3_CLASSES_LABEL, maxpad, mlp_3, file_log, subfolder, exp_folder, ds_folder)
                mlp_2, _ = train_test_export_save_per_exp_type(cls, 'ridge', params, search_method, X_train, X_test, y_train_2, y_test_2,
                                                               EXP_2_CLASSES_LABEL, maxpad, mlp_2, file_log, subfolder, exp_folder, ds_folder)

                file_log.flush()
                # ================
                # K-means
                # ================
                #svd = TruncatedSVD(n_components=100, n_iter=7, random_state=42)
                #X_tr_pca = svd.fit_transform(X_train)
                #X_te_pca = svd.transform(X_test)
                #pca = PCA()
                #X_tr_pca = pca.fit_transform(X_train)
                #X_te_pca = pca.transform(X_test)

                # ================
                # SVC
                # ================
                cls, params, search_method = CONFIGS_HIGH_DIMEN_CLASSIFICATION[3]
                svc_3, _ = train_test_export_save_per_exp_type(cls, 'svc', params, search_method, X_train, X_test, y_train_3, y_test_3,
                                                               EXP_3_CLASSES_LABEL, maxpad, svc_3, file_log, subfolder, exp_folder, ds_folder)
                svc_2, _ = train_test_export_save_per_exp_type(cls, 'svc', params, search_method, X_train, X_test, y_train_2, y_test_2,
                                                               EXP_2_CLASSES_LABEL, maxpad, svc_2, file_log, subfolder, exp_folder, ds_folder)

                file_log.flush()

                # ==========================================================================================================
                # LSTM
                # ==========================================================================================================
                '''
                n_timesteps = 5

                X = np.array(X)  # .reshape(1, n_timesteps, 1)
                y = np.array(y)  # .reshape(1, n_timesteps, 1)

                model = Sequential()
                print(X.shape)
                model.add(LSTM(20, input_shape=(X.shape[0], 1), return_sequences=True))
                model.add(TimeDistributed(Dense(4, activation='sigmoid')))
                model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['acc'])
                print(model.summary())
                model.fit(X, y, epochs=10, batch_size=32, verbose=2, validation_split=0.33)
                # yhat = model.predict_classes(X, verbose=2)

                # for i in range(n_timesteps):
                #   print('Expected:', y[0, i], 'Predicted', yhat[0, i])
                '''

        config.logger.info('exporting graphs (classification)')
        title = 'HTML2Seq: performance varying window size'
        x_title = 'Padding window size (log scale)'
        y_title = 'F1-measure (average)'
        export_chart_scatter(pads, ['NB', 'BNB', 'SVC', 'SVM'],
                             [np.array(nb_3)[:, 2], np.array(bnb_3)[:, 2], np.array(svc_3)[:, 2], np.array(mlp_3)[:, 2]],
                             [np.array(nb_2)[:, 2], np.array(bnb_2)[:, 2], np.array(svc_2)[:, 2], np.array(mlp_2)[:, 2]],
                             'benchmark_html2seq', exp_folder, ds_folder, title, x_title, y_title)

    except Exception as e:
        config.logger.error(repr(e))
        raise


if __name__ == '__main__':
    try:
        config = DeFactoConfig()


        # benchmarking the best window for HTML2seq
        # HTML sequence windows
        PADS = [25, 50, 100, 175, 250, 500, 1000, 1250, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400,
                2500, 2600, 2700, 2800, 2900, 3000, 3500, 4000, 4500, 5000, 6000, 7000, 8000, 9000, 10000]


        config.logger.info('html2seq feature benchmark')
        #(features_seq, y5, y3, y2), le = get_html2sec_features(exp, ds)

        ds = 'microsoft/'
        #ds = 'c3/'

        K1 = '911'
        #K1 = '2977'

        exp = 'exp010/'

        features_html2seq_file = OUTPUT_FOLDER + exp + ds + 'features/' + 'features.html2seq.' + K1 + '.pkl'

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

        benchmark_html_sequence(X_html2seq, y5_html2seq, y3_html2seq, y2_html2seq, exp, ds, RANDOM_STATE, TEST_SIZE, PADS)

    except:
        raise