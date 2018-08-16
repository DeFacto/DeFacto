## Automated Web Credibility

This project provides the data and models described on the paper:

"**Belliting the Source: Trustworthiness Indicators to Obfuscate Fake News on the Web**"

Module: trustworthiness

#### Pre-processing (preprocessing/)

-  ``fix_dataset_microsoft.py`` to fix the original Microsoft Credibility dataset.

- ``openpg.py`` exports OpenPageRank data given a set of URLs (datasets) as input

#### Feature Extraction

-  ``feature_extractor.py`` extract and caches the features for all websites and creates a single file for a given dataset (multithreading).

- ``features_merge.py`` merges the features files (*.pkl) for a given dataset into a single file
    - ``read_feat_files_and_merge('exp003/', 'microsoft')``


#### Run

- ``classifiers\benchmark.py`` to obtain the results and save the models

#### FactBench Eval

- ``factbench.py`` extracts the features and uses a trained model to make predictions on each URL from the **FactBench2012_Credibility** dataset. This dataset is created from URLs obtained from DeFacto's output over positive and negative data from **FactBench** dataset.