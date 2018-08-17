import sqlite3

from util.definitions import DB_PATH
from util.metaquery import MetaQuery
from util.triple import Triple
from util.webpage import WebPage


class SQLiteHelper(object):

    TYPE_QUERY_TEXT = 0
    TYPE_QUERY_TRIPLE = 1
    TYPE_QUERY_CLAIM = 2
    NO_CLAIM_ID = 0
    DB_PATH = ''

    def __init__(self):
        self.DB_PATH = DB_PATH

    def __enter__(self):
        try:
            self.conn = sqlite3.connect(self.DB_PATH)
            self.conn.text_factory = str
            return self.conn
        except Exception as error:
            print(error)
            exit(-1)

    def __exit__(self, *args):
        self.conn.close()

class TripeScorerDB(object):

    def __init__(self, conn):
        self.conn = conn

    def commit(self):
        self.conn.commit()

    def __exists_record(self, sql, param):
        try:
            c = self.conn.execute(sql, param)
            res = c.fetchall()
            if res is None or len(res) == 0:
                return False
            else:
                return res
        except Exception as e:
            raise e

    def save_result_web(self, id_metaquery, url, title, snippet, html, body, search_rank):
        try:
            webpage = WebPage(url, title, snippet, html, body, search_rank, id_metaquery)
            sel_sql = "SELECT ID FROM TB_RESULTS_WEB " \
                      "WHERE ID_METAQUERY = ? AND PAGE_URL = ? "
            ret = self.__exists_record(sel_sql, [id_metaquery, url])
            if ret is False:
                values = [id_metaquery, url, webpage.get_page_domain(),
                          title, snippet, html, body, search_rank]
                sql = """INSERT INTO TB_RESULTS_WEB(id_metaquery, page_url, page_domain, 
                                                    page_title, page_snippet, page_html, 
                                                    page_body, page_search_rank) 
                         VALUES (?,?,?,?,?,?,?,?)"""
                id = self.conn.cursor().execute(sql, values)
                #self.conn.commit()
                #print('website %s saved' % url)
                return id.lastrowid
            else:
                #print('website %s already cached' % url)
                return ret[0]
        except Exception as e:
            raise e

    def get_triple_by_id(self, id):
        sel_sql = "SELECT SUBJECT, PREDICATE, OBJECT, LANG, ID " \
                  "FROM TB_TRIPLE WHERE ID = ?"
        ret = self.__exists_record(sel_sql, [id])
        if ret is not False:
            triple = Triple(ret[0][0], ret[0][1], ret[0][2], ret[0][3], ret[0][4])
            return triple
        else:
            return None

    def get_triple_by_spolang(self, s, p, o, lang):
        sel_sql = "SELECT SUBJECT, PREDICATE, OBJECT, LANG, ID " \
                  "FROM TB_TRIPLE WHERE SUBJECT = ? AND PREDICATE = ? " \
                  "AND OBJECT = ? AND LANG = ? "
        ret = self.__exists_record(sel_sql, [s, p, o, lang])
        if ret is not False:
            triple = Triple(ret[0][0], ret[0][1], ret[0][2], ret[0][3], ret[0][4])
            return triple
        else:
            return None

    def update_total_hits_count_query(self, idmetaquery, total_sites, total_hits):
        sql = """UPDATE TB_METAQUERY SET TOTAL_RESULTS_CACHED = ?, TOTAL_HITS = ? WHERE ID = ?"""
        self.conn.cursor().execute(sql, [total_sites, total_hits, idmetaquery])
        return True

    def metaquery_cached(self, id_triple, querystr, codification):
        metaquery = MetaQuery(id_triple, querystr, codification=codification)
        sel_sql = "SELECT ID, TOTAL_HITS FROM TB_METAQUERY WHERE QUERYSTR = ?"
        return self.__exists_record(sel_sql, [querystr]), metaquery

    def save_triple(self, subject, predicate, object, lang):
        try:
            triple = self.get_triple_by_spolang(subject, predicate, object, lang)
            if triple is None:
                to_string = "%s|-|%s|-|%s|-|%s" % (subject, predicate, object, lang)
                values = [subject, predicate, object, lang, to_string]
                sql = """INSERT INTO TB_TRIPLE(subject, predicate, object, lang, to_string) 
                            VALUES (?,?,?,?,?)"""
                id = self.conn.cursor().execute(sql, values)
                self.conn.commit()
                triple = Triple(subject, predicate, object, lang, id.lastrowid)
            return triple
        except Exception as e:
            raise e

    def save_metaquery(self, id_triple, querystr, codification, total_hit=0):
        try:
            metaquery = MetaQuery(id_triple, querystr, codification)
            sel_sql = "SELECT ID FROM TB_METAQUERY " \
                      "WHERE ID_TRIPLE = ? AND QUERYSTR = ?"
            ret = self.__exists_record(sel_sql, [id_triple, querystr])
            if ret is False:
                values = [id_triple, querystr, codification, total_hit]
                sql = """INSERT INTO TB_METAQUERY(id_triple, querystr, 
                         codification, total_hits) 
                         VALUES (?,?,?,?)"""
                id = self.conn.cursor().execute(sql, values)
                #self.conn.commit()
                print('SE: t:%s|%s[%s]' % (metaquery.id_triple, metaquery.codification,
                                                     metaquery.querystr))
                return id.lastrowid
            else:
                print('CACHE: t:%s|%s[%s]' % (metaquery.id_triple, metaquery.codification,
                                                     metaquery.querystr))
                return ret[0]
        except Exception as e:
            raise e

    def get_websites_by_metaquery_id(self, id):
        sites = []
        try:
            sel_sql = "SELECT ID, ID_METAQUERY, PAGE_URL, PAGE_DOMAIN, PAGE_TITLE, PAGE_SNIPPET, " \
                      "PAGE_HTML, PAGE_BODY, PAGE_SEARCH_RANK " \
                      "FROM TB_RESULTS_WEB WHERE ID_METAQUERY = ? "
            ret = self.__exists_record(sel_sql, [id])
            if ret is not False:
                for w in ret:
                    site = WebPage(w[2], w[4], w[5], w[6], w[7], w[8], w[1], w[0])
                    sites.append(site)
            return sites
        except Exception as e:
            raise e

    def get_metaquery_by_id(self, id):
        try:
            sel_sql = "SELECT ID_TRIPLE, QUERYSTR, CODIFICATION," \
                      "ID, TOTAL_HITS, TOTAL_RESULTS_CACHED " \
                      "FROM TB_METAQUERY WHERE ID = ?"
            ret = self.__exists_record(sel_sql, [id])
            if ret is False:
                return None
            else:
                ret = ret[0]
                metaquery = MetaQuery(ret[0], ret[1], ret[2], ret[3], ret[4], ret[5])
                return metaquery
        except Exception as e:
            raise e

    def get_metaquery_by_value(self, querystr):
        try:
            sel_sql = "SELECT ID_TRIPLE, QUERYSTR, CODIFICATION," \
                      "ID, TOTAL_HITS, TOTAL_RESULTS_CACHED " \
                      "FROM TB_METAQUERY WHERE ID = ?"
            ret = self.__exists_record(sel_sql, [querystr])
            if ret is False:
                return None
            else:
                ret = ret[0]
                metaquery = MetaQuery(ret[0], ret[1], ret[2], ret[3], ret[4], ret[5])
                return metaquery
        except Exception as e:
            raise e

# *******************************************
# Usage example
#
#with SQLiteHelper() as sqlcon:
#    ts = TripeScorerDB(sqlcon)
#    ts.save_metaquery('diego', '?S? lives ?O?', 'bonn', 'en')
#
#    test1 = ts.get_metaquery_by_id(1)
#    if test1 is not None:
#        print(test1.to_string())
#
#    test2 = ts.get_metaquery_by_value('diego|-|?S? lives ?O?|-|bonn|-|en')
#    if test2 is not None:
#        print(test2.to_string())
#
#print('done')
# *******************************************