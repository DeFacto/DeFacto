from keras.preprocessing.sequence import pad_sequences
from sklearn.cross_validation import train_test_split
from sklearn.grid_search import GridSearchCV
import plotly.plotly as py
import plotly.graph_objs as go
import math
from sklearn.cluster import KMeans
from sklearn.decomposition import PCA
from sklearn.metrics import precision_recall_fscore_support, explained_variance_score, r2_score

from defacto.definitions import OUTPUT_FOLDER, TEST_SIZE, \
    PADS, HEADER, EXP_5_CLASSES_LABEL, EXP_3_CLASSES_LABEL, EXP_2_CLASSES_LABEL, LINE_TEMPLATE, \
    LABELS_2_CLASSES, LABELS_5_CLASSES, CROSS_VALIDATION_K_FOLDS, SEARCH_METHOD_RANDOMIZED_GRID, SEARCH_METHOD_GRID, \
    CONFIGS_CLASSIFICATION, CONFIGS_REGRESSION, CONFIGS_HIGH_DIMEN, LABELS_3_CLASSES, THRESHOLD_LABEL_2class, \
    THRESHOLD_LABEL_3class
from trustworthiness.feature_extractor import *
from sklearn.model_selection import train_test_split, GridSearchCV, cross_validate, KFold, RandomizedSearchCV
from itertools import product
from sklearn.neural_network import MLPClassifier
from config import DeFactoConfig
from sklearn.ensemble import *
from sklearn.externals import joblib
import numpy as np
import matplotlib.pyplot as plt
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import mean_absolute_error
from sklearn.metrics import mean_squared_error
from math import sqrt
__author__ = "Diego Esteves"
__copyright__ = "Copyright 2018, DeFacto Project"
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

def train_test_export_save_per_exp_type(estimator, estimator_label, hyperparameters, search_method,
                                        X_train, X_test, y_train, y_test, experiment_type, padding,
                                        out_chart, file_log, subfolder, exp_folder, ds_folder):

    try:
        config.logger.info(estimator_label)
        # file dump info
        file = BENCHMARK_FILE_NAME_TEMPLATE % (estimator_label.lower(), padding, experiment_type)

        ## loading the classifier
        #if isinstance(clf, str):
        #    clf = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'models/' + subfolder + clf)

        # grid search on 10-fold cross validation
        if experiment_type == EXP_2_CLASSES_LABEL:
            scoring = ['precision', 'recall', 'f1']
            refit = 'f1'
        elif experiment_type == EXP_3_CLASSES_LABEL:
            scoring = ['precision', 'precision_micro', 'precision_macro', 'precision_weighted',
                       'recall', 'recall_micro', 'recall_macro', 'recall_weighted',
                       'f1', 'f1_micro', 'f1_macro', 'f1_weighted']
            refit = 'f1_weighted'
        elif experiment_type == EXP_5_CLASSES_LABEL:
            scoring = ['r2', 'neg_mean_squared_error', 'neg_mean_absolute_error', 'explained_variance']
            refit='r2'

        else:
            raise Exception ('not supported! ' + experiment_type)

        if search_method == 'grid':
            clf = GridSearchCV(estimator, hyperparameters, cv=CROSS_VALIDATION_K_FOLDS, scoring=scoring, n_jobs=-1,
                               refit=refit)
        elif search_method == 'random':
            clf = RandomizedSearchCV(estimator, hyperparameters, cv=CROSS_VALIDATION_K_FOLDS, scoring=scoring, n_jobs=-1,
                                     refit=refit)
        else:
            raise Exception('not supported! ' + search_method)

        clf.fit(X_train, y_train)
        config.logger.info('best training set parameters: ')
        config.logger.info(clf.best_params_)
        config.logger.info(clf.best_score_)
        config.logger.info(experiment_type)
        predicted = clf.best_estimator_.predict(X_test)

        # saving the best model
        _path = OUTPUT_FOLDER + exp_folder + ds_folder + 'models/' + subfolder + experiment_type + '/'
        if not os.path.exists(_path):
            os.mkdir(_path)
        joblib.dump(clf.best_estimator_, _path + file)

        # saving the best parameters
        best_parameters_file_name = file.replace('.pkl', '.best_param')
        with open(_path + best_parameters_file_name, "w") as best:
            best.write(' -- best params \n')
            best.write(str(clf.best_params_))
            best.write(' -- best score \n')
            best.write(str(clf.best_score_))

        if experiment_type == EXP_2_CLASSES_LABEL or experiment_type == EXP_3_CLASSES_LABEL:
            p, r, f, s = precision_recall_fscore_support(y_test, predicted)
            p_weighted, r_weighted, f_weighted, s_weighted = precision_recall_fscore_support(y_test, predicted, average='weighted')
            p_micro, r_micro, f_micro, s_micro = precision_recall_fscore_support(y_test, predicted, average='micro')
            p_macro, r_macro, f_macro, s_macro = precision_recall_fscore_support(y_test, predicted, average='macro')

            if experiment_type == EXP_2_CLASSES_LABEL:
                d = LABELS_2_CLASSES
            else:
                d = LABELS_3_CLASSES

            for i in range(len(p)):
                file_log.write(LINE_TEMPLATE % (
                estimator_label, experiment_type, padding, d.get(i + 1), p[i], r[i], f[i], s[i], 0))
            file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, 'weighted', p_weighted, r_weighted, f_weighted, 0, 0))
            file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, 'micro', p_micro, r_micro, f_micro, 0, 0))
            file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, 'macro', p_macro, r_macro, f_macro, 0, 0))

            out_chart.append([p_weighted, r_weighted, f_weighted])
            config.logger.info(
                'padding: %s F1 test (avg): %.3f' % (padding, f_weighted))
            config.logger.info('----------------------------------------------------')

            file_log.flush()
            return out_chart, clf.best_estimator_

        elif experiment_type == EXP_5_CLASSES_LABEL:
            mae = mean_absolute_error(y_test, predicted)
            rmse = sqrt(mean_squared_error(y_test, predicted))
            evar = explained_variance_score(y_test, predicted)
            r2 = r2_score(y_test, predicted)
            file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, experiment_type, r2, rmse, mae, evar, 0))
            file_log.flush()
            config.logger.info(
                'padding: %s cls: %s exp_type: %s r2: %.3f rmse: %.3f mae: %.3f evar: %.3f' %
                (padding, estimator_label, experiment_type, r2, rmse, mae, evar))
            config.logger.info('----------------------------------------------------')
            return None, clf.best_estimator_

        else:
            raise Exception('not supported! ' + experiment_type)


        '''
        
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
        '''



    except Exception as e:
        config.logger.error(repr(e))
        raise

    #return test(clf, X_test, y_test, out_chart, padding, estimator_label, experiment_type, file_log, subfolder, exp_folder, ds_folder)

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
        # ch2 = SelectKBest(chi2, k=10)
        # X_train = ch2.fit_transform(X_train, y_train)
        # X_test = ch2.transform(X_test)
        #TODO: implement (only top 20th percentile is relevant? 20, 50, 80 and 100
    except:
        raise

