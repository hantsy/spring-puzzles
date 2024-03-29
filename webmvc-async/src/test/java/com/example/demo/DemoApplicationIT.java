package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class DemoApplicationIT {

    TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    @DisplayName(" GET '/' should return status 200")
    void getAllPosts() {
        var resEntity = restTemplate.getForEntity("http://localhost:8080/posts", Post[].class);
        assertThat(resEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        var posts = resEntity.getBody();
        assertThat(posts.length).isEqualTo(2);
        assertThat(posts).anyMatch(p -> p.getTitle().contains("Spring"));
    }
}
