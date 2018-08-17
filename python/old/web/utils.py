#!/usr/bin/env python
import importlib
import urllib, sys, re

import lxml
from lxml import html
from lxml import etree
import requests
from bs4 import BeautifulSoup
from selenium import webdriver
#import eventlet
#eventlet.monkey_patch()
from urllib.parse import urlparse
import timeit
requests.packages.urllib3.disable_warnings()
from lxml.html.clean import Cleaner

driver = webdriver.PhantomJS()
driver.set_window_size(1366, 768)  # set the window size that you need
driver.set_script_timeout(10)

def get_message_feedback(url, status_code=200):
    if status_code != 200:
        return "Fail: " + str(status_code) + "[" + url + "]"
    else:
        return "OK: " + str(status_code) + "[" + url + "]"

def get_string_time_elapsed(t2, t1):
    try:
        t_sec = round(t2 - t1)
        (t_min, t_sec) = divmod(t_sec, 60)
        (t_hour, t_min) = divmod(t_min, 60)
        return 'Time elapsed: {}hour:{}min:{}sec'.format(t_hour, t_min, t_sec)
    except:
        return 'something strange has happened...'

def get_alexa(url):
    xml = urllib.urlopen('http://data.alexa.com/data?cli=10&dat=s&url=%s' % url).read()
    try:
        rank = int(re.search(r'RANK="(\d+)"', xml).groups()[0])
    except:
        rank = -1
    print('Your rank for %s is %d!\n' % (url, rank))

def crawl2(url, folder_location, file_name, timeout=15, extract_html=True, save_printscreen=False):
    try:
        initt = timeit.default_timer()
        u_parse = urllib.parse(url)
        code = 0

        if extract_html is True:
            if str(url).startswith('http://') or str(url).startswith('https://'):
                r = requests.get(url, timeout=timeout, verify=False)
            elif str(url).startswith('www'):
                r = requests.get("http://" + url, timeout=timeout, verify=False)
            else:
                r = requests.get("http://www." + url, timeout=timeout, verify=False)
            if r.status_code != 200:
                code = r.status_code
            else:
                if 'text/html' not in str(r.headers['content-type']):
                    code = 999
                else:
                    #tree = etree.HTML(r.text)
                    #body = tree.xpath('/html/body')
                    #content = etree.tostring(body[0])
                    if extract_html is True:
                        soup = BeautifulSoup(r.content, 'html.parser')
                        body = soup.find('body')
                        #content_body = body.findChildren()
                        with open(folder_location + file_name + ".html", "w+") as f:
                            f.write(soup.prettify())
                        with open(folder_location + file_name + ".txt", "w+") as f:
                            f.write(body.text)
                    if save_printscreen is True:
                        driver.get(url)
                        driver.save_screenshot(folder_location + file_name + ".png")

                    code = r.status_code
        if save_printscreen is True and extract_html is False:
            code=200
            driver.get(url)
            driver.save_screenshot(folder_location + file_name + ".png")

        print(get_message_feedback(url, code) + " - " + get_string_time_elapsed(timeit.default_timer(), initt))
        return code
    except Exception as e:
        print('Couldnt get webpage: ' + str(e))
        print(get_message_feedback(url, 479) + " - " + get_string_time_elapsed(timeit.default_timer(), initt))
        return 479

def crawl(url, folder_location, file_name):
    import sys
    importlib.reload(sys)
    sys.setdefaultencoding('utf8')
    try:
        response = urllib.urlopen(url)
        html = response.read()
        if response.getcode() != 200:
            return response.getcode()
        driver = webdriver.PhantomJS()
        driver.set_window_size(1366, 768)  # set the window size that you need
        driver.get(url)
        driver.save_screenshot(folder_location + file_name + ".png")
        with open(folder_location + file_name + ".html", "w+") as f:
            f.write(driver.page_source)
        with open(folder_location + file_name + ".txt", "w+") as f:
            f.write(driver.find_element_by_tag_name('body').text)
        return 200
    except:
        print("Unknown error")
        return 479

def remove_script_tags_with_bs4(soup):
    for script in soup(["script", "style"]):
        script.extract()
    text = soup.get_text()
    # break into lines and remove leading and trailing space on each
    lines = (line.strip() for line in text.splitlines())
    # break multi-headlines into a line each
    chunks = (phrase.strip() for line in lines for phrase in line.split("  "))
    # drop blank lines
    text = '\n'.join(chunk for chunk in chunks if chunk)
    return text.encode('utf-8')

def remove_script_tags_with_cleaner(url):
    """
    Remove script and style tags from HTML code
    more info: info: http://lxml.de/api/lxml.html.clean.Cleaner-class.html

    cleaner.kill_tags = ['a', 'h1'] -> remove tags and its content
    cleaner.remove_tags = ['p'] -> remove tags

    :param url: url
    :returns: cleaned version of the html code
    :raises keyError: raises an exception
    """
    try:
        c = Cleaner()
        c.javascript = True
        c.style = True
        return lxml.html.tostring(c.clean_html(lxml.html.parse(url)))
    except:
        raise
