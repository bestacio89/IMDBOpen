FROM eclipse-temurin:21-jdk AS builder

WORKDIR /usr/src/app

COPY .mvn ./.mvn
COPY mvnw ./mvnw
COPY pom.xml ./pom.xml

RUN chmod +x ./mvnw \
    && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src

RUN ./mvnw --batch-mode --no-transfer-progress clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

EXPOSE 8080

COPY --from=builder /usr/src/app/target/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
