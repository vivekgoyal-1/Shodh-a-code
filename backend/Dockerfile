FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN apt-get update && apt-get install -y docker.io && rm -rf /var/lib/apt/lists/*
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
