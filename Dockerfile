# === Etapa 1: build ===
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiar solo pom.xml primero para cachear dependencias
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copiar el resto del código fuente
COPY src ./src

# Compilar en modo producción y omitir tests
RUN mvn clean package -Pproduction -DskipTests

# === Etapa 2: runtime ===
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copiar el JAR compilado desde la etapa builder
COPY --from=builder /app/target/*.jar app.jar

# Copiar los datos de Tesseract a una ruta accesible
COPY src/main/resources/tessdata /app/tessdata

# Exponer el puerto de la aplicación
EXPOSE 8080

# Perfil de Spring activo
ENV SPRING_PROFILES_ACTIVE=prod

# ENTRYPOINT flexible para pasar opciones de Java
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
