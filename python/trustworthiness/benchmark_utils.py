import math
from math import sqrt
import plotly.plotly as py
import plotly.graph_objs as go
import numpy as np
from sklearn.externals import joblib
from sklearn.metrics import precision_recall_fscore_support, mean_absolute_error, mean_squared_error, \
    explained_variance_score, r2_score
from sklearn.model_selection import GridSearchCV, RandomizedSearchCV
import os
from config import DeFactoConfig
from defacto.definitions import BENCHMARK_FILE_NAME_TEMPLATE, EXP_2_CLASSES_LABEL, EXP_3_CLASSES_LABEL, \
    EXP_5_CLASSES_LABEL, CROSS_VALIDATION_K_FOLDS, OUTPUT_FOLDER, LABELS_2_CLASSES, LABELS_3_CLASSES, LINE_TEMPLATE

config = DeFactoConfig()

SERIES_COLORS = ['rgb(205, 12, 24)', 'rgb(22, 96, 167)', 'rgb(128, 128, 128)', 'rgb(0, 0, 139)',
                         'rgb(192,192,192)', 'rgb(211,211,211)', 'rgb(255,255,0)', 'rgb(0,128,0)']
# TOT_TEXT_FEAT = 53

BAR_COLOR = 'rgb(128,128,128)'

def verify_and_create_experiment_folders(out_exp_folder, dataset):
    try:
        path = OUTPUT_FOLDER + out_exp_folder + dataset + '/'
        if not os.path.exists(path):
            os.makedirs(path)


        folders_text = ['benchmark/text/2-classes/cls/', 'benchmark/text/3-classes/cls/', 'benchmark/text/5-classes/cls/',
                      'benchmark/text/2-classes/log/', 'benchmark/text/3-classes/log/', 'benchmark/text/5-classes/log/',
                      'benchmark/text/2-classes/graph/', 'benchmark/text/3-classes/graph/', 'benchmark/text/5-classes/graph/']

        folders_html = ['benchmark/html/2-classes/cls/', 'benchmark/html/3-classes/cls/', 'benchmark/html/5-classes/cls/',
                      'benchmark/html/2-classes/log/', 'benchmark/html/3-classes/log/', 'benchmark/html/5-classes/log/',
                      'benchmark/html/2-classes/graph/', 'benchmark/html/3-classes/graph/', 'benchmark/html/5-classes/graph/']

        folders_text_html = ['benchmark/text_html/2-classes/cls/', 'benchmark/text_html/3-classes/cls/', 'benchmark/text_html/5-classes/cls/',
                      'benchmark/text_html/2-classes/log/', 'benchmark/text_html/3-classes/log/', 'benchmark/text_html/5-classes/log/',
                      'benchmark/text_html/2-classes/graph/', 'benchmark/text_html/3-classes/graph/', 'benchmark/text_html/5-classes/graph/']

        folders_text_html = ['benchmark/html2seq/2-classes/cls/',
                             'benchmark/html2seq/3-classes/cls/',
                             'benchmark/html2seq/5-classes/cls/',
                             'benchmark/html2seq/2-classes/log/',
                             'benchmark/html2seq/3-classes/log/',
                             'benchmark/html2seq/5-classes/log/',
                             'benchmark/html2seq/2-classes/graph/',
                             'benchmark/html2seq/3-classes/graph/',
                             'benchmark/html2seq/5-classes/graph/']

        subfolders = ['features/ok/', 'features/error/', 'features/html/']

        subfolders.extend(folders_text)
        subfolders.extend(folders_html)
        subfolders.extend(folders_text_html)
        subfolders.extend(folders_text_html)

        for subfolder in subfolders:
            if not os.path.exists(path + subfolder):
                os.makedirs(path + subfolder)

        config.logger.info('experiment sub-folders created successfully: ' + path)

    except Exception as e:
        raise e

