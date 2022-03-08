package com.aaaTradeApi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.aaaTradeApi.model.aaaTradeModel;
import com.aaaTradeApi.repository.aaaTradeRepository;

@RestController
public class AaaTradeRestController {
	
	@Autowired
	aaaTradeRepository rep;
	
	@Value("${server.port}")
	String port;
	
	@Value("${my.header.type}")
	private String HEADER_KEY;
	
	@Autowired
    private LoadBalancerClient loadBalancerClient;
	
    @PreAuthorize("hasRole('USER')")
	@GetMapping("/all")
	public String getAll() {
		List<aaaTradeModel> ll = rep.getAll();
		System.out.println("Trade size "+ll.size());
		return ll.toString();
	}
    
    @PreAuthorize("hasRole('USER')")
	@GetMapping("/other")
	public String getother(@RequestHeader("Authorization") String jwt) {

	    System.out.println("=== In Trade/other");
	    System.out.println(jwt);
		String ret = "Error";
		try {
			
			//ret = restTemplate.getForObject(getOtherBaseUri()+"/all", String.class);
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", jwt);
			
			HttpEntity<String> entity = new HttpEntity<>("body", headers);
			ResponseEntity<String> sss = restTemplate.exchange(getOtherBaseUri()+"/all", HttpMethod.GET, entity, String.class);
			ret = sss.getBody();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	private String getOtherBaseUri(){
        ServiceInstance serviceInstance =  loadBalancerClient.choose("USERAPI");
        return serviceInstance.getUri().toString();
    }
}
