import multiprocessing

#from coffeeandnoodles.web.microsoft_azure.microsoft_azure_helper import MicrosoftAzurePlatform
#from coffeeandnoodles.web.scrap.scrap import WebScrap
from bs4 import BeautifulSoup
from keras.preprocessing.sequence import pad_sequences
from sklearn.externals import joblib
from textstat.textstat import textstat
from sumy.parsers.html import HtmlParser
from sumy.parsers.plaintext import PlaintextParser
from sumy.nlp.tokenizers import Tokenizer
from sumy.summarizers.lsa import LsaSummarizer as Summarizer
from sumy.nlp.stemmers import Stemmer
from sumy.utils import get_stop_words
from singleton_decorator import singleton
from urllib.request import urlopen
from keras.datasets import imdb
from keras.models import load_model
from keras.preprocessing import sequence
import lxml.html
import json
import re
import numpy as np
import socket
from multiprocessing.dummy import Pool
import pandas as pd
from tldextract import tldextract
from pathlib import Path

from coffeeandnoodles.core.util import get_md5_from_string
from coffeeandnoodles.core.web.microsoft_azure.microsoft_azure_helper import MicrosoftAzurePlatform
from coffeeandnoodles.core.web.scrap.scrap import WebScrap
from config import DeFactoConfig
#from src.coffeeandnoodles.core.util import get_md5_from_string
#from src.coffeeandnoodles.core.web.scrap.scrap import WebScrap
#from src.core.classifiers.credibility.util import get_html_file_path, get_features_web
#from src.core.web.credibility.topicUtils import TopicTerms
from urllib.parse import urlparse
import os

import warnings

from trustworthiness.util import get_html_file_path, get_features_web
from trustworthiness.topic_utils import TopicTerms

with warnings.catch_warnings():
    warnings.filterwarnings("ignore",category=FutureWarning)

__author__ = "Diego Esteves"
__copyright__ = "Copyright 2018, DeFacto Project"
__credits__ = ["Diego Esteves", "Aniketh Reddy", "Piyush Chawla"]
__license__ = "Apache"
__version__ = "0.0.1"
__maintainer__ = "Diego Esteves"
__email__ = "diegoesteves@gmail.com"
__status__ = "Dev"

config = DeFactoConfig()
bing = MicrosoftAzurePlatform(config.translation_secret)

class Singleton(object):
    _instance = None
    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            cls._instance = object.__new__(cls, *args, **kwargs)
        return cls._instance

@singleton
class Classifiers():
    def __init__(self):
        config.logger.info('loading classifiers...')
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
        config.logger.info('done')

@singleton
class OpenSourceData():
    def __init__(self):
        config.logger.info('loading open source data...')
        try:
            sources = '../data/datasets/opensources/sources.csv'
            sources = open(sources, "r").readlines()[1:]
            types = {}
            for source in sources:
                source = source.split(",")
                types[source[0]] = [source[1], source[2], source[3]]
            self.types = types
        except:
            raise

@singleton
class PageRankData():
    def __init__(self):
        config.logger.info('loading page rank data...')
        try:
            pgs=dict()
            for file in os.listdir(config.root_dir_data + 'pagerank/'):
                if file.endswith(".json"):
                    with open(config.root_dir_data + 'pagerank/' + file, 'r') as fh:
                        temp=json.load(fh)
                        if temp['status_code'] == 200:
                            for w in temp['response']:
                                if w['status_code'] == 200:
                                    pgs[w['domain']] = {'page_rank_decimal': float(w['page_rank_decimal']), 'rank': int(w['rank'])}
            self.pg = pgs
            config.logger.info('done')
        except Exception as e:
            config.logger.error(repr(e))
            raise e