def append_annotation_style(x, y, extra_text_y=None):
    if extra_text_y is not None and extra_text_y != '':
        text='{0:.3f}'.format(y) + ' - size: ' + str(extra_text_y)
    else:
        text='{0:.3f}'.format(y)
    return dict(
            x=x,
            y=y,
            xref='x',
            yref='y',
            text=text,
            showarrow=True,
            font=dict(
                family='Helvetica',
                size=11,
                color='#ffffff'
            ),
            align='center',
            arrowhead=2,
            arrowsize=1,
            arrowwidth=2,
            arrowcolor='#636363',
            ax=20,
            ay=-30,
            bordercolor='#c7c7c7',
            borderwidth=2,
            borderpad=4,
            bgcolor='#ff7f0e',
            opacity=0.8
        )

def get_annotation_from_max(traces, paddings, x_labels):

    annotations = []
    for trace in traces:
        max_trace=-999999
        index=-1
        label_y = None
        for i in range(len(trace)):
            if trace[i] > max_trace:
                label_y, max_trace, index = x_labels[i], trace[i], i

        annotations.append(append_annotation_style(paddings[index], max_trace, extra_text_y=label_y))

    return annotations

def export_chart_scatter(x, y_labels, y_5_f1, y_3_f1, y_2_f1, filename, exp_folder, ds_folder, title, x_title, y_title, log_mode=True):

    try:
        if log_mode == True:
            x_labels = x.copy()
            x = [math.log(pad) for pad in x]
        line_width=1
        mode='lines+markers'
        data_5 = []
        data_3 = []
        data_2 = []

        assert log_mode == True # otherwise need to adjust the function
        assert len(y_5_f1) == len(y_2_f1) == len(y_3_f1)
        assert len(y_5_f1) <= len(SERIES_COLORS)

        i=0
        for trace in y_5_f1:
            s = go.Scatter(
                x=x,
                y=trace,
                text=trace,
                name=y_labels[i],
                mode=mode,
                line=dict(
                    color=(SERIES_COLORS[i]),
                    width=line_width))
            data_5.append(s)
            i += 1

        i = 0
        for trace in y_3_f1:
            s = go.Scatter(
                x=x,
                y=trace,
                text=trace,
                name=y_labels[i],
                mode=mode,
                line=dict(
                    color=(SERIES_COLORS[i]),
                    width=line_width,
                    dash='dot'))
            data_3.append(s)
            i += 1

        i=0
        for trace in y_2_f1:
            s = go.Scatter(
                x=x,
                y=trace,
                text=trace,
                name=y_labels[i],
                mode=mode,
                line=dict(
                    color=(SERIES_COLORS[i]),
                    width=line_width,
                    dash='dash'))
            data_2.append(s)
            i += 1

        y_5 = np.array(y_5_f1)
        y_3 = np.array(y_3_f1)
        y_2 = np.array(y_2_f1)

        # Edit the layout
        layout_5 = dict(title=title,
                        xaxis=dict(title=x_title, showticklabels=True, showline=True,
                                 autorange=True, showgrid=True, zeroline=True, gridcolor='#bdbdbd'),
                        yaxis=dict(title=y_title, showticklabels=True, showline=True, autorange=True),
                        show_legend=True,
                        legend=dict(orientation='h',
                                  x=math.log(1) if log_mode == True else 1,
                                  y=-20,
                                  bordercolor='#808080',
                                  borderwidth=2
                                  ),
                        annotations=get_annotation_from_max(y_5, x, x_labels),
                        font=dict(family='Helvetica', size=14)
                        )
        layout_3 = layout_5.copy()
        layout_2 = layout_5.copy()

        layout_3['annotations'] = get_annotation_from_max(y_3, x, x_labels)
        layout_2['annotations'] = get_annotation_from_max(y_2, x, x_labels)

        #from plotly import tools
        #fig = tools.make_subplots(rows=2, cols=1, subplot_titles=('Likert Scale',
          #                                                        'Non-credible x Credible'))
        #fig.append_trace(trace0, 1, 1)
        #fig.append_trace(trace1, 1, 1)
        #fig.append_trace(trace2, 1, 1)
        #fig.append_trace(trace3, 1, 1)

        #fig.append_trace(trace4, 2, 1)
        #fig.append_trace(trace5, 2, 1)
        #fig.append_trace(trace6, 2, 1)
        #fig.append_trace(trace7, 2, 1)

        #fig['layout'].update = layout
        #py.plot(fig, filename='paddings_f1')

        _path = OUTPUT_FOLDER + exp_folder + ds_folder + 'graphs/'
        if not os.path.exists(_path):
            os.mkdir(_path)

        fig = dict(data=data_5, layout=layout_5)
        py.image.save_as(fig, filename=_path + filename + '_5class.png')

        fig = dict(data=data_3, layout=layout_3)
        py.image.save_as(fig, filename=_path + filename + '_3class.png')

        fig = dict(data=data_2, layout=layout_2)
        py.image.save_as(fig, filename=_path + filename + '_2class.png')

    except Exception as e:
        raise e

