import util
import os
from random import shuffle

from algorithms.trust.definitions import FILES_PATH

all_urls = []
num_blacklist = 5000
for f in os.listdir(FILES_PATH+'blacklists'):
    if os.path.isdir(FILES_PATH+'blacklists/' + f):
        filepath = FILES_PATH+'blacklists/' + f + '/urls'
        if os.path.isfile(filepath):
            urls = open(filepath, "r").readlines()
            urls = [word.strip() for word in urls]
            all_urls = all_urls + urls
shuffle(all_urls)

crawl_failed_urls = open(FILES_PATH + "crawl_fail_urls_blacklist.txt","r").readlines()
failed_urls = [x.strip() for x in crawl_failed_urls]

already_crawled = []
url_file = open(FILES_PATH+'blacklisted/url_file.txt',"r").readlines()
filenum = 1
if len(url_file)!=0:
    filenum = int(url_file[-1].split('\t')[0]) + 1
for lines in url_file:
    lines = lines.strip()
    lines = lines.split('\t')
    already_crawled.append(lines[1])

crawl_failed_urls = open(FILES_PATH+"crawl_fail_urls_blacklist.txt","a",0)
url_file = open(FILES_PATH+'blacklisted/url_file.txt',"a",0)

for url in all_urls:
    if filenum > num_blacklist:
        break
    if url in already_crawled:
        print("Already crawled " + url)
        continue
    if url in failed_urls:
        print("Crawl failed for this url before " + url)
        continue
    code = util.crawl2(url, FILES_PATH+'blacklisted/', str(filenum))
    if code == 200:
        filenum = filenum + 1
        url_file.write(str(filenum - 1) + '\t' + url + '\n')
    else:
        if code!=479:
            crawl_failed_urls.write(url + '\n')