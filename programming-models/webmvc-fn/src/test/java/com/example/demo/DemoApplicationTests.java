package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class DemoApplicationTests {

    @LocalServerPort
    Integer port;

    RestClient client;

    @BeforeEach
    public void setup() {
        var jdkHttpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(30_000))
                .build();

        this.client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .requestFactory(new JdkClientHttpRequestFactory(jdkHttpClient))
                .build();
    }

    @Test
    void getAllPosts() {
        var response = this.client
                .get().uri("/posts")
                .retrieve()
                //@formatter:off
                .toEntity(new ParameterizedTypeReference<List<Post>>() { });
                //@formatter:on

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.hasBody()).isTrue();
        assertThat(response.getBody().size()).isEqualTo(2);
    }

    @Test
    void getPostByNonExistingId() {
        assertThatThrownBy(() -> this.client
                .get().uri("/posts/" + new Random(10).nextLong(10_0000))
                .retrieve()
                .toEntity(Post.class)
        ).isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void deletePostByNonExistingId() {
        assertThatThrownBy(() -> this.client
                .delete().uri("/posts/" + new Random(10).nextLong(10_0000))
                .retrieve()
                .toBodilessEntity()
        ).isInstanceOf(HttpClientErrorException.NotFound.class);
    }
}
