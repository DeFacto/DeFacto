"""
Project: DeFacto
Module: Web Credibility
Author: Diego Esteves
Date: 15-Aug-2018
"""
from sklearn import decomposition
from sklearn.cluster import KMeans, AgglomerativeClustering, AffinityPropagation
from sklearn.ensemble import GradientBoostingClassifier, RandomForestClassifier, ExtraTreesClassifier, \
    BaggingClassifier, AdaBoostClassifier
from sklearn.feature_extraction.text import CountVectorizer, TfidfTransformer
from sklearn.linear_model import LogisticRegression, Ridge, PassiveAggressiveClassifier, SGDClassifier, BayesianRidge, \
    RidgeClassifier
from sklearn.naive_bayes import BernoulliNB, MultinomialNB, GaussianNB
from sklearn.neural_network import MLPClassifier, MLPRegressor
from sklearn.pipeline import Pipeline
from sklearn.svm import SVR, LinearSVC, LinearSVR, SVC
from sklearn.tree import DecisionTreeClassifier
import numpy as np

VERSION_LABEL = 'DeFacto 3'
VERSION = '3.0.0'
ROOT_FOLDER_PATH = '/Users/diegoesteves/DropDrive/CloudStation/experiments_cache/'
LABELS_FEVER_DATASET = {1: 'SUPPORTS', 2: 'REFUTES', 3: 'NOT ENOUGH INFO'}

# ----------------------------------------------------------------------------------------------------------------------
# DEPENDENCIES
# ----------------------------------------------------------------------------------------------------------------------
CLAUSIE_PATH = ROOT_FOLDER_PATH + 'clausie/clausie/clausie.jar'
STANFORD_CORE_MODEL_PATH = ROOT_FOLDER_PATH + 'stanford_models/3.5.1/stanford-parser-full-2015-01-30/'
STANFORD_MODEL_PATH = ROOT_FOLDER_PATH + 'stanford_models/3.5.1/stanford-parser-3.5.1-models/edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz'
STANFORD_3_8_PATH = ROOT_FOLDER_PATH + 'stanford_models/3.8/stanford-corenlp-full-2017-06-09'

# ----------------------------------------------------------------------------------------------------------------------
# MAIN PROJECT
# ----------------------------------------------------------------------------------------------------------------------
ROOT_PROJECT_PATH = ROOT_FOLDER_PATH + 'web_credibility/'
OUTPUT_FOLDER = ROOT_PROJECT_PATH + 'output/'
DATASET_3C_SITES_PATH = ROOT_PROJECT_PATH + 'datasets/C3/c3.sites.csv'
DATASET_3C_SCORES_PATH = ROOT_PROJECT_PATH + 'datasets/C3/reconcile_mturk_dm_ready.csv'
DATASET_MICROSOFT_PATH = ROOT_PROJECT_PATH + 'datasets/microsoft/web_credibility_1000_url_ratings.fixed.tsv'
DATASET_MICROSOFT_TEST_PATH = ROOT_PROJECT_PATH + 'datasets/microsoft/web_credibility_expert_ratings_for_test_set.tsv'
DATASET_MICROSOFT_PATH_PAGES_CACHED = ROOT_PROJECT_PATH + 'datasets/microsoft/Cached Pages/'
DATASET_MICROSOFT_PATH_PAGES_MISSING = ROOT_PROJECT_PATH + 'datasets/microsoft/Cached Pages Missing/'

DEFACTO_LEXICON_GI_PATH = ROOT_PROJECT_PATH + 'general inquirer/inquireraugmented.csv'

SOCIAL_NETWORK_NAMES = ['Facebook', 'WhatsApp', 'QQ', 'TencentQQ', 'WeChat', 'QZone', 'Tumblr', 'Instagram', 'Twitter',
                        'Google', 'Google+', 'BaiduTieba', 'Postbar', 'Skype', 'Viber', 'SinaWeibo', 'Line', 'Snapchat',
                        'YY', 'VK', 'VKontakte', 'Pinterest', 'LinkedIn', 'Telegram', 'Reddit', 'Taringa', 'Foursquare',
                        'Renren', 'Tagged', 'Badoo', 'MySpace', 'StumbleUpon', 'TheDots', 'KiwiBox', 'Skyrock',
                        'Delicious', 'Snapfish', 'ReverbNation', 'Flixster', 'Care2', 'CafeMom', 'Ravelry', 'Nextdoor',
                        'Wayn', 'Cellufun', 'YouTube', 'Vine', 'Classmates', 'MyHeritage', 'Viadeo', 'Xing', 'Xanga',
                        'LiveJournal', 'Friendster', 'FunnyorDie', 'GaiaOnline', 'WeHeartIt', 'Buzznet', 'DeviantArt',
                        'Flickr', 'MeetMe', 'Meetup', 'Tout', 'Mixi', 'Douban', 'Vero', 'Quora']

