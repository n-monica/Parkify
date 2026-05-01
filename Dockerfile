FROM openjdk:17-jdk-slim

WORKDIR /app

# copy your backend project
COPY backend/ /app/

# build with Maven wrapper
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

# run the jar (adjust name if different)
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]