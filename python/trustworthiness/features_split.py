import os
from sklearn.externals import joblib
from defacto.definitions import OUTPUT_FOLDER, ENC_WEB_DOMAIN, ENC_WEB_DOMAIN_SUFFIX, CONFIG_FEATURES_BASIC, \
    CONFIG_FEATURES
import pandas as pd

def features_split(out_exp_folder, dataset, complex_features_file):
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

        full_path_features_file = OUTPUT_FOLDER + out_exp_folder + dataset + '/features/' + complex_features_file

        if dataset == 'microsoft':
            dataset_y_label = 'likert'
        elif dataset == 'c3':
            dataset_y_label = 'likert_mode'
        else:
            raise Exception('not implemented! ' + dataset)

        print('loading complex features file')
        complex_features = joblib.load(full_path_features_file)

        for configuration in CONFIG_FEATURES:
            running_features = configuration[1]
            matrix_html2seq = []
            matrix = []
            tot_valid_features = 0
            for features in complex_features:
                if features is None:
                    continue
                else:
                    # get hash
                    id = features.get('hash')
                    if id is None:
                        continue
                    X_features = [id]
                    # get the label (y)
                    label = features.get(dataset_y_label)
                    if type(label) == pd.core.series.Series:
                        label = label.iloc[0]
                    X_features.extend([label])
                    # get the selected features
                    for key in features.get('features'):
                        if key in running_features:
                            data = features.get('features').get(key)
                            X_features.extend(data)

                    if configuration[0] == 'all+html2seq':
                        # exports also the HTML2Seq features
                        html2seq_single = features.get('html2seq')
                        if html2seq_single is None:
                            print(id)
                            continue
                        X_features.extend(html2seq_single)

                        # exports also the single html2seq features file
                        html_features = [id]
                        html_features.extend([label])
                        html_features.extend(html2seq_single)
                        matrix_html2seq.append(html_features)

                    tot_valid_features += 1
                    matrix.append(X_features)

            print(tot_valid_features, len(matrix))
            assert (tot_valid_features == len(matrix))
            # saving the file
            name = 'features.' + configuration[0] + '.' + str(len(matrix)) + '.pkl'
            _path = OUTPUT_FOLDER + out_exp_folder + dataset + '/features/'
            joblib.dump(matrix, _path + name)
            print('full features exported: ' + _path + name)

            if configuration[0] == 'all+html2seq':
                name = 'features.html2seq.' + str(len(matrix)) + '.pkl'
                _path = OUTPUT_FOLDER + out_exp_folder + dataset + '/features/'
                joblib.dump(matrix_html2seq, _path + name)
                print('full features exported: ' + _path + name)


    except Exception as e:
        print(repr(e))
        raise

if __name__ == '__main__':

    try:

        # experiment folder, dataset, name of the features complex file
        # features_split('exp010/', 'c3', 'features.complex.all.5691.pkl')
        features_split('exp010/', 'microsoft', 'features.complex.all.994.pkl')

    except:
        raise