# encoder web domains
ENC_WEB_DOMAIN = ROOT_PROJECT_PATH + 'encoders/encoder_webdomain.pkl'
ENC_WEB_DOMAIN_SUFFIX = ROOT_PROJECT_PATH + 'encoders/encoder_webdomain_suffix.pkl'
ENC_TAGS = ROOT_PROJECT_PATH + 'encoders/encoder_html2seq.pkl'


PATH_FEATURES_FILES = OUTPUT_FOLDER + '%s%sfeatures/' # experiment_folder, dataset_folder
# the benchmark file name template
BENCHMARK_FILE_NAME_TEMPLATE = 'cls_%s_%s_%s.pkl'

# enable or disable the bing language detection module (microsoft bing API)
BING_LANG_DISABLED = 1

# when processing a dataset, limits the maximum number of URL to process (useful for dev/debug mode)
MAX_WEBSITES_PROCESS = 9999999 # 999999999

# max timeout to scrap a given URL
TIMEOUT_MS = 3

# summarization lengh
SUMMARIZATION_LEN = 100

# sampling parameters
CROSS_VALIDATION_K_FOLDS = 10

TEST_SIZE = 0.25

RANDOM_STATE = 53

SEARCH_METHOD_RANDOMIZED_GRID = 'random'
SEARCH_METHOD_GRID = 'grid'


LINE_TEMPLATE = '%s\t%s\t%s\t%s\t%.3f\t%.3f\t%.3f\t%d\t%.3f\n'
EXP_2_CLASSES_LABEL = '2-classes'
EXP_3_CLASSES_LABEL = '3-classes'
EXP_5_CLASSES_LABEL = '5-classes'
LABELS_5_CLASSES = {1: 'non-credible', 2: 'low', 3: 'neutral', 4: 'likely', 5: 'credible'}
LABELS_3_CLASSES = {0: 'low', 1: 'medium', 2: 'high'}
LABELS_2_CLASSES = {0: 'low', 1: 'high'}
HEADER_CLASSIFICATION = 'cls\texperiment_type\tpadding\tklass\tprecision\trecall\tf-measure\tsupport\trate\n'
HEADER_REGRESSION = 'cls\texperiment_type\tpadding\tklass\tr2\trmse\tmae\tevar\n'

# best model's info (used in the combined evaluation)
MICROSOFT_BEST_MODEL_2_KLASS = 'cls_bnb_100_2-classes.pkl' #'bnb100'
MICROSOFT_BEST_MODEL_3_KLASS = 'cls_bnb_100_3-classes.pkl' #'bnb100'
MICROSOFT_BEST_MODEL_5_KLASS =  'cls_linsvr_250_5-classes.pkl'#'linsvr250'

MICROSOFT_BEST_MODEL = MICROSOFT_BEST_MODEL_2_KLASS
MICROSOFT_BEST_K = '100'

C3_BEST_MODEL_2_KLASS = 'cls_bnb_100_2-classes.pkl' #'bnb100'
C3_BEST_MODEL_3_KLASS = 'cls_bnb_2800_3-classes.pkl' #'bnb2800'
C3_BEST_MODEL_5_KLASS =  'cls_linsvr_2300_5-classes.pkl' #'linsvr2300'

C3_BEST_MODEL = 'cls_bnb_100_2-classes.pkl'
C3_BEST_K = '100'

BEST_PAD_EXPERIMENT_TYPE = EXP_5_CLASSES_LABEL
BEST_PAD_ALGORITHM = 'nb'


THRESHOLD_LABEL_2class = 0.68
THRESHOLD_LABEL_3class = 0.45

# classifiers x hyper-parameters x search method

trees_param_basic = {"max_features": ['auto', 'sqrt'],
                     "max_depth": [int(x) for x in np.linspace(10, 110, num=11)],
                     "min_samples_split": [2, 5, 10],
                     "min_samples_leaf": [1, 2, 4]}

