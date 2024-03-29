# Build stage
FROM maven:3.8.4-jdk-11-slim AS build-stage

COPY ./src /usr/src/ldap2azure/src
COPY ./pom.xml /usr/src/ldap2azure/pom.xml
WORKDIR /usr/src/ldap2azure
RUN mvn clean package

# Prod stage
FROM openjdk:11-jre-slim

LABEL maintainer="hi@bluemedia.dev"

ENV DEBIAN_FRONTEND noninteractive

RUN apt update -y && \
    apt upgrade -y && \
    apt clean && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build-stage /usr/src/ldap2azure/target/*dependencies.jar /opt/ldap2azure/ldap2azure.jar
WORKDIR /opt/ldap2azure

RUN useradd --system --shell /usr/sbin/nologin ldap2azure
RUN chown -R ldap2azure:ldap2azure /opt/ldap2azure

USER ldap2azure
CMD ["java", "-jar", "/opt/ldap2azure/ldap2azure.jar"]
