## Automated Web Credibility

This project provides the data and models described in the paper:

"**Belliting the Source: Trustworthiness Indicators to Obfuscate Fake News on the Web, Esteves et. al. 2018**"

Module: trustworthiness

#### 0. Configurations

- ``definitions.py`` update local paths here!

#### 1. Pre-processing
preprocessing/

-  ``fix_dataset_microsoft.py`` to fix the original Microsoft Credibility dataset.

- ``openpg.py`` exports OpenPageRank data given a set of URLs (datasets) as input

#### 2. Feature Extraction

- ``feature_extractor.py`` extract and caches the features for all URLs existing in a given dataset, creating one feature file (*.pkl) for each URL (multithreading).
    - ``export_features_multithread('exp003/', 'microsoft', export_html_tags=True, force=True)``

    - **export_html_tags** = saves locally the HTML code.
    - **force** = forces reprocessing, even if the file already exists.

    - ``/out/[expX]/[dataset]/features/``
        - ``ok/`` -> features files (.pkl for each URL)
        - ``error/`` -> extraction error (one for each URL)
        - ``html/`` -> HTML content for each (successfully) URL


- ``features_merge.py`` merges the features files (*.pkl) for a given dataset into a single file
    - ``read_feat_files_and_merge('exp004/', 'microsoft', CONFIG_FEATURES_BASIC[0][0], CONFIG_FEATURES_BASIC[1], 'likert')``

- others
    - ``features_core.py`` implements the features
#### 3. Run
classifiers/

- ``benchmark.py`` to obtain the results and save the models


#### 4. FactBench Eval

- ``factbench.py`` extracts the features and uses a trained model to make predictions on each URL from the **FactBench2012_Credibility** dataset. This dataset is created from URLs obtained from DeFacto's output over positive and negative data from **FactBench** dataset.


#### Release Notes

**version 1.0**

currently supports the following datasets:
- Microsoft
- C3 Corpus