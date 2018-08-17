import feature_extractor

f = open('test_data_all_3_predicates.csv',"r")
g = open('class.txt',"w+", 0)
lines = f.readlines()
lines = lines[1:]
features1 = []
features2 = []
features3 = []
for line in lines:
    line = line.split(",")
    p = []
    for w in line:
        w = w[1:]
        w= w[:-1]
        p.append(w)
    line = p
    x = 0
    if line[1].strip()!="" and bool(set(line[1].strip()) & set("http")):
        print line[2]
        domicile = feature_extractor.FeatureExtractor(line[0],"isDomiciledIn",line[2])
        idnum = line[1].strip()
        idnum = idnum[18:]
        domicile.query_s(idnum)
        domicile.query_so()
        # domicile.query_spo()
        f = domicile.get_features()
        arr = []
        arr.append(line[1])
        arr = arr + list(f)
        x = arr[1]
        features1.append(arr)
        if arr[2]>0:
            temp = "<" + line[1] + '> <http://swc2017.aksw.org/hasTruthValue> "' + str(1) + '"^^<http://www.w3.org/2001/XMLSchema#double> .\n'
            g.write(temp)
        else:
            temp = "<" + line[1] + '> <http://swc2017.aksw.org/hasTruthValue> "' + str(0) + '"^^<http://www.w3.org/2001/XMLSchema#double> .\n'
            g.write(temp)
    if line[3]!="" and bool(set(line[3].strip()) & set("http")):
        print line[4]
        phone = feature_extractor.FeatureExtractor(line[0],"hasHeadquartersPhoneNumber",line[4])
        # phone.query_s()
        phone.s_initial = x
        phone.query_so()
        # phone.query_spo()
        f = phone.get_features()
        arr = []
        arr.append(line[3])
        arr = arr + list(f)
        features2.append(arr)
        if arr[2]>0:
            temp = "<" + line[3] + '> <http://swc2017.aksw.org/hasTruthValue> "' + str(1) + '"^^<http://www.w3.org/2001/XMLSchema#double> .\n'
            g.write(temp)
        else:
            temp = "<" + line[3] + '> <http://swc2017.aksw.org/hasTruthValue> "' + str(0) + '"^^<http://www.w3.org/2001/XMLSchema#double> .\n'
            g.write(temp)
    if line[5]!="" and bool(set(line[5].strip()) & set("http")):
        line[6] = line[6][:-1]
        print line[6]
        date = feature_extractor.FeatureExtractor(line[0],"hasLatestOrganizationFoundedDate",line[6])
        # date.query_s()
        date.s_initial = x
        date.query_so()
        # date.query_spo()
        f = date.get_features()
        arr = []
        arr.append(line[5])
        arr = arr + list(f)
        features3.append(arr)
        if arr[2]>0:
            temp = "<" + line[5] + '> <http://swc2017.aksw.org/hasTruthValue> "' + str(1) + '"^^<http://www.w3.org/2001/XMLSchema#double> .\n'
            g.write(temp)
        else:
            temp = "<" + line[5] + '> <http://swc2017.aksw.org/hasTruthValue> "' + str(0) + '"^^<http://www.w3.org/2001/XMLSchema#double> .\n'
            g.write(temp)

file = open("iswc_features1.txt","w+")
for f in features1:
    file.write(str(f))

file = open("iswc_features2.txt","w+")
for f in features2:
    file.write(str(f))

file = open("iswc_features3.txt","w+")
for f in features3:
    file.write(str(f))