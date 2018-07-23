import wikipediaapi
from sklearn.feature_extraction.text import CountVectorizer
from nltk import sent_tokenize

from coffeeandnoodles.SolrUtils import SolrUtils


class WikiPediaUtils(object):

    def __init__(self, tt_top, ngram=3):
        try:
            self.wiki = wikipediaapi.Wikipedia(language='en', extract_format=wikipediaapi.ExtractFormat.WIKI)
            self.count_vec = CountVectorizer(stop_words='english', analyzer='word',
                                        ngram_range=(1, ngram), max_df=0.5, min_df=1, max_features=tt_top)
        except Exception as error:
            raise error

    def get_page_object(self, text):
        try:
            return self.wiki.page(text)
        except:
            raise



    def get_wikipage_url(self, page):
        try:
            return page.fullurl
        except:
            raise

if __name__ == '__main__':

    try:
        text = 'rio de janeiro'
        wiki_utils = WikiPediaUtils(tt_top=20)
        page = wiki_utils.get_page_object(text)
        if page.exists():
            tt, freq = wiki_utils.get_topic_terms(page.text)

            server = SolrUtils()
            server.add_document(text, tt, freq)
            server.commit()

    except Exception as e:
        print(e)
