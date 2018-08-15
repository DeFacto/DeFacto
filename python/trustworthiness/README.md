## Automated Web Credibility

This project provides the data and models described on the paper:

"**Belliting the Source: Trustworthiness Indicators to Obfuscate Fake News on the Web**"

#### Pre-processing

-  ``fix_dataset_microsoft.py`` to fix the original Microsoft Credibility dataset.

#### Feature Extraction

-  ``feature_extractor.py`` extract and caches the features for all websites.

#### Run

- ``classifiers\benchmark.py`` to obtain the results and save the models

#### FactBench Eval

- ``factbench.py`` extracts the features and uses a trained model to make predictions on each URL from the **FactBench2012_Credibility** dataset. This dataset is created from URLs obtained from DeFacto's output over positive and negative data from **FactBench** dataset.