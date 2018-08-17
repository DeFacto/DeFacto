import itertools
from sklearn import metrics
import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import CountVectorizer, TfidfVectorizer, HashingVectorizer
from sklearn.linear_model import PassiveAggressiveClassifier
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt
from sklearn.naive_bayes import MultinomialNB

df = pd.read_csv("https://s3.amazonaws.com/assets.datacamp.com/blog_assets/fake_or_real_news.csv")
df.shape
df.head()
df = df.set_index("Unnamed: 0")
df.head()
y = df.label
df.drop("label", axis=1)
X_train, X_test, y_train, y_test = train_test_split(df['text'], y, test_size=0.33, random_state=53)

#bag of words
count_vect = CountVectorizer(stop_words='english')
count_train = count_vect.fit_transform(X_train)
count_test = count_vect.transform(X_test)

#tfidf
tfidf_vect = TfidfVectorizer(stop_words='english', max_df=0.7)
tfidf_train = tfidf_vect.fit_transform(X_train)
tfidf_test = tfidf_vect.transform(X_test)

#hash
hash_vect = HashingVectorizer(stop_words='english', non_negative=True)
hash_train = hash_vect.fit_transform(X_train)
hash_test = hash_vect.transform(X_test)

# getting feature names
print(tfidf_vect.get_feature_names()[-10:])
print(count_vect.get_feature_names()[:10])

count_df = pd.DataFrame(count_train.A, columns=count_vect.get_feature_names())
tfidf_df = pd.DataFrame(tfidf_train.A, columns=tfidf_vect.get_feature_names())
difference = set(count_df.columns) - set(tfidf_df.columns)

print(count_df.equals(tfidf_df))
count_df.head()
tfidf_df.head()


def plot_confusion_matrix(filename, cm, classes, normalize=False, title='confusion matrix', cmap=plt.cm.Blues):
    """
    http://scikit-learn.org/stable/auto_examples/model_selection/plot_confusion_matrix.html
    """
    plt.imshow(cm, interpolation='nearest', cmap=cmap)
    plt.title(title)
    plt.colorbar()
    tick_marks = np.arange(len(classes))
    plt.xticks(tick_marks, classes, rotation=45)
    plt.yticks(tick_marks, classes)

    if normalize:
        cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]

    thresh = cm.max() / 2.
    for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
        plt.text(j, i, cm[i, j],
                 horizontalalignment="center",
                 color="red" if cm[i, j] > thresh else "black")

    plt.tight_layout()
    plt.ylabel('true')
    plt.xlabel('predicted')
    plt.savefig(filename, bbox_inches='tight')

clf = MultinomialNB(alpha=0.1)
last_score = 0
for alpha in np.arange(0,1,.1):
    nb_classifier = MultinomialNB(alpha=alpha)
    nb_classifier.fit(tfidf_train, y_train)
    pred = nb_classifier.predict(tfidf_test)
    score = metrics.accuracy_score(y_test, pred)
    if score > last_score:
        clf = nb_classifier
    print("Alpha: {:.2f} Score: {:.5f}".format(alpha, score))


def most_informative_feature(vectorizer, classifier, n=100):
    """
    See: https://stackoverflow.com/a/26980472
    most important features if given a vectorizer and binary classifier. n = number
    of weighted features to show
    """

    class_labels = classifier.classes_
    feature_names = vectorizer.get_feature_names()
    topn_class1 = sorted(zip(classifier.coef_[0], feature_names))[:n]
    topn_class2 = sorted(zip(classifier.coef_[0], feature_names))[-n:]

    for coef, feat in topn_class1:
        print(class_labels[0], coef, feat)

    print()

    for coef, feat in reversed(topn_class2):
        print(class_labels[1], coef, feat)

def process(clf, vectorizer, label, y_train, y_test):
    vec_train = vectorizer.fit_transform(X_train)
    vec_test = vectorizer.transform(X_test)
    clf.fit(vec_train, y_train)
    pred = clf.predict(vec_test)
    score = metrics.accuracy_score(y_test, pred)
    print("accuracy:   %0.3f" % score)
    cm = metrics.confusion_matrix(y_test, pred, labels=['FAKE', 'REAL'])
    plot_confusion_matrix(label, cm, classes=['FAKE', 'REAL'])


feature_names = tfidf_vect.get_feature_names()
### Most real
sorted(zip(clf.coef_[0], feature_names), reverse=True)[:20]
### Most fake
sorted(zip(clf.coef_[0], feature_names))[:20]
tokens_with_weights = sorted(list(zip(feature_names, clf.coef_[0])))

clf_nb = MultinomialNB()
clf_linear_pa = PassiveAggressiveClassifier(n_iter=50)
clf_nb_alpha001 = MultinomialNB(alpha=.01)

process(clf_nb, tfidf_vect, 'nb_tfidf', y_train, y_test)
process(clf_nb, count_vect, 'nb_bow', y_train, y_test)
process(clf_linear_pa, tfidf_vect, 'linear_tfidf', y_train, y_test)
process(clf_nb_alpha001, hash_vect, 'nb_hash', y_train, y_test)
process(clf_linear_pa, hash_vect, 'pa_hash', y_train, y_test)

most_informative_feature(tfidf_vect, clf_linear_pa, n=30)