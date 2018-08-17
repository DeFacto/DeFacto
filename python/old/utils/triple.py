
class Triple(object):
    def __init__(self, s, p, o, lang, id=-1):
        try:
            self.id = id
            self.subject = s
            self.predicate = p
            self.object = o
            self.language = lang
        except Exception as error:
            raise error

    def to_string(self):
        return "%s|-|%s|-|%s|-|%s" % (self.subject, self.predicate, self.object, self.language)