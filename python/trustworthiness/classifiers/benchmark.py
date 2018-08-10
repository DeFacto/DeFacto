import os

from bs4 import BeautifulSoup
from keras import Sequential
from keras.layers import LSTM, TimeDistributed, Dense
from keras.preprocessing.sequence import pad_sequences
from sklearn.cross_validation import train_test_split
from pathlib import Path

from sklearn.grid_search import GridSearchCV
from sklearn.metrics import precision_recall_fscore_support

import plotly.plotly as py
import plotly.graph_objs as go
import math
from sklearn.cluster import KMeans
from sklearn.decomposition import PCA

from defacto.definitions import WEB_CREDIBILITY_DATA_PATH
from trustworthiness.util import print_report
from trustworthiness.feature_extractor import *

from sklearn.metrics import confusion_matrix
from sklearn.model_selection import train_test_split, GridSearchCV
from itertools import product

from sklearn.neural_network import MLPClassifier

from config import DeFactoConfig

from sklearn.naive_bayes import *
from sklearn.dummy import *
from sklearn.ensemble import *
from sklearn.neighbors import *
from sklearn.tree import *
from sklearn.calibration import *
from sklearn.linear_model import *
from sklearn.svm import *
from sklearn.externals import joblib
import numpy as np
import matplotlib.pyplot as plt
from sklearn.preprocessing import StandardScaler

__author__ = "Diego Esteves"
__copyright__ = "Copyright 2018, DeFacto Project"
__credits__ = ["Diego Esteves", "Aniketh Reddy", "Piyush Chawla"]
__license__ = "Apache"
__version__ = "0.0.1"
__email__ = "diegoesteves@gmail.com"
__status__ = "Dev"


LINE_TEMPLATE = '%s\t%s\t%s\t%s\t%.3f\t%.3f\t%.3f\t%d\t%.3f\n'
likert_labels = {1: 'non-credible', 2: 'low', 3: 'neutral', 4: 'likely', 5: 'credible'}
likert_labels_short = {0: 'non-credible', 1: 'credible'}
HEADER = 'cls\texperiment_type\tpadding\tklass\tprecision\trecall\tf-measure\tsupport\trate\n'
np.random.seed(7)
config = DeFactoConfig()
scaler = StandardScaler()

def matplotlib_to_plotly(cmap, pl_entries):
    h = 1.0 / (pl_entries - 1)
    pl_colorscale = []

    for k in range(pl_entries):
        C = map(np.uint8, np.array(cmap(k * h)[:3]) * 255)
        pl_colorscale.append([k * h, 'rgb' + str((C[0], C[1], C[2]))])

    return pl_colorscale

def k_means(XX, y):
    n_digits = len(np.unique(y))
    pca = PCA(n_components=n_digits).fit(XX)
    reduced_data = PCA(n_components=n_digits).fit_transform(XX)
    #kmeans = KMeans(init=pca.components_, n_clusters=n_digits, n_init=1)
    kmeans = KMeans(n_clusters=n_digits)
    kmeans.fit(reduced_data)
    # Step size of the mesh. Decrease to increase the quality of the VQ.
    h = .02  # point in the mesh [x_min, x_max]x[y_min, y_max].

    # Plot the decision boundary. For that, we will assign a color to each
    x_min, x_max = reduced_data[:, 0].min() - 1, reduced_data[:, 0].max() + 1
    y_min, y_max = reduced_data[:, 1].min() - 1, reduced_data[:, 1].max() + 1
    print(x_min, x_max, y_min, y_max)
    xx, yy = np.meshgrid(np.arange(x_min, x_max, h), np.arange(y_min, y_max, h))

    # Obtain labels for each point in mesh. Use last trained model.
    Z = kmeans.predict(np.c_[xx.ravel(), yy.ravel()])

    # Put the result into a color plot
    Z = Z.reshape(xx.shape)

    back = go.Heatmap(x=xx[0][:len(Z)],
                      y=xx[0][:len(Z)],
                      z=Z,
                      showscale=False,
                      colorscale=matplotlib_to_plotly(plt.cm.Paired, len(Z)))

    markers = go.Scatter(x=reduced_data[:, 0],
                         y=reduced_data[:, 1],
                         showlegend=False,
                         mode='markers',
                         marker=dict(
                             size=3, color='black'))

    # Plot the centroids as a white
    centroids = kmeans.cluster_centers_
    center = go.Scatter(x=centroids[:, 0],
                        y=centroids[:, 1],
                        showlegend=False,
                        mode='markers',
                        marker=dict(
                            size=10, color='white'))
    data = [back, markers, center]

    layout = dict(title='K-means clustering on the digits dataset (PCA-reduced data)<br>'
                             'Centroids are marked with white',
                       xaxis=dict(ticks='', showticklabels=False,
                                  zeroline=False),
                       yaxis=dict(ticks='', showticklabels=False,
                                  zeroline=False))
    fig = dict(data=data, layout=layout)
    py.image.save_as(fig, filename='clusters.png')
    #py.iplot(fig)