class FeatureExtractor:
    """The feature extractor for the trustworthiness module.

    It implements a set of feature extractors for a given web page.
    """

    def __init__(self, url, timeout=15, local_file_path=None, error=False, save_webpage_file=False):
        #self.DataTable = pd.read_table(config.dataset_ext_microsoft_webcred_webpages_cache,sep=",",header=None,names=["topic","query","rank","url","rating"])
        assert (local_file_path is not None and save_webpage_file is False) or \
               (local_file_path is None)
        self.url = url
        self.local_file_path = local_file_path
        self.error = error
        self.webscrap = WebScrap(url, timeout, 'lxml', local_file_path)
        self.title = self.webscrap.get_title()
        self.body = self.webscrap.get_body()
        clf = Classifiers()
        self.classifiers = clf
        self.topic = TopicTerms()
        self.sources = OpenSourceData()
        self.page_rank = PageRankData()

    def get_feature_vector(self):
        return []
    # TODO: implement

    def filterTerm(self,word):
        if word is not None:
            temp = word.lower()
            return re.sub(r"[^A-Za-z]+", '',temp)
        else:
            return ''

    def get_feat_majorityweb(self):
        query = self.DataTable[self.DataTable['url'] == self.url].iloc[0,1]
        websites = self.DataTable[self.DataTable['query'] == query]
        terms =  self.topic.extractTopicTerm(query)
        DF = 0
        pageTerms = self.topic.generatePageTerms(self.url)
        pageTerms= sorted(pageTerms, key=pageTerms.get, reverse = True)
        inter = [item for item in terms if item in pageTerms]
        parameter_s = 5  #TODO tune this
        inter = inter[:parameter_s]
        for i in range(int(websites.shape[0])):
            website_i = websites['url'].iloc[i]
            if website_i == self.url:
                continue
            try:
                pageTerms = self.topic.generatePageTerms(website_i)
                pageTerms= sorted(pageTerms, key=pageTerms.get, reverse = True)
            except Exception as e:
                config.logger.error(repr(e))
                continue
            inter_i = [item for item in terms if item in pageTerms]
            if len(set(inter).intersection(inter_i)) >=  parameter_s-2:
                DF+=1
        return DF

    def get_feat_coverage(self):
        query = self.DataTable[self.DataTable['url'] == self.url].iloc[0,1]
        websites = self.DataTable[self.DataTable['query'] == query]
        terms =  self.topic.extractTopicTerm(query)
        pageTerms = self.topic.generatePageTerms(self.url)
        pageTerms= sorted(pageTerms, key=pageTerms.get, reverse = True)
        intersection = [item for item in terms if item in pageTerms]
        return (len(intersection)*1.0)/len(terms)

    def get_feat_qtermstitle(self):
        title = self.title
        returnVal = 0
        query = self.DataTable[self.DataTable['url'] == self.url].iloc[0,1]
        query = query.split(' ')
        title = title.split(' ')
        for i in range(len(query)):
            query[i] = self.filterTerm(query[i])
        for i in query:
            for j in title:
                if i in j:
                    returnVal+=1
        return returnVal

    def get_feat_qtermsbody(self):
        returnVal = 0
        query = self.DataTable[self.DataTable['url'] == self.url].iloc[0,1]
        query = query.split(' ')
        for i in range(len(query)):
            query[i] = self.filterTerm(query[i])
        try:
            pageTerms = self.topic.generatePageTerms(self.url)
        except Exception as e:
            config.logger.error(repr(e))
            return -1
        for i in query:
            if i in pageTerms:
                returnVal+=pageTerms[i]
        return returnVal

    def findIP(self, url):
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
        query = self.DataTable[self.DataTable['url'] == self.url].iloc[0,1]
        websites = self.DataTable[self.DataTable['query'] == query]
        terms =  self.topic.extractTopicTerm(query)
        termList = []
        websiteTerms = self.topic.generatePageTerms(self.url)
        websiteTerms = websiteTerms.values()
        websiteTerms = sorted(websiteTerms,reverse=True)
        length = len(websiteTerms)

        for i in range(int(websites.shape[0])):
            website_i = websites['url'].iloc[i]
            if websites['url'].iloc[i] == self.url:
                continue
            try:
                pageTerms = self.topic.generatePageTerms(website_i)
                pageTerms = pageTerms.values()
                pageTerms = sorted(pageTerms,reverse=True)
            except Exception as e:
                config.logger.error(repr(e))
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
        query = self.DataTable[self.DataTable['url'] == self.url]['query']
        websites = self.DataTable[self.DataTable['query'] == query]
        lgDis = 0
        count  = 0
        ip = self.findIP(self.url)
        for i in range(websites.shape[0]):
            if websites['url'].iloc[i] == self.url:
                continue
            try:
                ip_i = self.findIP(websites['url'].iloc[i])
                count+=1
            except Exception as e:
                config.logger.error(repr(e))
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
        try:
            if text is None or len(text.split()) == 0:
                return [0,0,0,0,0,0]
            else:
                aux = text.split()
                limit = min(len(aux), 1000)
                text = " ".join(aux[i] for i in range(0, limit-1))

                try:
                    if bing.bing_detect_language(text) != 'en':
                        text_en = bing.bing_translate_text(text, 'en')
                        text=text_en
                except Exception as e:
                    config.logger.error(repr(e))

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
        except Exception as e:
            config.logger.error(repr(e))
            return [0,0,0,0,0,0]

    def get_feat_spam(self, text):
        '''
        returns the class distribution (SPAM/HAM) for an input text
        i.e., [[predicted ham prob, predicted spam prob], [predicted class]]
        '''
        try:
            if text is None or text == '':
                return 0, 0, 0

            vec_text = self.classifiers.vec_spam_1.transform([text])
            # attention here, if the classifiers supports probabilities, otherwise need to change to predict()
            pred_klass = 0 if self.classifiers.clf_spam_1.predict(vec_text)[0] == 'ham' else 1
            pred_probs = self.classifiers.clf_spam_1.predict_proba(vec_text)[0]
            return [pred_probs[0], pred_probs[1], pred_klass]
        except Exception as e:
            config.logger.error(repr(e))
            return 0,0,0

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
        try:
            return self.webscrap.get_domain()
        except Exception as e:
            config.logger.error(repr(e))
            return ''

    def get_feat_suffix(self):
        try:
            return self.webscrap.get_suffix()
        except Exception as e:
            config.logger.error(repr(e))
            return ''

    def get_feat_source_info(self):
        try:
            return self.webscrap.get_tot_occurences_authority()
        except Exception as e:
            config.logger.error(repr(e))
            return 0

    def get_feat_tot_outbound_links(self):
        try:
            return len(self.webscrap.get_outbound_links())
        except Exception as e:
            config.logger.error(repr(e))
            return 0

    def get_feat_tot_outbound_domains(self):
        try:
            return len(self.webscrap.get_outbound_domains())
        except Exception as e:
            config.logger.error(repr(e))
            return 0

    def get_feat_archive_tot_records(self, w, tot_records=None):
        '''
        returns basic statistics about cached data for a URL
        :param w: penalization factor for 404 URL (tries domain)
        :param tot_records: the max number of records to search (optional) and just for 'get_wayback_tot_via_api' calls
        :return:
        '''
        out=0
        last=None
        try:
            w=float(w)
            out, last =self.webscrap.get_wayback_tot_via_memento(w)
            if out == 0:
                out, last = self.webscrap.get_wayback_tot_via_memento(w, self.webscrap.get_full_domain())
        except Exception as e:
            config.logger.error(repr(e))

        return [out, 0 if last is None else last]

    def get_feat_readability_metrics(self):
        # https://github.com/shivam5992/textstat

        try:
            test_data = self.webscrap.get_body()
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
        except Exception as e:
            config.logger.error(repr(e))
            return [0] * 9

    def get_feat_social_media_tags(self):
        try:
            return len(self.webscrap.get_total_social_media_tags())
        except Exception as e:
            config.logger.error(repr(e))
            return 0

    def get_opensources_classification(self, url):
        '''
        Function returns 0 if there is no information on the webpage in the OpenSources database, 1 if it is a reliable source and 2 if it is an unreliable source
        '''
        try:
            parsed_url = urlparse(url)
            hostname = str(parsed_url.hostname)
            if hostname.startswith('www'):
                hostname = hostname[4:]
            if hostname not in self.sources.types.keys():
                return 0  # no info on this page
            if "reliable" in self.sources.types[hostname]:
                return 1  # this is a reliable sourc
            else:
                return 2  # not a very reliable source
        except Exception as e:
            config.logger.error(repr(e))
            return 0

    def get_opensources_count(self, url):
        '''
        Function return the number of reliable sources, number of unreliable sources and the total number of sources referenced by a webpage in that order
        '''
        try:
            connection = urlopen(url, timeout=5)
            dom = lxml.html.fromstring(connection.read())
            num_reliable = 0
            num_unreliable = 0
            total_num_sources = 0

            for link in dom.xpath('//a/@href'):  # select the url in href for all a tags(links)
                total_num_sources += 1
                hostname = str(urlparse(link).hostname)
                if hostname.startswith('www'):
                    hostname = hostname[4:]
                if hostname not in self.sources.types.keys():
                    continue  # no info on this lin
                elif "reliable" in self.sources.types[hostname]:
                    num_reliable += 1  # this is a reliable source
                else:
                    num_unreliable += 1  # not a very reliable source
        except Exception as e:
            config.logger.error(repr(e))
            return [0, 0, 0]

        return [num_reliable, num_unreliable, total_num_sources]

    def get_number_of_arguments(self, url):
        #TODO: wait aniketh processing
        return 0
        try:
            urlcode = get_md5_from_string(url)
            with open(config.root_dir_data + 'marseille/output.json', 'r') as fh:
                dargs = json.load(fh)
            try:
                tot_args=dargs[urlcode]
            except KeyError:
                config.logger.warn('this should not happen but lets move on for now, check marseille dump files/pre-processing!')
                tot_args=0
        except Exception as e:
            config.logger.error(repr(e))
            raise

    def get_open_page_rank(self, url):
        try:
            o = tldextract.extract(url)
            domain=('%s.%s' % (o.domain, o.suffix))
            try:
                pginfo=self.page_rank.pg[domain]
            except KeyError:
                config.logger.warn('page rank information for domain [' + domain + '] not found')
                return [0, 0]
            return [pginfo['page_rank_decimal'], pginfo['rank']]
        except Exception as e:
            config.logger.error(repr(e))
            return 0

    def get_sequence_html(self):
        try:
            return self.webscrap.get_body_sequence_tags()
        except Exception as e:
            config.logger.error(repr(e))
            return ''

