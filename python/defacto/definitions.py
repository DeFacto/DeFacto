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