def __append_annotation_style(x, y, extra_text_y=None):
    if extra_text_y is not None and extra_text_y != '':
        text='{0:.3f}'.format(y) + ' - size: ' + str(extra_text_y)
    else:
        text='{0:.3f}'.format(y)
    return dict(
            x=x,
            y=y,
            xref='x',
            yref='y',
            text=text,
            showarrow=True,
            font=dict(
                family='Helvetica',
                size=11,
                color='#ffffff'
            ),
            align='center',
            arrowhead=2,
            arrowsize=1,
            arrowwidth=2,
            arrowcolor='#636363',
            ax=20,
            ay=-30,
            bordercolor='#c7c7c7',
            borderwidth=2,
            borderpad=4,
            bgcolor='#ff7f0e',
            opacity=0.8
        )

def get_annotation_from_max(traces, paddings, x_labels):

    annotations = []
    for trace in traces:
        max_trace=-999999
        index=-1
        label_y = None
        for i in range(len(trace)):
            if trace[i] > max_trace:
                label_y, max_trace, index = x_labels[i], trace[i], i

        annotations.append(__append_annotation_style(paddings[index], max_trace, extra_text_y=label_y))

    return annotations

def export_chart_bar_likert_bin(x, y, filename, exp_folder, title, x_title, y_title, annotation_threshold):
    try:
        trace0 = go.Bar(
            x=x,
            y=y,
            text=y,
            marker=dict(
                color=[BAR_COLOR] * len(x)),
            opacity=0.8
        )

        data = [trace0]

        layout = dict(title=title,
                        xaxis=dict(title=x_title, showticklabels=True, showline=True,
                                   autorange=True, showgrid=True, zeroline=True, gridcolor='#bdbdbd'),
                        yaxis=dict(title=y_title, showticklabels=True, showline=True, showgrid=True, gridcolor='#bdbdbd',
                                   range=[0, 1.0]),
                        font=dict(family='Helvetica', size=14)
                        )

        annotations = []
        for i in range(0, len(x)):
            if y[i] >= annotation_threshold:
                annotations.append(__append_annotation_style(x[i], y[i]))

        layout['annotations'] = annotations
        fig = go.Figure(data=data, layout=layout)

        _path = config.dir_output + exp_folder + 'graphs/'
        if not os.path.exists(_path):
            os.mkdir(_path)

        py.image.save_as(fig, filename=_path + filename)

    except Exception as e:
        raise e