def benchmark(X, y5, y3, y2, exp_folder, ds_folder, subfolder, random_state, test_size):

    config.logger.info('benchmark_text()')

    try:

        out_models_folder = OUTPUT_FOLDER + exp_folder + ds_folder + 'models/' + subfolder

        #input_layer_neurons = len(X) + 1
        #output_layer_neurons = 1
        #hidden_nodes = np.math.ceil(len(X) / (2 * (input_layer_neurons + output_layer_neurons)))

        X_train_5, X_test_5, y_train_5, y_test_5 = train_test_split(X, y5, test_size=test_size, random_state=random_state)
        X_train_3, X_test_3, y_train_3, y_test_3 = train_test_split(X, y3, test_size=test_size, random_state=random_state)
        X_train_2, X_test_2, y_train_2, y_test_2 = train_test_split(X, y2, test_size=test_size, random_state=random_state)


        # just to double check...
        assert np.all(X_train_5 == X_train_3)
        assert np.all(X_train_5 == X_train_2)

        scaler.fit(X_train_5)
        X_train = scaler.transform(X_train_5)
        X_test = scaler.transform(X_test_5)
        best_estimators = []

        x_axis_2 = []
        x_axis_3 = []
        y_axis_2 = []
        y_axis_3 = []

        title = 'Webpage Text Features'
        x_axis_label = 'Classifiers'
        y_axis_label = 'F1-measure'

        # --------------------------------------------------------------------------------------------------------------
        # classification experiment
        # --------------------------------------------------------------------------------------------------------------
        config.logger.info('starting experiments classification (2-classes and 3-classes)')
        i = 1
        for exp_type in (EXP_2_CLASSES_LABEL, EXP_3_CLASSES_LABEL):
            with open(out_models_folder + exp_type + 'perf.classification.log', "w") as file_log_classification:
                file_log_classification.write(HEADER)
                if exp_type == EXP_2_CLASSES_LABEL:
                    y_train = y_train_2
                    y_test = y_test_2
                    y_axis = y_axis_2
                    x_axis = x_axis_2
                    graph_file = 'graph.2-class.png'
                    threshold = THRESHOLD_LABEL_2class
                elif exp_type == EXP_3_CLASSES_LABEL:
                    y_train = y_train_3
                    y_test = y_test_3
                    y_axis = y_axis_3
                    x_axis = x_axis_3
                    graph_file = 'graph.3-class.png'
                    threshold = THRESHOLD_LABEL_3class
                else:
                    raise Exception('blah! error')

                for estimator, hyperparam, grid_method in CONFIGS_CLASSIFICATION:
                    out = []
                    out, best_estimator = train_test_export_save_per_exp_type(estimator, estimator.__class__.__name__, hyperparam, grid_method,
                                                              X_train, X_test, y_train, y_test, exp_type, 0,
                                                              out, file_log_classification, subfolder, exp_folder, ds_folder)
                    best_estimators.append((estimator.__class__.__name__, best_estimator))
                    i += 1
                    y_axis.extend(np.array(out)[:, 2])
                    x_axis.append(best_estimator.__class__.__name__.replace('Classifier', ''))

                estimator_ensamble = VotingClassifier(estimators=best_estimators)
                hyperparam_ensamble = dict(voting=['hard', 'soft'], flatten_transform=[True, False])

                out = []
                out, best_estimator = train_test_export_save_per_exp_type(estimator_ensamble, estimator_ensamble.__class__.__name__,
                                                                          hyperparam_ensamble, SEARCH_METHOD_GRID,
                                                          X_train, X_test, y_train, y_test, exp_type, 0,
                                                          out, file_log_classification, subfolder, exp_folder, ds_folder)
                y_axis.extend(np.array(out)[:, 2])
                x_axis.append(best_estimator.__class__.__name__.replace('Classifier', ''))

                config.logger.info('experiments classification done! exporting charts...')
                export_chart_bar(x_axis, y_axis, graph_file, exp_folder, title, x_axis_label, y_axis_label, threshold)
                config.logger.info('charts exported!')

            # --------------------------------------------------------------------------------------------------------------
            # regression experiment
            # --------------------------------------------------------------------------------------------------------------
            config.logger.info('starting experiments regression (5-classes)')

            with open(out_models_folder + exp_type + 'perf.regression.log', "w") as file_log_regression:
                file_log_regression.write(HEADER)
                for estimator, hyperparam, grid_method in CONFIGS_REGRESSION:
                    out = []
                    cls_label = estimator.__class__.__name__ + '_' + EXP_5_CLASSES_LABEL
                    train_test_export_save_per_exp_type(estimator, cls_label, hyperparam, grid_method,
                                                        X_train, X_test, y_train_5, y_test_5, EXP_5_CLASSES_LABEL, 0,
                                                        out, file_log_regression, subfolder, exp_folder, ds_folder)

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
            bnb_2 = [], bnb_3 = [], bnb_5 = []
            agg_2 = [], agg_3 = [], agg_5 = []
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
                cls, params, search_method = CONFIGS_HIGH_DIMEN[0]
                nb_5 = train_test_export_save_per_exp_type(cls, 'nb', params, search_method, X_train, X_test, y_train_5, y_test_5, EXP_5_CLASSES_LABEL, maxpad, nb_5, file_log, subfolder, exp_folder, ds_folder)
                nb_3 = train_test_export_save_per_exp_type(cls, 'nb', params, search_method, X_train, X_test, y_train_3, y_test_3, EXP_3_CLASSES_LABEL, maxpad, nb_3, file_log, subfolder, exp_folder, ds_folder)
                nb_2 = train_test_export_save_per_exp_type(cls, 'nb', params, search_method, X_train, X_test, y_train_2, y_test_2, EXP_2_CLASSES_LABEL, maxpad, nb_2, file_log, subfolder, exp_folder, ds_folder)
                # ==========================================================================================================
                # BernoulliNB
                # ==========================================================================================================
                cls, params, search_method = CONFIGS_HIGH_DIMEN[1]
                bnb_5 = train_test_export_save_per_exp_type(cls, 'sgd', params, search_method, X_train, X_test, y_train_5, y_test_5, EXP_5_CLASSES_LABEL, maxpad, bnb_5, file_log, subfolder, exp_folder, ds_folder)
                bnb_3 = train_test_export_save_per_exp_type(cls, 'sgd', params, search_method, X_train, X_test, y_train_3, y_test_3, EXP_3_CLASSES_LABEL, maxpad, bnb_3, file_log, subfolder, exp_folder, ds_folder)
                bnb_2 = train_test_export_save_per_exp_type(cls, 'sgd', params, search_method, X_train, X_test, y_train_2, y_test_2, EXP_2_CLASSES_LABEL, maxpad, bnb_2, file_log, subfolder, exp_folder, ds_folder)
                # ==========================================================================================================
                # K-means
                # ==========================================================================================================
                pca = PCA()
                X_tr_pca = pca.fit_transform(X_train)
                X_te_pca = pca.transform(X_test)
                cls, params, search_method = CONFIGS_HIGH_DIMEN[3]
                k_5 = train_test_export_save_per_exp_type(cls, 'kmeans', params, search_method, X_tr_pca, X_te_pca, y_train_5, y_test_5, EXP_5_CLASSES_LABEL, maxpad, k_5, file_log, subfolder, exp_folder, ds_folder)
                k_3 = train_test_export_save_per_exp_type(cls, 'kmeans', params, search_method, X_tr_pca, X_te_pca, y_train_3, y_test_3, EXP_3_CLASSES_LABEL, maxpad, k_3, file_log, subfolder, exp_folder, ds_folder)
                k_2 = train_test_export_save_per_exp_type(cls, 'kmeans', params, search_method, X_tr_pca, X_te_pca, y_train_2, y_test_2, EXP_2_CLASSES_LABEL, maxpad, k_2, file_log, subfolder, exp_folder, ds_folder)

                # ==========================================================================================================
                # AgglomerativeClustering
                # ==========================================================================================================
                cls, params, search_method = CONFIGS_HIGH_DIMEN[4]
                agg_5 = train_test_export_save_per_exp_type(cls, 'agg', params, search_method, X_train, X_test, y_train_5, y_test_5, EXP_5_CLASSES_LABEL, maxpad, agg_5, file_log, subfolder, exp_folder, ds_folder)
                agg_3 = train_test_export_save_per_exp_type(cls, 'agg', params, search_method, X_train, X_test, y_train_3, y_test_3, EXP_3_CLASSES_LABEL, maxpad, agg_3, file_log, subfolder, exp_folder, ds_folder)
                agg_2 = train_test_export_save_per_exp_type(cls, 'agg', params, search_method, X_train, X_test, y_train_2, y_test_2, EXP_2_CLASSES_LABEL, maxpad, agg_2, file_log, subfolder, exp_folder, ds_folder)

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


        EXP_CONFIGS = dict(EXP_FOLDER = ['exp010/', 'exp011/', 'exp012/'],
                           DS_FOLDER = ['microsoft/', 'c3/'],
                           FEATURES_FILE = ['features_basic_227.pkl',
                                            'features_basic+gi_277.pkl',
                                            'features_all_277.pkl'])

        RANDOM_STATE = 53
        #TOT_TEXT_FEAT = 53
        SERIES_COLORS = ['rgb(205, 12, 24)', 'rgb(22, 96, 167)', 'rgb(128, 128, 128)', 'rgb(0, 0, 139)',
                        'rgb(192,192,192)', 'rgb(211,211,211)', 'rgb(255,255,0)', 'rgb(0,128,0)']
        BAR_COLOR = 'rgb(128,128,128)'

        for conf in EXP_CONFIGS:
            '''
            ------------------------------------------------------------------
            01. TEXT FEATURES (only)
            ------------------------------------------------------------------
            '''
            config.logger.info('text feature benchmark')
            features_tex, y5, y3, y2 = get_text_features(conf['EXP_FOLDER'], conf['DS_FOLDER'], conf['FEATURES_FILE'], html2seq=False)

            benchmark(features_tex, y5, y3, y2, conf['EXP_FOLDER'], conf['DS_FOLDER'], 'text/', RANDOM_STATE, TEST_SIZE)

            ''' 
            ------------------------------------------------------------------
            02. HTML2Seq FEATURES (only)
            ------------------------------------------------------------------
            '''
            config.logger.info('html2seq feature benchmark')

            (features_seq, y5, y3, y2), le = get_html2sec_features(conf['EXP_FOLDER'], conf['DS_FOLDER'])

            benchmark_html_sequence(features_seq, y5, y3, y2, conf['EXP_FOLDER'], conf['DS_FOLDER'], RANDOM_STATE, TEST_SIZE, PADS)

            '''
            ------------------------------------------------------------------
            03. TEXT + HTML2Seq features combined (out of best configurations)
            ------------------------------------------------------------------
            '''
            config.logger.info('text+html2seq feature benchmark')

            features_combined, y5, y3, y2 = get_text_features(conf['EXP_FOLDER'], conf['DS_FOLDER'], conf['FEATURES_FILE'], html2seq=True)

            benchmark(features_combined, y5, y3, y2, conf['EXP_FOLDER'], conf['DS_FOLDER'], 'text+html/', RANDOM_STATE, TEST_SIZE)


    except Exception as e:
        config.logger.error(repr(e))
        raise