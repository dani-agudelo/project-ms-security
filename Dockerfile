# Usa una imagen base de OpenJDK para compilar y ejecutar la aplicación
FROM openjdk:17-jdk-alpine

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo pom.xml y las dependencias del proyecto para compilarlas
COPY pom.xml ./
COPY src ./src

# Compila la aplicación usando Maven
RUN ./mvnw package -DskipTests

# Expone el puerto en el que la aplicación se ejecutará
EXPOSE 8080

# Define las variables de entorno
ENV SPRING_PROFILES_ACTIVE=default

# Establece el comando de inicio para la aplicación
CMD ["java", "-jar", "target/*.jar"]
