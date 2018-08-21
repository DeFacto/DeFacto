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

from defacto.definitions import OUTPUT_FOLDER, BEST_CLS_BIN, TEST_SIZE, BEST_CLS_LIKERT, BEST_PAD_BIN, \
    BEST_PAD_LIKERT, PADS, HEADER, EXP_5_CLASSES_LABEL, EXP_3_CLASSES_LABEL, EXP_2_CLASSES_LABEL, LINE_TEMPLATE, \
    LABELS_2_CLASSES, LABELS_5_CLASSES, CROSS_VALIDATION_K_FOLDS, SEARCH_METHOD_RANDOMIZED_GRID, SEARCH_METHOD_GRID
from trustworthiness.util import print_report
from trustworthiness.feature_extractor import *

from sklearn.metrics import confusion_matrix
from sklearn.model_selection import train_test_split, GridSearchCV, cross_validate, KFold, RandomizedSearchCV
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

def export_chart_bar(x, y, filename, exp_folder, title, x_title, y_title, annotation_threshold):
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

        _path = OUTPUT_FOLDER + exp_folder + 'graphs/'
        if not os.path.exists(_path):
            os.mkdir(_path)

        py.image.save_as(fig, filename=_path + filename)

    except Exception as e:
        raise e

def export_chart_scatter(x, y_labels, y_5_f1, y_3_f1, y_2_f1, filename, exp_folder, ds_folder, title, x_title, y_title, log_mode=True):

    try:
        if log_mode == True:
            x_labels = x.copy()
            x = [math.log(pad) for pad in x]
        line_width=1
        mode='lines+markers'
        data_5 = []
        data_3 = []
        data_2 = []

        assert log_mode == True # otherwise need to adjust the function
        assert len(y_5_f1) == len(y_2_f1) == len(y_3_f1)
        assert len(y_5_f1) <= len(SERIES_COLORS)

        i=0
        for trace in y_5_f1:
            s = go.Scatter(
                x=x,
                y=trace,
                text=trace,
                name=y_labels[i],
                mode=mode,
                line=dict(
                    color=(SERIES_COLORS[i]),
                    width=line_width))
            data_5.append(s)
            i += 1

        i = 0
        for trace in y_3_f1:
            s = go.Scatter(
                x=x,
                y=trace,
                text=trace,
                name=y_labels[i],
                mode=mode,
                line=dict(
                    color=(SERIES_COLORS[i]),
                    width=line_width,
                    dash='dot'))
            data_3.append(s)
            i += 1

        i=0
        for trace in y_2_f1:
            s = go.Scatter(
                x=x,
                y=trace,
                text=trace,
                name=y_labels[i],
                mode=mode,
                line=dict(
                    color=(SERIES_COLORS[i]),
                    width=line_width,
                    dash='dash'))
            data_2.append(s)
            i += 1

        y_5 = np.array(y_5_f1)
        y_3 = np.array(y_3_f1)
        y_2 = np.array(y_2_f1)

        # Edit the layout
        layout_5 = dict(title=title,
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
                        annotations=get_annotation_from_max(y_5, x, x_labels),
                        font=dict(family='Helvetica', size=14)
                        )
        layout_3 = layout_5.copy()
        layout_2 = layout_5.copy()

        layout_3['annotations'] = get_annotation_from_max(y_3, x, x_labels)
        layout_2['annotations'] = get_annotation_from_max(y_2, x, x_labels)

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
        #py.plot(fig, filename='paddings_f1')

        _path = OUTPUT_FOLDER + exp_folder + ds_folder + 'graphs/'
        if not os.path.exists(_path):
            os.mkdir(_path)

        fig = dict(data=data_5, layout=layout_5)
        py.image.save_as(fig, filename=_path + filename + '_5class.png')

        fig = dict(data=data_3, layout=layout_3)
        py.image.save_as(fig, filename=_path + filename + '_3class.png')

        fig = dict(data=data_2, layout=layout_2)
        py.image.save_as(fig, filename=_path + filename + '_2class.png')

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

