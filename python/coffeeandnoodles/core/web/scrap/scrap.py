import re
import requests
import urllib3

from src.coffeeandnoodles.core.config import CoffeeAndNoodlesConfig

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
from bs4 import BeautifulSoup
from urllib.parse import urlparse
import tldextract
from pandas._libs.tslib import Timestamp
#requests.packages.urllib3.disable_warnings()

config = CoffeeAndNoodlesConfig()

class WebScrap:
    def __init__(self, url, timeout=15, parser='html.parser', local_file_path=None):
        if url is None or url == '':
            raise Exception('url can not be empty')
        self.url = url
        if local_file_path is not None:
            try:
                self.soup = BeautifulSoup(open(local_file_path, encoding="UTF-8"), parser)
            except:
                try:
                    self.soup = BeautifulSoup(open(local_file_path, encoding="ISO-8859-1"), parser)
                except:
                    raise
        else:
            x, y, z = self.__check_url(url, timeout)
            if x is False:
                raise Exception('error on scraping the web page: ' + url)
            self.soup = BeautifulSoup(z, parser)
        #self.title = re.sub("\s\s+" , " ",re.sub(r"[^A-Za-z]+", '',self.__get_title().lower()))
        self.title = self.get_title().lower()
        self.body = re.sub("\s\s+", " ", re.sub(r"[^A-Za-z ]+", '', self.get_body().lower()))

    def __check_url(self, url, timeout):
        code = 0
        try:
            if str(url).startswith('http://') or str(url).startswith('https://'):
                r = requests.get(url, timeout=timeout, verify=False)
            elif str(url).startswith('www'):
                r = requests.get("http://" + url, timeout=timeout, verify=False)
            else:
                r = requests.get("http://www." + url, timeout=timeout, verify=False)
            if r.status_code != 200:
                return False, r.status_code, ''
            else:
                if 'text/html' not in str(r.headers['content-type']):
                    return False, 999, r.headers['content-type']
                else:
                    return True, r.status_code, r.content
        except Exception as e:
            return False, code, e

    def get_body(self):
        try:
            #TODO: check here to get just thr body actually
            for s in self.soup(['script', 'style']):
                s.decompose()
            res =  ' '.join(self.soup.stripped_strings)
            return res.strip()
        except Exception as e:
            raise e

    def get_body_sequence_tags(self):
        try:
            return self.soup()
        except Exception as e:
            raise e


    def get_title(self):
        try:
            title = self.soup.find('title')
            if title is None:
                return ''
            return title.text.strip()
        except Exception as e:
            raise e

    def get_full_domain(self):
        return self.get_domain() + '.' + self.get_suffix()

    def get_domain(self):
        try:
            o = tldextract.extract(self.url)
            return o.domain
        except Exception as e:
            raise e

    def get_suffix(self):
        try:
            o = tldextract.extract(self.url)
            if o.suffix is None:
                return 'com'
            return str(o.suffix).lower()
        except Exception as e:
            raise e

    def __query_wayback(self, url, year=None):
        try:
            if year is None:
                link = 'http://archive.org/wayback/available?url=' + url
            else:
                link = 'http://archive.org/wayback/available?url=' + url + \
                '&timestamp=' + str(year)
            r = requests.get(url=link)
            return r.json()
        except:
            raise

    def __query_memento(self, url, year=None):
        try:
            if year is None:
                # we input some random year, it does not matter in the end
                link = 'http://timetravel.mementoweb.org/api/json/2000/' + url
            else:
                link = 'http://timetravel.mementoweb.org/api/json/' + str(year) + '/' + url
            r = requests.get(url=link)
            return r.json()
        except:
            raise

    def get_wayback_tot_via_memento(self, w, urlalt=None):
        '''
        returns archive information for the URL (via memento protocol)
        :param w: the penalization value for domain search
        :param urlalt: an alternative URL (e.g., the URL domain)
        :return: number of years the URL was cached, since the caching started.
        '''
        k=0
        y=None
        try:
            if urlalt is None:
                urlalt = self.url
                w = 1.0
            out=self.__query_memento(urlalt)
            if out['mementos']:
                k+=1
                t1 = Timestamp(out['mementos']['last']['datetime'], tz=None)
                t2 = Timestamp(out['mementos']['first']['datetime'], tz=None)
                k = (t1.year - t2.year)
                k = k * w
                y = t2.year
            return k, y
        except:
            return k, None

    def get_wayback_tot_via_api(self, x, w, urlalt=None):
        '''
        returns archive information for the URL (via waybackmachine)
        :param x: the number of historical data to look back (years)
        :param w: the penalization value for domain search
        :param urlalt: an alternative URL (e.g., the URL domain)
        :return: number of years the URL was cached, the last cache (year)
        '''
        k=0
        y=None
        try:
            if urlalt is None:
                urlalt = self.url
                w = 1.0
            out=self.__query_wayback(urlalt)
            if out['archived_snapshots']['closest']['status'] == '200':
                k+=1
                tm = Timestamp(out['archived_snapshots']['closest']['timestamp'], tz=None)
                y = tm.year
                for i in range(1,x):
                    try:
                        out = self.__query_wayback(urlalt, str(tm.year - i) + '0101')
                        if out['archived_snapshots']['closest']['status'] == '200':
                            k+=w
                    except:
                        pass

            return k, y
        except:
            return k, None

    def get_outbound_links(self):
        try:
            links = []
            for link in self.soup.findAll('a', attrs={'href': re.compile("^http://")}):
                links.append(link.get('href'))
            return links
        except Exception as e:
            raise e

    def get_outbound_domains(self):
        try:
            domains = []
            for link in self.soup.findAll('a', attrs={'href': re.compile("^http://")}):
                domains.append(tldextract.extract(link.get('href')))
            return list(set(domains))
        except Exception as e:
            raise e

    def get_tot_occurences_authority(self):
        '''
        this should be improved, not easy to tackle this problem though
        :return: number of authorativity tags
        '''
        try:
            data = set(self.soup.text.split(' '))
            str_data = ' '.join(data)
            emails = set(re.findall(r"[a-z0-9\.\-+_]+@[a-z0-9\.\-+_]+\.[a-z]+", str_data, re.I))
            sources = set(re.findall("source:", str_data, re.I))
            author = set(re.findall("author", str_data, re.I))
            i=0
            for a in self.soup.find_all('meta'):
                for att in a.attrs:
                    if a.attrs[att] == 'author':
                        i += 1
            return (len(emails) + len(sources) + len(author) + i)
        except:
            raise

    def get_total_social_media_tags(self):
        try:
            data = set(self.soup.text.split(' '))
            str_data = ' '.join(data)
            tweeter = set(re.findall("https://twitter.com", str_data, re.I))
            return tweeter
        except Exception as e:
            raise e