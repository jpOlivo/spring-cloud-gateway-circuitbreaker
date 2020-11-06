# Circuit Breaker on Spring Cloud Gateway

Demo que implementa Circuit Breaker sobre un api gateway.
El gateway implementado en esta demo esta basado sobre Spring Cloud Gateway y hace uso de [Spring Cloud Circuit Breaker](https://docs.spring.io/spring-cloud-circuitbreaker/docs/1.0.4.RELEASE/reference/html/) y [resilence4j](https://resilience4j.readme.io/docs/circuitbreaker).


## Examples

1. Tests __without__ fallback

```yaml
spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      #metrics.enabled: true    
      routes:
      - id: account-service-mvc
        uri: http://localhost:8091
        predicates:
        - Path=/mvc/account/**
        filters:
        - StripPrefix=1
        - name: CircuitBreaker
          args:
            name: exampleCircuitBreaker
```

```bash
$ ./mvnw -Dtest=CircuitbreakerGwApplicationTests#testAccountServiceMvc test

13:24:23.184 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #1 - Received: status->504 GATEWAY_TIMEOUT http://localhost:64074/mvc/account/1 GET
13:24:23.409 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #2 - Received: status->200 OK http://localhost:64074/mvc/account/1 GET
13:24:23.621 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #3 - Received: status->200 OK http://localhost:64074/mvc/account/1 GET
13:24:23.782 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #4 - Received: status->200 OK http://localhost:64074/mvc/account/1 GET
13:24:23.930 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #5 - Received: status->200 OK http://localhost:64074/mvc/account/1 GET
13:24:24.159 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #6 - Received: status->504 GATEWAY_TIMEOUT http://localhost:64074/mvc/account/1 GET
13:24:24.264 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #7 - Received: status->200 OK http://localhost:64074/mvc/account/1 GET
13:24:24.431 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #8 - Received: status->200 OK http://localhost:64074/mvc/account/1 GET
13:24:24.648 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #9 - Received: status->200 OK http://localhost:64074/mvc/account/1 GET
13:24:24.738 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #10 - Received: status->200 OK http://localhost:64074/mvc/account/1 GET
13:24:24.963 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #11 - Received: status->504 GATEWAY_TIMEOUT http://localhost:64074/mvc/account/1 GET
13:24:25.174 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #12 - Received: status->200 OK http://localhost:64074/mvc/account/1 GET
13:24:25.324 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #13 - Received: status->200 OK http://localhost:64074/mvc/account/1 GET
13:24:25.544 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #14 - Received: status->504 GATEWAY_TIMEOUT http://localhost:64074/mvc/account/1 GET
13:24:25.780 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #15 - Received: status->504 GATEWAY_TIMEOUT http://localhost:64074/mvc/account/1 GET
13:24:26.010 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #16 - Received: status->504 GATEWAY_TIMEOUT http://localhost:64074/mvc/account/1 GET
13:24:26.185 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #17 - Received: status->200 OK http://localhost:64074/mvc/account/1 GET
13:24:26.420 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #18 - Received: status->504 GATEWAY_TIMEOUT http://localhost:64074/mvc/account/1 GET
2020-11-02T13:24:26.639224-03:00[America/Argentina/Buenos_Aires]: CircuitBreaker 'exampleCircuitBreaker' changed state from CLOSED to OPEN
13:24:26.644 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #19 - Received: status->504 GATEWAY_TIMEOUT http://localhost:64074/mvc/account/1 GET
13:24:26.655 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #20 - Received: status->503 SERVICE_UNAVAILABLE http://localhost:64074/mvc/account/1 GET
13:24:26.666 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #21 - Received: status->503 SERVICE_UNAVAILABLE http://localhost:64074/mvc/account/1 GET
13:24:26.674 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #22 - Received: status->503 SERVICE_UNAVAILABLE http://localhost:64074/mvc/account/1 GET
...
```


```bash
$ ./mvnw -Dtest=CircuitbreakerGwApplicationTests#testAccountServiceMvcWithFallback

...
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.22.2:test (default-test) on project circuitbreaker-gw: There are test failures.
```

2. Test __with__ fallback

```yaml
spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      #metrics.enabled: true    
      routes:
      - id: account-service-mvc
        uri: http://localhost:8091
        predicates:
        - Path=/mvc/account/**
        filters:
        - StripPrefix=1
        - name: CircuitBreaker
          args:
            name: exampleCircuitBreaker
            fallbackUri: forward:/fallback/account
            
      - id: external-fallback
        uri: http://localhost:8091
        predicates:
        - Path=/fallback/account
        filters:
        - name: FallbackHeaders
          #args:
          #  executionExceptionTypeHeaderName: Test-Header
```

```bash
$ ./mvnw -Dtest=CircuitbreakerGwApplicationTests#testAccountServiceMvcWithFallback

13:31:56.191 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #1 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:56.309 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #2 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:56.517 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #3 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:56.695 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #4 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:56.909 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #5 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:57.111 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #6 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:57.307 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #7 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:57.504 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #8 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:57.670 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #9 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:57.879 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #10 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:58.125 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #11 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:58.360 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #12 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:58.470 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #13 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:58.711 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #14 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:58.845 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #15 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:59.086 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #16 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:59.325 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #17 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
2020-11-02T13:31:59.539674-03:00[America/Argentina/Buenos_Aires]: CircuitBreaker 'exampleCircuitBreaker' changed state from CLOSED to OPEN
13:31:59.551 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #18 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:59.567 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #19 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
13:31:59.586 [main] INFO  c.d.c.g.CircuitbreakerGwApplicationTests - #20 - Received: status->200 OK http://localhost:64162/mvc/account/1 GET
...

```

## References
* [Spring Cloud Circuit Breaker](https://docs.spring.io/spring-cloud-circuitbreaker/docs/1.0.4.RELEASE/reference/html/)

* [Spring Cloud Gateway - Circuit Breaker Filter](https://cloud.spring.io/spring-cloud-gateway/reference/html/#spring-cloud-circuitbreaker-filter-factory)

