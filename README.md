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
- "application.yml" in Git repo
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
- Gateway Filter for reference
```Java
@Component
public class AuthFilter implements GatewayFilterFactory<AuthFilter.Config> {

	@Override
	public GatewayFilter apply(Config config) {
		return ((exchange, chain) -> {
			
			ServerHttpRequest req = exchange.getRequest();
			ServerHttpResponse rsp = exchange.getResponse();
			rsp.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
			rsp.getHeaders().set("Access-Control-Allow-Origin", "*");
			rsp.getHeaders().set("Cache-Control", "no-cache");
			rsp.setStatusCode(HttpStatus.UNAUTHORIZED);
			
			System.out.println("=== getURI: " + req.getURI());
			System.out.println("=== getPath: " + req.getPath());
			System.out.println("=== getLocalAddress: " + req.getLocalAddress());
			
			String body = "Message to user";
			DataBuffer buffer = null;
			try {
				buffer = rsp.bufferFactory().wrap(body.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return chain.filter(exchange);
            //return rsp.writeWith(Mono.just(buffer));
		});
	}

	@Override
	public Class<Config> getConfigClass() {
		return Config.class;
	}

	@Override
	public Config newConfig() {
		Config c = new Config();
		return c;
	}

	public static class Config {}
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
- Key Dependency: Web, Cloud, JPA, Config client, Eureka client, Lombok, jjwt, jaxb
- Annotation
```Java
@SpringBootApplication
@EnableEurekaClient
```
- application.properities
```Java
server.port=8081
spring.cloud.config.uri=http://localhost:8888
spring.cloud.config.enabled=true
spring.cloud.config.discovery.enabled=true
spring.application.name=userapi
```
- RestController Sample Code
```Java
@RestController
public class aaaUserRestController {
	
	@Autowired
	aaaUserRepository rep;
	
	@Value("${server.port}")
	String port;
	
	@Value("${my.cookieName}")
    private String COOKIE;
	
	@Value("${my.keyDuration}")
    private String DURATION;
	
	@Value("${my.header.type}")
	private String HEADER_KEY;
	
	@Autowired
    private LoadBalancerClient loadBalancerClient;
    private RestTemplate restTemplate = new RestTemplate();
    
    @Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtTokenUtil;

	@Autowired
	private aaaUserDetailService userDetailsService;
	
	@PreAuthorize("hasRole('USER')")
	@GetMapping("/all")
	public String getAll(HttpServletRequest req) {
		List<aaaUserModel> ll = rep.findAll();
		return ll.toString();
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin")
	public String getAdmin(HttpServletRequest req) {
		return "This is Admin page";
	}
	
	@GetMapping("/logout")
	public String getlogout(HttpServletResponse resp, HttpServletRequest req) {
		Cookie[] arry = req.getCookies();
		for(Cookie c:arry) {
			if(c.getName().equals(COOKIE)) {
				System.out.println("=== Cookie found!!!");
				c.setMaxAge(0);
				c.setDomain("localhost");
				c.setPath("/");
				resp.addCookie(c);
				break;
			}
		}
		return "Logout Successfully";
	}
	
	@GetMapping("/exist")
	public Boolean exist(@RequestParam String name) {
		Boolean found = rep.existByName(name);
		System.out.println("=== found user:"+name +" "+ found);
		return found;
	}
	
	
	@GetMapping("/login")
	public String userlogin(HttpServletResponse resp, @RequestParam Map<String, String> map) {
		System.out.println("=== user login");
		String username = map.getOrDefault("name", "");
		String pwd = map.getOrDefault("pwd", "");
		if(username.length()==0 || pwd.length()==0)
			return "Username or Password cannot be empty!!!";
		
		try {
			authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(username, pwd));
		}
		catch (InternalAuthenticationServiceException e) {
			return "Username not found";
		}
		catch (BadCredentialsException e) {
			return "Incorrect password";
		}
		catch (Exception e) {
			return "Login FAIL, unknown errors";
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		String jwt = jwtTokenUtil.generateToken(userDetails);
		Cookie cookie = new Cookie(COOKIE, jwt);
        cookie.setMaxAge(Integer.parseInt(DURATION));
		resp.addCookie(cookie);
		return "Login Successfully !!!";
	}
}
```
- Data Model sample code
```Java
@Entity
@Data
@Table(name="`users`")
public class aaaUserModel {
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;
	
	@Column(name="username")
    private String username;
    
	@Column(name="password")
    private String password;
	
	@Column(name="roles")
    private String roles;
}
```

- Repository sample code
```Java
@Repository
public interface aaaUserRepository extends JpaRepository<aaaUserModel, Long> {

    Optional<aaaUserModel> findByUsername(String username);
	
    @Query(value="select exists(select 1 from users where username = :name)", nativeQuery=true)
    Boolean existByName(String name);
	
    //@Query(value="select * from inventory a where a.item = :item", nativeQuery=true)
    //List<shopModel> getItem(String item);
}
```

- Web Security Configuration
```Java
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class aaaUserSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
    private UserDetailsService userDetailsService;
	
	@Autowired
	private aaaUserSecurityFilter securityfilter;

	@Override 
	protected void configure(HttpSecurity http) throws Exception {
		http
        .authorizeRequests()
        .antMatchers(HttpMethod.GET, "/login", "/logout", "signup").permitAll()
        .anyRequest().authenticated()
        .and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
        .csrf().disable();
		
        http.formLogin().disable();
	http.logout().disable();
	http.addFilterBefore(securityfilter, UsernamePasswordAuthenticationFilter.class);
		
    }
	
	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
	    return super.authenticationManagerBean();
	}
	
	@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
		.userDetailsService(userDetailsService)
		.passwordEncoder(new BCryptPasswordEncoder());
    }
}
```

- Web Security Filter
```Java
@Component
public class aaaUserSecurityFilter extends OncePerRequestFilter {
	
	@Value("${my.header.type}")
	private String HEADER_KEY;

    @Autowired
    aaaUserDetailService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

    	//No token, no authentication
    	if(request.getHeader(HEADER_KEY) == null) {
    		System.out.println("=== No token, no authentication");
    		chain.doFilter(request, response);
    		return;
    	}
    	
    	System.out.println("=== in Filter, doFilterInternal");
    	
    	//Remove "Bearer " substring
        String jwtToken = request.getHeader(HEADER_KEY).substring(7);
        String username = jwtUtil.extractUsername(jwtToken);
 
        if(username != null) {
        	if(SecurityContextHolder.getContext().getAuthentication() == null) {
        		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        		
	            if(jwtUtil.validateToken(jwtToken, userDetails)){
	                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
	                        userDetails, null, userDetails.getAuthorities());
	                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                SecurityContextHolder.getContext().setAuthentication(token);
	            }
        	}
        }
        chain.doFilter(request, response);
    }
}
```

- Define UserDetailService
```Java
@Service
public class aaaUserDetailService implements UserDetailsService{
	
	@Autowired
	aaaUserRepository rep;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

			System.out.println("=== in UserDetailService, loadUserByUsername");
            aaaUserModel dto = rep.findByUsername(username).get();
            //set roles
            List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
            list.add(new SimpleGrantedAuthority(dto.getRoles()));
            
            return new User(dto.getUsername(), dto.getPassword(), list);
	}
}
```

- JWT Util
```Java
@Component
public class JwtUtil {

	@Value("${my.secretKey}")
    private String SECRET_KEY;
	
	@Value("${my.keyDuration}")
    private String DURATION;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + Integer.parseInt(DURATION)*1000))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

```





