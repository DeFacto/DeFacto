import urllib.request
import json
import pysolr

class SolrUtils(object):

    def __init__(self):
        try:
            self.version = '7.4.0'
            self.solr = pysolr.Solr('http://localhost:8983/solr/topic_terms', timeout=10)



        except Exception as error:
            raise error

    def query_topic_terms(self, text):
        try:
            results = self.solr.search('id:{0}'.format(text))
            print("got {0} result(s).".format(len(results)))
            return results

        except Exception as error:
            raise error

    def commit(self):
        try:
            self.solr.commit()
        except:
            raise

    def delete_document(self, text):
        try:
            return self.solr.delete(text)
        except:
            raise

    def add_document(self, text, topic_terms, freq):
        try:

            if len(self.query_topic_terms(text)) == 0:

                obj = {"id": text,
                       "related": []}

                ordered = [(_freq, _tt) for _freq, _tt in sorted(zip(freq, topic_terms), reverse=True)]

                for i in range(len(ordered)):
                    tt = {}
                    tt["id"] = i+1
                    tt["token"] = ordered[i][1]
                    tt["total"] = ordered[i][0]
                    obj["related"].append(tt)


                self.solr.add([obj])

        except Exception as error:
            raise error

if __name__ == '__main__':

    try:
        server = SolrUtils()

        server.delete_document('rio de janeiro')


        res = server.query_topic_terms('NYC')
        for dict_tt in res:
            print(dict_tt)


    except Exception as e:
        print(e)