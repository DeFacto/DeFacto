import pandas as pd
from coffeeandnoodles.web.scrap.scrap import WebScrap
from nltk.corpus import stopwords

from config import WebTrustworthinessConfig

config = WebTrustworthinessConfig()

class TopicTerms():

    def generatePageTerms(self, website):
        webscrap = WebScrap(website, 15, 'lxml', None)
        page = self.removeStopWords(webscrap.body.split(' '))
        pageTerms = dict()
        return self.generateDict(page,pageTerms)

    def removeStopWords(self,page):
        stop_words = set(stopwords.words('english'))
        returnList = []
        for i in page:
            if i not in stop_words:
                returnList.append(i)
        return returnList   

    def generateDict(self,page,termDict):
    
        for j in page:
            if j in termDict:
                termDict[j]+=1
            else:
                termDict.update({j:1})
        return termDict

    def extractTopicTerm(self,query):
        df = pd.read_table(config.dataset_microsoft_webcred, sep="\t", header=None)
        df = df[df.iloc[:,1] == query ]
        
        terms = dict()

        if "en.wikipedia.org" in df.iloc[0,3]:
            webscrap = WebScrap(df.iloc[0,3], 15, 'lxml', None)
            page = self.removeStopWords(webscrap.body.split(' '))
            terms = self.generateDict(page,terms)


        else:
            N = 1
            for i in range(N):
                if "en.wikipedia.org" in df.iloc[i,3]:
                    continue
                webscrap = WebScrap(df.iloc[i,3], 15, 'lxml', None)
                page = self.removeStopWords(webscrap.body.split(' '))
                terms = self.generateDict(page,terms)
        
        terms = sorted(terms, key=terms.get, reverse = True)

        return terms