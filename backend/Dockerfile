FROM maven:3.9.10-eclipse-temurin-25
WORKDIR /app
COPY . .
RUN mvn -q -DskipTests package
CMD ["java","-jar","target/marketplace-backend-0.0.1-SNAPSHOT.jar"]
