package com.epam.esm.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "resilence4j.circuitbreaker")
public class CircuitBreakerConfiguration {

    @Bean
    public CircuitBreakerRegistry getRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .waitDurationInOpenState(Duration.ofMillis(7000L))
                .permittedNumberOfCallsInHalfOpenState(2)
                .recordExceptions(org.springframework.web.client.HttpServerErrorException.class)
                .build();
        return CircuitBreakerRegistry.of(config);
    }

}
