package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DemoApplicationTests {

    private TestRestTemplate client;

    @BeforeEach
    void setUp() {
        this.client = new TestRestTemplate();
    }

    @Test
    void getAllPosts() {
        var response = this.client.getForEntity("http://localhost:8080/", Post[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().length).isEqualTo(2);
    }

}
