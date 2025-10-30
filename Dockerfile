# ============================================
# Etapa 1: Build (compilación del proyecto)
# ============================================
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Directorio de trabajo
WORKDIR /app

# Copiamos el POM primero para aprovechar caché de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el resto del proyecto
COPY . .

# Compilamos el proyecto, incluyendo el frontend de Vaadin
RUN mvn clean package -DskipTests

# ============================================
# Etapa 2: Runtime (ejecución de la app)
# ============================================
FROM eclipse-temurin:17-jdk

# Directorio de trabajo
WORKDIR /app

# Copiamos solo el jar generado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto (Railway usará la variable PORT)
EXPOSE 8080

# Variable de entorno de Spring Boot
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV PORT=8080

# Comando de ejecución
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]
