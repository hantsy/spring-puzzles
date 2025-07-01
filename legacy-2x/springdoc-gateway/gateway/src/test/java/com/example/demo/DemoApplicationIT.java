package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DemoApplicationIT {//does not depend on Spring IOC

    TestRestTemplate restTemplate;
    String baseUrl = "http://localhost:8080";

    @BeforeEach
    public void setup() {

        if (System.getenv().containsKey("BASE_API_URL")) {
            baseUrl = System.getenv("BASE_API_URL");
        }
        restTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri(baseUrl));
    }

    @Test
    @DisplayName(" GET '/customers' should return status 200")
    void getAll() {
        var resEntity = restTemplate.getForEntity("/customers", CustomerDto[].class);
        assertThat(resEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        var customerDtos = resEntity.getBody();
        log.info("All courses: {}", customerDtos);
        assertThat(customerDtos.length).isEqualTo(2);
        assertThat(customerDtos).anyMatch(c -> c.name().equals("Hantsy"));
    }

}

record CustomerDto(String name) {
}