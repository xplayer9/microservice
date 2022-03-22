package com.aaaUserApi.service;

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

import com.aaaUserApi.model.aaaUserModel;
import com.aaaUserApi.repository.aaaUserRepository;


@Service
public class aaaUserDetailService implements UserDetailsService{
	
	@Autowired
	aaaUserRepository rep;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

			//System.out.println("=== in UserDetailService, loadUserByUsername");
            aaaUserModel dto = rep.findByUsername(username).get();
            //set roles
            List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
            list.add(new SimpleGrantedAuthority(dto.getRoles()));
            
            return new User(dto.getUsername(), dto.getPassword(), list);
	}

}