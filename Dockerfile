FROM maven:3.9.9-eclipse-temurin-11 AS builder
WORKDIR /app

COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM tomcat:9.0-jdk11-temurin
WORKDIR /usr/local/tomcat

COPY --from=builder /app/target/ta-recruitment.war /usr/local/tomcat/webapps/ROOT.war
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
