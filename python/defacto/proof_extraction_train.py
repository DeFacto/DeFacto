
'''
------------------------------------------------------------------------------------------------------------------
UTILS
------------------------------------------------------------------------------------------------------------------
'''
import sklearn

from sklearn.externals import joblib
from sklearn.metrics import classification_report
from sklearn.model_selection import train_test_split


def get_cosine(vec1, vec2):
     intersection = set(vec1.keys()) & set(vec2.keys())
     numerator = sum([vec1[x] * vec2[x] for x in intersection])

     sum1 = sum([vec1[x]**2 for x in vec1.keys()])
     sum2 = sum([vec2[x]**2 for x in vec2.keys()])
     denominator = math.sqrt(sum1) * math.sqrt(sum2)

     if not denominator:
        return 0.0
     else:
        return float(numerator) / denominator

def text_to_vector(text):
     words = WORD.findall(text)
     return Counter(words)

def levenshtein_distance(str1, str2):
    m = len(str1)
    n = len(str2)
    lensum = float(m + n)
    d = []
    for i in range(m + 1):
        d.append([i])
    del d[0][0]
    for j in range(n + 1):
        d[0].append(j)
    for j in range(1, n + 1):
        for i in range(1, m + 1):
            if str1[i - 1] == str2[j - 1]:
                d[i].insert(j, d[i - 1][j - 1])
            else:
                minimum = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + 2)
                d[i].insert(j, minimum)
    ldist = d[-1][-1]
    ratio = (lensum - ldist) / lensum
    return (ldist, ratio)

def _get_vectors(*strs):
    text = [t for t in strs]
    vectorizer = CountVectorizer(text)
    vectorizer.fit(text)
    return vectorizer.transform(text).toarray()

def get_cosine_sim(*strs):
    vectors = [t for t in _get_vectors(*strs)]
    return cosine_similarity(vectors)

def get_jaccard_sim(str1, str2):
    a = set(str1.split())
    b = set(str2.split())
    c = a.intersection(b)
    return float(len(c)) / (len(a) + len(b) - len(c))

def smith_waterman_distance(seq1, seq2, match=3, mismatch=-1, insertion=-1, deletion=-1, normalize=1):
    if len(seq2) > len(seq1): seq1, seq2 = seq2, seq1
    mat = np.zeros((len(seq2) + 1, len(seq1) + 1))
    for i in range(1, mat.shape[0]):
        for j in range(1, mat.shape[1]):
            mat[i, j] = max(
                0,
                mat[i - 1, j - 1] + (match if seq1[j - 1] == seq2[i - 1] else mismatch),
                mat[i - 1, j] + deletion,
                mat[i, j - 1] + insertion
            )
    return np.max(mat) / (len(seq2) * match) if normalize else np.max(mat)

'''
------------------------------------------------------------------------------------------------------------------
DeFacto
------------------------------------------------------------------------------------------------------------------
'''

def getDocContentFromFile(doc_filename):
    try:
        content=[]
        entities=[]
        fullpath = PATH_WIKIPAGES + doc_filename + ".json"
        with open(fullpath) as f:
            fileContent = json.load(f)
        #file = codecs.open(PATH_WIKIPAGES + doc_filename + ".json")
        #fileContent = json.load(file)
            for lines in fileContent["lines"]:
                x = lines["content"].strip()
                x = x.replace("-LRB-", " (")
                x = x.replace("-RRB-",") ")
                x = x.replace("-SLH-", "/")
                x = x.replace("-LSB-", " [")
                x = x.replace("-RSB-", "] ")
                x = x.replace("_", " ")
                x = x.replace("-COLON-", ":")
                content.append(x)
                if lines["namedEntitiesList"] is not None and len(lines["namedEntitiesList"])>0:
                    entities.append(list(set(lines["namedEntitiesList"])))
                else:
                    entities.append([])

        return content, entities
    except Exception as e:
        print("Could not find or open file: ")
        print(fullpath)
        print(repr(e))
        print("")
        return None