def export_chart_scatter_likert_bin(x, y_labels, y_likert_f1, y_bin_f1, filename, exp_folder, ds_folder, title, x_title, y_title, log_mode=True):

    try:
        if log_mode == True:
            x_labels = x.copy()
            x = [math.log(pad) for pad in x]
        line_width=1
        mode='lines+markers'
        data_likert = []
        data_bin = []
        dash='dash' # dash options include 'dash', 'dot', and 'dashdot'

        assert len(y_likert_f1) == len(y_bin_f1)
        assert len(y_likert_f1) <= len(SERIES_COLORS)

        i=0
        for trace in y_likert_f1:
            s = go.Scatter(
                x=x,
                y=trace,
                text=trace,
                name=y_labels[i],
                mode=mode,
                line=dict(
                    color=(SERIES_COLORS[i]),
                    width=line_width))
            data_likert.append(s)
            i += 1

        i=0
        for trace in y_bin_f1:
            s = go.Scatter(
                x=x,
                y=trace,
                text=trace,
                name=y_labels[i],
                mode=mode,
                line=dict(
                    color=(SERIES_COLORS[i]),
                    width=line_width,
                    dash=dash))
            data_bin.append(s)
            i += 1

        y_likert = np.array(y_likert_f1)
        y_bin = np.array(y_bin_f1)

        # Edit the layout
        layout_1 = dict(title=title,
                        xaxis=dict(title=x_title, showticklabels=True, showline=True,
                                 autorange=True, showgrid=True, zeroline=True, gridcolor='#bdbdbd'),
                        yaxis=dict(title=y_title, showticklabels=True, showline=True, autorange=True),
                        show_legend=True,
                        legend=dict(orientation='h',
                                  x=math.log(1) if log_mode == True else 1,
                                  y=-20,
                                  bordercolor='#808080',
                                  borderwidth=2
                                  ),
                        annotations=get_annotation_from_max(y_likert, x, x_labels),
                        font=dict(family='Helvetica', size=14)
                        )
        layout_2 = layout_1.copy()
        layout_2['annotations']=get_annotation_from_max(y_bin, x, x_labels)
        #from plotly import tools
        #fig = tools.make_subplots(rows=2, cols=1, subplot_titles=('Likert Scale',
          #                                                        'Non-credible x Credible'))
        #fig.append_trace(trace0, 1, 1)
        #fig.append_trace(trace1, 1, 1)
        #fig.append_trace(trace2, 1, 1)
        #fig.append_trace(trace3, 1, 1)

        #fig.append_trace(trace4, 2, 1)
        #fig.append_trace(trace5, 2, 1)
        #fig.append_trace(trace6, 2, 1)
        #fig.append_trace(trace7, 2, 1)

        #fig['layout'].update = layout

        fig = dict(data=data_likert, layout=layout_1)
        #py.plot(fig, filename='paddings_f1')

        _path = WEB_CREDIBILITY_DATA_PATH + exp_folder + ds_folder + 'graphs/'
        if not os.path.exists(_path):
            os.mkdir(_path)

        py.image.save_as(fig, filename=_path + filename + '_likert.png')

        fig = dict(data=data_bin, layout=layout_2)
        py.image.save_as(fig, filename=_path + filename + '_bin.png')

    except Exception as e:
        raise e

def report(results, n_top=3):
    for i in range(1, n_top + 1):
        candidates = np.flatnonzero(results['rank_test_score'] == i)
        for candidate in candidates:
            print("Model with rank: {0}".format(i))
            print("Mean validation score: {0:.3f} (std: {1:.3f})".format(
                  results['mean_test_score'][candidate],
                  results['std_test_score'][candidate]))
            print("Parameters: {0}".format(results['params'][candidate]))
            print("")

