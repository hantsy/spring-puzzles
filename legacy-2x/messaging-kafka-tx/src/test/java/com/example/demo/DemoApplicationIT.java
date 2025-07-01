package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class DemoApplicationIT {

    TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    @DisplayName(" POST '/' should return status 202")
    void hello() {
        GreetingRequest requestBody = GreetingRequest.builder().name("Hantsy").build();
        var resEntity = restTemplate.postForEntity("http://localhost:8080/", requestBody, GreetingResult.class);
        assertThat(resEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

}
