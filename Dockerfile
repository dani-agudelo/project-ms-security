FROM ubuntu:latest as runtime

# Use an official Maven image to build the project
RUN apt-get update && apt-get install -y maven openjdk-17-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline


# Copy the rest of the application source code
COPY src ./src

# Package the application
RUN mvn clean install

# Use a minimal base image to reduce the size of the final image

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=runtime /app/target/*.jar app.jar

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
