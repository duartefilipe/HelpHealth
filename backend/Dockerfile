# Estágio 1: Build da aplicação Java Spring Boot
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Imagem final de execução
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instala curl e tzdata para fuso horário do Brasil
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/America/Sao_Paulo /etc/localtime && \
    echo "America/Sao_Paulo" > /etc/timezone

COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta da API REST
EXPOSE 8090

# Ponto de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]
