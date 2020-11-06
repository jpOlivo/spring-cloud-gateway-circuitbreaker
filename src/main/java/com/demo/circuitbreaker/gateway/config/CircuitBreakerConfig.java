package com.demo.circuitbreaker.gateway.config;

import java.time.Duration;

import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;


@Configuration
public class CircuitBreakerConfig {
	
	@Bean
	public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
	return factory -> {
			factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				//.circuitBreakerConfig(o.github.resilience4j.circuitbreaker.CircuitBreakerConfig.ofDefaults()) // slidingWindowSize: 100 requests // failureRateThreshold: 50%
				.circuitBreakerConfig(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
			            .custom()
			            .slidingWindowSize(10)
			            .failureRateThreshold(60.0F)
			            .waitDurationInOpenState(Duration.ofMillis(1000))
			            .permittedNumberOfCallsInHalfOpenState(1)
			            .build())
				.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(200)).build())
				.build());
			
			
			factory.addCircuitBreakerCustomizer(
					Customizer.once(circuitBreaker -> circuitBreaker.getEventPublisher()
							.onStateTransition(e -> System.out.println(e)), CircuitBreaker::getName),
					"exampleCircuitBreaker");
		};
	}
}
