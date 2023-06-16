# Use an official OpenJDK base image
FROM openjdk:17-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the project files from your local machine to the container
COPY ./target/master-query-service-1.0-SNAPSHOT.jar /app/master-query-service-1.0-SNAPSHOT.jar

# Set the entry point for the container
ENTRYPOINT ["java", "-jar", "master-query-service-1.0-SNAPSHOT.jar"]
