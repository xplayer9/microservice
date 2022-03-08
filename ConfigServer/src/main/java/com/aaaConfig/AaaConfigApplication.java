package com.aaaConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class AaaConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(AaaConfigApplication.class, args);
	}

}
