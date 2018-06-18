import time
from sklearn import datasets

from sklearn.metrics import confusion_matrix, classification_report, mean_absolute_error, mean_squared_error
from sklearn.model_selection import train_test_split, GridSearchCV
from itertools import product

from sklearn.neural_network import MLPClassifier

from config import WebTrustworthinessConfig
import pandas as pd
import collections

from sklearn.naive_bayes import *
from sklearn.dummy import *
from sklearn.ensemble import *
from sklearn.neighbors import *
from sklearn.tree import *
from sklearn.calibration import *
from sklearn.linear_model import *
from sklearn.multiclass import *
from sklearn.svm import *
from sklearn.externals import joblib
from sklearn.metrics import accuracy_score, recall_score, precision_score, f1_score
import numpy as np
import matplotlib.pyplot as plt

from src.core.feature_extractor import get_full_features

__author__ = "Diego Esteves"
__copyright__ = "Copyright 2018, DeFacto Project"
__credits__ = ["Diego Esteves", "Aniketh Reddy", "Piyush Chawla", "Jens Lehmann"]
__license__ = "Apache"
__version__ = "0.0.1"
__maintainer__ = "Diego Esteves"
__email__ = "diegoesteves@gmail.com"
__status__ = "Dev"

config = WebTrustworthinessConfig()


class WebCredibilityExperiments():
    def __init__(self, exp_folder):
        self.version = '0.1'
        self.exp_folder = exp_folder

    def get_plot_voting(self, X, y, classifiers, labels):


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


    def get_report_regression(self, clf_name, predictions, y_test, targets):
        print('MAE', mean_absolute_error(y_test, predictions))
        print('RMSE', np.math.sqrt(mean_squared_error(y_test, predictions)))
        print("-----------------------------------------------------------------------")

    def get_report(self, clf_name, predictions, y_test, targets):

        print("Classifier: ", clf_name)
        print(confusion_matrix(y_test, predictions))
        print("accuracy: ", accuracy_score(y_test, predictions))
        print(classification_report(y_test, predictions, target_names=targets))
        #print(":: recall: ", recall_score(y_test, predictions, average='weighted'))
        #print(":: precision: ", precision_score(y_test, predictions, average='weighted'))
        #print(":: f1: ", f1_score(y_test, predictions, average='weighted'))
        print("-----------------------------------------------------------------------")

    def mlp_param_selection(self, X, y, nfolds):
        alphas = 10.0 ** -np.arange(1, 7)
        solvers = ['lbfgs', 'adam', 'sgd']
        param_grid = {'solver': solvers, 'alpha': alphas}
        grid_search = GridSearchCV(MLPClassifier(), param_grid, cv=nfolds)
        grid_search.fit(X, y)
        return grid_search.best_params_

    def train_baselines(self, features):
        path_models = config.dir_models + '/credibility/'

        parameters_svm = {'vect__ngram_range': [(1, 1), (1, 2)],
                          'tfidf__use_idf': (True, False),
                          'clf-svm__alpha': (1e-2, 1e-3)}

        input_layer_neurons = len(features[0].get('features')) + 1
        output_layer_neurons = 1
        hidden_nodes = np.math.ceil(len(features) / (2 * (input_layer_neurons + output_layer_neurons)))

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
            PassiveAggressiveClassifier(),
            RidgeClassifier(),
            RidgeClassifierCV(),
            SGDClassifier(),
            LogisticRegression(),
            KNeighborsClassifier(),
            MLPClassifier(hidden_layer_sizes=(hidden_nodes,hidden_nodes,hidden_nodes), solver='adam', alpha=1e-05)
            #OneVsRestClassifier(SVC(kernel='linear', probability=True))

        ]

        XX = []
        y = []
        y2 = []
        encoder = joblib.load(config.enc_domain)
        for d in features:
            feat = d.get('features')
            if feat is None:
                raise Exception('error in the feature extraction! No features extracted...')
            #feat[2] = encoder.transform([feat[2]])
            feat[3] = encoder.transform([feat[3]])
            del feat[2]
            XX.append(feat)
            likert=int(d.get('likert'))
            y.append(likert)
            if likert in (1,2,3):
                y2.append(0)
            elif likert in (4,5):
                y2.append(1)
            # elif likert==3:
            #    y2.append(2)
            else:
                raise Exception('error y')
            #elif likert in (1,2,3):
            #    y2.append(0)
            #else:
            #    y2.append(-1)

        from sklearn.preprocessing import StandardScaler
        scaler = StandardScaler()


        i = 1
        print('split data...')
        X_train, X_test, y_train, y_test = train_test_split(XX, y2, test_size=0.20, random_state=42)

        scaler.fit(X_train)
        X_train = scaler.transform(X_train)
        X_test = scaler.transform(X_test)

        #print(self.mlp_param_selection(XX, y2, 5))
        #exit(0)

        estimators = []
        models = []
        labels = []
        #klass_labels=['not reliable', 'trustworthy', 'borderline']
        klass_labels = ['non-credible', 'credible']
        try:
            for clf in classifiers:
                string = ''
                string += clf.__class__.__name__
                clf.fit(X_train, y_train)
                models.append(clf)
                labels.append(string)
                joblib.dump(clf, path_models + 'clf_' + str(i) + '_cred_.pkl')
                estimators.append((string, clf))
                i += 1
                predictions = clf.predict(X_test)
                self.get_report(string, predictions, y_test, klass_labels)

                #gs_clf_svm = GridSearchCV(clf, parameters_svm, n_jobs=-1)
                #gs_clf_svm = gs_clf_svm.fit(X_train, y_train)
                #print(gs_clf_svm.best_score_)
                #print(gs_clf_svm.best_params_)

            eclf1 = VotingClassifier(estimators=estimators, voting='hard')
            eclf1.fit(X_train, y_train)
            models.append(eclf1)
            labels.append('Voting')
            epredictions = eclf1.predict(X_test)
            self.get_report('Voting', epredictions, y_test, klass_labels)
            #self.get_plot_voting(X_test, y_test, models[0:4], labels[0:4])
        except Exception as e:
            print(e)


    def benchmark(self):
        try:
            features = get_full_features(self.exp_folder)
            self.train_baselines(features)
        except Exception as e:
            print(e)


if __name__ == '__main__':
    cred = WebCredibilityExperiments('exp001/')
    cred.benchmark()
