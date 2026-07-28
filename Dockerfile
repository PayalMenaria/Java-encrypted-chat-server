
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN javac Server.java CryptoUtils.java


FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app /app
EXPOSE 5000
CMD ["java", "Server"]