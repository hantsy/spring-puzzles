package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class DemoApplicationIT {

    RestTemplate restTemplate = new RestTemplate();

    @Test
    @DisplayName(" GET '/' should return status 200")
    void getAllPosts() {
        var resEntity = restTemplate.getForEntity("http://localhost:8080/demo", Post[].class);
        assertThat(resEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        var posts = resEntity.getBody();
        assertThat(posts.length).isEqualTo(1);
        assertThat(posts[0].getTitle()).contains("Tomcat");
    }

}
