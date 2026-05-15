FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Instalar Maven
RUN apk add --no-cache maven

# Copiar settings.xml com mirror rápido
COPY settings.xml /root/.m2/settings.xml

# Copiar pom.xml e baixar dependências (camada cacheável)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código e buildar
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
