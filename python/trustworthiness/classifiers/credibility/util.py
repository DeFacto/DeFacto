import collections
from pathlib import Path

import pdfkit as pdfkit
from coffeeandnoodles.core.web.scrap.scrap import WebScrap
from sklearn.externals import joblib

from coffeeandnoodles.core.util import get_md5_from_string
from config import DeFactoConfig


config = DeFactoConfig()

def get_html_file_path(url):
    path = url.replace('http://', '')
    last = path.split('/')[-1]

    path_root = None
    if ('.html' not in last) and ('.htm' not in last) and ('.shtml' not in last):
        if path[-1] != '/':
            path = path + '/'
        path_root1 = Path(config.dataset_ext_microsoft_webcred_webpages_cache + path + 'index.html')
        path_root2 = Path(config.dataset_ext_microsoft_webcred_webpages_cache_missing + path + 'index.html')
    else:
        path_root1 = Path(config.dataset_ext_microsoft_webcred_webpages_cache + path)
        path_root2 = Path(config.dataset_ext_microsoft_webcred_webpages_cache_missing + path)

    if path_root1.exists():
        path_root = path_root1
    elif path_root2.exists():
        path_root = path_root2
    else:
        # sometimes the last part is not a folder, but the file itself without the ".html" , try it as a last attempt
        path_root3a = Path(
            config.dataset_ext_microsoft_webcred_webpages_cache + path.replace(last, '') + last + '.html')
        path_root3b = Path(
            config.dataset_ext_microsoft_webcred_webpages_cache + path.replace(last, '') + last + '.htm')
        path_root3c = Path(
            config.dataset_ext_microsoft_webcred_webpages_cache + path.replace(last, '') + last + '.shtml')
        if path_root3a.exists():
            path_root = path_root3a
        elif path_root3b.exists():
            path_root = path_root3b
        elif path_root3c.exists():
            path_root = path_root3c
        else:
            # url_broken.append(url)
            raise Exception(
                ':: this should not happen, double check core/web/credibility/fix_dataset_microsoft.py | url = ' + url)

    return path_root

def get_encoder_domain():

    import pandas as pd
    from sklearn import preprocessing
    le = preprocessing.LabelEncoder()

    domains = []

    # appending upper level domains, from http://data.iana.org/TLD/tlds-alpha-by-domain.txt
    # Version 2018040300, Last Updated Tue Apr  3 07:07:01 2018 UTC
    df = pd.read_csv(config.datasets + 'data/iana/org/TLD/tlds-alpha-by-domain.txt', sep=" ", header=None)
    for index, row in df.iterrows():
        print(index, row[0])
        domains.append(str(row[0]).lower())

    df = pd.read_csv(config.dataset_microsoft_webcred, delimiter='\t', header=0)
    for index, row in df.iterrows():
        url = str(row[3])
        print(index, url)
        path = get_html_file_path(url)
        web = WebScrap(url, 15, 'lxml', path)
        domains.append(web.get_suffix())

    le.fit(domains)
    joblib.dump(le, config.enc_domain)
    print(le.classes_)

def get_features_web(extractor, topic, query, rank, url, likert, output_filename):
    try:

        config.logger.info('process starts for : ' + extractor.url)

        data = collections.defaultdict(dict)
        data['topic'] = topic
        data['query'] = query
        data['rank'] = rank
        data['url'] = url
        data['likert'] = likert

        out = []
        out.extend(extractor.get_feat_archive_tot_records(config.waybackmachine_weight, config.waybackmachine_tot))
        out.append(extractor.get_feat_domain())
        out.append(extractor.get_feat_suffix())
        out.append(extractor.get_feat_source_info())
        out.append(extractor.get_feat_tot_outbound_links())
        out.append(extractor.get_feat_tot_outbound_domains())
        out.extend(extractor.get_feat_text_category(extractor.title))
        out.extend(extractor.get_feat_text_category(extractor.body))
        out.extend(extractor.get_feat_text_category(extractor.get_summary_lex_rank(100)))
        out.extend(extractor.get_feat_text_category(extractor.get_summary(100)))
        out.extend(extractor.get_feat_readability_metrics())
        out.extend(extractor.get_feat_spam(extractor.title))
        out.extend(extractor.get_feat_spam(extractor.body))
        out.append(extractor.get_feat_social_media_tags())
        out.append(extractor.get_opensources_classification(extractor.url))
        out.extend(extractor.get_opensources_count(extractor.url))
        out.append(extractor.get_number_of_arguments(extractor.url))
        out.extend(extractor.get_open_page_rank(extractor.url))



        tempname = str(output_filename).replace('microsoft_dataset_features_', 'microsoft_dataset_visual_features_')
        tempname = tempname.replace('.pkl', '.txt')
        with open(tempname, "w") as file:
            file.write(str(extractor.get_sequence_html()))

        data['features'] = out
        config.logger.info('features extracted - OK: ' + extractor.url)
        joblib.dump(data, output_filename)


        return data
    except:
        config.logger.error('features extraction - ERROR: ' + extractor.url)
        raise

def save_url_body(extractor):
    try:
        config.logger.info('extracting features for: ' + extractor.url)
        hash = get_md5_from_string(extractor.local_file_path)
        text=extractor.webscrap.get_body()
        with open(config.root_dir_output + 'marseille/input/' + hash + '.txt', "w") as file:
            file.write(text)

    except Exception as e:
        config.logger.error(repr(e))
        raise
