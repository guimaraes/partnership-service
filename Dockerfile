# Dockerfile para Partnership Service
FROM openjdk:17-jdk-slim

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivo de dependências
COPY pom.xml .

# Baixar dependências (cache layer)
RUN apt-get update && \
    apt-get install -y maven && \
    mvn dependency:go-offline -B && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copiar código fonte
COPY src ./src

# Compilar aplicação
RUN mvn clean package -DskipTests

# Expor porta
EXPOSE 8080

# Comando para executar a aplicação
CMD ["java", "-jar", "target/partnership-service-0.0.1-SNAPSHOT.jar"]