def test(clf, X_test, y_test, out, padding, cls_label, experiment_type, file_log, subfolder, exp_folder, ds_folder):
    try:
        if isinstance(clf, str):
            clf=joblib.load(WEB_CREDIBILITY_DATA_PATH + exp_folder + ds_folder + 'models/' + subfolder + clf)

        predicted = clf.predict(X_test)
        p_avg, r_avg, f_avg, s_avg = precision_recall_fscore_support(y_test, predicted, average='weighted')
        out.append([p_avg, r_avg, f_avg])
        config.logger.info('padding: %s cls: %s exp_type: %s f1: %.3f' % (padding, cls_label, experiment_type, f_avg))

        # file logging details
        p, r, f, s = precision_recall_fscore_support(y_test, predicted)
        if experiment_type == 'bin':
            tn, fp, fn, tp =confusion_matrix(y_test, predicted).ravel()
            fpr=fp/(fp+tn)
            fnr=fn/(tp+fn)

            unique, counts = np.unique(y_test, return_counts=True)
            tot = counts[0] + counts[1]

            rate_avg=np.average([fpr, fnr], weights=[counts[0]/tot, counts[1]/tot])
            #fpr_0, tpr_0, thresholds_0 = roc_curve(y_test, predicted, pos_label=0)
            #fpr_1, tpr_1, thresholds_1 = roc_curve(y_test, predicted, pos_label=1)

            file_log.write(LINE_TEMPLATE % (cls_label, experiment_type, padding, likert_labels_short.get(0), p[0], r[0], f[0], s[0], fpr))
            file_log.write(LINE_TEMPLATE % (cls_label, experiment_type, padding, likert_labels_short.get(1),  p[1], r[1], f[1], s[1], fnr))
            file_log.write(LINE_TEMPLATE % (cls_label, experiment_type, padding, 'average', p_avg, r_avg, f_avg, s[0]+s[1], rate_avg))

        else:
            for i in range(len(p)):
                file_log.write(LINE_TEMPLATE % (cls_label, experiment_type, padding, likert_labels.get(i+1), p[i], r[i], f[i], s[i], 0))

        return out

    except Exception as e:
        config.logger.error(repr(e))
        raise

def train_test_export_save(clf, X_train, y_train, X_test, y_test, out, cls_label, padding, experiment_type,
                           file_log, subfolder, exp_folder, ds_folder):

    clf.fit(X_train, y_train)
    file = BENCHMARK_FILE_NAME_TEMPLATE % (cls_label.lower(), padding, experiment_type)
    _path = WEB_CREDIBILITY_DATA_PATH + exp_folder + ds_folder + 'models/' + subfolder
    if not os.path.exists(_path):
        os.mkdir(_path)
    joblib.dump(clf, _path + file)

    return test(clf, X_test, y_test, out, padding, cls_label, experiment_type, file_log, subfolder, exp_folder, ds_folder)

def get_plot_voting(X, y, classifiers, labels):


    _X = np.array(X)

    x_min, x_max = _X[:, 0].min() - 1, _X[:, 0].max() + 1
    y_min, y_max = _X[:, 1].min() - 1, _X[:, 1].max() + 1
    xx, yy = np.meshgrid(np.arange(x_min, x_max, 0.1),
                         np.arange(y_min, y_max, 0.1))

    f, axarr = plt.subplots(2, 2, sharex='col', sharey='row', figsize=(10, 8))

    for idx, clf, tt in zip(product([0, 1], [0, 1]),
                            classifiers,
                            labels):
        Z = clf.predict(np.c_[xx.ravel(), yy.ravel()])
        Z = Z.reshape(xx.shape)

        axarr[idx[0], idx[1]].contourf(xx, yy, Z, alpha=0.4)
        axarr[idx[0], idx[1]].scatter(_X[:, 0], _X[:, 1], c=y,
                                      s=20, edgecolor='k')
        axarr[idx[0], idx[1]].set_title(tt)

    plt.show()
    fig = plt.figure()
    fig.savefig(config.dir_output + 'plot.png')

def mlp_param_selection(X, y, nfolds):
    alphas = 10.0 ** -np.arange(1, 7)
    solvers = ['lbfgs', 'adam', 'sgd']
    param_grid = {'solver': solvers, 'alpha': alphas}
    grid_search = GridSearchCV(MLPClassifier(), param_grid, cv=nfolds)
    grid_search.fit(X, y)
    return grid_search.best_params_

