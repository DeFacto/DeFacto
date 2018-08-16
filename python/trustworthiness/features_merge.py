import os
from sklearn.externals import joblib
from defacto.definitions import OUTPUT_FOLDER

def read_feat_files_and_merge(out_exp_folder, dataset):
    try:
        assert (out_exp_folder is not None and out_exp_folder != '')
        assert (dataset is not None and dataset != '')

        features = []
        path = OUTPUT_FOLDER + out_exp_folder + dataset + '/text/'
        for file in os.listdir(path):
            #if file.endswith('.pkl') and not file.startswith('_microsoft'):
            f=joblib.load(path + file)
            features.append(f)

        name = dataset + '_dataset_' + str(len(features)) + '_text_features.pkl'
        _path = OUTPUT_FOLDER + out_exp_folder + dataset + '/'
        joblib.dump(features, _path + name)
        print('full features exported: ' + _path + name)
        return features

    except Exception as e:
        print(repr(e))
        raise

if __name__ == '__main__':

    try:
        read_feat_files_and_merge('exp003/', 'microsoft')

        read_feat_files_and_merge('exp003/', '3c')

    except:
        raise