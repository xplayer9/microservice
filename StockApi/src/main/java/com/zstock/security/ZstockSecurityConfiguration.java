package com.zstock.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ZstockSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private ZstockSecurityFilter securityfilter;

	@Override 
	protected void configure(HttpSecurity http) throws Exception {
		http
        .authorizeRequests()
        .anyRequest().authenticated()
        .and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
        .csrf().disable();
		
        http.formLogin().disable();
		http.logout().disable();
		http.addFilterBefore(securityfilter, UsernamePasswordAuthenticationFilter.class);
		
    }
}
