FROM rawmind/alpine-jdk8:1.8.181-0


maintainer rameshkjes@gmail.com

RUN apk add --no-cache curl


ENV MAVEN_VERSION 3.5.2


RUN mkdir -p /usr/share/maven \
  && curl -fsSL http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven

RUN set MAVEN_OPTS= -Dfile.encoding=UTF-8

RUN apk add --no-cache git
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

RUN git clone https://github.com/DeFacto/DeFacto.git
WORKDIR /usr/src/app/DeFacto

RUN git checkout defacto-docker 
					

RUN sed "2s#/home/user/Repos/DeFacto/data/#/usr/src/app/DeFacto/data/#g" -i defacto-core/src/main/resources/defacto.ini


RUN sed "36s#/home/user/Repos/DeFacto/data/crawl_repo#/usr/src/app/DeFacto/data/crawl_repo#g" -i defacto-core/src/main/resources/defacto.ini


RUN sed "92s#/home/user/Repos/DeFacto/data/wordnet/dict#/usr/src/app/DeFacto/data/wordnet/dict#g" -i defacto-core/src/main/resources/defacto.ini

RUN mvn clean install

WORKDIR /usr/src/app/DeFacto/defacto-restful

RUN mvn clean install

ARG google_api_key_client_id_variable=default_value
ARG google_api_key_secret_variable=default_value

ENV google_api_key_client_id=$google_api_key_client_id_variable


ENV google_api_key_secret=$google_api_key_secret_variable

RUN sed "33s#dummy#$google_api_key_client_id#g" -i src/main/resources/application.yml

RUN sed "34s#dummy#$google_api_key_secret#g" -i src/main/resources/application.yml

VOLUME root/.m2

RUN export MAVEN_OPTS=-Xmx4G

CMD ["-Dexec.mainClass=org.aksw.defacto.restful.App"]

ENTRYPOINT ["mvn", "compile", "exec:java"]