package com.aaaTradeApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class AaaTradeApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AaaTradeApiApplication.class, args);
	}

}
