DeFacto (Deep Fact Validation)
==============================
[![Build Status](https://travis-ci.org/SmartDataAnalytics/DeFacto.svg?branch=master)](https://travis-ci.org/SmartDataAnalytics/DeFacto)

DeFacto is a framework for validating statements by finding confirming sources for it on the web. It takes a statement (such as “<i>Jamaica Inn was directed by Alfred Hitchcock</i>”) as input and then tries to find evidence for the truth of that statement by searching for information in the web ([more information](http://aksw.org/projects/DeFacto)). Check out our [demo](http://defacto.aksw.org/).

## How to cite
```Tex
@Article{gerber2015,
  Title = {DeFacto - Temporal and Multilingual Deep Fact Validation},
  Author = {Daniel Gerber and Diego Esteves and Jens Lehmann and Lorenz B{\"u}hmann and Ricardo Usbeck and Axel-Cyrille {Ngonga Ngomo} and Ren{\'e} Speck},
  Journal = {Web Semantics: Science, Services and Agents on the World Wide Web},
  Year = {2015},
  Url = {http://svn.aksw.org/papers/2015/JWS_DeFacto/public.pdf}
}
```

## Installation
If you want to try Defacto all you need to do is:

- 1. check out DeFacto (https://github.com/SmartDataAnalytics/DeFacto.git)
- 2. copy defacto.ini.default to defacto.ini
- 3. get your own [bing search api key](http://www.bing.com/toolbox/bingsearchapi) 
- 4. write this key to the defacto.ini (adjust your settings based on changes informed below)
- 5. set up the environment
  - 5.1 [Apache solr 4.5](https://archive.apache.org/dist/lucene/solr/4.5.0/) for indexing
  - 5.2 [MySQL 5.5](https://dev.mysql.com/doc/refman/5.5/en/) for query support
    - 5.2.1 create a new database dubbed ```dbpedia_metrics``` and import the file ```dbpedia_metrics.sql```
  - 5.3 [WordNET 3.1](https://wordnet.princeton.edu/wordnet/download/)
- 6. run DefactoDemo.java

## Bugs
Found a :bug: bug? [Open an issue](https://github.com/AKSW/fox/issues/new) 

## Requirements
Java 7, Maven, Solr 4.5, MySQL 5.5, Wordnet, Weka 3.3.6 (in case you want to check models out)

## Related Projects
Please see more information about related projects: [FactBench](https://github.com/AKSW/FactBench) and [BOA](http://aksw.org/Projects/BOA.html)

## Changelog
### [v2.1](https://github.com/AKSW/DeFacto/releases/tag/v2.1)
	- HTTP service support
  
### [v2.0](https://github.com/AKSW/DeFacto/releases/tag/v2.0)
	- Multilingual Deep Fact Validation Feature

### [v1.0](https://github.com/AKSW/DeFacto/releases/tag/v1.0)
	- No support for http service
	- Vaadin component required (user interface)
    - Uses FactBench v1.0
