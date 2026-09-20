package com.sai.ticketing.api_gateway;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
	@PostConstruct
	public void init() {
		// Crucial for Spring Cloud Gateway / WebFlux tracing to work
		Hooks.enableAutomaticContextPropagation();
	}

}
