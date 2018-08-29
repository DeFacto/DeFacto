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

2.1 ``feature_extractor.py`` extract and caches the features for all URLs existing in a given dataset, creating one feature file (*.pkl) for each URL as well as a single final file (features.complex.all.X.pkl) merging all files (multithreading).

    - folder: experiment's folder
    - dataset: dataset
    - export_html_tags: saves locally the HTML code.
    - force: forces reprocessing, even if the file already exists.
    - outputs:
        - /out/[expX]/[dataset]/features/
            - ok/ -> features files (.pkl for each URL)
            - error/ -> extraction error (one for each URL)
            - html/ -> HTML content for each (successfully) URL
            - features.complex.all.X.pkl (a single file containing: all features (text and html2seq) + y + hash [for all URLs])

2.2 ``features_split.py`` splits the features files (features.complex.all.X.pkl) for a given dataset into a set of group of features, converting the features from a json-like format to a np.array ready to be used for training.

    - folder: experiment's folder
    - dataset: dataset
    - outputs: (K=number of ok/ files, where K<=X)
        - /out/[expX]/[dataset]/features/
            1. features.basic.K.pkl
            2. features.basic_gi.K.pkl
            3. features.all.K.pkl
            4. features.all+html2seq.K.pkl
            5. features.html2seq.K.pkl
            6. features.all+html2seq_pad.K.pkl (linguistic features + padded HTML sequence based on best model HTML)


2.3 ``features_core.py`` implements all the features
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