# TODO:
# levenshtein_distance against claim subject X proof_candidates_subjects
# levenshtein_distance against claim object X proof_candidates_object

def substring_indexes(substring, string):
    """
    Generate indices of where substring begins in string
    """
    last_found = -1
    while True:
        # Find next index of substring, by starting after its last known position
        last_found = string.find(substring, last_found + 1)
        if last_found == -1:
            break
        yield last_found

def _extract_features(proof_candidate, claim, claim_spo_lst):
    '''

    :param proof_candidate: either a proof or just a sentence (not proof)
    :param claim: input claim
    :param claim_spo_lst: triples extracted from a claim
    :return:
    '''
    try:

        MAX_DIST = 99999
        THETA_RELAX = 0.9
        X=[]

        proof_doc = nlp(proof_candidate)

        tot_neg = 0
        for token in proof_doc:
            if token in neg_keyword_set:
                tot_neg += 1

        X.append(tot_neg)
        X.append(smith_waterman_distance(claim, proof_candidate, 3, -2, -2, -2, 1))
        X.append(get_jaccard_sim(claim, proof_candidate))
        X.append(get_cosine(text_to_vector(claim), text_to_vector(proof_candidate)))
        X.append(len([1 for t in proof_doc]))
        X.append(len([c for c in proof_candidate if c.isdigit()]))
        X.append(len([c for c in proof_candidate if not c.isalnum()]))
        X.append(len([c for c in proof_candidate if c == '!']))
        X.append(len([c for c in proof_candidate if c == ',']))
        X.append(len([c for c in proof_candidate if c == '?']))
        X.append(len([c for c in proof_candidate if c == '.']))
        X.append(len([t for t in proof_doc if t.text[0].isupper()]))
        X.append(len([t for t in proof_doc if t.text.isalpha()]))
        X.append(len([t for t in proof_doc if t.text in stopwords]))
        X.append(len(proof_doc.ents))

        swd_max = 0.0
        jac_max = 0.0
        cos_max = 0.0

        swd_max_s = 0.0
        jac_max_s = 0.0
        cos_max_s = 0.0

        swd_max_o = 0.0
        jac_max_o = 0.0
        cos_max_o = 0.0

        subject_found = 0
        predicate_found = 0
        object_found = 0

        s_o_found = 0
        s_p_o_found = 0
        s_o_rlx_found = 0

        max_jac_sim_subject = 0.0
        max_jac_sim_object = 0.0

        dist_s_o = MAX_DIST

        dist_tok_neg_s_min = MAX_DIST
        dist_tok_neg_p_min = MAX_DIST
        dist_tok_neg_o_min = MAX_DIST
        dist_indexes_relaxed = MAX_DIST

        for triple in claim_spo_lst:
            str_triple = ' '.join(triple)

            s_tokens = [tok for tok in triple[0].split() if tok not in stopwords and tok not in punctuations]
            p_tokens = [tok for tok in triple[1].split() if tok not in stopwords and tok not in punctuations]
            o_tokens = [tok for tok in triple[2].split() if tok not in stopwords and tok not in punctuations]

            triple_s_clean = ' '.join(s_tokens)
            triple_p_clean = ' '.join(p_tokens)
            triple_o_clean = ' '.join(o_tokens)

            triple_s_clean = triple_s_clean.lower()
            triple_p_clean = triple_p_clean.lower()
            triple_o_clean = triple_o_clean.lower()

            _s = smith_waterman_distance(proof_candidate, str_triple, 3, -2, -2, -2, 1)
            _j = get_jaccard_sim(proof_candidate, str_triple)
            _c = (get_cosine(text_to_vector(proof_candidate), text_to_vector(str_triple)))

            swd_max = _s if _s > swd_max else swd_max
            jac_max = _j if _j > jac_max else jac_max
            cos_max = _c if _c > cos_max else cos_max

            _s = smith_waterman_distance(proof_candidate, triple_s_clean, 3, -2, -2, -2, 1)
            _j = get_jaccard_sim(proof_candidate, triple_s_clean)
            _c = (get_cosine(text_to_vector(proof_candidate), text_to_vector(triple[0])))

            swd_max_s = _s if _s > swd_max_s else swd_max_s
            jac_max_s = _j if _j > jac_max_s else jac_max_s
            cos_max_s = _c if _c > cos_max_s else cos_max_s

            _s = smith_waterman_distance(proof_candidate, triple_o_clean, 3, -2, -2, -2, 1)
            _j = get_jaccard_sim(proof_candidate, triple_o_clean)
            _c = (get_cosine(text_to_vector(proof_candidate), text_to_vector(triple[2])))

            swd_max_o = _s if _s > swd_max_o else swd_max_o
            jac_max_o = _j if _j > jac_max_o else jac_max_o
            cos_max_o = _c if _c > cos_max_o else cos_max_o

            # exact string match
            subject_found_t = 0
            predicate_found_t = 0
            object_found_t = 0

            proof_doc_text_lower = proof_doc.text.lower()

            if triple_s_clean in proof_doc_text_lower: subject_found_t = 1
            if triple_p_clean in proof_doc_text_lower: predicate_found_t = 1
            if triple_o_clean in proof_doc_text_lower: object_found_t = 1

            if subject_found == 0: subject_found = subject_found_t
            if predicate_found == 0: predicate_found = predicate_found_t
            if object_found == 0: object_found = object_found_t

            if s_o_found == 0: s_o_found = int(subject_found_t and object_found_t)
            if s_p_o_found == 0: s_p_o_found = int(subject_found_t and predicate_found_t and object_found_t)

            if subject_found_t and object_found_t:

                idx_s = substring_indexes(triple_s_clean, proof_doc_text_lower)
                idx_p = substring_indexes(triple_p_clean, proof_doc_text_lower)
                idx_o = substring_indexes(triple_o_clean, proof_doc_text_lower)
                idx_neg = []
                for neg in neg_keyword_set:
                    idx_neg.extend(substring_indexes(neg, proof_doc_text_lower))


                for i_s in idx_s:
                    for i_o in idx_o:
                        if dist_s_o > (abs(i_s - i_o)): dist_s_o = abs(i_s - i_o)
                        for i_p in idx_p:
                            for i_neg in idx_neg:
                                if dist_tok_neg_s_min > (abs(i_s - i_neg)):
                                    dist_tok_neg_s_min = abs(i_s - i_neg)
                                if dist_tok_neg_o_min > (abs(i_o - i_neg)):
                                    dist_tok_neg_o_min = abs(i_o - i_neg)
                                if dist_tok_neg_p_min > (abs(i_p - i_neg)):
                                    dist_tok_neg_p_min = abs(i_p - i_neg)


            # relaxed string search
            index_max_jac_subject = -1
            index_max_jac_object = -1
            for i in range(len(proof_doc)):
                token=proof_doc[i].text
                _jts = get_jaccard_sim(token.lower(), triple_s_clean)
                if max_jac_sim_subject < _jts:
                    max_jac_sim_subject = _jts
                    index_max_jac_subject = i

                _jto = get_jaccard_sim(token.lower(), triple_o_clean)
                if max_jac_sim_object < _jto:
                    max_jac_sim_object = _jto
                    index_max_jac_object = i

                if index_max_jac_subject != -1 and index_max_jac_object != -1:
                    if dist_indexes_relaxed > (abs(index_max_jac_subject - index_max_jac_object)):
                        dist_indexes_relaxed = abs(index_max_jac_subject - index_max_jac_object)


                if s_o_rlx_found == 0: s_o_rlx_found = (1 if _jts >= THETA_RELAX and _jto >= THETA_RELAX else 0)


        X.append(swd_max)
        X.append(jac_max)
        X.append(cos_max)
        X.append(swd_max_s)
        X.append(jac_max_s)
        X.append(cos_max_s)
        X.append(swd_max_o)
        X.append(jac_max_o)
        X.append(cos_max_o)
        X.append(subject_found)
        X.append(predicate_found)
        X.append(object_found)
        X.append(s_o_found)
        X.append(dist_s_o)
        X.append(s_p_o_found)
        X.append(dist_tok_neg_s_min)
        X.append(dist_tok_neg_p_min)
        X.append(dist_tok_neg_o_min)
        X.append(max_jac_sim_subject)
        X.append(max_jac_sim_object)
        X.append(dist_indexes_relaxed)
        X.append(s_o_rlx_found)


        return X

    except Exception as e:
        raise e



