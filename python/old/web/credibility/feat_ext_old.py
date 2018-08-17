from sklearn.externals import joblib
from textstat.textstat import textstat
from classifiers.credibility.util import get_html_file_path, get_features_web
from core.web.scrap import WebScrap
from config import DeFactoConfig
from sumy.parsers.html import HtmlParser
from sumy.parsers.plaintext import PlaintextParser
from sumy.nlp.tokenizers import Tokenizer
from sumy.summarizers.lsa import LsaSummarizer as Summarizer
from sumy.nlp.stemmers import Stemmer
from sumy.utils import get_stop_words
from singleton_decorator import singleton
from utils.microsoft_azure_helper import MicrosoftAzurePlatform

from keras.datasets import imdb
from keras.models import load_model
from keras.preprocessing import sequence

import re
import numpy as np
import pandas as pd

import socket
import ipaddress
from geolite2 import geolite2
from core.web.credibility.topicUtils import TopicTerms

from defacto.definitions import OUTPUT_FOLDER

__author__ = "Diego Esteves"
__copyright__ = "Copyright 2018, DeFacto Project"
__credits__ = ["Diego Esteves", "Aniketh Reddy", "Piyush Chawla", "Jens Lehmann"]
__license__ = "Apache"
__version__ = "0.0.1"
__maintainer__ = "Diego Esteves"
__email__ = "diegoesteves@gmail.com"
__status__ = "Dev"

config = DeFactoConfig()

DataTable = pd.read_table("filepath",sep="",header=None,names=["topic","query","rank","url","rating"])

class Singleton(object):
    _instance = None
    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            print('....')
            cls._instance = object.__new__(cls, *args, **kwargs)
        return cls._instance

@singleton
class Classifiers():
    def __init__(self):
        print('entering...')
        path_textclass = config.dir_models + 'textcategory/'
        self.clf_textclass_1 = joblib.load(path_textclass + 'clf_business_category_multinomialnb_tfidf.pkl')
        self.vec_textclass_1 = joblib.load(path_textclass + 'vec_business_category_multinomialnb_tfidf.pkl')
        self.clf_textclass_2 = joblib.load(path_textclass + 'clf_entertainment_category_multinomialnb_tfidf.pkl')
        self.vec_textclass_2 = joblib.load(path_textclass + 'vec_entertainment_category_multinomialnb_tfidf.pkl')
        self.clf_textclass_3 = joblib.load(path_textclass + 'clf_politics_category_multinomialnb_tfidf.pkl')
        self.vec_textclass_3 = joblib.load(path_textclass + 'vec_politics_category_multinomialnb_tfidf.pkl')
        self.clf_textclass_4 = joblib.load(path_textclass + 'clf_religion_category_multinomialnb_tfidf.pkl')
        self.vec_textclass_4 = joblib.load(path_textclass + 'vec_religion_category_multinomialnb_tfidf.pkl')
        self.clf_textclass_5 = joblib.load(path_textclass + 'clf_sports_category_multinomialnb_tfidf.pkl')
        self.vec_textclass_5 = joblib.load(path_textclass + 'vec_sports_category_multinomialnb_tfidf.pkl')
        self.clf_textclass_6 = joblib.load(path_textclass + 'clf_tech_category_multinomialnb_tfidf.pkl')
        self.vec_textclass_6 = joblib.load(path_textclass + 'vec_tech_category_multinomialnb_tfidf.pkl')

        path_spam = config.dir_models + '/spam/'
        self.clf_spam_1 = joblib.load(path_spam + 'clf_41_spam_onevsrestclassifier_tfidf.pkl')
        self.vec_spam_1 = joblib.load(path_spam + 'vec_41_spam_onevsrestclassifier_tfidf.pkl')

        path_sentiment = config.dir_models + '/sentimentanalysis/'
        self.clf_sentiment_1 = load_model(path_sentiment + 'imdb_1600')
        self.vec_sentiment_1 = imdb.get_word_index()

