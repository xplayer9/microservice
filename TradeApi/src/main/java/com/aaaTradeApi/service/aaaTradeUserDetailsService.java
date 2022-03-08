package com.aaaTradeApi.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aaaTradeApi.model.aaaUserModel;
import com.aaaTradeApi.repository.aaaUserApiRepository;

@Service
public class aaaTradeUserDetailsService implements UserDetailsService{
	
	@Autowired
	aaaUserApiRepository rep;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

            aaaUserModel dto = rep.findByUsername(username).get(0);
            //set roles
            List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
            list.add(new SimpleGrantedAuthority(dto.getRoles()));
            
            return new User(dto.getUsername(), dto.getPassword(), list);
	}

}