def likert2bin(likert):

    assert likert>=1 and likert <=5

    if likert in (1, 2, 3):
        return 0
    elif likert in (4, 5):
        return 1
    else:
        raise Exception('error y')


def get_html2sec_features(exp_folder):
    tags_set = []
    sentences = []
    y = []
    y2 = []
    tot_files = 0

    data = None
    from sklearn import preprocessing
    le = preprocessing.LabelEncoder()

    try:
        my_file = Path(config.dir_output + exp_folder + 'microsoft_dataset_html2seq.pkl')
        if not my_file.exists():
            for file in os.listdir(config.dir_output + exp_folder + '/html'):
                tags = []
                if file.startswith('microsoft_dataset_visual_features_') and file.endswith('.txt'):
                    tot_files += 1
                    print('processing file ' + str(tot_files))
                    path=config.dir_output + exp_folder + 'html/' + file
                    print(path)
                    soup = BeautifulSoup(open(path), "html.parser")
                    html = soup.prettify()
                    for line in html.split('\n'):
                        if isinstance(line, str) and len(line.strip()) > 0:
                            if (line.strip()[0] == '<') and (line.strip()[0:2] != '<!'):
                                if len(line.split()) > 1:
                                    tags.append(line.split()[0] + '>')
                                else:
                                    tags.append(line.split()[0])
                            elif (line.strip()[0:2] == '</' and line.strip()[0:2] != '<!'):
                                tags.append(line.split()[0])

                    if len(tags) > 0:
                        sentences.append(tags)
                        tags_set.extend(tags)
                        tags_set = list(set(tags_set))
                        #print(len(tags))
                        #print(tags)

                    # getting y
                    features_file = file.replace('microsoft_dataset_visual_features_', 'microsoft_dataset_features_')
                    features_file = features_file.replace('.txt', '.pkl')

                    path=config.dir_output + exp_folder + 'text/' + features_file
                    data = joblib.load(path)
                    y.append(int(data['likert']))
                    y2.append(likert2bin(int(data['likert'])))

                    # dump html2seq features
                    html2seq_feature_file = file.replace('.txt', '.pkl')
                    joblib.dump(tags, config.dir_output + exp_folder + 'html2seq/' + html2seq_feature_file)


            print('tot files: ', tot_files)
            print('dictionary size: ', len(tags_set))
            print('dictionary: ', tags_set)

            le.fit(tags_set)

            X = [le.transform(s) for s in sentences]
            print(len(X))
            print(len(y))

            data = (X, y, y2)

            joblib.dump(data, config.dir_output + exp_folder + 'microsoft_dataset_html2seq.pkl')
            joblib.dump(le, config.dir_output + exp_folder + 'microsoft_dataset_html2seq_enc.pkl')
        else:
            data = joblib.load(config.dir_output + exp_folder + 'microsoft_dataset_html2seq.pkl')
            le = joblib.load(config.dir_output + exp_folder + 'microsoft_dataset_html2seq_enc.pkl')

        return (data, le)

    except Exception as e:
        config.logger.error(repr(e))
        raise

