FROM ubuntu:latest as build
RUN apt-get update && apt-get install build-essential -y
RUN apt-get install openjdk-17
COPY . .
RUN ./mvnw clean install package
RUN java -jar target/*.jar
