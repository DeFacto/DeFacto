
# coding: utf-8

# In[9]:


import pandas as pd
import numpy as np
import itertools

from IPython import get_ipython
from sklearn.feature_extraction.text import CountVectorizer, TfidfVectorizer, HashingVectorizer
from sklearn.model_selection import train_test_split
from sklearn.linear_model import PassiveAggressiveClassifier, SGDClassifier
from sklearn.svm import LinearSVC
from sklearn.naive_bayes import MultinomialNB
from sklearn import metrics
import matplotlib.pyplot as plt

from algorithms.natural_language_claims.temp import constants
from algorithms.natural_language_claims.temp.constants import LIAR_HEADER


LABEL_FIELD = 'label'
STATEMENT_FIELD = 'text' # 'statement' (fake news) or statement (liar liar)

LABEL_TRUE_FIELD = 'REAL' # 'true' #
LABEL_FALSE_FIELD = 'FAKE' # 'false' #


df = pd.read_csv(constants.DATASET_FAKE_OR_REAL_NEWS)
y = df.label
print(set(df.label))
df = df.drop(LABEL_FIELD, axis=1)

##df = pd.read_csv(constants.DATASET_PAINTS_ON_FIRE_TEST, sep='\t', names = LIAR_HEADER)
##temp = LIAR_HEADER.copy()
##temp.remove(STATEMENT_FIELD)
##temp.remove(LABEL_FIELD)
##df = df.drop(temp, axis=1)

#df.loc[df.label == 'half-true', LABEL_FIELD] = 'true'
#df.loc[df.label == 'mostly-true', LABEL_FIELD] = 'true'
#df.loc[df.label == 'barely-true', LABEL_FIELD] = 'true'

#df.loc[df.label == 'pants-fire', LABEL_FIELD] = 'false'



##df.drop(df[(df.label != LABEL_FALSE_FIELD) & (df.label != LABEL_TRUE_FIELD)].index, inplace=True)
##y = df.label
##df.drop(LABEL_FIELD, axis=1, inplace=True)

print(len(df))


X_train, X_test, y_train, y_test = train_test_split(df[STATEMENT_FIELD], y, test_size=0.33, random_state=53)

print(len(X_train))
print(len(X_test))

count_vectorizer = CountVectorizer(stop_words='english')
count_train = count_vectorizer.fit_transform(X_train)
count_test = count_vectorizer.transform(X_test)

tfidf_vectorizer = TfidfVectorizer(stop_words='english', max_df=0.7)
tfidf_train = tfidf_vectorizer.fit_transform(X_train)
tfidf_test = tfidf_vectorizer.transform(X_test)


mn_count_clf = MultinomialNB(alpha=0.1) 
pa_tfidf_clf = PassiveAggressiveClassifier(max_iter=50)
mn_tfidf_clf = MultinomialNB(alpha=0.1)
svc_tfidf_clf = LinearSVC()
sgd_tfidf_clf = SGDClassifier()


mn_count_clf.fit(count_train, y_train)
pred = mn_count_clf.predict(count_test)
score = metrics.accuracy_score(y_test, pred)
print("accuracy:   %0.3f" % score)

mn_tfidf_clf.fit(tfidf_train, y_train)
pred = mn_tfidf_clf.predict(tfidf_test)
score = metrics.accuracy_score(y_test, pred)
print("accuracy:   %0.3f" % score)

pa_tfidf_clf.fit(tfidf_train, y_train)
pred = pa_tfidf_clf.predict(tfidf_test)
score = metrics.accuracy_score(y_test, pred)
print("accuracy:   %0.3f" % score)

svc_tfidf_clf.fit(tfidf_train, y_train)
pred = svc_tfidf_clf.predict(tfidf_test)
score = metrics.accuracy_score(y_test, pred)
print("accuracy:   %0.3f" % score)

sgd_tfidf_clf.fit(tfidf_train, y_train)
pred = sgd_tfidf_clf.predict(tfidf_test)
score = metrics.accuracy_score(y_test, pred)
print("accuracy:   %0.3f" % score)

plt.figure(0).clf()

for model, name in [ (mn_count_clf, 'multinomial nb count'),
                     (mn_tfidf_clf, 'multinomial nb tfidf'),
                     (pa_tfidf_clf, 'passive aggressive'),
                     (svc_tfidf_clf, 'svc'),
                     (sgd_tfidf_clf, 'sgd')]:
    if 'count' in name:
        pred = model.predict_proba(count_test)[:,1]
    elif 'multinomial' in name:
        pred = model.predict_proba(tfidf_test)[:,1]
    else: 
        pred = model.decision_function(tfidf_test)
    fpr, tpr, thresh = metrics.roc_curve(y_test.values, pred, pos_label=LABEL_TRUE_FIELD)
    plt.plot(fpr,tpr,label="{}".format(name))

plt.legend(loc=0)


