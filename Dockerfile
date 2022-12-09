FROM eclipse-temurin:11-alpine
COPY . /src
RUN set -ex && \
    cd /src && \
    ./mvnw -P use-slf4j-simple clean package && \
    [ -f "/src/target/github-oauth-proxy-jar-with-dependencies.jar" ]

FROM eclipse-temurin:11-jre-alpine
COPY --from=0 /src/target/github-oauth-proxy-jar-with-dependencies.jar /opt/github-oauth-proxy/github-oauth-proxy-jar-with-dependencies.jar
EXPOSE 8080
CMD ["java", "-server", "-jar", "/opt/github-oauth-proxy/github-oauth-proxy-jar-with-dependencies.jar"]