class FeatExtOld:
    """The feature extractor for the trustworthiness module.

    It implements a set of feature extractors for a given web page.
    """

    def __init__(self, url, timeout=15, local_file_path=None, error=False):
        self.DataTable = pd.read_table("web_credibility_1000_url_ratings.csv",sep=",",header=None,names=["topic","query","rank","url","rating"])
        self.url = url
        self.error = error
        self.webscrap = WebScrap(url, timeout, 'lxml', local_file_path)
        self.title = self.webscrap.title
        self.body = self.webscrap.body
        #TODO: uncomment this later...
        #bing = MicrosoftAzurePlatform(config.translation_secret)
        #if bing.bing_detect_language(self.title) != 'en':
        #    a = bing.bing_translate_text(self.title, 'en')
        #    self.title = a
        #if bing.bing_detect_language(self.body) != 'en':
        #    b = bing.bing_translate_text(self.body, 'en')
        #    self.body = b
        clf = Classifiers()
        self.classifiers = clf

    def filterTerm(self,word):
        temp = word.lower()
        return re.sub(r"[^A-Za-z]+", '',temp)

    def get_feat_majorityweb(self):
        query = DataTable[DataTable['url'] == self.url].iloc[0,1]
        websites = DataTable[DataTable['query'] == query]
        terms =  topic.extractTopicTerm(query)
        DF = 0
        pageTerms = topic.generatePageTerms(self.url)
        pageTerms= sorted(pageTerms, key=pageTerms.get, reverse = True)
        inter = [item for item in terms if item in pageTerms]
        parameter_s = 5  #TODO tune this
        inter = inter[:parameter_s]
        for i in range(int(websites.shape[0])):
            website_i = websites['url'].iloc[i]
            if website_i == self.url:
                continue
            try:
                pageTerms = topic.generatePageTerms(website_i)
                pageTerms= sorted(pageTerms, key=pageTerms.get, reverse = True)
            except:
                print('exception occured')
                continue
            inter_i = [item for item in terms if item in pageTerms]
            if len(set(inter).intersection(inter_i)) >=  parameter_s-2:
                DF+=1
        return DF

    def get_feat_coverage(self):
        query = DataTable[DataTable['url'] == self.url].iloc[0,1]
        websites = DataTable[DataTable['query'] == query]
        terms =  topic.extractTopicTerm(query)
        pageTerms = topic.generatePageTerms(self.url)
        pageTerms= sorted(pageTerms, key=pageTerms.get, reverse = True)
        intersection = [item for item in terms if item in pageTerms]
        return (len(intersection)*1.0)/len(terms)

    def get_feat_qtermstitle(self):
        title = self.title
        returnVal = 0
        query = DataTable[DataTable['url'] == self.url].iloc[0,1]
        query = query.split(' ')
        title = title.split(' ')
        for i in range(len(query)):
            query[i] = filterTerm(query[i])
        for i in query:
            for j in title:
                if i in j:
                    returnVal+=1
        return returnVal

    def get_feat_qtermsbody(self):
        returnVal = 0
        query = DataTable[DataTable['url'] == self.url].iloc[0,1]
        query = query.split(' ')
        for i in range(len(query)):
            query[i] = filterTerm(query[i])
        try:
            pageTerms = topic.generatePageTerms(self.url)
        except:
            print("exception occured")
            return -1
        for i in query:
            if i in pageTerms:
                returnVal+=pageTerms[i]
        return returnVal

    def findIP(self):
        url = self.url
        if "http://" in url:
            url = url[7:]
        if "www." in url:
            url = url[4:]
        url = url.split('/')
        url = url[0]
        ip = socket.gethostbyname(url)
        reader = geolite2.reader()
        return reader.get(ip)

    def rms(self,vec):
        vec = np.multiply(vec,vec)
        vec = np.sum(vec)
        return np.sqrt(vec)

    def distance(self,vec1,vec2):
        vec1 = np.array(vec1)
        vec2 = np.array(vec2)
        prod = np.sum(np.multiply(vec1,vec2))
        return (prod*1.0)/(self.rms(vec1)*self.rms(vec2))

    def get_feat_majoritysearch(self):
        query = DataTable[DataTable['url'] == self.url].iloc[0,1]
        websites = DataTable[DataTable['query'] == query]
        terms =  topic.extractTopicTerm(query)
        termList = []
        websiteTerms = topic.generatePageTerms(self.url)
        websiteTerms = websiteTerms.values()
        websiteTerms = sorted(websiteTerms,reverse=True)
        length = len(websiteTerms)

        for i in range(int(websites.shape[0])):
            website_i = websites['url'].iloc[i]
            if websites['url'].iloc[i] == self.url:
                continue
            try:
                pageTerms = topic.generatePageTerms(website_i)
                pageTerms = pageTerms.values()
                pageTerms = sorted(pageTerms,reverse=True)
            except:
                print('exception occured')
                continue
            if len(pageTerms) == 0:
                continue
            length = min(length,len(pageTerms))
            termList.append(pageTerms)

        temp = []
        websiteTerms = websiteTerms[:length]
        for i in range(len(termList)):
            termList[i] = termList[i][:length]
        returnVal = 0
        for i in termList:
            returnVal = max(returnVal,self.distance(websiteTerms,i))
        return returnVal

    def get_feat_locality(self):
        query = DataTable[DataTable['url'] == self.url]['query']
        websites = DataTable[DataTable['query'] == query]
        lgDis = 0
        count  = 0
        ip = self.findIP(self.url)
        for i in range(websites.shape[0]):
            if websites['url'].iloc[i] == self.url:
                continue
            try:
                ip_i = self.findIP(websites['url'].iloc[i])
                count+=1
            except:
                print('exception occured')
                continue
            lgDis+= np.log(1+np.sqrt((ip_i['location']['latitude']-ip['location']['latitude'])**2+(ip_i['location']['longitude']-ip['location']['longitude'])**2))
        return count/lgDis


    def get_summary(self,num_sentence):
        out = ''
        try:
            try:
                parser = HtmlParser.from_url(self.url, Tokenizer("english"))
            except:
                try:
                    parser = PlaintextParser.from_string(self.body, Tokenizer("english"))
                except Exception as e:
                    raise(e)

            stemmer = Stemmer('english')
            summarizer = Summarizer(stemmer)
            summarizer.stop_words = get_stop_words('english')

            for sentence in summarizer(parser.document, num_sentence):
                out+=str(sentence)
        except:
            return self.body

        return out

    def get_summary_lex_rank(self,num_sentence):
        from sumy.parsers.plaintext import PlaintextParser  # other parsers available for HTML etc.
        from sumy.nlp.tokenizers import Tokenizer
        from sumy.summarizers.lex_rank import LexRankSummarizer  # We're choosing Lexrank, other algorithms are also built in

        try:
            parser = HtmlParser.from_url(self.url, Tokenizer("english"))
        except:
            try:
                parser = PlaintextParser.from_string(self.body, Tokenizer("english"))
            except Exception as e:
                raise(e)

        summarizer = LexRankSummarizer()
        summary = summarizer(parser.document, num_sentence)
        out=''
        for sentence in summary:
            out+= str(sentence)
        return out

    def get_feat_text_category(self, text):
        '''
        returns a vector of classes of (6) categories (0=yes,1=no) for an input text
        i.e., [0 0 0 0 0 0]
        '''
        if text is None or text == '':
            return [0,0,0,0,0,0]
        vec_text_1 = self.classifiers.vec_textclass_1.transform([text])
        vec_text_2 = self.classifiers.vec_textclass_2.transform([text])
        vec_text_3 = self.classifiers.vec_textclass_3.transform([text])
        vec_text_4 = self.classifiers.vec_textclass_4.transform([text])
        vec_text_5 = self.classifiers.vec_textclass_5.transform([text])
        vec_text_6 = self.classifiers.vec_textclass_6.transform([text])

        out = []
        out.append(round(self.classifiers.clf_textclass_1.predict_proba(vec_text_1)[0][1],3))
        out.append(round(self.classifiers.clf_textclass_2.predict_proba(vec_text_2)[0][1],3))
        out.append(round(self.classifiers.clf_textclass_3.predict_proba(vec_text_3)[0][1],3))
        out.append(round(self.classifiers.clf_textclass_4.predict_proba(vec_text_4)[0][1],3))
        out.append(round(self.classifiers.clf_textclass_5.predict_proba(vec_text_5)[0][1],3))
        out.append(round(self.classifiers.clf_textclass_6.predict_proba(vec_text_6)[0][1],3))

        return out

    def get_feat_spam(self, text):
        '''
        returns the class distribution (SPAM/HAM) for an input text
        i.e., [[predicted ham prob, predicted spam prob], [predicted class]]
        '''
        if text is None or text == '':
            return 0, 0, 0

        vec_text = self.classifiers.vec_spam_1.transform([text])
        # attention here, if the classifiers supports probabilities, otherwise need to change to predict()
        pred_klass = 0 if self.classifiers.clf_spam_1.predict(vec_text)[0] == 'ham' else 1
        pred_probs = self.classifiers.clf_spam_1.predict_proba(vec_text)[0]
        return pred_probs[0], pred_probs[1], pred_klass

    def filterTerm(self,word):
        if word is not None:
            temp = word.lower()
            return re.sub(r"[^A-Za-z]+", '',temp)
        else:
            return ''

    def get_feat_sentiment(self):
        '''
        returns probability 0-1
        '''
        text = self.get_summary_lex_rank(200)
        top_words = 10000
        textList = []
        text = text.split(' ')
        for i in text:
            i = self.filterTerm(i)
            if i in self.classifiers.vec_sentiment_1:
                textList.append(self.classifiers.vec_sentiment_1[i])
            else:
                textList.append(0)

        max_review_length = 1600

        #if(len(textList)>max_review_length):
        #    textList = textList(:max_review_length)

        inputList = []
        textList = np.array(textList)
        inputList.append(textList)
        modelInput = sequence.pad_sequences(inputList,maxlen = max_review_length)

        return self.classifiers.clf_sentiment_1.predict(modelInput)

    def get_feat_domain(self):
        return self.webscrap.get_domain()

    def get_feat_suffix(self):
        return self.webscrap.get_suffix()

    def get_feat_source_info(self):
        return self.webscrap.get_tot_occurences_authority()

    def get_feat_tot_outbound_links(self):
        return len(self.webscrap.get_outbound_links())

    def get_feat_tot_outbound_domains(self):
        return len(self.webscrap.get_outbound_domains())

    def get_feat_archive_tot_records(self, w, tot_records=None):
        '''
        returns basic statistics about cached data for a URL
        :param w: penalization factor for 404 URL (tries domain)
        :param tot_records: the max number of records to search (optional) and just for 'get_wayback_tot_via_api' calls
        :return:
        '''
        try:
            w=float(w)
            out, last =self.webscrap.get_wayback_tot_via_memento(w)
            if out == 0:
                out, last = self.webscrap.get_wayback_tot_via_memento(w, self.webscrap.get_full_domain())
        except:
            raise
        return out, (0 if last is None else last)

    def get_feat_readability_metrics(self):
        # https://github.com/shivam5992/textstat
        test_data = self.webscrap.body
        out = []
        out.append(textstat.flesch_reading_ease(test_data))
        out.append(textstat.smog_index(test_data))
        out.append(textstat.flesch_kincaid_grade(test_data))
        out.append(textstat.coleman_liau_index(test_data))
        out.append(textstat.automated_readability_index(test_data))
        out.append(textstat.dale_chall_readability_score(test_data))
        out.append(textstat.difficult_words(test_data))
        out.append(textstat.linsear_write_formula(test_data))
        out.append(textstat.gunning_fog(test_data))
        #out.append(textstat.text_standard(test_data))
        return out

