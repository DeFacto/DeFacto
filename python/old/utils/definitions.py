CONST_SEPARATOR = '|-|'
DB_PATH = '/mnt/SERVER_DATA/Dropbox/Doutorado_Alemanha/#Papers/#DeFacto Files/ISWC2017_Challenge/triple_scorer.db'
SOLR_URL = 'http://52.173.249.140:8983/solr/webpages/'

JSON_FILES_CACHE_PATH = '/mnt/SERVER_DATA/Dropbox/Doutorado_Alemanha/#Papers/#DeFacto Files/ISWC2017_Challenge/json_cache/'

PREDICATE_ORG_DOMICILED_IN = 'isDomiciledIn'
PREDICATE_ORG_FOUNDED_DATE = 'hasLatestOrganizationFoundedDate'
PREDICATE_ORG_PHONE_NUMBER = 'hasHeadquartersPhoneNumber'
PREDICATE_ORG_WEBSITE_LINK = 'hasLink'

PREDICATE_ORG_DOMICILED_IN_ID = 0
PREDICATE_ORG_FOUNDED_DATE_ID = 1
PREDICATE_ORG_PHONE_NUMBER_ID = 2
PREDICATE_ORG_WEBSITE_LINK_ID = 3

PREDICATES = [
    {'name': 'foundedDate', 'id': 0, 'uri': 'hasLatestOrganizationFoundedDate'},
    {'name': 'phone', 'id': 1, 'uri': 'hasHeadquartersPhoneNumber'},
    {'name': 'link', 'id': 2, 'uri': 'hasLink'}
]
