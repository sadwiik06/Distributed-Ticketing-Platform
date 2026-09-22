package com.sai.ticketing.api_gateway.config;


import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

@Configuration
public class GatewayObservationConfig {

    @Bean
    public WebFilter traceObservationFilter(@Autowired(required = false) ObservationRegistry registry) {
        if (registry == null) {
            return (exchange, chain) -> chain.filter(exchange);
        }

        return (exchange, chain) -> {
            Observation observation = Observation.createNotStarted("gateway.request", registry);

            return chain.filter(exchange)
                    .doFirst(observation::start)
                    .doOnError(observation::error)
                    .doOnTerminate(observation::stop);
        };
    }
}