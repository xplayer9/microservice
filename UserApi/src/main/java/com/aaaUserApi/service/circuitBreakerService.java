package com.aaaUserApi.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class circuitBreakerService {

	@CircuitBreaker(name = "processService", fallbackMethod = "dept_TimeoutHandler")
    public String deptInfo_Timeout(Integer id) {
        int outTime = 3;
        try {
            TimeUnit.SECONDS.sleep(outTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Normal deptInfo_Timeout";
    }

    public String dept_TimeoutHandler(Integer id, Exception ex) {
    	return "Fallback dept_TimeoutHandler";
    }
	
}
