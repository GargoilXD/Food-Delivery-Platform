FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY shared ./shared
COPY auth-microservice ./auth-microservice
COPY customer-microservice ./customer-microservice
COPY order-microservice ./order-microservice
COPY delivery-microservice ./delivery-microservice
COPY restaurant-microservice ./restaurant-microservice
COPY api-gateway ./api-gateway
COPY eureka-server ./eureka-server
RUN mvn -q -DskipTests -pl auth-microservice -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/auth-microservice/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]