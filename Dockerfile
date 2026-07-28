FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app
RUN javac Server.java CryptoUtils.java
EXPOSE 5000
CMD ["java", "Server"]