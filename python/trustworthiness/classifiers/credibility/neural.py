import os
from bs4 import BeautifulSoup
from bs4 import Comment
from keras import Sequential
from keras.layers import LSTM, TimeDistributed, Dense
from keras.preprocessing.sequence import pad_sequences
from sklearn.externals import joblib
from pathlib import Path
from config import WebTrustworthinessConfig
import numpy as np
np.random.seed(7)

config = WebTrustworthinessConfig()

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

    n_timesteps = 5

    X = data[0]
    y = data[1]

    X = np.array(X)#.reshape(1, n_timesteps, 1)
    y = np.array(y)#.reshape(1, n_timesteps, 1)

    maxsent=1000
    X = pad_sequences(X, maxlen=maxsent, dtype='float', padding='pre', truncating='pre', value=0.0)


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

