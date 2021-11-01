package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.ServletContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class DemoApplicationTests {

    TestRestTemplate restTemplate;

    @LocalServerPort
    int port;

    @Autowired
    ServletContext servletContext;

    @BeforeEach
    void setUp() {
        this.restTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:" + port));
    }

    @Test
    void testHelloServlet() throws Exception {
        var entity = this.restTemplate.getForEntity("/hello?name=Hantsy", Greeting.class);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody().getMessage()).isEqualTo("Hello, Hantsy!");
        assertThat(entity.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
    }

    @Test
    void testHelloFilter() throws Exception {
        var entity = this.restTemplate.getForEntity("/greet?name=Hantsy", Greeting.class);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody().getMessage()).isEqualTo("Hello, Hantsy!");
        assertThat(entity.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
    }

    @Test
    void testServletContext() {
        var greeting = servletContext.getAttribute("greeting");
        log.debug("attribute greeting in servlet context: {}", greeting);
        assertThat(greeting).isNotNull();
    }

}
