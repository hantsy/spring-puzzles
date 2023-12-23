package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest
@Slf4j
public class PostRepositoryTest {

    @TestConfiguration
    @Import(HibernateReactiveConfig.class)
    static class TestConfig {
    }

    // see: https://java.testcontainers.org/modules/databases/oraclexe/
    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
        .withDatabaseName("blogdb")
        .withUsername("user")
        .withPassword("password");

    @Autowired
    PostRepository posts;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        var latch = new CountDownLatch(1);
        this.posts.deleteAll()
            .onTermination().invoke(latch::countDown)
            .subscribe().with(data -> log.debug("post data is removed"), err -> log.error("error:" + err));
        latch.await(500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testPostRepositoryExisted() {
        assertNotNull(posts);
    }

    @Test
    public void testInsertAndQuery() throws InterruptedException {
        var data = Post.builder().title("test title").content("content of test").build();

        var latch = new CountDownLatch(1);
        this.posts.save(data)
            .chain(() ->
                this.posts.findAll()
            )
            .log()
            .onTermination().invoke(latch::countDown)
            .subscribe().with(
                p -> {
                    log.info("saved post: {}", p);
                    assertThat(p.size()).isEqualTo(2);
                    assertThat(p.getFirst().getTitle()).isEqualTo("test title");
                }
            );
        latch.await(500, TimeUnit.MILLISECONDS);
    }
}
