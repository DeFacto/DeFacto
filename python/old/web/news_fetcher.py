import pandas as pd
import tldextract

from algorithms.trust import utils
from algorithms.trust.definitions import FILES_PATH

url_file_news = open(FILES_PATH+'news/url_file.txt',"r").readlines()
url_file_blogs = open(FILES_PATH+'blogs/url_file.txt',"r").readlines()
crawl_failed_urls = open(FILES_PATH+"crawl_fail_urls.txt","r").readlines()
failed_urls = [x.strip() for x in crawl_failed_urls]
filenum_news = 1
filenum_blogs = 1

if len(url_file_news)!=0:
    filenum_news = int(url_file_news[-1].split('\t')[0]) + 1
if len(url_file_blogs)!=0:
    filenum_blogs_html = int(url_file_blogs[-1].split('\t')[0]) + 1

already_crawled_news = []
already_crawled_blogs = []

for lines in url_file_news:
    lines = lines.strip()
    lines = lines.split('\t')
    already_crawled_news.append(lines[1])
    utils.crawl2(lines[1], FILES_PATH + 'news/', str(lines[0]), extract_html=False, save_printscreen=True)


for lines in url_file_blogs:
    lines = lines.strip()
    lines = lines.split('\t')
    already_crawled_blogs.append(lines[1])
    utils.crawl2(lines[1], FILES_PATH + 'blogs/', str(lines[0]), extract_html=False, save_printscreen=True)

exit(0)
dataset = pd.read_csv(FILES_PATH + 'uci-news-aggregator.csv')
url_file_news = open(FILES_PATH + 'news/url_file.txt',"a",0)
url_file_blogs = open(FILES_PATH + 'blogs/url_file.txt',"a",0)
crawl_failed_urls = open(FILES_PATH + "crawl_fail_urls.txt","a",0)
trusted_sources = open(FILES_PATH + 'whitelist_news').readlines()
blogs_sources = open(FILES_PATH + 'whitelist_blogs').readlines()
trusted_sources = [word.strip() for word in trusted_sources]
blogs_sources = [word.strip() for word in blogs_sources]
num_blogs = 5000
num_news = 5000
dataset = dataset.sample(frac=1).reset_index(drop=True)

for article in dataset.itertuples():
    if filenum_blogs > num_blogs and filenum_news > num_news:
        break
    url = article[3]
    if url in failed_urls:
        print("Crawl failed for this url before " + url)
        continue
    if url in already_crawled_news or url in already_crawled_blogs:
        print("Already crawled " + url)
        continue
    ext = tldextract.extract(url)
    domain = ext.domain
    if domain in trusted_sources:
        if filenum_news > num_news:
            continue
        code = utils.crawl2(url, FILES_PATH + 'news/', str(filenum_news), extract_html=False, save_printscreen=True)
        if code == 200:
            filenum_news = filenum_news + 1
            url_file_news.write(str(filenum_news-1)+'\t'+url+'\n')
        else:
            if code!=479:
                crawl_failed_urls.write(url + '\n')
    if domain in blogs_sources:
        if filenum_blogs > num_blogs:
            continue
        code = utils.crawl2(url, FILES_PATH + 'blogs/', str(filenum_blogs), extract_html=False, save_printscreen=True)
        if code == 200:
            filenum_blogs = filenum_blogs + 1
            url_file_blogs.write(str(filenum_blogs-1)+'\t'+url+'\n')
        else:
            if code!=479:
                crawl_failed_urls.write(url + '\n')