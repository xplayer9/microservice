package com.zdb.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ZdbSecurityFilter extends OncePerRequestFilter {

	@Value("${my.header.type}")
	private String HEADER_KEY;
    
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		
		//No token, no authentication
    	if(request.getHeader(HEADER_KEY) == null) {
    		chain.doFilter(request, response);
    		return;
    	}
    	
    	//Remove "Bearer " substring
        String jwtToken = request.getHeader(HEADER_KEY).substring(7);
        String username = getUsernameFromUserAPI(jwtToken);
        
        if(username != null) {
        	if(SecurityContextHolder.getContext().getAuthentication() == null) {
        		UserDetails userDetails = getUserDetailsFromUserAPI(username, jwtToken);
        		if(checkJWTFromUserAPI(jwtToken, userDetails)) {
        			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
	                        userDetails, null, userDetails.getAuthorities());
	                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                SecurityContextHolder.getContext().setAuthentication(token);
        		}
        	}
        }
        chain.doFilter(request, response);
	}
	
	private String getUsernameFromUserAPI(String jwt) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer "+jwt);
			
			HttpEntity<String> entity = new HttpEntity<>("body", headers);
			ResponseEntity<String> ret = restTemplate.exchange(getUserBaseUri()+"/getUsername",
					HttpMethod.GET, entity, String.class);
            return ret.getBody();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Boolean checkJWTFromUserAPI(String jwt, UserDetails userDetails) {
		if(userDetails==null) {
			System.out.println("=== userDetails is null");
			return false;
		}
		Boolean ret = false;
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer "+jwt);
			
			HttpEntity<String> entity = new HttpEntity<>(userDetails.getUsername(), headers);
			ret = restTemplate.exchange(getUserBaseUri()+"/isJWTvalid",
					HttpMethod.POST, entity, Boolean.class).getBody();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	private UserDetails getUserDetailsFromUserAPI(String username, String jwt) {

		try {
			//ret = restTemplate.getForObject(getOtherBaseUri()+"/all", String.class);
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer "+jwt);
			
			HttpEntity<String> entity = new HttpEntity<>(username, headers);
			
			ResponseEntity<String> ret = restTemplate.exchange(getUserBaseUri()+"/getUserDetails",
					HttpMethod.POST, entity, String.class);
			String[] arry = ret.getBody().split(":");
			//set roles
            List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
            list.add(new SimpleGrantedAuthority(arry[2]));
            return new User(arry[0], arry[1], list);
            
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private String getUserBaseUri(){
        ServiceInstance serviceInstance =  loadBalancerClient.choose("USERAPI");
        return serviceInstance.getUri().toString();
    }
}