trees_param = trees_param_basic.copy()
trees_param["n_estimators"] = [10, 25, 50, 100, 200, 400]

trees_param_bootstrap = trees_param.copy()
trees_param_bootstrap["bootstrap"] = [True, False]

gb_param = trees_param.copy()
gb_param["criterion"] = ['friedman_mse', 'mse', 'mae']

dt_param = trees_param_basic.copy()
dt_param["criterion"] = ['gini', 'entropy']

BEST_FEATURES_PERCENT = [100, 85, 70, 55, 40, 25, 10, 5, 3, 1]

CONFIG_FEATURES_BASIC = ['split.basic',
                         ['basic_text', 'domain', 'suffix', 'source', 'outbound_links_http', 'outbound_links_https',
                       'outbound_links_ftp', 'outbound_links_ftps', 'outbound_domains_http', 'outbound_domains_https',
                       'outbound_domains_ftp', 'outbound_domains_ftps', 'text_categ_title', 'text_categ_body',
                       'readability_metrics', 'css', 'open_page_rank',
                       'sent_probs_title', 'sent_probs_body']]

CONFIG_FEATURES_BASIC_GI = ['split.basic_gi',
                            ['basic_text', 'domain', 'suffix', 'source', 'outbound_links_http',
                        'outbound_links_https',
                        'outbound_links_ftp', 'outbound_links_ftps', 'outbound_domains_http',
                        'outbound_domains_https',
                        'outbound_domains_ftp', 'outbound_domains_ftps', 'text_categ_title', 'text_categ_body',
                        'readability_metrics', 'css', 'open_page_rank',
                        'sent_probs_title', 'sent_probs_body', 'general_inquirer_body', 'general_inquirer_title']]


CONFIG_FEATURES_ALL = ['split.all', ['basic_text', 'domain', 'suffix', 'source', 'outbound_links_http', 'outbound_links_https',
              'outbound_links_ftp', 'outbound_links_ftps', 'outbound_domains_http', 'outbound_domains_https',
              'outbound_domains_ftp', 'outbound_domains_ftps', 'text_categ_title', 'text_categ_body',
              'text_categ_summary_lex', 'text_categ_summary_lsa', 'readability_metrics', 'spam_title',
              'spam_body', 'social_links', 'css', 'open_source_class', 'open_source_count', 'open_page_rank',
                                 'general_inquirer_body', 'general_inquirer_title', 'vader_body', 'vader_title', 'who_is',
                                 'sent_probs_title', 'sent_probs_body', 'archive']]

CONFIG_FEATURES_ALL_HTML2SEQ = ['split.all+html2seq', ['basic_text', 'domain', 'suffix', 'source', 'outbound_links_http', 'outbound_links_https',
              'outbound_links_ftp', 'outbound_links_ftps', 'outbound_domains_http', 'outbound_domains_https',
              'outbound_domains_ftp', 'outbound_domains_ftps', 'text_categ_title', 'text_categ_body',
              'text_categ_summary_lex', 'text_categ_summary_lsa', 'readability_metrics', 'spam_title',
              'spam_body', 'social_links', 'css', 'open_source_class', 'open_source_count', 'open_page_rank',
                                 'general_inquirer_body', 'general_inquirer_title', 'vader_body', 'vader_title', 'who_is',
                                 'sent_probs_title', 'sent_probs_body', 'archive']]

CONFIG_FEATURES = [CONFIG_FEATURES_BASIC, CONFIG_FEATURES_BASIC_GI, CONFIG_FEATURES_ALL, CONFIG_FEATURES_ALL_HTML2SEQ]


CONFIGS_HIGH_DIMEN_CLASSIFICATION = [(MultinomialNB(), dict(alpha=[1.0, 0.7, 0.5, 0.1]), SEARCH_METHOD_GRID),
                                     (BernoulliNB(), dict(alpha=[1.0, 0.7, 0.5, 0.1]), SEARCH_METHOD_GRID),
                                     (RidgeClassifier(), dict(alpha=[1e0, 1e-1],
                                                    solver=['sag'],
                                                    tol=[1e0, 1e-1, 1e-2, 1e-3, 1e-4]),
                                      SEARCH_METHOD_RANDOMIZED_GRID),
                                     (RandomForestClassifier(), trees_param_bootstrap, SEARCH_METHOD_RANDOMIZED_GRID),
                                     ]

