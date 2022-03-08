# Spring Boot Microservices

## High Level System Architecture

<img width="799" alt="Screenshot01" src="https://user-images.githubusercontent.com/48862763/157296158-a41ef82a-ed7d-43fe-9f2f-f5b0de8e7dc3.png">


## Eureka Server
- Dependency: Web, Eureka Server
- application.properties
```Java
server.port = 8761
eureka.client.registerWithEureka = false
eureka.client.fetchRegistry = false
```
- Annotation
```Java
@SpringBootApplication
@EnableEurekaServer
```

## Config Server
- Dependency: Web, Config Server
- application.properties
```Java
server.port = 8888
spring.cloud.config.server.git.uri = https://github.com/xplayer9/config-repo
spring.cloud.config.server.git.default-label = main
```
- Annotation
```Java
@SpringBootApplication
@EnableConfigServer
```
- "application.yml" in git repo
```Java
my:
  greeting: This is greeting
  list:
    value: one, two , Five, Six, Seven, Eight
  
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
        include: info, health, mappings
```

## Gateway Server
- Dependency: Web, Gateway
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
        - id: USERAPI  #路由的ID，建議配合服務名
          uri: lb://USERAPI # 匹配後提供服務的路由地址，lb代表從註冊中心獲取服務，且以負載均衡方式轉發
          predicates:
            - Path=/user/**
          filters: # 加上StripPrefix=1，否則轉發到後端服務時會帶上consumer字首
            - StripPrefix=1
        - id: TRADEAPI  #路由的ID，建議配合服務名
          uri: lb://TRADEAPI # 匹配後提供服務的路由地址，lb代表從註冊中心獲取服務，且以負載均衡方式轉發
          predicates:
            - Path=/trade/**
          filters: # 加上StripPrefix=1，否則轉發到後端服務時會帶上consumer字首
            - StripPrefix=1
```

## Actuator and Devtools
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
```

## UserApi

- Dependency: Web, Cloud, JPA, Config client, Eureka client, Lombok
- Annotation
```Java
@SpringBootApplication
@EnableEurekaClient
```
- application.properities
```Java
server.port=8082
spring.cloud.config.uri=http://localhost:8888
spring.application.name=tradeapi
```
- RestController Sample Code
```Java
@RestController
public class AaaTradeRestController {
	
	@Autowired
	aaaTradeRepository rep;
	
	@Value("${server.port}")
	String port;
	
	@Autowired
    private LoadBalancerClient loadBalancerClient;
    private RestTemplate restTemplate = new RestTemplate();
	
	@GetMapping("/all")
	public String getAll() {
		List<AaaTradeModel> ll = rep.getAll();
		System.out.println("Trade size "+ll.size());
		return ll.toString();
	}
	@GetMapping("/other")
	public String getother() {
		System.out.println("=== From Trade:"+ port + " try to call other");
		String ret = "Error";
		try {
			ret = restTemplate.getForObject(getOtherBaseUri()+"/all", String.class);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	private String getOtherBaseUri(){
        ServiceInstance serviceInstance =  loadBalancerClient.choose("USERAPI");
        return serviceInstance.getUri().toString();
    }
}

```





