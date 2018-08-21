"""
Project: DeFacto
Module: Web Credibility
Author: Diego Esteves
Date: 15-Aug-2018
"""

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
ENC_WEB_DOMAIN = '/Users/diegoesteves/Github/factchecking/DeFacto/python/data/encoders/encoder_webdomain.pkl'
ENC_WEB_DOMAIN_SUFFIX = '/Users/diegoesteves/Github/factchecking/DeFacto/python/data/encoders/encoder_webdomain_suffix.pkl'

# the benchmark file name template
BENCHMARK_FILE_NAME_TEMPLATE = 'cls_%s_%s_%s.pkl'

# enable or disable the bing language detection module (microsoft bing API)
BING_LANG_DISABLED = 1

# when processing a dataset, limits the maximum number of URL to process (useful for dev/debug mode)
MAX_WEBSITES_PROCESS = 999999

# max timeout to scrap a given URL
TIMEOUT_MS = 3

# summarization lengh
SUMMARIZATION_LEN = 100

# sampling parameters
CROSS_VALIDATION_K_FOLDS = 10

TEST_SIZE = 0.2

# HTML sequence windows
PADS = [25, 50, 100, 175, 250, 500, 1000, 1250, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600,
        2700, 2800, 2900, 3000, 3500, 4000, 4500, 5000, 6000, 7000, 8000, 9000, 10000]

# best model's info (used in the combined evaluation)
BEST_PAD_BIN = 2900
BEST_PAD_LIKERT = 2000
BEST_CLS_BIN = 'nb'
BEST_CLS_LIKERT = 'nb'
LINE_TEMPLATE = '%s\t%s\t%s\t%s\t%.3f\t%.3f\t%.3f\t%d\t%.3f\n'
EXP_2_CLASSES_LABEL = '2-classes'
EXP_3_CLASSES_LABEL = '3-classes'
EXP_5_CLASSES_LABEL = '5-classes'
LABELS_5_CLASSES = {1: 'non-credible', 2: 'low', 3: 'neutral', 4: 'likely', 5: 'credible'}
LABELS_3_CLASSES = {0: 'low', 1: 'medium', 2: 'high'}
LABELS_2_CLASSES = {0: 'low', 1: 'high'}
HEADER = 'cls\texperiment_type\tpadding\tklass\tprecision\trecall\tf-measure\tsupport\trate\n'

