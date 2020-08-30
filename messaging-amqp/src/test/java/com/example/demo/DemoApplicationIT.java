package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class DemoApplicationIT {

    TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    @DisplayName(" POST '/' should return status 200")
    void getAllPosts() {
        var requestBody= SignupRequest.builder().fullName("John Doe").phone("5 46 31 71 71").build();
        var resEntity = restTemplate.postForEntity("http://localhost:8080/", requestBody, SignupResult.class);
        assertThat(resEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        var result = resEntity.getBody();
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getPhone()).isEqualTo("+33546317171");
    }

}
