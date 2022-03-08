package com.aaaUserApi.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.aaaUserApi.model.aaaUserModel;
import com.aaaUserApi.repository.aaaUserRepository;
import com.aaaUserApi.service.aaaUserDetailService;
import com.aaaUserApi.util.JwtUtil;

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
