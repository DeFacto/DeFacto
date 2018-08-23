from keras.preprocessing.sequence import pad_sequences
from sklearn.externals import joblib
import os
from sklearn.model_selection import train_test_split
import plotly.plotly as py
from trustworthiness.benchmark_utils import train_test_export_save_per_exp_type, export_chart_scatter
from trustworthiness.benchmark_utils import append_annotation_style
from config import DeFactoConfig
from defacto.definitions import OUTPUT_FOLDER, HEADER, CONFIGS_HIGH_DIMEN, EXP_5_CLASSES_LABEL, EXP_3_CLASSES_LABEL, \
    EXP_2_CLASSES_LABEL, TEST_SIZE, RANDOM_STATE
from trustworthiness.feature_extractor import likert2bin, likert2tri
import numpy as np
import math
import plotly.graph_objs as go

def benchmark_html_sequence(X, y5, y3, y2, exp_folder, ds_folder, random_state, test_size, pads):
    try:
        config.logger.info('benchmark_html_sequence()')

        maxsent = -1
        for e in X:
            maxsent = len(e) if len(e) > maxsent else maxsent
        print('max_sent: ', maxsent)
        maxpad = 1000

        out_performance_file = 'out_performance_html2seq.txt'
        subfolder = 'html2seq/'

        with open(OUTPUT_FOLDER + exp_folder + ds_folder + 'benchmark/' + subfolder + 'out_performance_file', "w") as file_log:
            file_log.write(HEADER)
            nb_2 = []
            nb_3 = []
            nb_5 = []
            bnb_2 = []
            bnb_3 = []
            bnb_5 = []
            agg_2 = []
            agg_3 = []
            agg_5 = []
            k_2 = []
            k_3 = []
            k_5 = []

            for maxpad in pads:
                XX = pad_sequences(X, maxlen=maxpad, dtype='int', padding='pre', truncating='pre', value=0)
                X_train, X_test, y_train_5, y_test_5 = train_test_split(XX, y5, test_size=test_size,
                                                                        random_state=random_state)
                X_train, X_test, y_train_3, y_test_3 = train_test_split(XX, y3, test_size=test_size,
                                                                        random_state=random_state)
                X_train, X_test, y_train_2, y_test_2 = train_test_split(XX, y2, test_size=test_size,
                                                                        random_state=random_state)

                ## no need to perform pre-processing nor tokenization here
                # tfidf = TfidfVectorizer(preprocessor=lambda x: x, tokenizer=lambda x: x)
                # X_train = tfidf.fit_transform(X_train)
                # X_test = tfidf.transform(X_test)

                # ==========================================================================================================
                # NB
                # ==========================================================================================================
                cls, params, search_method = CONFIGS_HIGH_DIMEN[0]
                nb_5 = train_test_export_save_per_exp_type(cls, 'nb', params, search_method, X_train, X_test, y_train_5,
                                                           y_test_5, EXP_5_CLASSES_LABEL, maxpad, nb_5, file_log,
                                                           subfolder, exp_folder, ds_folder)
                nb_3 = train_test_export_save_per_exp_type(cls, 'nb', params, search_method, X_train, X_test, y_train_3,
                                                           y_test_3, EXP_3_CLASSES_LABEL, maxpad, nb_3, file_log,
                                                           subfolder, exp_folder, ds_folder)
                nb_2 = train_test_export_save_per_exp_type(cls, 'nb', params, search_method, X_train, X_test, y_train_2,
                                                           y_test_2, EXP_2_CLASSES_LABEL, maxpad, nb_2, file_log,
                                                           subfolder, exp_folder, ds_folder)
                # ==========================================================================================================
                # BernoulliNB
                # ==========================================================================================================
                cls, params, search_method = CONFIGS_HIGH_DIMEN[1]
                bnb_5 = train_test_export_save_per_exp_type(cls, 'sgd', params, search_method, X_train, X_test,
                                                            y_train_5, y_test_5, EXP_5_CLASSES_LABEL, maxpad, bnb_5,
                                                            file_log, subfolder, exp_folder, ds_folder)
                bnb_3 = train_test_export_save_per_exp_type(cls, 'sgd', params, search_method, X_train, X_test,
                                                            y_train_3, y_test_3, EXP_3_CLASSES_LABEL, maxpad, bnb_3,
                                                            file_log, subfolder, exp_folder, ds_folder)
                bnb_2 = train_test_export_save_per_exp_type(cls, 'sgd', params, search_method, X_train, X_test,
                                                            y_train_2, y_test_2, EXP_2_CLASSES_LABEL, maxpad, bnb_2,
                                                            file_log, subfolder, exp_folder, ds_folder)
                # ==========================================================================================================
                # K-means
                # ==========================================================================================================
                pca = PCA()
                X_tr_pca = pca.fit_transform(X_train)
                X_te_pca = pca.transform(X_test)
                cls, params, search_method = CONFIGS_HIGH_DIMEN[3]
                k_5 = train_test_export_save_per_exp_type(cls, 'kmeans', params, search_method, X_tr_pca, X_te_pca,
                                                          y_train_5, y_test_5, EXP_5_CLASSES_LABEL, maxpad, k_5,
                                                          file_log, subfolder, exp_folder, ds_folder)
                k_3 = train_test_export_save_per_exp_type(cls, 'kmeans', params, search_method, X_tr_pca, X_te_pca,
                                                          y_train_3, y_test_3, EXP_3_CLASSES_LABEL, maxpad, k_3,
                                                          file_log, subfolder, exp_folder, ds_folder)
                k_2 = train_test_export_save_per_exp_type(cls, 'kmeans', params, search_method, X_tr_pca, X_te_pca,
                                                          y_train_2, y_test_2, EXP_2_CLASSES_LABEL, maxpad, k_2,
                                                          file_log, subfolder, exp_folder, ds_folder)

                # ==========================================================================================================
                # AgglomerativeClustering
                # ==========================================================================================================
                cls, params, search_method = CONFIGS_HIGH_DIMEN[4]
                agg_5 = train_test_export_save_per_exp_type(cls, 'agg', params, search_method, X_train, X_test,
                                                            y_train_5, y_test_5, EXP_5_CLASSES_LABEL, maxpad, agg_5,
                                                            file_log, subfolder, exp_folder, ds_folder)
                agg_3 = train_test_export_save_per_exp_type(cls, 'agg', params, search_method, X_train, X_test,
                                                            y_train_3, y_test_3, EXP_3_CLASSES_LABEL, maxpad, agg_3,
                                                            file_log, subfolder, exp_folder, ds_folder)
                agg_2 = train_test_export_save_per_exp_type(cls, 'agg', params, search_method, X_train, X_test,
                                                            y_train_2, y_test_2, EXP_2_CLASSES_LABEL, maxpad, agg_2,
                                                            file_log, subfolder, exp_folder, ds_folder)

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

                file_log.flush()

        title = 'HTML2Seq: performance varying window size'
        x_title = 'Padding window size (log scale)'
        y_title = 'F1-measure (average)'
        export_chart_scatter(pads, ['NB', 'BNB', 'K-means', 'AGG'],
                             [np.array(nb_5)[:, 2], np.array(bnb_5)[:, 2], np.array(k_5)[:, 2], np.array(agg_5)[:, 2]],
                             [np.array(nb_3)[:, 2], np.array(bnb_3)[:, 2], np.array(k_3)[:, 2], np.array(agg_3)[:, 2]],
                             [np.array(nb_2)[:, 2], np.array(bnb_2)[:, 2], np.array(k_2)[:, 2], np.array(agg_2)[:, 2]],
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
                2500, 2600,
                2700, 2800, 2900, 3000, 3500, 4000, 4500, 5000, 6000, 7000, 8000, 9000, 10000]



        config.logger.info('html2seq feature benchmark')
        #(features_seq, y5, y3, y2), le = get_html2sec_features(exp, ds)

        ds = 'microsoft/'
        K1 = '86'
        exp = 'exp010/'

        features_html2seq_file = OUTPUT_FOLDER + exp + ds + 'features/' + 'features.html2seq.' + K1 + '.pkl'
        X_html2seq_f = joblib.load(features_html2seq_file)
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

        for x in X_html2seq_f:
            hash = x[0]
            y = x[1]
            x = np.array(x)
            html2seq = np.delete(x, np.s_[0:2], axis=0)
            X_html2seq.append(html2seq)
            y5_html2seq.append(y)
            y3_html2seq.append(likert2tri(int(y)))
            y2_html2seq.append(likert2bin(int(y)))


        benchmark_html_sequence(X_html2seq, y5_html2seq, y3_html2seq, y2_html2seq, exp, ds, RANDOM_STATE, TEST_SIZE, PADS)

    except:
        raise