from nltk import sent_tokenize


def get_topic_terms(self, document):
    try:
        sentences = sent_tokenize(document)
        count_train = self.count_vec.fit(sentences)
        bow = self.count_vec.transform(sentences)
        totals = bow.toarray().sum(axis=0)
        return self.count_vec.get_feature_names(), totals
    except:
        raise