def train_model():
    try:
        from sklearn.ensemble import RandomForestClassifier

        with open(defacto_output_folder + 'features.proof.train', 'rb') as handle:
            data = pickle.load(handle)
            data = np.array(data)
            data_X = data[:, 0]
            data_y = data[:, 1]

            print('reshaping the data...')
            X = []
            [X.extend(row) for row in [row2 for row2 in data_X]]

            y = []
            [y.extend(row) for row in [r for r in data_y]]

            X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)

            print('training the classifier...')
            clf = RandomForestClassifier()
            model = clf.fit(X_train, y_train)
            predictions = model.predict(X_test)
            #P, R, F, S = sklearn.metrics.precision_recall_fscore_support(y_test, predictions)
            print(classification_report(y_test, predictions, digits=3))

            filename = defacto_output_folder + 'rfc.mod'
            joblib.dump(model, filename)

    except Exception as e:
        print(repr(e))

'''
out = predict('aniketh was born in rio', [['dddasd'], ['asdsadasd']])
= [1, 0]




def predict(claim, sentences):
    X = feature.extract(claim, sentences)
    pred = model.predict(X)
    return pred
'''



def extract_features(defactoNL_file):
    try:
        import os
        from defacto.model_nl import ModelNL
        print(os.getcwd() + '/' + defactoNL_file)
        with open(os.getcwd() + '/' + defactoNL_file, 'rb') as handle:
            defactoNL = pickle.load(handle)
            X = []
            y = []
            if defactoNL.error_on_extract_triples is True:
                raise Exception('error on defacto triple extraction')

            for proof in defactoNL.proofs:
                y.append(1)
                X.append(_extract_features(proof, defactoNL.claim, defactoNL.triples))

            for non_proof in defactoNL.sentences:
                y.append(0)
                X.append(_extract_features(non_proof, defactoNL.claim, defactoNL.triples))

            assert len(X) == len(y)
            return (X, y)

    except Exception as e:
        print('-- error ', repr(e))

