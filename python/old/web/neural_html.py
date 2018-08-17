from keras.layers import Embedding, LSTM, Dense, Dropout, Conv1D, MaxPooling1D
from keras.models import Sequential
from keras.preprocessing import sequence
from sklearn.datasets import load_breast_cancer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.neural_network import MLPClassifier
from sklearn.metrics import classification_report, confusion_matrix
from keras.preprocessing.sequence import pad_sequences
from keras.preprocessing import sequence
from keras.preprocessing.text import Tokenizer
from keras.preprocessing.sequence import pad_sequences
from keras.utils import np_utils

import numpy as np

from algorithms.trust.definitions import FILES_PATH

np.random.seed(7)

# @aniketh, I' like to see results here with and without this parameter set
# let's try [top_words] and also [html_truncated_at_encode]
# - 100% of the dictionary
# - 75% of the dictionary
# - 50% of the dictionary
# - 25% of the dictionary

top_lines_per_document = 500
top_words_per_line_in_document = 1000
#the top n tokens of the dictionary
top_words = 50000
# the top n tokens from a given website. I have set it to the average size of the sequence. See below.
html_truncated_at_encode = 0

# pre-processing
all_text = ''
X = []
y = []

news_file = open(FILES_PATH+"news/url_file.txt","r").readlines()
blogs_file = open(FILES_PATH+"blogs/url_file.txt","r").readlines()
blacklisted_file = open(FILES_PATH+"blacklisted/url_file.txt","r").readlines()

# y=1 for news y=0.5 for blogs and y=0 for blacklisted websites
print('news data...')
filecount=0
for line in news_file:
    filecount+=1
    if filecount % 1000 == 0:
        print(str(filecount))
    if line=='':
        continue
    line = line.split('\t')
    file = open(FILES_PATH+"news/" + line[0] + '.html',"r")
    file_lines = file.readlines()
    text = ''
    count=0
    for file_line in file_lines:
        if count==top_lines_per_document:
            break
        file_line = file_line.strip()
        all_text = all_text + ' ' + file_line[0:top_words_per_line_in_document]
        text = text + file_line + '\n'
        count=count+1
    X.append(text)
    y.append(1)

print('blogs data...')
filecount=0
for line in blogs_file:
    if line=='':
        continue
    filecount += 1
    if filecount % 1000 == 0:
        print(str(filecount))
    line = line.split('\t')
    file = open(FILES_PATH+"blogs/" + line[0] + '.html',"r")
    file_lines = file.readlines()
    text = ''
    for file_line in file_lines:
        if count==top_lines_per_document:
            break
        file_line = file_line.strip()
        all_text = all_text + ' ' + file_line[0:top_words_per_line_in_document]
        text = text + file_line + '\n'
    X.append(text)
    y.append(0.5)

print('blacklist data...')
filecount=0
for line in blacklisted_file:
    if line=='':
        continue
    filecount += 1
    if filecount % 1000 == 0:
        print(str(filecount))
    line = line.split('\t')
    file = open(FILES_PATH+"blacklisted/" + line[0] + '.html',"r")
    file_lines = file.readlines()
    text = ''
    for file_line in file_lines:
        if count==top_lines_per_document:
            break
        file_line = file_line.strip()
        all_text = all_text + ' ' + file_line[0:top_words_per_line_in_document]
        text = text + file_line + '\n'
    X.append(text)
    y.append(0)

# words = list(set(all_text))  # distinct tokens
# word2ind = {word: index for index, word in enumerate(words)}  # indexes of words
# ind2word = {index: word for index, word in enumerate(words)}
# label2ind = {"news": 1, "blog": 2, "blacklist": 3}
# print('Vocabulary size:', len(word2ind), len(label2ind))
# lengths = [len(x) for x in X]
# html_truncated_at_encode = max(lengths)
# print('min sentence / max sentence: ', min(lengths), html_truncated_at_encode)


# max_label = max(label2ind.values()) + 1
# y_enc = [[0] * (html_truncated_at_encode - len(ey)) + [label2ind[c] for c in ey] for ey in y]
# print y_enc
# y_enc = [[encode(c, max_label) for c in ey] for ey in y_enc]
#
# max_features = len(word2ind)
# out_size = len(label2ind) + 1

#tfidf_vect = TfidfVectorizer(stop_words='english', max_features=top_words, max_df=0.7)
#tfidf_data = tfidf_vect.fit_transform(X)

#tokenize the text and convert them to sequences of integers
print('tokenizing...')
tokenizer = Tokenizer(num_words=top_words)
tokenizer.fit_on_texts(X)
X1 = tokenizer.texts_to_matrix(X, mode='tfidf')
X2 = tokenizer.texts_to_sequences(X)

X = X2
word_index = tokenizer.word_index

print("Vocabulary size = " + str(len(word_index.keys())))
len_sequences = [len(x) for x in X]
print("Max size of sequence = " + str(max(len_sequences)))
print("Min size of sequence = " + str(min(len_sequences)))
print("Average size of sequence = " + str(sum(len_sequences)/len(len_sequences)))

#set max length of sequence to average length
html_truncated_at_encode = sum(len_sequences)/len(len_sequences)

print('padding...')
#make all sequences of equal length = html_truncated_at_encode
X = pad_sequences(X, maxlen=html_truncated_at_encode)
y = np.asarray(y)

# split the data into training, validation and test sets - TODO - please do this according to your specifications. Maybe segregate the sources into test and train?
#indices = np.arange(X.shape[0])
#np.random.shuffle(indices)
#X = X[indices]
#y = y[indices]

print(X.shape)
print(y.shape)

print('configuring NNs...')
# LSTM
embedding_vecor_length = 64
model_lstm = Sequential()
model_lstm.add(Embedding(top_words, embedding_vecor_length, input_length=html_truncated_at_encode))
model_lstm.add(LSTM(128, dropout=0.2, recurrent_dropout=0.2))
model_lstm.add(Dense(1, activation='sigmoid'))
model_lstm.compile(loss='mse', optimizer='adam', metrics=['accuracy'])
print(model_lstm.summary())

# LSTM/CNN
model_cnn = Sequential()
model_cnn.add(Embedding(top_words, embedding_vecor_length, input_length=html_truncated_at_encode))
model_cnn.add(Conv1D(filters=32, kernel_size=3, padding='same', activation='relu'))
model_cnn.add(MaxPooling1D(pool_size=2))
model_cnn.add(LSTM(128))
model_cnn.add(Dense(1, activation='sigmoid'))
model_cnn.compile(loss='mse', optimizer='adam', metrics=['accuracy'])
print(model_cnn.summary())

random_state = [9, 22, 25, 33, 42]
for i in range(1):
    X_train, X_test, y_train, y_test = \
        train_test_split(X, y, test_size=0.2, random_state=random_state[i])
    model_lstm.fit(X_train, y_train, epochs=100, batch_size=64)
    model_cnn.fit(X_train, y_train, epochs=100, batch_size=64)
    scores_lstm = model_lstm.evaluate(X_test, y_test, verbose=0)
    scores_cnn = model_cnn.evaluate(X_test, y_test, verbose=0)
    print("accuracy = %.2f%%" % (scores_lstm[1] * 100))
    print("accuracy = %.2f%%" % (scores_cnn[1] * 100))