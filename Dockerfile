FROM maven:3.9.16-eclipse-temurin-26
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package
CMD ["mvn", "-q", "exec:java", "-Dexec.mainClass=com.retailpos.WebMain"]