def export_training_data_proof_detection(defacto_output_folder):
    import glob

    try:

        job_args=[]
        i = 0
        f = Path(ROOT_PATH + "/" + defacto_output_folder + 'features.proof.train')
        if not f.exists():
            for file in glob.glob(defacto_output_folder + "*.pkl"):
                if i > MAX_TRAINING_DATA:
                    break
                job_args.append(file)
                i += 1

        print('export_training_data_proof_detection: job args created: ' + str(len(job_args)))
        if len(job_args) > 0:
            with Pool(processes=int(4)) as pool:
                out = pool.map(extract_features, job_args)

            with open(defacto_output_folder + 'features.proof.train', 'wb') as handle:
                pickle.dump(out, handle, protocol=pickle.HIGHEST_PROTOCOL)
    except Exception as e:
        print(repr(e))

def export_defacto_models(train_file):
    try:
        job_args = []
        with jsonlines.open(train_file, mode='r') as reader:
            for obj in reader:
                f = Path(ROOT_PATH + "/" + defacto_output_folder + 'defacto_' + str(obj["id"]) + '.pkl')
                if not f.exists() and obj["label"] != 'NOT ENOUGH INFO':
                    job_args.append((obj["id"], obj["claim"], obj["label"], obj["evidence"][0]))

        print('export_defacto_models: job args created: ' + str(len(job_args)))

        if len(job_args) > 0:
            with Pool(processes=int(4)) as pool:
                err_asyncres = pool.starmap(save_defacto_model, job_args)
            print('done! tot errors:', np.count_nonzero(err_asyncres, 0))

    except Exception as e:
        print(e)

