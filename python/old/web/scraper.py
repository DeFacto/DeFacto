from scrapy.spiders import CrawlSpider, Rule
from scrapy.linkextractors.lxmlhtml import LxmlLinkExtractor
from scrapy.selector import Selector
from scrapy.http import HtmlResponse
from scrapy.item import Item, Field

#
# scrapy runspider scraper.py -o statusExample.csv -t csv
# https://jeremyb.me/2017/06/29/creating-a-web-crawling-seo-tool-with-scrapy/
class MyItem(Item):
	url= Field()
	status= Field()
	title= Field()

class someSpider(CrawlSpider):
	name = 'crawltest'
	allowed_domains = ["netvasco.com.br"]
	start_urls = ["http://netvasco.com.br"]
	rules = (Rule(LxmlLinkExtractor(allow=()), callback='parse_obj', follow=True),)

	def parse_obj(self,response):
		item = MyItem()
		item['status'] = response.status
		item['url'] = response.url
		item['title'] = response.xpath('//title/text()').extract_first()
		return item