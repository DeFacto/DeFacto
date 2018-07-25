#### CORE
---

#### core/triple
- fact-checking over RDF-like claims

#### core/natural_language_claims
- fact-checking over NL claims

#### [core/classifiers](https://github.com/SmartDataAnalytics/DeFacto3/wiki/classifiers)
- generic algorithms experiments

#### core/web
- web-related functionalities

#### core/web/credibility
- algorithms experiments related to the **trustworthiness** module

#### GENERIC
---

#### output
- any output, such as logs, temporary files, graphs, etc..

#### data/models
- save the models here

#### [util](https://github.com/SmartDataAnalytics/DeFacto3/wiki/util)
- to save any implementaiton of generic features, such as scraping, translation, etc..


#### Installation

1. Set the paths and constants in ```defacto/definitions.py```
2. Install the ClausIE [link](https://github.com/AnthonyMRios/pyclausie)
3. Download and start the StanfordCoreNLP server: ``` java -cp "*" -Xmx4g edu.stanford.nlp.pipeline.StanfordCoreNLPServer -port 9000 -timeout=5000 ```
4. Install Solr 7.4.0
5. Download extra models: ```python -m spacy download en_core_web_lg```
