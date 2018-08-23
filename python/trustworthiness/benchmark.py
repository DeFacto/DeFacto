from keras.preprocessing.sequence import pad_sequences
from sklearn.cross_validation import train_test_split
from sklearn.grid_search import GridSearchCV
import plotly.plotly as py
import plotly.graph_objs as go
from sklearn.cluster import KMeans
from sklearn.decomposition import PCA
from sklearn.metrics import precision_recall_fscore_support, explained_variance_score, r2_score

from defacto.definitions import OUTPUT_FOLDER, TEST_SIZE, \
    HEADER, EXP_5_CLASSES_LABEL, EXP_3_CLASSES_LABEL, EXP_2_CLASSES_LABEL, LINE_TEMPLATE, \
    LABELS_2_CLASSES, LABELS_5_CLASSES, CROSS_VALIDATION_K_FOLDS, SEARCH_METHOD_RANDOMIZED_GRID, SEARCH_METHOD_GRID, \
    CONFIGS_CLASSIFICATION, CONFIGS_REGRESSION, CONFIGS_HIGH_DIMEN, LABELS_3_CLASSES, THRESHOLD_LABEL_2class, \
    THRESHOLD_LABEL_3class
from trustworthiness.benchmark_utils import train_test_export_save_per_exp_type
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
        if not os.path.exists(out_models_folder):
            os.makedirs(out_models_folder)

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
            with open(out_models_folder + exp_type + '/perf.classification.log', "w") as file_log_classification:
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

            with open(out_models_folder + exp_type + '/perf.regression.log', "w") as file_log_regression:
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


if __name__ == '__main__':
    try:

        ds = 'microsoft/'
        K1 ='9'
        exp ='exp010/'


        EXP_CONFIGS = [
            {'EXP_FOLDER': exp, 'DS_FOLDER': ds, 'FEATURES_FILE': 'features.basic.' + K1 + '.pkl'},
            {'EXP_FOLDER': exp, 'DS_FOLDER': ds, 'FEATURES_FILE': 'features.basic_gi.' + K1 + '.pkl'},
            {'EXP_FOLDER': exp, 'DS_FOLDER': ds, 'FEATURES_FILE': 'features.all.' + K1 + '.pkl'},
            {'EXP_FOLDER': exp, 'DS_FOLDER': ds, 'FEATURES_FILE': 'features.all+html2seq.' + K1 + '.pkl'},
            {'EXP_FOLDER': exp, 'DS_FOLDER': ds, 'FEATURES_FILE': 'features.html2seq.' + K1 + '.pkl'},
        ]


        exit(0)

        # benchmarking text features and text features + html2seq (with best HTML2seq model)

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
            02. TEXT + HTML2Seq features combined (out of best configurations)
            ------------------------------------------------------------------
            '''
            config.logger.info('text+html2seq feature benchmark')

            features_combined, y5, y3, y2 = get_text_features(conf['EXP_FOLDER'], conf['DS_FOLDER'], conf['FEATURES_FILE'], html2seq=True)

            benchmark(features_combined, y5, y3, y2, conf['EXP_FOLDER'], conf['DS_FOLDER'], 'text+html/', RANDOM_STATE, TEST_SIZE)


    except Exception as e:
        config.logger.error(repr(e))
        raise