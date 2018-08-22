import os
from sklearn.externals import joblib
from defacto.definitions import OUTPUT_FOLDER, ENC_WEB_DOMAIN, ENC_WEB_DOMAIN_SUFFIX, CONFIG_FEATURES_BASIC, \
    CONFIG_FEATURES


def read_feat_files_and_merge(out_exp_folder, dataset):
    '''
    generates the following features files:
    - text.basic
    - text.basic+gi
    - text.all
    - html
    - text.all + html
    :param out_exp_folder: the experiment output folder.
    :param dataset: the dataset
    :return:
    '''
    try:

        # label, running_features
        # CONFIG_FEATURES_BASIC[0][0], CONFIG_FEATURES_BASIC[1]
        # , CONFIG_FEATURES_BASIC[0][0], CONFIG_FEATURES_BASIC[1]
        # CONFIG_FEATURES_BASIC_GI[0][0], CONFIG_FEATURES_BASIC_GI[1]
        # CONFIG_FEATURES_ALL[0][0], CONFIG_FEATURES_ALL[1]



        assert (out_exp_folder is not None and out_exp_folder != '')
        assert (dataset is not None and dataset != '')
        encoder1 = joblib.load(ENC_WEB_DOMAIN)
        encoder2 = joblib.load(ENC_WEB_DOMAIN_SUFFIX)


        path_features = OUTPUT_FOLDER + out_exp_folder + dataset + '/features/ok/'

        if dataset == 'microsoft':
            dataset_y_label = 'likert'
        elif dataset == 'c3':
            dataset_y_label = 'likert_mode'
        else:
            raise Exception('not implemented! ' + dataset)

        for configuration in CONFIG_FEATURES:
            running_features = configuration[1]
            matrix_html2seq = []
            matrix = []
            for file in os.listdir(path_features):
                if file.endswith('.pkl'):
                    # loading the features file
                    features_file = joblib.load(path_features + file)
                    # get hash
                    text_features = [file.replace('.pkl', '')]
                    # get the label (y)
                    text_features.extend([features_file.get(dataset_y_label)])
                    # get the text features
                    for key in features_file.get('features'):
                        if key in running_features:
                            data = features_file.get('features').get(key)
                            if key == 'domain':
                                data = encoder1.transform(data)
                            elif key == 'suffix':
                                data = encoder2.transform(data)
                            text_features.extend(data)

                    if configuration[0] == 'all+html2seq':
                        # exports also the HTML2Seq features
                        html2seq_single = features_file.get('html2seq')
                        text_features.extend(html2seq_single)

                        # exports also the single html2seq features file
                        html_features = [file.replace('.pkl', '')]
                        html_features.extend([features_file.get(dataset_y_label)])
                        html_features.extend(html2seq_single)


                    matrix.append(text_features)
                    if configuration[0] == 'all+html2seq':
                        matrix_html2seq.append(html_features)


            # saving the file
            name = 'features.' + configuration[0] + '.' + str(len(matrix)) + '.pkl'
            _path = OUTPUT_FOLDER + out_exp_folder + dataset + '/features/'
            joblib.dump(matrix, _path + name)
            print('full features exported: ' + _path + name)

            if configuration[0] == 'all+html2seq':
                name = 'features.html2seq.' + str(len(matrix)) + '.pkl'
                _path = OUTPUT_FOLDER + out_exp_folder + dataset + '/features/'
                joblib.dump(matrix, _path + name)
                print('full features exported: ' + _path + name)


    except Exception as e:
        print(repr(e))
        raise

if __name__ == '__main__':

    try:

        read_feat_files_and_merge('exp010/', 'microsoft')

        #read_feat_files_and_merge('exp010/', 'c3')

    except:
        raise