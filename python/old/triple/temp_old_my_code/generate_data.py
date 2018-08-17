from algorithms.triple import feature_extractor
from util.sqlite_helper import SQLiteHelper, TripeScorerDB

f = open('../../data/iswc/final_iswc_2017.csv',"r")
lines = f.readlines()
lines = lines[1:]
features = []
aux = 0
for line in lines:
    aux = aux + 1
    print('example: ' + str(aux))
    line = line.split(",")
    p = []
    for w in line:
        p.append(w.strip()[1:][:-1])
    line = p
    #with SQLiteHelper() as sqlcon:
    #    t = TripeScorerDB(sqlcon)
    #    if line[1] != "":
    #        idtriple1 = t.get_triple_by_spolang(line[0], "isDomiciledIn", line[1], "en")
    #    if line[2] != "":
    #        idtriple2 = t.get_triple_by_spolang(line[0], "hasHeadquartersPhoneNumber", line[2], "en")
    #    if line[3] != "":
    #        idtriple3 = t.get_triple_by_spolang(line[0], "hasLatestOrganizationFoundedDate", line[3], "en")

    if line[1]!="":
        #with SQLiteHelper() as sqlcon:
        #    t = TripeScorerDB(sqlcon)
            #if t.metaquery_processed(idtriple1.id, "SPO") == False:
                domicile = feature_extractor.FeatureExtractor(line[0],"isDomiciledIn",line[1])
                #domicile.query_s()
                #domicile.query_so()
                domicile.query_spo()

                #f = domicile.get_features()
                #arr = []
                #arr.append(line[0])
                #arr.append("isDomiciledIn")
                #arr.append(line[1])%S "address" %O
                #arr = arr + list(f)
                #features.append(arr)

    if line[2]!="":
        #with SQLiteHelper() as sqlcon:
        #    t = TripeScorerDB(sqlcon)
        #    if t.metaquery_processed(idtriple2.id, "SPO") == False:
                phone = feature_extractor.FeatureExtractor(line[0],"hasHeadquartersPhoneNumber",line[2])
                #phone.query_s()
                #phone.query_so()
                phone.query_spo()

                #f = phone.get_features()
                #arr = []
                #arr.append(line[0])
                #arr.append("hasHeadquartersPhoneNumber")
                #arr.append(line[2])
                #arr = arr + list(f)
                #features.append(arr)

    if line[3]!="":
        #with SQLiteHelper() as sqlcon:
        #    t = TripeScorerDB(sqlcon)
        #    if t.metaquery_processed(idtriple3.id, "SPO") == False:
                date = feature_extractor.FeatureExtractor(line[0],"hasLatestOrganizationFoundedDate",line[3])
                #date.query_s()
                #date.query_so()
                date.query_spo()

                #f = date.get_features()
                #arr = []
                #arr.append(line[0])
                #arr.append("hasLatestOrganizationFoundedDate")
                #arr.append(line[3])
                #arr = arr + list(f)
                #features.append(arr)

exit(0)
file = open("iswc_features.txt","w+")
for f in features:
    file.write(str(f))