def get_text_features(exp_folder, html2seq = False, pad=0):
    try:
        assert (exp_folder is not None and exp_folder != '')

        XX = []
        y = []
        y2 = []
        encoder = joblib.load(config.enc_domain)

        if html2seq is True:
            le = joblib.load(config.dir_output + exp_folder + 'microsoft_dataset_html2seq_enc.pkl')

        for file in os.listdir(config.dir_output + exp_folder):
            if file.endswith('_text_features.pkl') and file.startswith('microsoft_dataset'):
                config.logger.info('features file found: ' + file)
                features=joblib.load(config.dir_output + exp_folder + file)
                for d in features:
                    feat = d.get('features')
                    if feat is None:
                        raise Exception('error in the feature extraction! No features extracted...')
                    # feat[2] = encoder.transform([feat[2]])
                    feat[3] = encoder.transform([feat[3]])
                    del feat[2]

                    if html2seq is True:
                        hash = get_md5_from_string(d.get('url'))
                        file_name = 'microsoft_dataset_visual_features_%s.pkl' % (hash)
                        x=joblib.load(config.dir_output + exp_folder + 'html2seq/' + file_name)
                        x2 = le.transform(x)
                        feat.extend(list(x2))
                        XX.append(feat)
                    else:
                        XX.append(feat)
                    likert = int(d.get('likert'))
                    y.append(likert)
                    y2.append(likert2bin(likert))


        if len(XX) == 0:
            raise Exception('processed full file not found for this folder! ' + config.dir_output + exp_folder)

        return XX, y, y2


    except Exception as e:
        config.logger.error(repr(e))
        raise

