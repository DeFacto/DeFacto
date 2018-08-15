import re
import numpy as np
import socket

class Nakamura:
    def __init__(self):
        self.DataTable = None
        self.topic = None

    def filterTerm(self, word):
        if word is not None:
            temp = word.lower()
            return re.sub(r"[^A-Za-z]+", '', temp)
        else:
            return ''


    def get_feat_majorityweb(self):
        query = self.DataTable[self.DataTable['url'] == self.url].iloc[0, 1]
        websites = self.DataTable[self.DataTable['query'] == query]
        terms = self.topic.extractTopicTerm(query)
        DF = 0
        pageTerms = self.topic.generatePageTerms(self.url)
        pageTerms = sorted(pageTerms, key=pageTerms.get, reverse=True)
        inter = [item for item in terms if item in pageTerms]
        parameter_s = 5  # TODO tune this
        inter = inter[:parameter_s]
        for i in range(int(websites.shape[0])):
            website_i = websites['url'].iloc[i]
            if website_i == self.url:
                continue
            try:
                pageTerms = self.topic.generatePageTerms(website_i)
                pageTerms = sorted(pageTerms, key=pageTerms.get, reverse=True)
            except Exception as e:
                config.logger.error(repr(e))
                continue
            inter_i = [item for item in terms if item in pageTerms]
            if len(set(inter).intersection(inter_i)) >= parameter_s - 2:
                DF += 1
        return DF


    def get_feat_coverage(self):
        query = self.DataTable[self.DataTable['url'] == self.url].iloc[0, 1]
        websites = self.DataTable[self.DataTable['query'] == query]
        terms = self.topic.extractTopicTerm(query)
        pageTerms = self.topic.generatePageTerms(self.url)
        pageTerms = sorted(pageTerms, key=pageTerms.get, reverse=True)
        intersection = [item for item in terms if item in pageTerms]
        return (len(intersection) * 1.0) / len(terms)


    def get_feat_qtermstitle(self):
        title = self.title
        returnVal = 0
        query = self.DataTable[self.DataTable['url'] == self.url].iloc[0, 1]
        query = query.split(' ')
        title = title.split(' ')
        for i in range(len(query)):
            query[i] = self.filterTerm(query[i])
        for i in query:
            for j in title:
                if i in j:
                    returnVal += 1
        return returnVal


    def get_feat_qtermsbody(self):
        returnVal = 0
        query = self.DataTable[self.DataTable['url'] == self.url].iloc[0, 1]
        query = query.split(' ')
        for i in range(len(query)):
            query[i] = self.filterTerm(query[i])
        try:
            pageTerms = self.topic.generatePageTerms(self.url)
        except Exception as e:
            config.logger.error(repr(e))
            return -1
        for i in query:
            if i in pageTerms:
                returnVal += pageTerms[i]
        return returnVal


    def get_feat_majoritysearch(self):
        query = self.DataTable[self.DataTable['url'] == self.url].iloc[0,1]
        websites = self.DataTable[self.DataTable['query'] == query]
        terms =  self.topic.extractTopicTerm(query)
        termList = []
        websiteTerms = self.topic.generatePageTerms(self.url)
        websiteTerms = websiteTerms.values()
        websiteTerms = sorted(websiteTerms,reverse=True)
        length = len(websiteTerms)

        for i in range(int(websites.shape[0])):
            website_i = websites['url'].iloc[i]
            if websites['url'].iloc[i] == self.url:
                continue
            try:
                pageTerms = self.topic.generatePageTerms(website_i)
                pageTerms = pageTerms.values()
                pageTerms = sorted(pageTerms,reverse=True)
            except Exception as e:
                config.logger.error(repr(e))
                continue
            if len(pageTerms) == 0:
                continue
            length = min(length,len(pageTerms))
            termList.append(pageTerms)

        temp = []
        websiteTerms = websiteTerms[:length]
        for i in range(len(termList)):
            termList[i] = termList[i][:length]
        returnVal = 0
        for i in termList:
            returnVal = max(returnVal,self.distance(websiteTerms,i))
        return returnVal

    def get_feat_locality(self):
        query = self.DataTable[self.DataTable['url'] == self.url]['query']
        websites = self.DataTable[self.DataTable['query'] == query]
        lgDis = 0
        count  = 0
        ip = self.findIP(self.url)
        for i in range(websites.shape[0]):
            if websites['url'].iloc[i] == self.url:
                continue
            try:
                ip_i = self.findIP(websites['url'].iloc[i])
                count+=1
            except Exception as e:
                config.logger.error(repr(e))
                continue
            lgDis+= np.log(1+np.sqrt((ip_i['location']['latitude']-ip['location']['latitude'])**2+(ip_i['location']['longitude']-ip['location']['longitude'])**2))
        return count/lgDis

    def findIP(self, url):
        if "http://" in url:
            url = url[7:]
        if "www." in url:
            url = url[4:]
        url = url.split('/')
        url = url[0]
        ip = socket.gethostbyname(url)
        reader = geolite2.reader()
        return reader.get(ip)
