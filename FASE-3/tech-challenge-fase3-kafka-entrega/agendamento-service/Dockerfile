# Estágio 1: Build com Maven
FROM maven:3.9-eclipse-temurin-17 AS build

# Define o diretório de trabalho
WORKDIR /app

# Copia o pom.xml e baixa as dependências (cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o restante do código-fonte
COPY src ./src

# Compila a aplicação e gera o JAR
RUN mvn package -DskipTests

# Estágio 2: Imagem final leve
FROM eclipse-temurin:17-jre-alpine

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que a aplicação vai rodar (definida em application.properties)
EXPOSE 8081

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]

