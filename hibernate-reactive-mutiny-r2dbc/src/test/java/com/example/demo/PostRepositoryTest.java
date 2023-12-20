package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

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
    static class TestConfig {}

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");
       // .withCopyFileToContainer(MountableFile.forClasspathResource("init.sql"), "/docker-entrypoint-initdb.d/init.sql");

//    @DynamicPropertySource
//    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
//            + postgreSQLContainer.getHost() + ":" + postgreSQLContainer.getFirstMappedPort()
//            + "/" + postgreSQLContainer.getDatabaseName());
//        registry.add("spring.r2dbc.username", () -> postgreSQLContainer.getUsername());
//        registry.add("spring.r2dbc.password", () -> postgreSQLContainer.getPassword());
//    }


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
