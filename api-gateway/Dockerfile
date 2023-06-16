# Use an official OpenJDK base image
FROM openjdk:17-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the project files from your local machine to the container
COPY ./target/api-gateway-1.0-SNAPSHOT.jar /app/api-gateway-1.0-SNAPSHOT.jar

# Set the entry point for the container
ENTRYPOINT ["java", "-jar", "api-gateway-1.0-SNAPSHOT.jar"]
