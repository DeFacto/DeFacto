import os
from bs4 import BeautifulSoup
from bs4 import Comment
from keras import Sequential
from keras.layers import LSTM, TimeDistributed, Dense
from keras.preprocessing.sequence import pad_sequences
from sklearn.cluster import KMeans
from sklearn.cross_validation import train_test_split
from sklearn.decomposition import PCA
from sklearn.externals import joblib
from pathlib import Path

from sklearn.grid_search import GridSearchCV
from sklearn.linear_model import SGDClassifier
from sklearn.metrics import precision_recall_fscore_support

from config import DeFactoConfig
import numpy as np
import plotly.plotly as py
import plotly.graph_objs as go
import math
from sklearn import metrics
from sklearn.cluster import KMeans
from sklearn.datasets import load_digits
from sklearn.decomposition import PCA
from sklearn.preprocessing import scale
import matplotlib.pyplot as plt
from trustworthiness.classifiers.credibility.util import print_report


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


def get_annotation_from_max(traces, paddings):

    annotations = []
    for trace in traces:
        max_trace=-999999
        index=-1
        for aux in range(len(trace)):
            if trace[aux] > max_trace:
                max_trace, index = trace[aux], aux
        annotations.append(dict(
            #x=np.log(paddings[index]),
            x=paddings[index],
            y=max_trace,
            xref='x',
            yref='y',
            text='{0:.3f}'.format(max_trace),
            showarrow=True,
            font=dict(
                family='Courier New, monospace',
                size=14,
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
        ))

    return annotations

def save_plot(paddings, f1_traces):


    paddings = [math.log(pad) for pad in paddings]

    trace0 = go.Scatter(
        x=paddings,
        y=f1_traces[0],
        text=f1_traces[0],
        name='Multinomial NB [1-5]',
        mode='lines+markers',
        line=dict(
            color=('rgb(205, 12, 24)'),
            width=2)
    )
    trace1 = go.Scatter(
        x=paddings,
        y=f1_traces[1],
        text=f1_traces[1],
        name='SVM [1-5]',
        mode='lines+markers',
        line=dict(
            color=('rgb(22, 96, 167)'),
            width=2)
    )
    trace2 = go.Scatter(
        x=paddings,
        y=f1_traces[2],
        text=f1_traces[2],
        name='Multinomial NB [0-1]',
        mode='lines+markers',
        line=dict(
            color=('rgb(205, 12, 24)'),
            width=2,
            dash='dash')  # dash options include 'dash', 'dot', and 'dashdot'
    )
    trace3 = go.Scatter(
        x=paddings,
        y=f1_traces[3],
        text=f1_traces[3],
        name='SVM [0-1]',
        mode='lines+markers',
        line=dict(
            color=('rgb(22, 96, 167)'),
            width=2,
            dash='dash')
    )

    data = [trace0, trace1, trace2, trace3]

    # Edit the layout
    layout = dict(title='Encoding HTML code: performance varying window size',
                  xaxis=dict(title='Padding window size (log scale)', showticklabels=True, showline=True, autorange=True),
                  yaxis=dict(title='F1-measure (average)', showticklabels=True, showline=True, autorange=True),
                  show_legend=True,
                  legend=dict(orientation='h',
                              x=math.log(1),
                              y=-20,
                              bordercolor='#808080',
                              borderwidth=2
                              ),
                  annotations=get_annotation_from_max(f1_traces, paddings),
                  font=dict(family='Helvetica', size=14)
                  )

    fig = dict(data=data, layout=layout)
    #py.plot(fig, filename='paddings_f1')
    py.image.save_as(fig, filename='paddings_f1.png')


np.random.seed(7)

config = DeFactoConfig()

expfolder = 'exp002/'
tags_set = []
sentences = []
y = []
tot_files = 0

my_file = Path(config.dir_output + expfolder + 'microsoft_dataset_visual.data.nn')
if not my_file.exists():
    for file in os.listdir(config.dir_output + expfolder):
        tags=[]
        if file.endswith(".txt"):
            tot_files += 1
            print('processing file ' + str(tot_files))
            soup = BeautifulSoup(open(config.dir_output + expfolder + file), "html.parser")
            html = soup.prettify()
            for line in html.split('\n'):
                if isinstance(line, str) and len(line.strip())>0:
                    if (line.strip()[0]=='<') and (line.strip()[0:2]!='<!'):
                        if len(line.split())>1:
                            tags.append(line.split()[0] + '>')
                        else:
                            tags.append(line.split()[0])
                    elif (line.strip()[0:2] =='</' and line.strip()[0:2]!='<!'):
                        tags.append(line.split()[0])

            if len(tags) > 0:
                sentences.append(tags)
                tags_set.extend(tags)
                tags_set = list(set(tags_set))
                print(len(tags))
                print(tags)

            # getting y
            features_file = file.replace('microsoft_dataset_visual_features_', 'microsoft_dataset_features_')
            features_file = features_file.replace('.txt', '.pkl')

            data = joblib.load(config.dir_output + expfolder + features_file)
            y.append(data['likert'])


    print('tot files', tot_files)
    print(len(tags_set))
    print(tags_set)

    from sklearn import preprocessing
    le = preprocessing.LabelEncoder()
    le.fit(tags_set)

    X = [le.transform(s) for s in sentences]
    print(len(X))
    print(len(y))

    joblib.dump((X,y), config.dir_output + expfolder + 'microsoft_dataset_visual.data.nn')
    joblib.dump(le, config.dir_output + expfolder + 'microsoft_dataset_visual.le.nn')

else:
    data = joblib.load(config.dir_output + expfolder + 'microsoft_dataset_visual.data.nn')
    le = joblib.load(config.dir_output + expfolder + 'microsoft_dataset_visual.le.nn')

    X = data[0]
    y = data[1]

    likert_labels = {1: 'non-credible', 2: 'low', 3: 'neutral', 4: 'likely', 5: 'credible'}
    likert_labels_short = {0: 'non-credible', 1: 'credible'}

    maxsent=-1
    for e in X:
        maxsent = len(e) if len(e) > maxsent else maxsent
    print(maxsent)
    maxpad = 1000

    line_template = '%s\t%s\t%s\t%.3f\t%.3f\t%.3f\n'

    with open(config.dir_output + expfolder  + '_performances.nn', "w") as file:
        pads = [500, 1000, 1250, 1500, 1750, 2000, 2250, 2500, 2750, 3000, 3500, 4000, 4500, 5000, 6000, 7000, 8000, 9000, 10000, 20000, 30000]
        #pads = [500, 1000, 1250, 1500]
        nb_01_prec = []
        nb_01_recal = []
        nb_01_f1 = []
        nb_15_prec = []
        nb_15_recal = []
        nb_15_f1 = []

        svm_01_prec = []
        svm_01_recal = []
        svm_01_f1 = []
        svm_15_prec = []
        svm_15_recal = []
        svm_15_f1 = []


        for maxpad in pads:
            print('padding: ', maxpad)

            XX = pad_sequences(X, maxlen=maxpad, dtype='int', padding='pre', truncating='pre', value=0)
            X_train, X_test, y_train, y_test = train_test_split(XX, y, test_size=0.20, random_state=53)

            y2_train=np.array(y_train)
            y2_train[y2_train<4] = 0
            y2_train[y2_train>=4] = 1

            y2_test=np.array(y_test)
            y2_test[y2_test<4] = 0
            y2_test[y2_test>=4] = 1


            # NB
            from sklearn.naive_bayes import MultinomialNB
            clf = MultinomialNB().fit(X_train, y_train)
            clf2 = MultinomialNB().fit(X_train, y2_train)
            predicted = clf.predict(X_test)
            predicted2 = clf2.predict(X_test)
            np.mean(predicted == y_test)
            #print_report('html_encoded_visual_nb', predicted, y_test, likert_labels.values())
            #print_report('html_encoded_visual_nb', predicted2, y2_test, likert_labels_short.values())

            p, r, f, s = precision_recall_fscore_support(y_test, predicted, average='weighted')
            nb_15_prec.append(p)
            nb_15_recal.append(r)
            nb_15_f1.append(f)
            #file.write(line_template % ('NB', '1-5', maxpad, p, r, f))

            p, r, f, s = precision_recall_fscore_support(y2_test, predicted2, average='weighted')
            #file.write(line_template % ('NB', '0-1', maxpad, p, r, f))
            nb_01_prec.append(p)
            nb_01_recal.append(r)
            nb_01_f1.append(f)

            # SVM
            clf = SGDClassifier(loss='hinge', penalty='l2', alpha = 1e-3).fit(X_train, y_train)
            clf2 = SGDClassifier(loss='hinge', penalty='l2', alpha=1e-3).fit(X_train, y2_train)
            predicted = clf.predict(X_test)
            predicted2 = clf2.predict(X_test)
            #print_report('html_encoded_visual_svm', predicted, y_test, likert_labels.values())
            #print_report('html_encoded_visual_svm', predicted2, y2_test, likert_labels_short.values())

            p, r, f, s = precision_recall_fscore_support(y_test, predicted, average='weighted')
            #file.write(line_template % ('SVM', '1-5', maxpad, p, r, f))
            svm_15_prec.append(p)
            svm_15_recal.append(r)
            svm_15_f1.append(f)

            p, r, f, s = precision_recall_fscore_support(y2_test, predicted2, average='weighted')
            #file.write(line_template % ('SVM', '0-1', maxpad, p, r, f))
            svm_01_prec.append(p)
            svm_01_recal.append(r)
            svm_01_f1.append(f)

            #K-means
            kmeans = KMeans(n_clusters=5, random_state=0).fit(X_train, y_train)
            predicted2=kmeans.predict(X_test)
            print('---', np.mean(predicted2 == y_test))


            #k_means(XX, y)
            #exit(0)




    save_plot(pads, [nb_15_f1, svm_15_f1, nb_01_f1, svm_01_f1])
    exit(0)

    # gridsearch
    parameters = {'alpha': (1e-2, 1e-3)}
    gs_clf = GridSearchCV(clf2, parameters, n_jobs=-1)
    gs_clf = gs_clf.fit(X_train, y2_train)
    print(gs_clf.best_score_)
    print(gs_clf.best_params_)


    from sklearn.feature_extraction.text import CountVectorizer
    count_vect = CountVectorizer()
    X_train_counts = count_vect.fit_transform(data[0])
    print(X_train_counts.shape)




    n_timesteps = 5



    X = np.array(X)#.reshape(1, n_timesteps, 1)
    y = np.array(y)#.reshape(1, n_timesteps, 1)



    model = Sequential()
    print(X.shape)
    model.add(LSTM(20, input_shape=(X.shape[0], 1), return_sequences=True))
    model.add(TimeDistributed(Dense(4, activation='sigmoid')))
    model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['acc'])
    print(model.summary())
    model.fit(X, y, epochs=10, batch_size=32, verbose=2, validation_split=0.33)
    #yhat = model.predict_classes(X, verbose=2)

    #for i in range(n_timesteps):
     #   print('Expected:', y[0, i], 'Predicted', yhat[0, i])

