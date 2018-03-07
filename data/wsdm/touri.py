# coding=utf-8
from lxml import html
import requests
import xmltodict
import json
import codecs

f = codecs.open('nationality.test','r+', "utf-8")
g = codecs.open('nationality.test.uri','w+', "utf-8")
lines = f.readlines()
for line in lines:
	line = line
	line = line.strip()
	line = line.split('\t')
	cnt = 0
	for entity in line:
		cnt = cnt+1
		if cnt == 3:
			g.write(entity)
			break
		if entity == u'Alami Ahannach':
			g.write(u'http://dbpedia.org/page/Alami_Ahannach\t')
			continue
		if entity == u'John Kemeny (film producer)':
			g.write(u'http://dbpedia.org/page/John_Kemeny_(film_producer)\t')
			continue
		if entity == u'Arash Amel':
			g.write(u'http://dbpedia.org/page/Arash_Amel\t')
			continue
		if entity == u'Franz Humer':
			g.write(u'http://dbpedia.org/page/Franz_Humer\t')
			continue
		page = requests.get('http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=' + entity)
		page = xmltodict.parse(page.text)		
		if 'Result' not in page['ArrayOfResult'].keys():
			print entity
			g.write(entity + '\t')
			continue
		page = page['ArrayOfResult']['Result']
		if isinstance(page, list):
			page = page[0]
		page = page['URI']
		page = page
		g.write(page + u'\t')
	g.write('\n')