def train_test_export_save_per_exp_type(estimator, estimator_label, hyperparameters, search_method,
                                        X_train, X_test, y_train, y_test, experiment_type, padding,
                                        out_chart, file_log, subfolder, exp_folder, ds_folder):
    try:
        config.logger.info(estimator_label)
        # file dump info
        file = BENCHMARK_FILE_NAME_TEMPLATE % (estimator_label.lower(), padding, experiment_type)

        ## loading the classifier
        # if isinstance(clf, str):
        #    clf = joblib.load(OUTPUT_FOLDER + exp_folder + ds_folder + 'models/' + subfolder + clf)

        # grid search on 10-fold cross validation
        if experiment_type == EXP_2_CLASSES_LABEL:
            scoring = ['precision', 'recall', 'f1']
            refit = 'f1'
        elif experiment_type == EXP_3_CLASSES_LABEL:
            scoring = ['precision_weighted',
                       'recall_weighted',
                       'f1_weighted']
            refit = 'f1_weighted'
        elif experiment_type == EXP_5_CLASSES_LABEL:
            scoring = ['r2', 'neg_mean_squared_error', 'neg_mean_absolute_error', 'explained_variance']
            refit = 'r2'

        else:
            raise Exception('not supported! ' + experiment_type)

        if search_method == 'grid':
            clf = GridSearchCV(estimator, hyperparameters, cv=CROSS_VALIDATION_K_FOLDS, scoring=scoring, n_jobs=-1,
                               refit=refit)
        elif search_method == 'random':
            clf = RandomizedSearchCV(estimator, hyperparameters, cv=CROSS_VALIDATION_K_FOLDS, scoring=scoring,
                                     n_jobs=-1,
                                     refit=refit)
        else:
            raise Exception('not supported! ' + search_method)

        clf.fit(X_train, y_train)
        config.logger.info('best training set parameters: ')
        config.logger.info(clf.best_params_)
        config.logger.info(clf.best_score_)
        config.logger.info(experiment_type)
        predicted = clf.best_estimator_.predict(X_test)

        # saving the best model
        _path = OUTPUT_FOLDER + exp_folder + ds_folder + 'benchmark/' + subfolder + experiment_type + '/cls/'
        if not os.path.exists(_path):
            os.mkdir(_path)
        joblib.dump(clf.best_estimator_, _path + file)

        # saving the best parameters
        best_parameters_file_name = file.replace('.pkl', '.best_param')
        with open(_path + best_parameters_file_name, "w") as best:
            best.write(' -- best params \n')
            best.write(str(clf.best_params_))
            best.write(' -- best score \n')
            best.write(str(clf.best_score_))

        if experiment_type == EXP_2_CLASSES_LABEL or experiment_type == EXP_3_CLASSES_LABEL:
            p, r, f, s = precision_recall_fscore_support(y_test, predicted)
            p_weighted, r_weighted, f_weighted, s_weighted = precision_recall_fscore_support(y_test, predicted,
                                                                                             average='weighted')
            p_micro, r_micro, f_micro, s_micro = precision_recall_fscore_support(y_test, predicted, average='micro')
            p_macro, r_macro, f_macro, s_macro = precision_recall_fscore_support(y_test, predicted, average='macro')

            if experiment_type == EXP_2_CLASSES_LABEL:
                d = LABELS_2_CLASSES
            else:
                d = LABELS_3_CLASSES

            for i in range(len(p)):
                file_log.write(LINE_TEMPLATE % (
                    estimator_label, experiment_type, padding, d.get(i), p[i], r[i], f[i], s[i], 0))
            file_log.write(LINE_TEMPLATE % (
            estimator_label, experiment_type, padding, 'weighted', p_weighted, r_weighted, f_weighted, 0, 0))
            file_log.write(
                LINE_TEMPLATE % (estimator_label, experiment_type, padding, 'micro', p_micro, r_micro, f_micro, 0, 0))
            file_log.write(
                LINE_TEMPLATE % (estimator_label, experiment_type, padding, 'macro', p_macro, r_macro, f_macro, 0, 0))

            out_chart.append([p_weighted, r_weighted, f_weighted])
            config.logger.info(
                'padding: %s F1 test (avg): %.3f' % (padding, f_weighted))
            config.logger.info('----------------------------------------------------')

            file_log.flush()
            return out_chart, clf.best_estimator_

        elif experiment_type == EXP_5_CLASSES_LABEL:
            mae = mean_absolute_error(y_test, predicted)
            rmse = sqrt(mean_squared_error(y_test, predicted))
            evar = explained_variance_score(y_test, predicted)
            r2 = r2_score(y_test, predicted)
            file_log.write(
                LINE_TEMPLATE % (estimator_label, experiment_type, padding, experiment_type, r2, rmse, mae, evar, 0))
            file_log.flush()
            config.logger.info(
                'padding: %s cls: %s exp_type: %s r2: %.3f rmse: %.3f mae: %.3f evar: %.3f' %
                (padding, estimator_label, experiment_type, r2, rmse, mae, evar))
            config.logger.info('----------------------------------------------------')
            return None, clf.best_estimator_

        else:
            raise Exception('not supported! ' + experiment_type)

        '''

        # file logging details
        p, r, f, s = precision_recall_fscore_support(y_test, predicted)
        if experiment_type in (EXP_2_CLASSES_LABEL, EXP_3_CLASSES_LABEL):
            tn, fp, fn, tp =confusion_matrix(y_test, predicted).ravel()
            fpr=fp/(fp+tn)
            fnr=fn/(tp+fn)

            unique, counts = np.unique(y_test, return_counts=True)
            tot = counts[0] + counts[1]

            rate_avg=np.average([fpr, fnr], weights=[counts[0]/tot, counts[1]/tot])
            #fpr_0, tpr_0, thresholds_0 = roc_curve(y_test, predicted, pos_label=0)
            #fpr_1, tpr_1, thresholds_1 = roc_curve(y_test, predicted, pos_label=1)

            file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, LABELS_2_CLASSES.get(0), p[0], r[0], f[0], s[0], fpr))
            file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, LABELS_2_CLASSES.get(1), p[1], r[1], f[1], s[1], fnr))
            file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, 'average', p_avg, r_avg, f_avg, s[0] + s[1], rate_avg))

        else:
            for i in range(len(p)):
                file_log.write(LINE_TEMPLATE % (estimator_label, experiment_type, padding, LABELS_5_CLASSES.get(i + 1), p[i], r[i], f[i], s[i], 0))
        '''



    except Exception as e:
        config.logger.error(repr(e))
        raise

    # return test(clf, X_test, y_test, out_chart, padding, estimator_label, experiment_type, file_log, subfolder, exp_folder, ds_folder)