from urllib.parse import urlparse
import urllib

class WebPage(object):
    def __init__(self, url, title, snippet, html, body, search_rank, id_metaquery=-1, id=-1):
        try:
            self.id = id
            self.id_metaquery = id_metaquery
            self.url = url
            self.title = title
            self.snippet = snippet
            self.html = html
            self.body = body
            self.search_rank = search_rank
        except Exception as error:
            raise error

    def to_string(self):
        return "%s|-|%s" % (self.title, self.url)

    def get_page_domain(self):
        ret = 'err'
        try:
            x = urllib.parse(self.url)
            ret = x.netloc
        except Exception as error:
            print(error)
        return ret