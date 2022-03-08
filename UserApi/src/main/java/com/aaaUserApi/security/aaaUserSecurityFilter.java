package com.aaaUserApi.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
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

import com.aaaUserApi.service.aaaUserDetailService;
import com.aaaUserApi.util.JwtUtil;

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
