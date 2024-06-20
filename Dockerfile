FROM ubuntu:latest as build
RUN apt-get update && apt-get install build-essential -y
RUN apt-get install  openjdk-17-jdk -y
RUN apt-get install maven -y
WORKDIR /app
COPY . .
RUN mvn clean install
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
