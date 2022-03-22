package com.zstock.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;
import com.zstock.service.ZstockService;

@Component
public class ZstockSecurityFilter extends OncePerRequestFilter {

	@Value("${my.header.type}")
	private String HEADER_KEY;
	
	@Autowired
	ZstockService service;
    
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
        String username = service.getUsernameFromUserAPI(jwtToken);
        
        if(username != null) {
        	if(SecurityContextHolder.getContext().getAuthentication() == null) {
        		UserDetails userDetails = service.getUserDetailsFromUserAPI(username, jwtToken);
        		if(service.checkJWTFromUserAPI(jwtToken, userDetails)) {
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