def train_test_export_save(estimator, estimator_label, hyperparameters, search_method,
                           X_train, X_test, y_train, y_test, experiment_type, padding,
                           out, file_log, subfolder, exp_folder, ds_folder):

    try:
        # file dump info
        file = BENCHMARK_FILE_NAME_TEMPLATE % (estimator_label.lower(), padding, experiment_type)
        _path = OUTPUT_FOLDER + exp_folder + ds_folder + 'models/' + subfolder
        if not os.path.exists(_path):
            os.mkdir(_path)

        ## loading the classifier
        #if isinstance(clf, str):
        #    clf = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'models/' + subfolder + clf)

        # grid search on 10-fold cross validation
        if experiment_type == EXP_2_CLASSES_LABEL:
            scoring = ['precision', 'recall', 'f1']

        elif experiment_type == EXP_3_CLASSES_LABEL:
            scoring = ['precision', 'precision_micro', 'precision_macro', 'precision_weighted',
                       'recall', 'recall_micro', 'recall_macro', 'recall_weighted',
                       'f1', 'f1_micro', 'f1_macro', 'f1_weighted']

        elif experiment_type == EXP_5_CLASSES_LABEL:
            scoring = ['r2', 'neg_mean_squared_error', 'neg_mean_absolute_error', 'explained_variance']

        else:
            raise Exception ('not supported! ' + experiment_type)

        if search_method == 'grid':
            clf = GridSearchCV(estimator, hyperparameters, cv=CROSS_VALIDATION_K_FOLDS, scoring=scoring, n_jobs=-1)
        elif search_method == 'random':
            clf = RandomizedSearchCV(estimator, hyperparameters, cv=CROSS_VALIDATION_K_FOLDS, scoring=scoring, n_jobs=-1)
        else:
            raise Exception('not supported! ' + search_method)

        clf.fit(X_train, y_train)
        config.logger.info('best training set parameters: ')
        config.logger.info(clf.best_params_)
        config.logger.info('----------------------------------------------------')
        pred = clf.predict(X_test) # equivalent to clf.best_estimator_.predict()
        # p_avg, r_avg, f_avg, s_avg = precision_recall_fscore_support(y_test, predicted, average='weighted')

        scores = clf.score(X_test, y_test) # check if this score == score on pred

        # saving the best model
        joblib.dump(clf, _path + file)

        # saving the best parameters
        best_parameters_file_name = BENCHMARK_FILE_NAME_TEMPLATE.replace('.pkl', '.best_param')

        with open(OUTPUT_FOLDER + exp_folder + ds_folder + best_parameters_file_name, "w") as best:
            best.write(' -- best params')
            best.write(str(clf.best_params_))
            best.write(' -- best score')
            best.write(str(clf.best_score_))

        #cv_results = cross_validate(clf, X_test, y_test, return_train_score=False, cv=10, scoring=scoring)

        # get metrics for chart and full log
        if experiment_type == EXP_2_CLASSES_LABEL:
            # chart
            p_avg = scores['precision']
            r_avg = scores['recall']
            f_avg = scores['f1']

            # log
        elif experiment_type == EXP_3_CLASSES_LABEL:
            # chart
            p_avg = scores['precision_weighted']
            r_avg = scores['recall_weighted']
            f_avg = scores['f1_weighted']

            # log
        elif experiment_type == EXP_5_CLASSES_LABEL:

            # log
            for i in range(len(scores)):
                r2 = scores[i]['r2']
                rmse = scores[i]['rmse']
                mae = scores[i]['mae']
                evar = scores[i]['evar']
                file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, LABELS_5_CLASSES.get(i + 1), r2, rmse, mae, evar, 0))



        else:
            raise Exception('not supported! ' + experiment_type)




        out.append([p_avg, r_avg, f_avg])
        config.logger.info('padding: %s cls: %s exp_type: %s f1: %.3f' % (padding, estimator_label, experiment_type, f_avg))

        # file logging details
        p, r, f, s = precision_recall_fscore_support(y_test, predicted)
        if experiment_type in (EXP_2_CLASSES_LABEL, EXP_3_CLASSES_LABEL):
            tn, fp, fn, tp =confusion_matrix(y_test, predicted).ravel()
            fpr=fp/(fp+tn)
            fnr=fn/(tp+fn)

            unique, counts = np.unique(y_test, return_counts=True)
            tot = counts[0] + counts[1]

            rate_avg=np.average([fpr, fnr], weights=[counts[0]/tot, counts[1]/tot])
            #fpr_0, tpr_0, thresholds_0 = roc_curve(y_test, predicted, pos_label=0)
            #fpr_1, tpr_1, thresholds_1 = roc_curve(y_test, predicted, pos_label=1)

            file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, LABELS_2_CLASSES.get(0), p[0], r[0], f[0], s[0], fpr))
            file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, LABELS_2_CLASSES.get(1), p[1], r[1], f[1], s[1], fnr))
            file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, 'average', p_avg, r_avg, f_avg, s[0] + s[1], rate_avg))

        else:
            for i in range(len(p)):
                file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, LABELS_5_CLASSES.get(i + 1), p[i], r[i], f[i], s[i], 0))

        return out

    except Exception as e:
        config.logger.error(repr(e))
        raise

    #return test(clf, X_test, y_test, out, padding, estimator_label, experiment_type, file_log, subfolder, exp_folder, ds_folder)

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
    fig.savefig(OUTPUT_FOLDER + 'plot.png')

