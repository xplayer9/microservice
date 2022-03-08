# Spring Boot Microservices

## High Level System Architecture

<img width="799" alt="Screenshot01" src="https://user-images.githubusercontent.com/48862763/157296158-a41ef82a-ed7d-43fe-9f2f-f5b0de8e7dc3.png">


## Eureka Server
- Key Dependency: Web, Eureka Server
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
- Utlize Git repo
- Key Dependency: Web, Config Server

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
  cookieName: cookieJWT
  secretKey: mysecrect
  keyDuration: 3600
  header:
    type: Authorization
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
        include: "*"
  endpoint:
    health:
      show-details: always
```

## Gateway Server
- provides a flexible way of routing requests
- Key Dependency: Web, WebFlux, Gateway
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
          filters: # 加上StripPrefix=1，否則轉發到後端服務時會帶上consumer字首
            - StripPrefix=1
```

- Gateway Global Filter
```Java
@Component
public class AuthFilter implements GlobalFilter, Ordered {
	
	@Value("${my.cookieName}")
    private String COOKIE;
	
	@Value("${my.header.type}")
	private String HEADER_KEY;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("=== Global filter");
        ServerHttpRequest req = exchange.getRequest();
        ServerHttpResponse rsp = exchange.getResponse();
        
        //find JWT from cookie
        MultiValueMap<String, HttpCookie> cookieMap = req.getCookies();
        if(cookieMap.isEmpty() || !cookieMap.containsKey(COOKIE)){
        	rsp.setStatusCode(HttpStatus.UNAUTHORIZED);
        	
        	String str = "Cookie not found " + COOKIE;
        	byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
          DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        	return rsp.writeWith(Flux.just(buffer));
        }

        //add JWT to following request header
        HttpCookie token = cookieMap.getFirst(COOKIE);
        return chain.filter(
                exchange.mutate().request(
                        req.mutate()
                        .header(HEADER_KEY, "Bearer "+token.getValue())
                        .build()).build());
    }

    @Override
    public int getOrder() {
        //The smaller the value, the more priority is given to execution
        return 1;
    }
}
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
- Key Dependency: Web, Cloud, JPA, Config client, Eureka client, Lombok
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