def benchmark_text(X, y_likert, y_bin, exp_folder, ds_folder, random_state, test_size, combined=False, threshold_annotation_bin=0.7,
                   threshold_annotation_likert=0.45, exp_type_combined='bin'):

    config.logger.info('benchmark_text()')

    try:

        input_layer_neurons = len(X) + 1
        output_layer_neurons = 1
        hidden_nodes = np.math.ceil(len(X) / (2 * (input_layer_neurons + output_layer_neurons)))

        out_performance_file = 'out_performance_text.txt'
        graph_bin_file = 'benchmark_text_bin.png'
        graph_likert_file = 'benchmark_text_likert.png'
        if combined is True:
            out_performance_file = 'out_performance_combined_+' + exp_type_combined + '.txt'
            graph_bin_file = 'benchmark_combined_bin_+' + exp_type_combined + '.png'
            graph_likert_file = 'benchmark_combined_likert_+' + exp_type_combined + '.png'

        classifiers = [
            BernoulliNB(),
            RandomForestClassifier(n_estimators=100, n_jobs=-1),
            AdaBoostClassifier(),
            BaggingClassifier(),
            ExtraTreesClassifier(),
            GradientBoostingClassifier(),
            DecisionTreeClassifier(),
            CalibratedClassifierCV(),
            DummyClassifier(),
            PassiveAggressiveClassifier(max_iter=1000, tol=1e-3),
            RidgeClassifier(),
            RidgeClassifierCV(),
            SGDClassifier(max_iter=1000, tol=1e-3),
            LogisticRegression(),
            KNeighborsClassifier(),
            MLPClassifier(hidden_layer_sizes=(hidden_nodes,hidden_nodes,hidden_nodes), solver='adam', alpha=1e-05)
            ##OneVsRestClassifier(SVC(kernel='linear', probability=True))

        ]

        i = 1
        X_train, X_test, y_train_likert, y_test_likert = train_test_split(X, y_likert, test_size=test_size, random_state=random_state)
        X_train_bin, X_test_bin, y_train_bin, y_test_bin = train_test_split(X, y_bin, test_size=test_size, random_state=random_state)

        # just to double check...
        assert np.all(X_train == X_train_bin)

        scaler.fit(X_train)
        X_train = scaler.transform(X_train)
        X_test = scaler.transform(X_test)
        estimators = []

        x_axis_bin = []
        x_axis_likert = []
        y_axis_bin = []
        y_axis_likert = []

        print(X_train.shape)
        print(X_test.shape)

        with open(WEB_CREDIBILITY_DATA_PATH + exp_folder + ds_folder + out_performance_file, "w") as file_log:
            file_log.write(HEADER)
            for exp_type in ('bin', 'likert'):
                if exp_type == 'bin':
                    y_train = y_train_bin
                    y_test = y_test_bin
                    y_axis = y_axis_bin
                    x_axis = x_axis_bin
                else:
                    y_train = y_train_likert
                    y_test = y_test_likert
                    y_axis = y_axis_likert
                    x_axis = x_axis_likert
                for clf in classifiers:
                    out = []
                    cls_label = clf.__class__.__name__ + '_' + exp_type
                    out = train_test_export_save(clf, X_train, y_train, X_test, y_test, out, cls_label, 0,
                                                 exp_type, file_log, 'text_features/', exp_folder, ds_folder)
                    estimators.append((cls_label, clf))
                    i += 1
                    y_axis.extend(np.array(out)[:, 2])
                    x_axis.append(clf.__class__.__name__.replace('Classifier', ''))

                clf = VotingClassifier(estimators=estimators, voting='hard')
                cls_label = clf.__class__.__name__ + '_' + exp_type
                out = []
                out = train_test_export_save(clf, X_train, y_train, X_test, y_test, out, cls_label, 0,
                                             exp_type, file_log, 'text_features/', exp_folder, ds_folder)
                y_axis.extend(np.array(out)[:, 2])
                x_axis.append(clf.__class__.__name__.replace('Classifier', ''))


        export_chart_bar_likert_bin(x_axis_bin, y_axis_bin, graph_bin_file, exp_folder,
                                    'Webpage Text Features', 'Classifiers', 'F1-measure', threshold_annotation_bin)

        export_chart_bar_likert_bin(x_axis_likert, y_axis_likert, graph_likert_file, exp_folder,
                                    'Webpage Text Features', 'Classifiers', 'F1-measure', threshold_annotation_likert)

    except Exception as e:
        config.logger.error(repr(e))
        raise