def read_feat_files_and_merge(exp_folder):
    try:
        assert (exp_folder is not None and exp_folder != '')

        features = []
        for file in os.listdir(config.dir_output + exp_folder):
            if file.endswith('.pkl') and not file.startswith('_microsoft'):
                f=joblib.load(config.dir_output + exp_folder + file)
                features.append(f)

        name = 'microsoft_dataset_' + str(len(features)) + '_text_features.pkl'
        joblib.dump(features, config.dir_output + exp_folder + name)
        config.logger.info('full features exported: ' + name)

        return features

    except Exception as e:
        config.logger.error(repr(e))
        raise

def export_features_multi_proc_microsoft(exp_folder):

    assert (exp_folder is not None and exp_folder != '')
    # get the parameters
    config.logger.info('reading MS dataset...')
    df = pd.read_csv(config.dataset_microsoft_webcred, delimiter='\t', header=0)
    #extractors = []
    config.logger.info('creating job args...')
    job_args = []

    for index, row in df.iterrows():
        url = str(row[3])
        urlencoded = get_md5_from_string(url)
        name = 'microsoft_dataset_features_' + urlencoded + '.pkl'
        my_file = Path(config.dir_output + exp_folder + name)
        if not my_file.exists():
            topic = row[0]
            query = row[1]
            rank = int(row[2])
            likert = int(row[4])
            path = str(get_html_file_path(url))
            if path is not None:
                fe = FeatureExtractor(url, local_file_path=path)
            else:
                fe = FeatureExtractor(url)
            job_args.append((fe, topic, query, rank, url, likert, my_file)) # -> multiple arguments
            if index % 100 ==0:
                config.logger.info('processing job args ' + str(index))
            # extractors.append(fe) # -> single argument
    config.logger.info('%f job args created (out of %s): starting multi thread' % (len(job_args), len(df)))
    config.logger.info(str(multiprocessing.cpu_count()) + ' CPUs available')
    with Pool(processes=multiprocessing.cpu_count()) as pool:
        asyncres = pool.starmap(get_features_web, job_args)
        #asyncres = pool.map(get_features_web, extractors)

    config.logger.info('feature extraction done! saving...')
    #name = 'microsoft_dataset_' + time.strftime("%Y%m%d%H%M%S") + '_features.pkl'
    name = 'microsoft_dataset_' + str(len(job_args)) + '_text_features.pkl'
    joblib.dump(asyncres, config.dir_output + exp_folder + name)
    config.logger.info('done! file: ' + name)
    #asyncres = sorted(asyncres)


