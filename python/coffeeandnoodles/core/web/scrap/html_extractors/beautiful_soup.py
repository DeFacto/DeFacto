import requests
from bs4 import BeautifulSoup

class ExtractorBS(object):
    def __init__(self):
        print('starting HTML extraction...')

    def get_soup(self, url):
        r = requests.get(url)
        encoding = r.encoding if 'charset' in r.headers.get('content-type', '').lower() else None
        s =  BeautifulSoup(r.content, "lxml", from_encoding=encoding)
        r.close()
        return s

    def get_all_links(self, url):
        soup = self.get_soup(url)
        return (link.get('href') for link in soup.find_all('a'))

    def get_title_and_text(self, url):
        soup = self.get_soup(url)
        return soup.title.string, soup.get_text()