def benchmark_html_sequence(X, y_likert, y_bin, exp_folder, ds_folder, random_state, test_size, pads):

    try:
        config.logger.info('benchmark_html_sequence()')

        maxsent = -1
        for e in X:
            maxsent = len(e) if len(e) > maxsent else maxsent
        print('max_sent: ', maxsent)
        maxpad = 1000

        # X_tags = [le.inverse_transform(s) for s in X]

        out_performance_file = 'out_performance_html2seq.txt'
        with open(WEB_CREDIBILITY_DATA_PATH + exp_folder + ds_folder + out_performance_file, "w") as file_log:
            file_log.write(HEADER)
            nb_01 = []
            nb_15 = []
            svm_01 = []
            svm_15 = []
            sgd_01 = []
            sgd_15 = []
            k_01 = []
            k_15 = []

            subfolder= 'html2seq/'

            for maxpad in pads:
                XX = pad_sequences(X, maxlen=maxpad, dtype='int', padding='pre', truncating='pre', value=0)
                X_train, X_test, y_train_likert, y_test_likert = train_test_split(XX, y_likert, test_size=test_size, random_state=random_state)
                X_train, X_test, y_train_bin, y_test_bin = train_test_split(XX, y_bin, test_size=test_size, random_state=random_state)

                ## no need to perform pre-processing nor tokenization here
                # tfidf = TfidfVectorizer(preprocessor=lambda x: x, tokenizer=lambda x: x)
                # X_train = tfidf.fit_transform(X_train)
                # X_test = tfidf.transform(X_test)
                # ==========================================================================================================
                # NB
                # ==========================================================================================================
                clf = MultinomialNB()
                nb_15 = train_test_export_save(clf, X_train, y_train_likert, X_test, y_test_likert, nb_15, 'nb', maxpad, 'likert', file_log, subfolder, exp_folder, ds_folder)
                nb_01 = train_test_export_save(clf, X_train, y_train_bin, X_test, y_test_bin, nb_01, 'nb', maxpad, 'bin', file_log, subfolder, exp_folder, ds_folder)
                # ==========================================================================================================
                # SGD
                # ==========================================================================================================
                clf = SGDClassifier(loss='hinge', penalty='l2', alpha=1e-3)
                sgd_15 = train_test_export_save(clf, X_train, y_train_likert, X_test, y_test_likert, sgd_15, 'sgd', maxpad, 'likert',
                                                file_log, subfolder, exp_folder, ds_folder)
                sgd_01 = train_test_export_save(clf, X_train, y_train_bin, X_test, y_test_bin, sgd_01, 'sgd', maxpad, 'bin',
                                                file_log, subfolder, exp_folder, ds_folder)
                # ==========================================================================================================
                # K-means
                # ==========================================================================================================
                pca = PCA()
                X_tr_pca = pca.fit_transform(X_train)
                X_te_pca = pca.transform(X_test)
                clf = KMeans(n_clusters=5, random_state=0, init='k-means++', max_iter=100, n_init=1)
                k_15 = train_test_export_save(clf, X_tr_pca, y_train_likert, X_te_pca, y_test_likert, k_15, 'kmeans', maxpad, 'likert',
                                              file_log, subfolder, exp_folder, ds_folder)
                clf = KMeans(n_clusters=2, random_state=0, init='k-means++', max_iter=100, n_init=1)
                k_01 = train_test_export_save(clf, X_tr_pca, y_train_bin, X_te_pca, y_test_bin, k_01, 'kmeans', maxpad, 'bin',
                                              file_log, subfolder, exp_folder, ds_folder)
                # ==========================================================================================================
                # SVM
                # ==========================================================================================================
                clf = SVC(C=0.7, decision_function_shape='ovo', kernel='rbf', shrinking=True, tol=0.1, probability=True)
                svm_15 = train_test_export_save(clf, X_train, y_train_likert, X_test, y_test_likert, svm_15, 'svm', maxpad, 'likert',
                                                file_log, subfolder, exp_folder, ds_folder)
                svm_01 = train_test_export_save(clf, X_train, y_train_bin, X_test, y_test_bin, svm_01, 'svm', maxpad, 'bin',
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

        title='HTML2Seq: performance varying window size'
        x_title='Padding window size (log scale)'
        y_title='F1-measure (average)'
        export_chart_scatter_likert_bin(pads, ['NB','SGD','K-means','SVM'],
            [np.array(nb_15)[:, 2], np.array(sgd_15)[:, 2],np.array(k_15)[:, 2], np.array(svm_15)[:, 2]],
            [np.array(nb_01)[:, 2], np.array(sgd_01)[:, 2], np.array(k_01)[:, 2], np.array(svm_01)[:, 2]],
            'benchmark_html2seq', exp_folder, ds_folder, title, x_title, y_title)

    except Exception as e:
        config.logger.error(repr(e))
        raise

def benchmark_combined(X, y_likert, y_bin, test_size, random_state, bestpad, exp_folder, tot_text_feat):
    try:

        XX = pad_sequences(X, maxlen=bestpad+tot_text_feat, dtype='int', padding='pre', truncating='pre', value=0)

        X_train, X_test, y_train_likert, y_test_likert = train_test_split(XX, y_likert, test_size=test_size,
                                                                          random_state=random_state)
        X_train, X_test, y_train_bin, y_test_bin = train_test_split(XX, y_bin, test_size=test_size,
                                                                    random_state=random_state)

        sgd_15=[]
        sgd_01=[]

        with open(config.dir_output + exp_folder + 'exp_performances_combined.txt', "w") as file_log:
            file_log.write(HEADER)

            clf = SGDClassifier(loss='hinge', penalty='l2', alpha=1e-3)
            sgd_15 = train_test_export_save(clf, X_train, y_train_likert, X_test, y_test_likert, sgd_15, 'sgd', bestpad,
                                            'likert', file_log, 'combined/')
            sgd_01 = train_test_export_save(clf, X_train, y_train_bin, X_test, y_test_bin, sgd_01, 'sgd', bestpad,
                                            'bin', file_log, 'combined/')


    except Exception as e:
        config.logger.error(repr(e))

def param_optimization():
    probability=True
    #ch2 = SelectKBest(chi2, k=10)
    #X_train = ch2.fit_transform(X_train, y_train)
    #X_test = ch2.transform(X_test)

    param_grid = {"C": [0.5, 0.7, 0.9, 1.0],
                  "kernel": ['linear', 'rbf', 'sigmoid'],
                  "shrinking": [True, False],
                  "decision_function_shape": ['ovo', 'ovr'],
                  "tol": [0.1, 0.01, 0.001]}

    clf = SVC()

    grid_search = GridSearchCV(clf, param_grid=param_grid, n_jobs=-1)
    start = time()
    grid_search.fit(X_train, y2_train)
    print(grid_search.best_params_)
    print(grid_search.best_score_)



    param_grid = {"alpha": [0.01, 0.05, 0.1, 0.2, 0.5, 0.7, 1.0],
                  "fit_intercept": [True, False],
                  "normalize": [True, False],
                  "copy_X": [True, False],
                  "tol": [0.1, 0.01, 0.001]}

    #clf = SGDClassifier(alpha=.0001, penalty="elasticnet")
    #clf = LinearSVC(penalty='l2')
    #clf = PassiveAggressiveClassifier()
    #clf = Perceptron()
    clf = RidgeClassifier()

    grid_search = GridSearchCV(clf, param_grid=param_grid, n_jobs=-1)
    start = time()
    grid_search.fit(X_train, y2_train)
    print(grid_search.best_params_)
    print(grid_search.best_score_)




    # NearestCentroid
    clf.fit(X_train, y_train)
    predicted = clf.predict(X_test)
    p, r, f, s = precision_recall_fscore_support(y_test, predicted, average='weighted')
    print(f)
    #print(np.mean(predicted == y_test))

    #X_train = ch2.fit_transform(X_train, y2_train)
    #X_test = ch2.transform(X_test)

    clf.fit(X_train, y2_train)
    predicted2 = clf.predict(X_test)
    p, r, f, s = precision_recall_fscore_support(y2_test, predicted2, average='weighted')
    print(f)
    #print(np.mean(predicted2 == y2_test))

    clf = RandomForestClassifier(n_estimators=20)
    param_grid = {"max_depth": [3, None],
                  "max_features": [1, 3, 10],
                  "min_samples_split": [2, 3, 10],
                  "min_samples_leaf": [1, 3, 10],
                  "bootstrap": [True, False],
                  "criterion": ["gini", "entropy"]}
    grid_search = GridSearchCV(clf, param_grid=param_grid)
    start = time()
    grid_search.fit(X_train, y2_train)
    print(grid_search.best_params_)
    print(grid_search.best_score_)


    # gs_clf_svm = GridSearchCV(clf, parameters_svm, n_jobs=-1)
    # gs_clf_svm = gs_clf_svm.fit(X_train, y_train)
    # print(gs_clf_svm.best_score_)
    # print(gs_clf_svm.best_params_)

    # print(self.mlp_param_selection(XX, y2, 5))
    # exit(0)

    #parameters_svm = {'vect__ngram_range': [(1, 1), (1, 2)],
    #                  'tfidf__use_idf': (True, False),
    #                  'clf-svm__alpha': (1e-2, 1e-3)}

def feature_selection():
    #TODO: implement
    try:
        a=1
    except Exception as e:
        config.logger.error(repr(e))
        raise

if __name__ == '__main__':
    try:

        EXP_FOLDER = 'exp003/'
        DS_FOLDER = '3c/'

        RANDOM_STATE=53
        TEST_SIZE=0.2
        PADS = [25, 50, 100, 175, 250, 500, 1000, 1250, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600,
                2700, 2800, 2900, 3000, 3500, 4000, 4500, 5000, 6000, 7000, 8000, 9000, 10000, 20000, 30000]
        BEST_PAD_BIN = 2900
        BEST_PAD_LIKERT = 2000
        BEST_CLS_BIN = 'nb'
        BEST_CLS_LIKERT = 'nb'

        #TOT_TEXT_FEAT = 53
        SERIES_COLORS = ['rgb(205, 12, 24)', 'rgb(22, 96, 167)', 'rgb(128, 128, 128)', 'rgb(0, 0, 139)',
                        'rgb(192,192,192)', 'rgb(211,211,211)', 'rgb(255,255,0)', 'rgb(0,128,0)']
        BAR_COLOR = 'rgb(128,128,128)'

        # TEXT FEATURES
        #features_tex, y_likert, y_bin = get_text_features(EXP_FOLDER, DS_FOLDER)
        #benchmark_text(features_tex, y_likert, y_bin, EXP_FOLDER, DS_FOLDER, RANDOM_STATE, TEST_SIZE)

        # HTML2Seq FEATURES
        (features_seq, y_likert, y_bin), le = get_html2sec_features(EXP_FOLDER, DS_FOLDER)
        benchmark_html_sequence(features_seq, y_likert, y_bin, EXP_FOLDER, DS_FOLDER, RANDOM_STATE, TEST_SIZE, PADS)

        ### TEXT FEATURES + HTML2Seq klass as feature (out of best configurations)
        features_combined, y_likert, y_bin = get_text_features(EXP_FOLDER, DS_FOLDER, html2seq=True, best_pad=BEST_PAD_BIN,
                                                               best_cls=BEST_CLS_BIN, exp_type_combined='bin')
        benchmark_text(features_combined, y_likert, y_bin, EXP_FOLDER, RANDOM_STATE, TEST_SIZE, combined=True,
                       exp_type_combined='bin')

        features_combined, y_likert, y_bin = get_text_features(EXP_FOLDER, DS_FOLDER, html2seq=True, best_pad=BEST_PAD_LIKERT,
                                                               best_cls=BEST_CLS_LIKERT, exp_type_combined='likert')
        benchmark_text(features_combined, y_likert, y_bin, EXP_FOLDER, RANDOM_STATE, TEST_SIZE, combined=True,
                       exp_type_combined='likert')

        #benchmark_combined(features_combined, y_likert, y_bin, TEST_SIZE, RANDOM_STATE, BEST_PAD, EXP_FOLDER, TOT_TEXT_FEAT)

    except Exception as e:
        config.logger.error(repr(e))
        raise