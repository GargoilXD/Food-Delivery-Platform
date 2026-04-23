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
RUN mvn -q -DskipTests -pl delivery-microservice -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/delivery-microservice/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app/app.jar"]