if __name__ == '__main__':

    '''
    automatically exports all features from the microsoft dataset (cached websites)
    '''
    EXP_FOLDER = 'exp002/'
    if 1==2:
        #read_feat_files_and_merge()
        #exit(0)
        export_features_multi_proc_microsoft(EXP_FOLDER)
    else:
        '''
        manually example of features extracted from a given URL
        '''
        fe = FeatureExtractor('https://www.amazon.com/Aristocats-Phil-Harris/dp/B00A29IQPK')

        summary1 = fe.get_summary_lex_rank(5)
        summary2 = fe.get_summary(5)
        print(fe.get_feat_archive_tot_records(config.waybackmachine_weight, config.waybackmachine_tot))
        print(fe.get_feat_domain())
        print(fe.get_feat_suffix())
        print(fe.get_feat_source_info())
        print(fe.get_feat_tot_outbound_links())
        print(fe.get_feat_tot_outbound_domains())
        print(fe.get_feat_text_category(fe.title))
        print(fe.get_feat_text_category(fe.body))
        print(fe.get_feat_text_category(summary1))
        print(fe.get_feat_text_category(summary2))
        print(fe.get_feat_readability_metrics())
        print(fe.get_feat_spam(fe.title))
        print(fe.get_feat_spam(fe.body))
        print(fe.get_feat_social_media_tags())
        print(fe.get_opensources_classification(fe.url))
        print(fe.get_opensources_count(fe.url))
        print(fe.get_number_of_arguments(fe.url))
        print(fe.get_open_page_rank('jamesjema.es'))



