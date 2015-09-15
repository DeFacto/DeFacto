# Fusion RESTful Web service

##### resource:
    method: POST
    path: /fusion/download
    request: JSON Object with a triple
    response: NIF RDF in Turtle

##### example:
```bash
    curl -v -H "Content-Type: application/json" -X POST -d '{"s":"http://dbpedia.org/resource/Albert_Einstein", "p":"http://dbpedia.org/ontology/award", "o":"http://dbpedia.org/resource/Nobel_Prize_in_Physics"}' http://localhost:4441/fusion/download > out.ttl

```

##### resource:
    method: POST
    path: /fusion/input
    request: JSON Object with a triple
    response:JSON Object

##### example:
```bash
    curl -v -H "Content-Type: application/json" -X POST -d '{"s":"http://dbpedia.org/resource/Albert_Einstein", "p":"http://dbpedia.org/ontology/award", "o":"http://dbpedia.org/resource/Nobel_Prize_in_Physics"}' http://localhost:4441/fusion/input > out.json

```