def mlp_param_selection(X, y, nfolds):
    alphas = 10.0 ** -np.arange(1, 7)
    solvers = ['lbfgs', 'adam', 'sgd']
    param_grid = {'solver': solvers, 'alpha': alphas}
    grid_search = GridSearchCV(MLPClassifier(), param_grid, cv=nfolds)
    grid_search.fit(X, y)
    return grid_search.best_params_

def feature_selection():
    try:
        a=1
        #TODO: implement (only top 20th percentile is relevant? 20, 50, 80 and 100
    except:
        raise

def benchmark_text(X, y5, y3, y2, exp_folder, ds_folder, random_state, test_size, combined=False,
                   threshold_label_bin=0.7, threshold_label_likert=0.45, exp_type_combined=None):

    config.logger.info('benchmark_text()')

    try:

        out_performance_file_classification = 'out_performance_text_classification.txt'
        out_performance_file_regression = 'out_performance_text_regression.txt'
        graph_2_file = 'benchmark_text_2classes.png'
        graph_3_file = 'benchmark_text_3classes.png'
        graph_5_file = 'benchmark_text_5classes.png'

        if combined is True:
            out_performance_file_classification = 'out_performance_combined_+' + exp_type_combined + '_classification.txt'
            out_performance_file_regression = 'out_performance_combined_+' + exp_type_combined + '_regression.txt'
            graph_2_file = 'benchmark_combined_2classes_+' + exp_type_combined + '.png'
            graph_3_file = 'benchmark_combined_3classes_+' + exp_type_combined + '.png'
            graph_5_file = 'benchmark_combined_5classes_+' + exp_type_combined + '.png'

        #input_layer_neurons = len(X) + 1
        #output_layer_neurons = 1
        #hidden_nodes = np.math.ceil(len(X) / (2 * (input_layer_neurons + output_layer_neurons)))

        trees_param_basic = {"criterion": ['gini', 'entropy'],
                            "max_features": ['auto', 'sqrt'],
                            "max_depth": [int(x) for x in np.linspace(10, 110, num = 11)],
                            "min_samples_split":[2, 5, 10],
                            "min_samples_leaf": [1, 2, 4]}

        trees_param = trees_param_basic.copy()
        trees_param["n_estimators"] = [10, 25, 50, 100, 200, 400, 600, 1000, 1500, 2000]

        trees_param_bootstrap = trees_param.copy()
        trees_param_bootstrap["bootstrap"] = [True, False]


        # classifiers x hyper-parameters x search method

        config_regression = [(LogisticRegression(n_jobs=-1),
                              {"alpha": [1e0, 1e-1, 1e-2, 1e-3],
                               "solver": ["newton-cg", "lbfgs", "liblinear", "sag", "saga"],
                               "multi_class": ["ovr", "multinomial"],
                               "tol": [1e0, 1e-1, 1e-2, 1e-3],
                               "penalty": ["l1", "l2"],
                               "C": [0.1, 0.5, 1.0, 3.0, 5.0, 10.0, 50.0, 100.0]
                               },
                              SEARCH_METHOD_RANDOMIZED_GRID),
                             (Ridge(), {"alpha": [1e0, 1e-1, 1e-2, 1e-3],
                                        "solver": ['auto', 'svd', 'cholesky', 'lsqr', 'sparse_cg', 'sag','saga'],
                                        "tol": [1e0, 1e-1, 1e-2, 1e-3]},
                              SEARCH_METHOD_RANDOMIZED_GRID),
                             (SVR(),  {"epsilon": [1e0, 1e-1, 1e-2, 1e-3],
                               "kernel": ["linear", "poly", "rbf", "sigmoid"],
                               "tol": [1e0, 1e-1, 1e-2, 1e-3],
                               "C": [0.1, 0.5, 1.0, 3.0, 5.0, 10.0, 50.0, 100.0]
                               }, SEARCH_METHOD_GRID)
                             ]

        config_classification = [
            (DecisionTreeClassifier(), trees_param_basic, SEARCH_METHOD_RANDOMIZED_GRID),
            (GradientBoostingClassifier(), trees_param, SEARCH_METHOD_RANDOMIZED_GRID),
            (RandomForestClassifier(n_jobs=-1), trees_param_bootstrap, SEARCH_METHOD_RANDOMIZED_GRID),
            (ExtraTreesClassifier(n_jobs=-1), trees_param_bootstrap, SEARCH_METHOD_RANDOMIZED_GRID),
            (BaggingClassifier(), {"n_estimators": [10, 25, 50, 100, 200, 400, 600, 1000, 1500, 2000],
                                   "base_estimator__max_depth": [1, 2, 3, 4, 5],
                                   "max_samples": [0.05, 0.1, 0.2, 0.5]}, SEARCH_METHOD_RANDOMIZED_GRID),
            (AdaBoostClassifier(), {"n_estimators": [10, 25, 50, 100, 200, 400, 600, 1000, 1500, 2000],
                                    "algorithm": ["SAMME", "SAMME.R"]}, SEARCH_METHOD_RANDOMIZED_GRID),
            (PassiveAggressiveClassifier(n_jobs=-1), {"tol": [1e0, 1e-1, 1e-2, 1e-3],
                                                      "C": [0.1, 0.5, 1.0, 3.0, 5.0, 10.0, 50.0, 100.0],
                                                      "loss": ["hinge", "squared_hinge"]}, SEARCH_METHOD_GRID),
            (SGDClassifier(n_jobs=-1), {"loss": ["hinge", "log", "modified_huber", "squared_hinge", "perceptron"],
                                        "penality": ["none", "l2", "l1", "elasticnet"],
                                        "alpha": [1e0, 1e-1, 1e-2, 1e-3],
                                        "tol": [1e0, 1e-1, 1e-2, 1e-3],
                                        "learning_rate": ["constant", "invscaling", "optimal"]},
                                        SEARCH_METHOD_RANDOMIZED_GRID),
            (BernoulliNB(), {"alpha": [1e0, 1e-1, 1e-2, 1e-3]}, SEARCH_METHOD_GRID),
            #MLPClassifier(hidden_layer_sizes=(hidden_nodes,hidden_nodes,hidden_nodes), solver='adam', alpha=1e-05)
            ##OneVsRestClassifier(SVC(kernel='linear', probability=True))

        ]

        X_train_5, X_test_5, y_train_5, y_test_5 = train_test_split(X, y5, test_size=test_size, random_state=random_state)
        X_train_3, X_test_3, y_train_3, y_test_3 = train_test_split(X, y3, test_size=test_size, random_state=random_state)
        X_train_2, X_test_2, y_train_2, y_test_2 = train_test_split(X, y2, test_size=test_size, random_state=random_state)


        # just to double check...
        assert np.all(X_train_5 == X_train_3 == X_train_2)

        scaler.fit(X_train_5)
        X_train = scaler.transform(X_train_5)
        X_test = scaler.transform(X_test_5)
        estimators = []

        x_axis_2 = []
        x_axis_3 = []
        y_axis_2 = []
        y_axis_3 = []

        # --------------------------------------------------------------------------------------------------------------
        # classification experiment
        # --------------------------------------------------------------------------------------------------------------
        config.logger.info('starting experiments classification (2-classes and 3-classes)')
        i = 1
        with open(OUTPUT_FOLDER + exp_folder + ds_folder + out_performance_file_classification, "w") as file_log_classification:
            file_log_classification.write(HEADER)
            for exp_type in (EXP_2_CLASSES_LABEL, EXP_3_CLASSES_LABEL):
                if exp_type == EXP_2_CLASSES_LABEL:
                    y_train = y_train_2
                    y_test = y_test_2
                    y_axis = y_axis_2
                    x_axis = x_axis_2
                elif exp_type == EXP_3_CLASSES_LABEL:
                    y_train = y_train_3
                    y_test = y_test_3
                    y_axis = y_axis_3
                    x_axis = x_axis_3
                else:
                    raise Exception('blah! error')

                for estimator, hyperparam, grid_method in config_classification:
                    out = []
                    cls_label = estimator.__class__.__name__ + '_' + exp_type
                    out = train_test_export_save(estimator, cls_label, hyperparam, grid_method,
                                                    X_train, X_test, y_train, y_test, exp_type, 0,
                                                    out, file_log_classification, 'text_features/', exp_folder, ds_folder)
                    estimators.append((cls_label, estimator))
                    i += 1
                    y_axis.extend(np.array(out)[:, 2])
                    x_axis.append(estimator.__class__.__name__.replace('Classifier', ''))

                estimator = VotingClassifier(estimators=estimators, voting='hard')
                cls_label = estimator.__class__.__name__ + '_hard_' + exp_type
                out = []
                out = train_test_export_save(estimator, cls_label, hyperparam, grid_method,
                                                 X_train, X_test, y_train, y_test, exp_type, 0,
                                                 out, file_log_classification, 'text_features/', exp_folder, ds_folder)
                y_axis.extend(np.array(out)[:, 2])
                x_axis.append(estimator.__class__.__name__.replace('Classifier', ''))

                estimator = VotingClassifier(estimators=estimators, voting='soft')
                cls_label = estimator.__class__.__name__ + '_soft_' + exp_type
                out = []
                out = train_test_export_save(estimator, cls_label, hyperparam, grid_method,
                                                 X_train, X_test, y_train, y_test, exp_type, 0,
                                                 out, file_log_classification, 'text_features/', exp_folder, ds_folder)
                y_axis.extend(np.array(out)[:, 2])
                x_axis.append(estimator.__class__.__name__.replace('Classifier', ''))


        title = 'Webpage Text Features'
        x_axis_label = 'Classifiers'
        y_axis_label = 'F1-measure'

        config.logger.info('experiments classification done! exporting charts...')

        export_chart_bar(
            x_axis_2, y_axis_2, graph_2_file, exp_folder, title, x_axis_label, y_axis_label, threshold_label_bin)

        export_chart_bar(
            x_axis_3, y_axis_3, graph_3_file, exp_folder, title, x_axis_label, y_axis_label, threshold_label_likert)

        config.logger.info('charts exported!')

        # --------------------------------------------------------------------------------------------------------------
        # regression experiment
        # --------------------------------------------------------------------------------------------------------------
        config.logger.info('starting experiments regression (5-classes)')

        with open(OUTPUT_FOLDER + exp_folder + ds_folder + out_performance_file_regression, "w") as file_log_regression:
            file_log_regression.write(HEADER)
            for estimator, hyperparam, grid_method in config_regression:
                out = []
                cls_label = estimator.__class__.__name__ + '_' + EXP_5_CLASSES_LABEL
                out = train_test_export_save(estimator, cls_label, hyperparam, grid_method,
                                                 X_train, X_test, y_train_5, y_test_5, EXP_5_CLASSES_LABEL, 0,
                                                 out, file_log_regression, 'text_features/', exp_folder, ds_folder)

    except Exception as e:
        config.logger.error(repr(e))
        raise

