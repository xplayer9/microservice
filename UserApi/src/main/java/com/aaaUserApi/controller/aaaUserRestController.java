package com.aaaUserApi.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.aaaUserApi.model.aaaUserModel;
import com.aaaUserApi.repository.aaaUserRepository;
import com.aaaUserApi.service.aaaUserDetailService;
import com.aaaUserApi.service.circuitBreakerService;
import com.aaaUserApi.util.JwtUtil;

@RestController
public class aaaUserRestController {
	
	@Autowired
	aaaUserRepository rep;
	
	@Value("${my.cookieName}")
    private String COOKIE;
	
	@Value("${my.keyDuration}")
    private String DURATION;
	
    @Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtTokenUtil;

	@Autowired
	private aaaUserDetailService userDetailsService;
	
	@Autowired
	private circuitBreakerService circuitService;
	
	@PreAuthorize("hasRole('USER')")
	@GetMapping("/all")
	public String getAll(HttpServletRequest req) {
		List<aaaUserModel> ll = rep.findAll();
		return ll.toString();
	}
	
	@PreAuthorize("hasRole('USER')")
	@GetMapping("/test")
	public ResponseEntity<String> getTest() {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<11;i++) {
			sb.append("Test ").append(i).append(": ");
			sb.append(circuitService.deptInfo_Timeout(99)).append("<br>");
		}
		return new ResponseEntity<String>(sb.toString(), HttpStatus.OK);
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
	
	@PostMapping("/getUserDetails")
	public String getUserDetails(@RequestBody String name) {
		System.out.println("=== in getUserDetails: " + name);
		UserDetails user = userDetailsService.loadUserByUsername(name);
		return user.getUsername()+":"+user.getPassword()+":"+user.getAuthorities().toArray()[0];
	}
	
	@GetMapping("/getUsername")
	public String isJWTvalid(@RequestHeader("Authorization") String jwt) {
		return jwtTokenUtil.extractUsername(jwt.substring(7));
	}
	
	@PostMapping("/isJWTvalid")
	public Boolean isJWTvalid(@RequestHeader("Authorization") String jwt, @RequestBody String name) {
		return jwtTokenUtil.validateToken(jwt.substring(7), userDetailsService.loadUserByUsername(name));
	}
}