CONFIGS_CLASSIFICATION = [
    (DecisionTreeClassifier(), dt_param, SEARCH_METHOD_RANDOMIZED_GRID),
    (GradientBoostingClassifier(), gb_param, SEARCH_METHOD_RANDOMIZED_GRID),
    (RandomForestClassifier(), trees_param_bootstrap, SEARCH_METHOD_RANDOMIZED_GRID),
    (ExtraTreesClassifier(), trees_param_bootstrap, SEARCH_METHOD_RANDOMIZED_GRID),
    (BaggingClassifier(), dict(n_estimators=[10, 25, 50, 100, 200, 400], base_estimator__max_depth=[1, 2, 3, 4, 5],
          max_samples=[0.05, 0.1, 0.2, 0.5]), SEARCH_METHOD_RANDOMIZED_GRID),
    (AdaBoostClassifier(), dict(n_estimators=[10, 25, 50, 100, 200, 400], algorithm=["SAMME", "SAMME.R"]), SEARCH_METHOD_RANDOMIZED_GRID),
    (PassiveAggressiveClassifier(), dict(tol=[1e0, 1e-1, 1e-2, 1e-3], C=[0.1, 0.5, 1.0, 3.0, 5.0, 10.0], loss=["hinge", "squared_hinge"]), SEARCH_METHOD_RANDOMIZED_GRID),
    (SGDClassifier(n_jobs=-1), dict(loss=["hinge", "log", "modified_huber", "squared_hinge", "perceptron"],
                                    penality=["none", "l2", "l1", "elasticnet"], alpha=[1e0, 1e-1, 1e-2, 1e-3],
                                    tol=[1e0, 1e-1, 1e-2, 1e-3], learning_rate=["constant", "invscaling", "optimal"]), SEARCH_METHOD_RANDOMIZED_GRID),
    (BernoulliNB(), {"alpha": [1e0, 1e-1, 1e-2, 1e-3]}, SEARCH_METHOD_GRID),
    (MultinomialNB(), dict(alpha=[1.0, 0.7, 0.5, 0.1]), SEARCH_METHOD_GRID)]
    #MLPClassifier(hidden_layer_sizes=(hidden_nodes,hidden_nodes,hidden_nodes), solver='adam', alpha=1e-05)

'''
(Pipeline(steps=[('pca', decomposition.PCA()), ('cls', LinearSVR())]),
     dict(pca__n_components=N_COMPONENTS,
          cls__C=[1e0, 1e-1],), #, 1e-2, 1e-3
          #cls__epsilon=[1e-1, 1e-2, 1e-3],
          #cls__tol=[1e0, 1e-1, 1e-2, 1e-3, 1e-4]),
    SEARCH_METHOD_RANDOMIZED_GRID),
    '''
N_COMPONENTS = [10, 20, 40, 60, 80]

CONFIGS_HIGH_DIMEN_REGRESSION = [
    (LinearSVR(),
     dict(C=[1e0, 1e-1, 1e-2],
          epsilon=[1e-1, 1e-2, 1e-3],
          tol=[1e0, 1e-1, 1e-2, 1e-3, 1e-4]),
    SEARCH_METHOD_GRID),
    (Ridge(), dict(alpha=[1e0, 1e-1, 1e-2, 1e-3],
                   solver=['sag'],
                   tol=[1e0, 1e-1, 1e-2, 1e-3, 1e-4]),
     SEARCH_METHOD_GRID),]


CONFIGS_REGRESSION = [(LogisticRegression(),
                       dict(alpha=[1e0, 1e-1, 1e-2, 1e-3], solver=["newton-cg", "lbfgs", "liblinear", "sag", "saga"],
                            multi_class=["ovr", "multinomial"], tol=[1e0, 1e-1, 1e-2, 1e-3, 1e-4], penalty=["l1", "l2"],
                            C=[0.1, 0.5, 1.0, 3.0, 5.0, 10.0, 50.0, 100.0]),
                       SEARCH_METHOD_RANDOMIZED_GRID),
                      (Ridge(), dict(alpha=[1e0, 1e-1],
                                     solver=['sag']),
                       SEARCH_METHOD_RANDOMIZED_GRID),
                      (SVR(), dict(epsilon=[1e0, 1e-1, 1e-2, 1e-3], kernel=["linear", "sigmoid"], tol=[1e0, 1e-1, 1e-2, 1e-3],
                                   C=[1e0, 1e-1, 1e-2, 1e-3]), SEARCH_METHOD_RANDOMIZED_GRID)
                      ]