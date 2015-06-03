FROM coco/dropwizardbase

RUN git clone http://git.svc.ft.com/scm/cp/wordpress-article-transformer.git
RUN cd wordpress-article-transformer && mvn install

RUN cp /wordpress-article-transformer/target/wordpress-article-transformer-0.0.1-SNAPSHOT.jar /app.jar
RUN cp /wordpress-article-transformer/wordpress-article-transformer.yaml /config.yaml

CMD echo wordpress.contentApi.key=$WORDPRESS_CONTENTAPI_KEY > /credentials.properties && java -Ddw.credentialsPath=/credentials.properties -Ddw.healthCheckWordPressConnections[0].hostName=ftalphaville.ft.com -Ddw.healthCheckWordPressConnections[1].hostName=blogs.ft.com -Ddw.server.applicationConnectors[0].port=8080 -Ddw.server.adminConnectors[0].port=8081 -jar app.jar server config.yaml

