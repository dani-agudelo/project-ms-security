# Usa una imagen base de Maven para compilar y empaquetar la aplicación
FROM maven:3.8.6-openjdk-17 AS build

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo pom.xml y las dependencias del proyecto para compilarlas
COPY pom.xml .
COPY src ./src

# Compila la aplicación usando Maven
RUN mvn package -DskipTests

# Usa una imagen base más ligera para la ejecución de la aplicación
FROM openjdk:17-jdk-alpine

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR generado desde la fase de construcción
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto en el que la aplicación se ejecutará
EXPOSE 8080

# Define las variables de entorno
ENV SPRING_PROFILES_ACTIVE=default

# Establece el comando de inicio para la aplicación
CMD ["java", "-jar", "app.jar"]