def benchmark_html_sequence(X, y5, y3, y2, exp_folder, ds_folder, random_state, test_size, pads):

    try:
        config.logger.info('benchmark_html_sequence()')

        maxsent = -1
        for e in X:
            maxsent = len(e) if len(e) > maxsent else maxsent
        print('max_sent: ', maxsent)
        maxpad = 1000

        # X_tags = [le.inverse_transform(s) for s in X]

        out_performance_file = 'out_performance_html2seq.txt'
        with open(OUTPUT_FOLDER + exp_folder + ds_folder + out_performance_file, "w") as file_log:
            file_log.write(HEADER)
            nb_2 = [], nb_3 = [], nb_5 = []
            svm_2 = [], svm_3 = [], svm_5 = []
            sgd_2 = [], sgd_3 = [], sgd_5 = []
            k_2 = [], k_3 = [], k_5 = []

            subfolder= 'html2seq/'

            for maxpad in pads:
                XX = pad_sequences(X, maxlen=maxpad, dtype='int', padding='pre', truncating='pre', value=0)
                X_train, X_test, y_train_5, y_test_5 = train_test_split(XX, y5, test_size=test_size, random_state=random_state)
                X_train, X_test, y_train_3, y_test_3 = train_test_split(XX, y3, test_size=test_size, random_state=random_state)
                X_train, X_test, y_train_2, y_test_2 = train_test_split(XX, y2, test_size=test_size, random_state=random_state)

                ## no need to perform pre-processing nor tokenization here
                # tfidf = TfidfVectorizer(preprocessor=lambda x: x, tokenizer=lambda x: x)
                # X_train = tfidf.fit_transform(X_train)
                # X_test = tfidf.transform(X_test)
                # ==========================================================================================================
                # NB
                # ==========================================================================================================
                clf = MultinomialNB()
                nb_5 = train_test_export_save(clf, X_train, y_train_5, X_test, y_test_5, nb_5, 'nb', maxpad, EXP_5_CLASSES_LABEL, file_log, subfolder, exp_folder, ds_folder)
                nb_3 = train_test_export_save(clf, X_train, y_train_3, X_test, y_test_3, nb_3, 'nb', maxpad, EXP_3_CLASSES_LABEL, file_log, subfolder, exp_folder, ds_folder)
                nb_2 = train_test_export_save(clf, X_train, y_train_2, X_test, y_test_2, nb_2, 'nb', maxpad, EXP_2_CLASSES_LABEL, file_log, subfolder, exp_folder, ds_folder)
                # ==========================================================================================================
                # SGD
                # ==========================================================================================================
                clf = SGDClassifier(loss='hinge', penalty='l2', alpha=1e-3)
                sgd_5 = train_test_export_save(clf, X_train, y_train_5, X_test, y_test_5, sgd_5, 'sgd', maxpad, EXP_5_CLASSES_LABEL, file_log, subfolder, exp_folder, ds_folder)
                sgd_3 = train_test_export_save(clf, X_train, y_train_3, X_test, y_test_3, sgd_3, 'sgd', maxpad, EXP_3_CLASSES_LABEL, file_log, subfolder, exp_folder, ds_folder)
                sgd_2 = train_test_export_save(clf, X_train, y_train_2, X_test, y_test_2, sgd_2, 'sgd', maxpad, EXP_2_CLASSES_LABEL, file_log, subfolder, exp_folder, ds_folder)
                # ==========================================================================================================
                # K-means
                # ==========================================================================================================
                pca = PCA()
                X_tr_pca = pca.fit_transform(X_train)
                X_te_pca = pca.transform(X_test)
                clf5 = KMeans(n_clusters=5, random_state=0, init='k-means++', max_iter=100, n_init=1)
                clf3 = KMeans(n_clusters=3, random_state=0, init='k-means++', max_iter=100, n_init=1)
                clf2 = KMeans(n_clusters=2, random_state=0, init='k-means++', max_iter=100, n_init=1)
                k_5 = train_test_export_save(clf5, X_tr_pca, y_train_5, X_te_pca, y_test_5, k_5, 'kmeans', maxpad, EXP_5_CLASSES_LABEL, file_log, subfolder, exp_folder, ds_folder)
                k_3 = train_test_export_save(clf3, X_tr_pca, y_train_3, X_te_pca, y_test_3, k_3, 'kmeans', maxpad, EXP_3_CLASSES_LABEL, file_log, subfolder, exp_folder, ds_folder)
                k_2 = train_test_export_save(clf2, X_tr_pca, y_train_2, X_te_pca, y_test_2, k_2, 'kmeans', maxpad, EXP_2_CLASSES_LABEL, file_log, subfolder, exp_folder, ds_folder)

                # ==========================================================================================================
                # SVM
                # ==========================================================================================================
                clf = SVC(C=0.7, decision_function_shape='ovo', kernel='rbf', shrinking=True, tol=0.1, probability=True)

                svm_5 = train_test_export_save(clf, X_train, y_train_5, X_test, y_test_5, svm_5, 'svm', maxpad, EXP_5_CLASSES_LABEL, file_log, subfolder, exp_folder, ds_folder)
                svm_3 = train_test_export_save(clf, X_train, y_train_3, X_test, y_test_3, svm_3, 'svm', maxpad, EXP_3_CLASSES_LABEL, file_log, subfolder, exp_folder, ds_folder)
                svm_2 = train_test_export_save(clf, X_train, y_train_2, X_test, y_test_2, svm_2, 'svm', maxpad, EXP_2_CLASSES_LABEL, file_log, subfolder, exp_folder, ds_folder)

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
        export_chart_scatter(pads, ['NB', 'SGD', 'K-means', 'SVM'],
                             [np.array(nb_5)[:, 2], np.array(sgd_5)[:, 2], np.array(k_5)[:, 2], np.array(svm_5)[:, 2]],
                             [np.array(nb_3)[:, 2], np.array(sgd_3)[:, 2], np.array(k_3)[:, 2], np.array(svm_3)[:, 2]],
                             [np.array(nb_2)[:, 2], np.array(sgd_2)[:, 2], np.array(k_2)[:, 2], np.array(svm_2)[:, 2]],
        'benchmark_html2seq', exp_folder, ds_folder, title, x_title, y_title)

    except Exception as e:
        config.logger.error(repr(e))
        raise

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