def export_features_multi_proc():

    from multiprocessing.dummy import Pool
    import time
    import pandas as pd
    import collections
    # get the parameters
    print('reading dataset...')
    df = pd.read_csv(config.dataset_microsoft_webcred, delimiter='\t', header=0)
    extractors = []
    job_args = []
    for index, row in df.iterrows():
        url = str(row[3])
        topic = row[0]
        query = row[1]
        rank = int(row[2])
        likert = int(row[4])
        path = str(get_html_file_path(url))
        if path is not None:
            fe = FeatExtOld(url, local_file_path=path)
        else:
            fe = FeatExtOld(url)
        job_args.append((fe, topic, query, rank, url, likert)) # -> multiple arguments
        # extractors.append(fe) # -> single argument
    print('job args created, starting multi thread proc')
    with Pool(processes=int(config.nr_threads_feature_extractor)) as pool:
        asyncres = pool.starmap(get_features_web, job_args)
        #asyncres = pool.map(get_features_web, extractors)

    print('feature extraction done! saving...')
    name = 'microsoft_dataset_' + time.strftime("%Y%m%d%H%M%S") + '_features.pkl'
    print(asyncres)
    joblib.dump(asyncres, OUTPUT_FOLDER + name)
    print('done! file: ' + name)
    #asyncres = sorted(asyncres)


if __name__ == '__main__':

    export_features_multi_proc()
    exit(0)

    topic = TopicTerms()
    fe = FeatExtOld('http://www.dw.com/en/russia-expels-scores-of-diplomats-including-four-germans/a-43194430')

    summary1 = fe.get_summary_lex_rank(5)
    summary2 = fe.get_summary(5)
    print(summary1)
    print(summary2)
    print(fe.get_feat_archive_tot_records(config.waybackmachine_weight, config.waybackmachine_tot))
    print(fe.get_feat_domain())
    print(fe.get_feat_suffix())
    print(fe.get_feat_source_info())
    print(fe.get_feat_tot_outbound_links())
    print(fe.get_feat_tot_outbound_domains())
    print(fe.title)
    print(fe.body)
    print(fe.get_feat_text_category(fe.title))
    print(fe.get_feat_text_category(fe.body))
    print(fe.get_feat_text_category(summary1))
    print(fe.get_feat_text_category(summary2))
    print(fe.get_feat_readability_metrics())
    print(fe.get_feat_spam(fe.title))
    print(fe.get_feat_spam(fe.body))