def save_defacto_model(fever_id, claim, label, evidences_train):
    try:
        from defacto.model_nl import ModelNL
        defactoNL = ModelNL(claim=claim, label=label, language='en', fever_id=fever_id)
        id_sent_proofs = {}
        # first gets the sentence ids (from train.jsonl)
        for evidence_meta in evidences_train:
            filename = evidence_meta[2]
            proof_id = [evidence_meta[3]]
            if filename not in id_sent_proofs:
                id_sent_proofs[filename] = []
            id_sent_proofs[filename].extend(proof_id)

        # extracting sentences generically
        for evidence_meta in evidences_train:  # train file
            filename = evidence_meta[2]
            id_sentence_supports_refutes = evidence_meta[3]
            if filename not in defactoNL.external_documents_names:
                defactoNL.external_documents_names.append(filename)
                _e, _tts = getDocContentFromFile(filename)
                for index in range(len(_e)):
                    if _e[index] != '':
                        if index in id_sent_proofs[filename]:
                            defactoNL.proofs.append(_e[index])
                            defactoNL.proofs_tt.append(_tts[index])
                        else:
                            defactoNL.sentences.append(_e[index])
                            defactoNL.sentences_tt.append(_tts[index])

        with open(defacto_output_folder + 'defacto_' + str(defactoNL.id) + '.pkl', 'wb') as handle:
            pickle.dump(defactoNL, handle, protocol=pickle.HIGHEST_PROTOCOL)
        return 0
    except Exception as e:
        print(e)
        return 1


if __name__ == '__main__':

    # 1. proof candidate (YES, NO)
    # 2. fact-checking (REFUTES, SUPPORT, NOT ENOUGH INFO)
    # export PYTHONPATH=$PYTHONPATH:/Users/diegoesteves/Github/factchecking/DeFacto/python/

    try:
        import sys
        import os
        import codecs
        import numpy as np
        import json
        import jsonlines
        import pickle
        from multiprocessing.dummy import Pool
        import spacy

        # PATH_WIKIPAGES = '/data/defacto/github/fever/data/wiki-pages/wiki-pages-split/'
        PATH_WIKIPAGES = '/Users/diegoesteves/Github/factchecking/DeFacto/python/defacto/'

        # nlp = spacy.load('en_core_web_sm') # no vectors
        nlp = spacy.load('en_core_web_md')
        # lg ?

        from collections import Counter
        from sklearn.feature_extraction.text import CountVectorizer
        from sklearn.metrics.pairwise import cosine_similarity
        import re, math
        import string
        from pathlib import Path

        WORD = re.compile(r'\w+')

        neg_keyword_set = {"don't", "do not", "never", "nothing", "nowhere", "noone", "none", "not",
                           "hasn't", "has not", "hadn't", "had not", "haven't", "have not", "can't", "can not",
                           "couldn't", "could not", "shouldn't", "should not", "won't", "will not",
                           "wouldn't", "would not", "doesn't", "does not",
                           "didn't", "did not", "isn't", "is not", "aren't", "are not", "ain't", "am not"}

        from nltk.corpus import stopwords

        stopwords = stopwords.words('english')
        punctuations = string.punctuation

        ROOT_PATH = os.getcwd()
        MAX_TRAINING_DATA = 1000
        train_file = "small_train.jsonl"
        defacto_output_folder = 'defacto_models/'

        print('export_defacto_models()')
        export_defacto_models(train_file)
        print('=======================================================================================')
        print('export_training_data_proof_detection()')
        export_training_data_proof_detection(defacto_output_folder)
        print('=======================================================================================')
        print('train_model()')
        train_model()

    except Exception as e:
        print(e)