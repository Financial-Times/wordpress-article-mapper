FROM up-registry.ft.com/coco/dropwizardbase

ADD . /

RUN apk --update add git \
 && HASH=$(git log -1 --pretty=format:%H) \
 && BUILD_NUMBER=$(cat ./buildnum.txt) \
 && BUILD_URL=$(cat ./buildurl.txt) \
 && mvn install -Dbuild.git.revision=$HASH -Dbuild.number=$BUILD_NUMBER -Dbuild.url=$BUILD_URL -Djava.net.preferIPv4Stack=true \
 && rm -f target/wordpress-article-transformer-*sources.jar \
 && mv target/wordpress-article-transformer-*.jar /app.jar \
 && mv wordpress-article-transformer.yaml /config.yaml \
 && apk del git \
 && rm -rf /var/cache/apk/* \
 && rm -rf /root/.m2/*

EXPOSE 8080 8081

CMD exec java -Ddw.nativeReaderConfiguration.endpointConfiguration.primaryNodes=$READ_ENDPOINT \
     -Ddw.urlResolverConfiguration.documentStoreConfiguration.endpointConfiguration.primaryNodes=$READ_ENDPOINT \
     -Ddw.urlResolverConfiguration.contentReadConfiguration.endpointConfiguration.primaryNodes=$READ_ENDPOINT \
     -Ddw.server.applicationConnectors[0].port=8080 \
     -Ddw.server.adminConnectors[0].port=8081 \
     -Ddw.logging.appenders[0].logFormat="%-5p [%d{ISO8601, GMT}] %c: %X{transaction_id} %replace(%m%n[%thread]%xEx){'\n', '|'}%nopex%n" \
     -Dsun.net.http.allowRestrictedHeaders=true \
     -jar app.jar server config.yaml
