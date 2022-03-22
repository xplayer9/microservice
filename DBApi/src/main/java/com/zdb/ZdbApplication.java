package com.zdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ZdbApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZdbApplication.class, args);
	}

}
