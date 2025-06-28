package com.example.demo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.reset;
import static org.hamcrest.CoreMatchers.equalTo;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationRestAssuredTests {

    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    public void tearDown() {
        reset();
    }

    @Test
    void getAllPosts() {
        //@formatter:off
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/posts")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(2));
        //@formatter:on
    }

    @Test
    void getPostByNonExistingId() {
        //@formatter:off
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/posts/{id}", new Random(10).nextLong(10_0000))
        .then()
            .statusCode(404);
        //@formatter:on
    }

    @Test
    void deletePostByNonExistingId() {
        //@formatter:off
        given()
        .when()
            .delete("/posts/{id}", new Random(10).nextLong(10_0000))
        .then()
            .statusCode(404);
        //@formatter:on
    }

}
