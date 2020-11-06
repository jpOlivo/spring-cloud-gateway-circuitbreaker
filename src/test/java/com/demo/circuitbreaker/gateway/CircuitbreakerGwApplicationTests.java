package com.demo.circuitbreaker.gateway;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 8091)
@Testcontainers
@Slf4j
class CircuitbreakerGwApplicationTests {

	private static final double SIGMA_NORMAL_DISTRIBUTION = 0.4;
	private static final int MEDIAN_NORMAL_DISTRIBUTION = 180; // 200 400 180
	

	private static Jedis underTest;

	@Container
	public static GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
			.withExposedPorts(6379);

	@Autowired
	private WebTestClient webClient;
	

	@BeforeAll
	public static void init() {

		// Supplying a cache with some fallback values
		underTest = new Jedis(redis.getHost(), redis.getFirstMappedPort());
		underTest.set("fallbackAccount", "{\"id\":1,\"number\":\"1111111111\"}");

		// Stubbing WireMock
		stubFor(get(urlEqualTo("/account/1")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{\"id\":1,\"number\":\"1111111111\"}")
				.withLogNormalRandomDelay(MEDIAN_NORMAL_DISTRIBUTION, SIGMA_NORMAL_DISTRIBUTION)));

		stubFor(get(urlEqualTo("/fallback/account")).withHeader("Execution-Exception-Type", containing(""))
				.withHeader("Execution-Exception-Message", containing("")).willReturn(aResponse()
						.withHeader("Content-Type", "application/json").withBody(underTest.get("fallbackAccount"))));

	}

	@RepeatedTest(300)
	public void testAccountServiceMvc(RepetitionInfo repetitionInfo) {
		webClient.get().uri("/mvc/account/1").exchange().expectBody().consumeWith(
				response -> log.info("#{} - Received: status->{} {} {}", repetitionInfo.getCurrentRepetition(),
						response.getStatus(), response.getUrl(), response.getMethod()));
	}

	@RepeatedTest(300)
	public void testAccountServiceMvcWithFallback(RepetitionInfo repetitionInfo) {
		webClient.get().uri("/mvc/account/1").exchange().expectStatus().isOk()
				.expectBody().jsonPath("$.id").isEqualTo("1").jsonPath("$.number").isEqualTo("1111111111").consumeWith(
						response -> log.info("#{} - Received: status->{} {} {}", repetitionInfo.getCurrentRepetition(),
								response.getStatus(), response.getUrl(), response.getMethod()));
	}
	
}
