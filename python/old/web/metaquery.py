
class MetaQuery(object):
    def __init__(self, id_triple, querystr='', codification='SPO', id=-1, total=0, total_cached=0):
        try:
            self.id = id
            self.id_triple = id_triple
            self.querystr = querystr
            self.total_hit_count = total
            self.total_cached = total_cached
            self.codification = codification
        except Exception as error:
            raise error