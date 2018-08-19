import numpy as np
import sklearn
from sklearn.metrics import accuracy_score
from sklearn import svm
from sklearn.metrics import confusion_matrix
from sklearn.model_selection import train_test_split
from sklearn.model_selection import KFold
import matplotlib.pyplot as plt
import itertools
import csv

folds = 10

def plot_confusion_matrix(cm, classes,
                          normalize=False,
                          title='Confusion matrix',
                          cmap=plt.cm.Blues):
    """
    This function prints and plots the confusion matrix.
    Normalization can be applied by setting `normalize=True`.
    """
    plt.imshow(cm, interpolation='nearest', cmap=cmap)
    plt.title(title)
    plt.colorbar()
    tick_marks = np.arange(len(classes))
    plt.xticks(tick_marks, classes, rotation=45)
    plt.yticks(tick_marks, classes)

    if normalize:
        cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
        print("Normalized confusion matrix")
    else:
        print('Confusion matrix, without normalization')

    print(cm)

    thresh = cm.max() / 2.
    for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
        plt.text(j, i, cm[i, j],
                 horizontalalignment="center",
                 color="white" if cm[i, j] > thresh else "black")

    plt.tight_layout()
    plt.ylabel('True label')
    plt.xlabel('Predicted label')

x = [] #input features
y = [] #output labels

#read csv file containing data
file = open('proofs_manually_annotated_527.csv')
reader = csv.reader(file)
for row in reader:
	features = row[:9]
	for i in xrange(0,len(features)):
		features[i] = float(features[i])
	x.append(features)
	if row[-1] == 'TRUE':
		y.append(1)
	else:
		y.append(0)

sample_size = len(x)

x = np.array(x)
y = np.array(y)

indices = np.arange(x.shape[0])
np.random.shuffle(indices)
x = x[indices]
y = y[indices]

kf = KFold(n_splits=folds)

avg_accuracy = 0

for train_index, test_index in kf.split(x):
  x_train, x_test = x[train_index], x[test_index]
  y_train, y_test = y[train_index], y[test_index]
  clf = svm.SVC(kernel='rbf')
  clf.fit(x_train,y_train)
  res = clf.predict(x_test)
  print accuracy_score(y_test, res)
  avg_accuracy = avg_accuracy + accuracy_score(y_test,res)

avg_accuracy = avg_accuracy/folds

print avg_accuracy