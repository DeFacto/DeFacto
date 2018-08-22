import os
from sklearn.externals import joblib
from defacto.definitions import OUTPUT_FOLDER, ENC_WEB_DOMAIN, ENC_WEB_DOMAIN_SUFFIX


def read_feat_files_and_merge(out_exp_folder, dataset, label, run_features, dataset_y_label='likert'):
    try:
        assert (out_exp_folder is not None and out_exp_folder != '')
        assert (dataset is not None and dataset != '')
        encoder1 = joblib.load(ENC_WEB_DOMAIN)
        encoder2 = joblib.load(ENC_WEB_DOMAIN_SUFFIX)

        matrix = []
        path = OUTPUT_FOLDER + out_exp_folder + dataset + '/text/'
        for file in os.listdir(path):
            if file.endswith('.pkl'):
                f = joblib.load(path + file)
                used_features = [file.replace('.pkl', '')]
                used_features.extend([f.get(dataset_y_label)])
                for key in f.get('features'):
                    if key in run_features:
                        data = f.get('features').get(key)
                        if key == 'domain':
                            data = encoder1.transform(data)
                        elif key == 'suffix':
                            data = encoder2.transform(data)
                        used_features.extend(data)
                matrix.append(used_features)


        name = 'features_' + label + '_' + str(len(matrix)) + '.pkl'
        _path = OUTPUT_FOLDER + out_exp_folder + dataset + '/'
        joblib.dump(matrix, _path + name)
        print('full features exported: ' + _path + name)
        return matrix

    except Exception as e:
        print(repr(e))
        raise

if __name__ == '__main__':

    try:

        config_basic = [['basic'],
                      ['basic_text', 'domain', 'suffix', 'source', 'outbound_links_http', 'outbound_links_https',
                       'outbound_links_ftp', 'outbound_links_ftps', 'outbound_domains_http', 'outbound_domains_https',
                       'outbound_domains_ftp', 'outbound_domains_ftps', 'text_categ_title', 'text_categ_body',
                       'readability_metrics', 'css', 'open_page_rank',
                       'sent_probs_title', 'sent_probs_body']]

        config_basic_and_gi = [['basic_gi'],
                               ['basic_text', 'domain', 'suffix', 'source', 'outbound_links_http',
                                'outbound_links_https',
                                'outbound_links_ftp', 'outbound_links_ftps', 'outbound_domains_http',
                                'outbound_domains_https',
                                'outbound_domains_ftp', 'outbound_domains_ftps', 'text_categ_title', 'text_categ_body',
                                'readability_metrics', 'css', 'open_page_rank',
                                'sent_probs_title', 'sent_probs_body', 'general_inquirer_body', 'general_inquirer_title']]


        config_ALL = [['all'], ['basic_text', 'domain', 'suffix', 'source', 'outbound_links_http', 'outbound_links_https',
                      'outbound_links_ftp', 'outbound_links_ftps', 'outbound_domains_http', 'outbound_domains_https',
                      'outbound_domains_ftp', 'outbound_domains_ftps', 'text_categ_title', 'text_categ_body',
                      'text_categ_summary_lex', 'text_categ_summary_lsa', 'readability_metrics', 'spam_title',
                      'spam_body', 'social_links', 'css', 'open_source_class', 'open_source_count', 'open_page_rank',
                      'general_inquirer_body', 'general_inquirer_title', 'vader_body', 'vader_title', 'who_is',
                      'sent_probs_title', 'sent_probs_body', 'archive']]

        read_feat_files_and_merge('exp004/', 'microsoft', config_basic[0][0], config_basic[1], 'likert')
        #read_feat_files_and_merge('exp004/', 'c3', config_basic[0][0], config_basic[1], 'likert_mode')

        read_feat_files_and_merge('exp004/', 'microsoft', config_basic_and_gi[0][0], config_basic_and_gi[1], 'likert')
        #read_feat_files_and_merge('exp004/', 'c3', config_basic_and_gi[0][0], config_basic_and_gi[1], 'likert_mode')

        read_feat_files_and_merge('exp004/', 'microsoft', config_ALL[0][0], config_ALL[1], 'likert')
        #read_feat_files_and_merge('exp004/', 'c3', config_ALL[0][0], config_ALL[1], 'likert_mode')

    except:
        raise