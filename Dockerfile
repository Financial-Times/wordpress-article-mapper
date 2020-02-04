FROM openjdk:8u212-jdk-alpine3.9

COPY . /wordpress-article-mapper

ARG SONATYPE_USER
ARG SONATYPE_PASSWORD
ARG GIT_TAG

ENV MAVEN_HOME=/root/.m2
ENV TAG=$GIT_TAG

RUN apk --update add git maven curl \
 && mkdir $MAVEN_HOME \
 && curl -v -o $MAVEN_HOME/settings.xml "https://raw.githubusercontent.com/Financial-Times/nexus-settings/master/public-settings.xml" \
 && cd wordpress-article-mapper \
 && HASH=$(git log -1 --pretty=format:%H) \
 && TAG=$(git tag --sort=committerdate | tail -1) \
 && VERSION=${TAG:-untagged} \
 && mvn versions:set -DnewVersion=$VERSION \
 && mvn install -Dbuild.git.revision=$HASH -Djava.net.preferIPv4Stack=true \
 && rm -f target/wordpress-article-mapper-*sources.jar \
 && mv target/wordpress-article-mapper-*.jar /wordpress-article-mapper.jar \
 && mv wordpress-article-mapper.yaml /config.yaml \
 && apk del git maven \
 && rm -rf /var/cache/apk/* \
 && rm -rf $MAVEN_HOME/*

EXPOSE 8080 8081

CMD exec java $JAVA_OPTS \
     -Ddw.urlResolverConfiguration.documentStoreConfiguration.endpointConfiguration.primaryNodes=$READ_ENDPOINT \
     -Ddw.urlResolverConfiguration.contentReadConfiguration.endpointConfiguration.primaryNodes=$READ_ENDPOINT \
     -Ddw.consumer.messageConsumer.queueProxyHost=http://$KAFKA_PROXY \
     -Ddw.producer.messageProducer.proxyHostAndPort=$KAFKA_PROXY \
     -Ddw.server.applicationConnectors[0].port=8080 \
     -Ddw.server.adminConnectors[0].port=8081 \
     -Ddw.logging.appenders[0].logFormat="%-5p [%d{ISO8601, GMT}] %c: %X{transaction_id} %replace(%m%n[%thread]%xEx){'\n', '|'}%nopex%n" \
     -Dsun.net.http.allowRestrictedHeaders=true \
     -jar wordpress-article-mapper.jar server config.yaml
