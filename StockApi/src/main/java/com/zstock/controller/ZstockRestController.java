package com.zstock.controller;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.zstock.dto.SingleDTO;
import com.zstock.dto.ZstockDTO;
import com.zstock.service.ZstockService;

@RestController
public class ZstockRestController {

	@Autowired
	ZstockService service;
	
	@Autowired
    private LoadBalancerClient loadBalancerClient;
	
	@GetMapping("/default")
	public String getDefault(@RequestHeader("Authorization") String jwt) {
		String username = service.getUsernameFromUserAPI(jwt.substring(7));
		System.out.println("=== getDefault username:" + username);
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", jwt);

			HttpEntity<String> entity = new HttpEntity<>(username, headers);
			Object[] ret =  restTemplate.exchange(getDBUri()+"/userstock",
					HttpMethod.POST, entity, Object[].class).getBody();
			StringBuilder sb = new StringBuilder();
			sb.append("<h3>Username: ").append(username).append("</h3>");
			sb.append("<h3>Has ").append(ret.length).append(" stocks</h3>");
			// <a href="http://localhost:8080/stock/listdb/tsm">Visit W3Schools.com!</a>
			for(Object obj:ret) {
				sb.append("<a href=\"http://localhost:8080/stock/listdb/");
				sb.append((String)obj).append("\">").append((String)obj).append("</a><br>");
			}
			return sb.toString();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return "Error";
	}
	
	@PreAuthorize("hasRole('USER')")
	@GetMapping("/listdb/{sym}")
	public String getListdb(@PathVariable String sym, @RequestHeader("Authorization") String jwt) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", jwt);
			
			HttpEntity<String> entity = new HttpEntity<>(sym, headers);
			Object[] arry =  restTemplate.exchange(getDBUri()+"/listsymbol",
					HttpMethod.POST, entity, Object[].class).getBody();
			if(arry.length == 0)
				return "No Data !!!";
			
			StringBuilder sb = new StringBuilder();
			sb.append("<h3>").append(sym).append("</h3>");
			sb.append("<h3>").append(((LinkedHashMap)arry[0]).get("date"));
			sb.append(" ~ ").append(((LinkedHashMap)arry[arry.length-1]).get("date")).append("</h3>");
			sb.append("<table style=\"width:300px\"><tr><th>Date</th><th>Price</th><th>Volume</th></tr>");
			for(Object obj:arry) {
				Map<String, String> map = (LinkedHashMap)obj;
				sb.append("<tr>");
				sb.append("<td>").append(map.get("date")).append("</td>");
				String price = String.valueOf(map.get("close"));
				if(price.length()>5)
					price = price.substring(0,6);
				sb.append("<td>").append("$"+price).append("</td>");
				sb.append("<td>").append(map.get("volume")).append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
			return sb.toString();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return "Error";
	}
	
	@PreAuthorize("hasRole('USER')")
	@GetMapping("/list/{sym}")
	public String getList(@PathVariable String sym, 
						  @RequestParam Map<String, String> map, 
						  @RequestHeader("Authorization") String jwt) {
		
		if(!isSymbolExist(sym, jwt)) {
			System.out.println("Symbol not found or jwt expired");
			return "Error";
		}

		String from = map.getOrDefault("from", "");
		String to = map.getOrDefault("to", "");
		if(!isValidDate(from, to))
			return "Error";
		
		ZstockDTO dto = null;
		if(isDateRangeInDB(sym, from, to, jwt)) {
			System.out.println("=== get data from DB");
			try {
				dto = service.getHistoryFromDB(sym.toUpperCase(), from, to, jwt);
				return dto.toString();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("=== get data from FMP");
			try {
				dto = service.getHistoryFromFMP(sym.toUpperCase(), from, to);
				for(SingleDTO one:dto.getHistorical())
					one.setSymbol(dto.getSymbol());
				
				if(!pushDataToDB(dto, jwt))
					return "Error";
				return dto.toString();
			}
			catch(NullPointerException e) {
				return "Query Period has no data !!!";
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return "Error";
	}
	
	private Boolean isDateRangeInDB(String sym, String from, String to, String jwt) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", jwt);
			
			HttpEntity<String> entity = new HttpEntity<>(sym+":"+from+":"+to, headers);
			return restTemplate.exchange(getDBUri()+"/isDateRangeInDB",
					HttpMethod.POST, entity, Boolean.class).getBody();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private Boolean isValidDate(String from, String to) {
		//SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
		DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    try {
	    	LocalDate from_date = LocalDate.parse(from, dt);
	    	LocalDate to_date = LocalDate.parse(to, dt);
	    	if(from_date.isAfter(to_date)) {
	    		System.out.println("from after to, date Error !!!");
	    		return false;
	    	}
	    }
	    catch(Exception e) {
	    	System.out.println("date format not valid");
	    	e.printStackTrace();
	    	return false;
	    }
	    return true;
	}
	
	private Boolean pushDataToDB(ZstockDTO dto, String jwt) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", jwt);
			
			List<SingleDTO> ll = dto.getHistorical();
			HttpEntity<Object[]> entity = new HttpEntity<>(ll.toArray(), headers);
			return restTemplate.exchange(getDBUri()+"/saveHistoricalData",
					HttpMethod.POST, entity, Boolean.class).getBody();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private Boolean isSymbolExist(String sym, String jwt) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", jwt);
			
			HttpEntity<String> entity = new HttpEntity<>(sym, headers);
			return restTemplate.exchange(getDBUri()+"/findsymbol",
					HttpMethod.POST, entity, Boolean.class).getBody();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private String getDBUri(){
        ServiceInstance serviceInstance =  loadBalancerClient.choose("DBAPI");
        return serviceInstance.getUri().toString();
    }
}
