FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . /app
RUN javac Server.java CryptoUtils.java
EXPOSE 5000
CMD ["java", "Server"]