if __name__ == '__main__':
    try:

        EXP_FOLDER = 'exp003/'
        DS_FOLDER = 'microsoft/'
        FEATURES_FILE = 'microsoft_dataset_227_all_text_features.pkl'

        RANDOM_STATE = 53
        #TOT_TEXT_FEAT = 53
        SERIES_COLORS = ['rgb(205, 12, 24)', 'rgb(22, 96, 167)', 'rgb(128, 128, 128)', 'rgb(0, 0, 139)',
                        'rgb(192,192,192)', 'rgb(211,211,211)', 'rgb(255,255,0)', 'rgb(0,128,0)']
        BAR_COLOR = 'rgb(128,128,128)'

        '''
        01. TEXT FEATURES (only)
        '''
        config.logger.info('text feature benchmark')
        features_tex, y5, y3, y2 = get_text_features(EXP_FOLDER, DS_FOLDER, FEATURES_FILE)
        benchmark_text(features_tex, y5, y3, y2, EXP_FOLDER, DS_FOLDER, RANDOM_STATE, TEST_SIZE, combined=False)

        ''' 
        02. HTML2Seq FEATURES (only)
        '''
        config.logger.info('html2seq feature benchmark')
        (features_seq, y5, y3, y2), le = get_html2sec_features(EXP_FOLDER, DS_FOLDER)
        benchmark_html_sequence(features_seq, y5, y3, y2, EXP_FOLDER, DS_FOLDER, RANDOM_STATE, TEST_SIZE, PADS)

        '''
        03. TEXT + HTML2Seq features combined (out of best configurations)
        '''
        config.logger.info('text+html2seq feature benchmark')
        features_combined, y5, y3, y2 = get_text_features(EXP_FOLDER, DS_FOLDER, FEATURES_FILE,
                                                          html2seq=True, best_pad=BEST_PAD_BIN,
                                                          best_cls=BEST_CLS_BIN, exp_type_combined=EXP_2_CLASSES_LABEL)
        benchmark_text(features_combined, y5, y3, y2, EXP_FOLDER, DS_FOLDER, RANDOM_STATE, TEST_SIZE,
                       combined=True, exp_type_combined=EXP_2_CLASSES_LABEL)

        features_combined, y5, y3, y2 = get_text_features(EXP_FOLDER, DS_FOLDER, FEATURES_FILE,
                                                          html2seq=True, best_pad=BEST_PAD_LIKERT,
                                                          best_cls=BEST_CLS_LIKERT, exp_type_combined=EXP_3_CLASSES_LABEL)
        benchmark_text(features_combined, y5, y3, y2, EXP_FOLDER, DS_FOLDER, RANDOM_STATE, TEST_SIZE,
                       combined=True, exp_type_combined=EXP_3_CLASSES_LABEL)

        features_combined, y5, y3, y2 = get_text_features(EXP_FOLDER, DS_FOLDER, FEATURES_FILE,
                                                          html2seq=True, best_pad=BEST_PAD_LIKERT,
                                                          best_cls=BEST_CLS_LIKERT, exp_type_combined=EXP_5_CLASSES_LABEL)
        benchmark_text(features_combined, y5, y3, y2, EXP_FOLDER, DS_FOLDER, RANDOM_STATE, TEST_SIZE,
                       combined=True, exp_type_combined=EXP_5_CLASSES_LABEL)

    except Exception as e:
        config.logger.error(repr(e))
        raise