# ### Introspecting models
# My main goal for this notebook is not to compare accuracy, but to compare features learned.
# To do so, we can use the method shown in this [very useful StackOverflow answer]
# (https://stackoverflow.com/a/26980472) to show significant features in a binary classifier.
# I will use a modified version to return top features for each label.

def most_informative_feature_for_binary_classification(vectorizer, classifier, n=100):
    """
    See: https://stackoverflow.com/a/26980472
    
    Identify most important features if given a vectorizer and binary classifier. Set n to the number
    of weighted features you would like to show. (Note: current implementation merely prints and does not 
    return top classes.)
    
    Modified by @kjam to support a dict return.
    """

    class_labels = classifier.classes_
    feature_names = vectorizer.get_feature_names()
    topn_class1 = sorted(zip(classifier.coef_[0], feature_names))[:n]
    topn_class2 = sorted(zip(classifier.coef_[0], feature_names))[-n:]

    return {class_labels[0]: topn_class1,
            class_labels[1]: topn_class2
    }


print(most_informative_feature_for_binary_classification(tfidf_vectorizer, pa_tfidf_clf, n=20))



classifiers = [(mn_count_clf, count_vectorizer),
               (mn_tfidf_clf, tfidf_vectorizer),
               (pa_tfidf_clf, tfidf_vectorizer),
               (svc_tfidf_clf, tfidf_vectorizer),
               (sgd_tfidf_clf, tfidf_vectorizer)]

results = {}
for clf, vct in classifiers:
    results[clf] = most_informative_feature_for_binary_classification(vct, clf, n=10)

print(results)

# But this is both a bit hard to read and compare.
# What I really want is to see these possibly with ranks and compare the tokens to one another.
# Let's transform the data to look better for what we are trying to measure.

comparable_results = {LABEL_TRUE_FIELD: {}, LABEL_FALSE_FIELD: {}}
for clf, data in results.items():
    clf_name = clf.__class__.__name__
    for label, features in data.items():
        for rank, score_tuple in enumerate(features):
            if score_tuple[1] in comparable_results[label]:
                comparable_results[label][score_tuple[1]].append((rank + 1, clf_name))
            else:
                comparable_results[label][score_tuple[1]] = [(rank + 1, clf_name)]


# Now these are a bit easier to compare and read:

print(comparable_results[LABEL_FALSE_FIELD])


# I immediately noticed the multinomial models had picked up quite a bit of noise from the dataset.
# These models likely would have benefit from some preprocessing. I also noticed that *most* of the models
# had picked up what I would consider noise, such as `2016` and the words `print` and `share`
# (which are clearly scraping artifacts).
# Let's see if we can score the tokens by popularity and rank. I also wanted to add in a warning message
# in case I had overlap between my real and fake tokens.
#  (This may be the case if you take a larger n-features from each)

agg_results = {}
for label, features in comparable_results.items():
    for feature, ranks in features.items():
        if feature in agg_results:
            print("WARNING! DUPLICATE LABEL!!! {}".format(feature))
        agg_results[feature] = {
            'label': label,
            'agg_rank': np.mean([r[0] for r in ranks]),
            'count': len(ranks)
        }

comparison_df = pd.DataFrame(agg_results).T
comparison_df.head()

# To investigate the top real and fake labels, I would advise to sort by count.
# Let's see my top 10 tokens for real and fake news ranked by the number of classifiers
# that used them as a top feature.

print(comparison_df[comparison_df['label'] == LABEL_TRUE_FIELD].sort_values('count', ascending=0).head(20))
print(comparison_df[comparison_df['label'] == LABEL_FALSE_FIELD].sort_values('count', ascending=0).head(20))

# ### Conclusion
# 
# As expected, the bag-of-words and TF-IDF vectors didn't do much to determine meaningful
# features to classify fake or real news. As outlined in my DataCamp post, this problem is a lot
# harder than simple text classification.
# 
# That said, I did learn a few things. Namely, that linear models handle noise in this case better than
# the Naive Bayes multinomial classifier did. Also, finding a good dataset that has been scraped from the
# web and tagged for this problem would likely be a great help, and worth more of my time than parameter
# tuning on a clearly noisy and error prone dataset.
# 
# If you spend some time researching and find anything interesting, feel free to share your findings and notes
# in the comments or you can always reach out on Twitter (I'm [@kjam](https://twitter.com/kjam)).
# 
# I hope you had some fun exploring a new NLP dataset with me!

# ### Appendix A: Top features
# 
# Once I realized the Naive Bayes classifiers had identified many noisy tokens in alphabetical order as top
# fake news classifiers, I decided to see just how many "top features" the model had.

# In[40]:


feature_names = count_vectorizer.get_feature_names()
for idx, ftr_weight in enumerate(sorted(zip(mn_count_clf.coef_[0], feature_names))):
    if ftr_weight[0] <= -16.067750538483136:
        continue
    print(idx, ftr_weight)
    break

