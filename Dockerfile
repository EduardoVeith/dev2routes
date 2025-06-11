# Etapa 1: compila o projeto usando Maven + JDK 17
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: roda o .jar sombreado com dependências
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/target/fluxo-maximo-api-1.0-SNAPSHOT-shaded.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
