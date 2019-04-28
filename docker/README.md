To build defacto docker image, user needs to get his ```google api key client id ``` and ```google api key secret ``` by following steps mentioned [here](https://github.com/DeFacto/DeFacto/wiki/Get-Google-api-key-client-and-secret)

Build defacto docker image using:
```
docker build --build-arg google_api_key_client_id_variable=```paste here client id``` --build-arg google_api_key_secret_variable=```paste here client secret``` -t defacto_git .

```
To run defacto docker image:
```
docker run -p 4441:4441 defacto_git
```

After building docker image, you can also run ```docker-compose```:
```
docker-compose up
```

For demo: login to: ```localhost:4441```
