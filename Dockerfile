FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app

COPY --from=build /app/build/libs/board.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]