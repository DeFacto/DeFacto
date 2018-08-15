VERSION_LABEL = 'DeFacto 3'
VERSION = '3.0.0'


LABELS_FEVER_DATASET = {1: 'SUPPORTS', 2: 'REFUTES', 3: 'NOT ENOUGH INFO'}

EXPERIMENTS_FOLDER_PATH = '/Users/diegoesteves/DropDrive/CloudStation/experiments_cache/'

CLAUSIE_PATH = EXPERIMENTS_FOLDER_PATH + 'clausie/clausie/clausie.jar'
STANFORD_CORE_MODEL_PATH = EXPERIMENTS_FOLDER_PATH + 'stanford_models/3.5.1/stanford-parser-full-2015-01-30/'
STANFORD_MODEL_PATH = EXPERIMENTS_FOLDER_PATH + 'stanford_models/3.5.1/stanford-parser-3.5.1-models/edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz'
STANFORD_3_8_PATH = EXPERIMENTS_FOLDER_PATH + 'stanford_models/3.8/stanford-corenlp-full-2017-06-09'

DEFACTO_LEXICON_GI_PATH = EXPERIMENTS_FOLDER_PATH + 'web_credibility/general inquirer/inquireraugmented.csv'

SOCIAL_NETWORK_NAMES = ['Facebook', 'WhatsApp', 'QQ', 'TencentQQ', 'WeChat', 'QZone', 'Tumblr', 'Instagram', 'Twitter',
                        'Google', 'Google+', 'BaiduTieba', 'Postbar', 'Skype', 'Viber', 'SinaWeibo', 'Line', 'Snapchat',
                        'YY', 'VK', 'VKontakte', 'Pinterest', 'LinkedIn', 'Telegram', 'Reddit', 'Taringa', 'Foursquare',
                        'Renren', 'Tagged', 'Badoo', 'MySpace', 'StumbleUpon', 'TheDots', 'KiwiBox', 'Skyrock',
                        'Delicious', 'Snapfish', 'ReverbNation', 'Flixster', 'Care2', 'CafeMom', 'Ravelry', 'Nextdoor',
                        'Wayn', 'Cellufun', 'YouTube', 'Vine', 'Classmates', 'MyHeritage', 'Viadeo', 'Xing', 'Xanga',
                        'LiveJournal', 'Friendster', 'FunnyorDie', 'GaiaOnline', 'WeHeartIt', 'Buzznet', 'DeviantArt',
                        'Flickr', 'MeetMe', 'Meetup', 'Tout', 'Mixi', 'Douban', 'Vero', 'Quora']

BENCHMARK_FILE_NAME_TEMPLATE = 'cls_%s_%s_%s.pkl'
BING_LANG_DISABLED = 1

DATASET_3C_SITES_PATH = EXPERIMENTS_FOLDER_PATH + 'web_credibility/datasets/credibility corpus/c3.sites.csv'
DATASET_3C_SCORES_PATH = EXPERIMENTS_FOLDER_PATH + 'web_credibility/datasets/credibility corpus/reconcile_mturk_dm_ready.csv'

MAX_WEBSITES_PROCESS = 9999

WEB_CREDIBILITY_DATA_PATH = '/Users/diegoesteves/DropDrive/CloudStation/experiments_cache/web_credibility/output/'

TIMEOUT_MS = 3

TEST_SIZE=0.2
PADS = [25, 50, 100, 175, 250, 500, 1000, 1250, 1500, 1600, 1700, 1800, 1900, 2000, 2100, 2200, 2300, 2400, 2500, 2600,
        2700, 2800, 2900, 3000, 3500, 4000, 4500, 5000, 6000, 7000, 8000, 9000, 10000]
BEST_PAD_BIN = 2900
BEST_PAD_LIKERT = 2000
BEST_CLS_BIN = 'nb'
BEST_CLS_LIKERT = 'nb'