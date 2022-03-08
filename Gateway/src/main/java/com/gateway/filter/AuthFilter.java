package com.gateway.filter;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

/*
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
*/