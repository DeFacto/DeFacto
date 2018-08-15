from config import DeFactoConfig
import pandas as pd
import os
from pathlib import Path
import urllib.request

config = DeFactoConfig()


def __fix_url_path(url, oncemore=False):
    try:
        folder = url.replace('http://', '').split('/')[0]
        path_root = Path(config.dataset_ext_microsoft_webcred_webpages_cache + folder)
        if not path_root.exists():
            return False, path_root, folder
        else:
            return True, 'fake', 'fake'
            # first consider that the full path is available  = path+file
            path = Path(config.dataset_ext_microsoft_webcred_webpages_cache + url.replace('http://', ''))
            if oncemore is True:
                path = Path(config.dataset_ext_microsoft_webcred_webpages_cache + url.replace('http://', '') + '.html')
            if path.exists():
                if path.is_file():
                    return True, str(path.absolute())
                elif path.is_dir():
                    # maybe it has just the path without the filename
                    # in this case we should try index.html
                    for file in os.listdir(str(path)):
                        if file.endswith(".html") or file.endswith(".htm"):
                            file_generic = path / file
                            if file_generic.is_file():
                                return True, str(file_generic.absolute())
                    print(':: WARN the path ' + str(path) + ' does not have any html/htm file! try to scrap the web...')
                    return False, None
            else:
                # check if it's the file itself, without the extension
                if oncemore is False:
                    return __fix_url_path(url, True)
                else:
                    # path does not exist
                    print(':: WARN the path ' + str(path) + ' does not exist! try to scrap the web...')
                    return False, None
    except:
        raise



df = pd.read_csv(config.dataset_microsoft_webcred, encoding='utf-8', delimiter='\t', header=0)
web_err = []
folder_err = []
for index, row in df.iterrows():
    topic = row[0]
    query = row[1]
    rank = int(row[2])
    url = str(row[3])
    likert = int(row[4])

    works, path, folder = __fix_url_path(url)
    if works is False:
        folder_err.append(folder)
        web_err.append(url)
        newdir = config.dataset_ext_microsoft_webcred_webpages_cache_missing + url.replace('http://', '')
        file = 'index.html'
        if ('.html' or '.htm') in newdir:
            file = newdir.split('/')[-1]
            newdir = newdir.replace(file, '')
        if not os.path.exists(newdir):
            os.makedirs(newdir)
        if newdir[-1] != '/':
            newdir = newdir + '/'
        try:
            urllib.request.urlretrieve(url, newdir + file)
            print(':: OK: ' + url)
        except Exception as e:
            print(':: ERROR: ' + url)
            print(e)
            pass


print(':: number of missing websites = ', len(web_err))
print(web_err)
print(':: number of missing domains = ', len(set(folder_err)))
print(set(folder_err))
