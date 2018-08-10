import pandas as pd
from sklearn.externals import joblib

from defacto.definitions import WEB_CREDIBILITY_DATA_PATH, BENCHMARK_FILE_NAME_TEMPLATE

if __name__ == '__main__':
    try:
        df=pd.read_table(WEB_CREDIBILITY_DATA_PATH + 'exp003/factbench/factbench_annotations.tsv',
                         sep="\t", na_values=0, low_memory=False, skiprows=1)

        file = BENCHMARK_FILE_NAME_TEMPLATE % (best_cls.lower(), best_pad, exp_type_combined)
        print('loading model: ' + file)
        clf_html2seq = joblib.load(WEB_CREDIBILITY_DATA_PATH + '003/microsoft/models/html2seq/' + file)

        for index, row in df.iterrows():
            url = str(row[0])
            claim = row[1]
            likert = row[2]
    except:
        raise