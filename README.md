# SpringBoot Microservices -- Stock Price Services (Real Time Quoto and Historical Price)

# Technology
  - SpringBoot Architecture
  -- abc
  - Spring API Gateway
  - Configuration Server
  - Eureka Serivce Discovery Server
  - Kafka Message Queue
  - Spring Cloud Security JWT
  - Resilience4J Circuit Braker
  - DataBase: JPA, PostgreSQL
  - Development tools: Dev package, Lombok, Docker, Actuator
  - Frontend library: Thymeleaf, Webjar

# High Level System Architecture

<img width="790" alt="Screenshot03" src="https://user-images.githubusercontent.com/48862763/157563209-02321dd1-4767-458b-b04c-3fd079e8e42b.png">

# High Level Cloud Security Flow

<img width="789" alt="Screenshot02" src="https://user-images.githubusercontent.com/48862763/157300306-1c11a2ce-f356-4803-b6f8-5b2481c99146.png">

# Spring Cloud Components

## Eureka Serivce Discovery Server
- Purpose: Eureka Server holds the information about all client-service applications. Every Micro service will register into the Eureka server
- Key Dependency: Web, Eureka Server
- application.properties
```Java
server.port = 8761
eureka.client.registerWithEureka = false //do not register eureka self
eureka.client.fetchRegistry = false      //do not register eureka self
```

## Configuration Server
- Purpose: Spring Cloud Configuration server provides unified configuration settings in a distributed system
- You can save configuration file on Web(Ex:Git repo) or local storage
- Key Dependency: Web, Config Server

- application.properties
```Java
server.port = 8888
spring.cloud.config.server.git.uri = https://github.com/xplayer9/config-repo
spring.cloud.config.server.git.default-label = main   //Code branch name in Git
```

- "application.yml" in Git repo
```Java
my:
  greeting: This is greeting
  cookieName: cookieJWT
  secretKey: mysecrect
  keyDuration: 3600
  header:
    type: Authorization
  kafkatopic: json_topic
  kafkauri: localhost:9092

spring:
  datasource:
    url: jdbc:postgresql://localhost:1234/postgres
    username: postgres
    password: 1234
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka
  instance:
    hostname: localhost
    preferIpAddress: false

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always

resilience4j.circuitbreaker:
  instances:
    processService:
      slidingWindowSize: 5
      permittedNumberOfCallsInHalfOpenState: 1
      slidingWindowType: COUNT_BASED
      minimumNumberOfCalls: 10
      waitDurationInOpenState: 3s
      slowCallDurationThreshold: 2s
      failureRateThreshold: 20
      slowCallRateThreshold: 20

```

## Gateway Server
- Purpose: provides a flexible way of routing requests
- Key Dependency: Web, WebFlux, Gateway, Eureka Client
- Support GatewayFilter and GolbalFilter
- "application.yml"
```Java
server:
  port: 8080
  
spring:
  application:
    name: gateway
  cloud:
    config:
      uri: http://localhost:8888
    gateway:
      discovery:
        locator:
          enabled: true
      routes:
        - id: USERAPI
          uri: lb://USERAPI
          predicates:
            - Path=/user/**
          filters:
            - StripPrefix=1
        - id: TRADEAPI
          uri: lb://TRADEAPI
          predicates:
            - Path=/trade/**
          filters:
            - StripPrefix=1
        - id: STOCKAPI
          uri: lb://STOCKAPI
          predicates:
            - Path=/stock/**
          filters:
            - StripPrefix=1
        - id: DBAPI
          uri: lb://DBAPI
          predicates:
            - Path=/db/**
          filters:
            - StripPrefix=1
```

## Actuator, Devtools, Lombok, Gson
- Dependency
```Java
<dependency>
	<groupId>org.springframework.boot</groupId>    
	<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-devtools</artifactId>
	<optional>true</optional>
</dependency>
<dependency>
	<groupId>org.projectlombok</groupId>
	<artifactId>lombok</artifactId>
	<optional>true</optional>
</dependency>
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
</dependency>
```

## Microservice Component APIs: UserApi
- Purpose: To Authenticate and Authorize every incoming request, manage user account
- Key Dependency: Web, Cloud, JPA, Config client, Eureka client, Lombok, jjwt, jaxb

## Microservice Component APIs: StockApi
- Purpose: To Handle stock info, web request for real time stock price and historical price
- Utlize UserApi for JWT Authentication and Authorization
- Key Dependency: Web, Cloud, Config client, Eureka client, Lombok

## Microservice Component APIs: TradeApi
- Purpose: To perform stock trading exercise
- Utlize UserApi for JWT Authentication and Authorization
- Key Dependency: Web, Cloud, Config client, Eureka client, Lombok

## Microservice Component APIs: DBApi
- Purpose: To handle all database access operations
- Utlize UserApi for JWT Authentication and Authorization
- Key Dependency: Web, Cloud, JPA, Config client, Eureka client, Lombok

# Functional Flow Diagram

<img width="725" alt="Screenshot05" src="https://user-images.githubusercontent.com/48862763/159364270-42a4e261-1ada-466d-8a33-a54d0dbcae72.png">

# Demo Page -- Real Time Stock Quote

<img width="1315" alt="Screenshot08" src="https://user-images.githubusercontent.com/48862763/159366084-d1813a36-7223-4fef-9ae3-3a6568f6670e.png">

# Demo Page -- Historical Stock Price

<img width="410" alt="Screenshot07" src="https://user-images.githubusercontent.com/48862763/159366160-de483976-f1c4-4987-acb0-daff8d5a7055.png">

