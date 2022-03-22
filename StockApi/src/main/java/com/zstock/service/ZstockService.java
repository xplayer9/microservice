package com.zstock.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zstock.dto.SingleDTO;
import com.zstock.dto.ZstockDTO;

@Service
public class ZstockService {

	/*
	https://financialmodelingprep.com/api/v3/historical-price-full/
		AAPL?from=2018-03-12&to=2019-03-12&apikey=d1f8636b9df280532258cc61137e6f24
	*/
	
	@Autowired
    private LoadBalancerClient loadBalancerClient;
	
	@Value("${my.uri}")
	private String uri;
	
	@Value("${my.apiKey}")
	private String apikey;
	
	private HttpClient client = HttpClientBuilder.create().build();
	private Type aType = new TypeToken<ZstockDTO>() {}.getType();
	private Gson gson = new Gson();
	
	public ZstockDTO getHistoryFromDB(String symbol, String from, String to, String jwt) throws Exception{
		
		ZstockDTO dto = new ZstockDTO();
		dto.setSymbol(symbol);
		dto.setHistorical(new ArrayList<SingleDTO>()); 
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", jwt);

			org.springframework.http.HttpEntity<String> entity = 
					new org.springframework.http.HttpEntity<>(symbol+":"+from+":"+to, headers);
			Object[] ret =  restTemplate.exchange(getDBUri()+"/getDataFromDB",
					HttpMethod.POST, entity, Object[].class).getBody();
			
			
			//System.out.println("=== Display DTO");
			//System.out.println(Arrays.toString(ret));
			for(Object obj:ret) {
				Map<String, String> map = (LinkedHashMap)obj;
				SingleDTO one = new SingleDTO();
				one.setSymbol(map.get("symbol"));
				one.setDate(map.get("date"));
				one.setClose(String.valueOf(map.get("close")));
				one.setVolume(map.get("volume"));
				dto.getHistorical().add(one);
			}
			
			return dto;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	public ZstockDTO getHistoryFromFMP(String symbol, String from, String to) throws Exception{
		
		
		String query = uri+symbol+"?"+"from="+from+"&to="+to+"&apikey="+apikey;
		System.out.println("=== uri query command: " + query);
		
	    HttpGet httprequest = new HttpGet(query);
	    HttpResponse httpresponse = client.execute(httprequest);
	    HttpEntity entity = httpresponse.getEntity();
	    String content = EntityUtils.toString(entity);
	    return (ZstockDTO)gson.fromJson(content, aType);
	}
	
	public String getUsernameFromUserAPI(String jwt) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer "+jwt);
			
			org.springframework.http.HttpEntity<String> entity = 
					new org.springframework.http.HttpEntity<>("body", headers);
			ResponseEntity<String> ret = restTemplate.exchange(getUserBaseUri()+"/getUsername",
					HttpMethod.GET, entity, String.class);
            return ret.getBody();
            
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Boolean checkJWTFromUserAPI(String jwt, UserDetails userDetails) {
		if(userDetails==null) {
			System.out.println("=== userDetails is null");
			return false;
		}
		Boolean ret = false;
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer "+jwt);
			
			org.springframework.http.HttpEntity<String> entity = 
					new org.springframework.http.HttpEntity<>(userDetails.getUsername(), headers);
			ret = restTemplate.exchange(getUserBaseUri()+"/isJWTvalid",
					HttpMethod.POST, entity, Boolean.class).getBody();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public UserDetails getUserDetailsFromUserAPI(String username, String jwt) {

		try {
			//ret = restTemplate.getForObject(getOtherBaseUri()+"/all", String.class);
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer "+jwt);
			
			org.springframework.http.HttpEntity<String> entity = 
					new org.springframework.http.HttpEntity<>(username, headers);
			
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
	public String getUserBaseUri(){
        ServiceInstance serviceInstance =  loadBalancerClient.choose("USERAPI");
        return serviceInstance.getUri().toString();
    }
	
	public String getDBUri(){
        ServiceInstance serviceInstance =  loadBalancerClient.choose("DBAPI");
        return serviceInstance.getUri().toString();
    }
}
