package com.example.demo;

import com.example.demo.todo.Todo;
import com.example.demo.todo.TodoRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@DataR2dbcTest
@Slf4j
public class TodoRepositoryTest {

    // see: https://java.testcontainers.org/modules/databases/oraclexe/
    @Container
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
        .withDatabaseName("blogdb")
        .withUsername("user")
        .withPassword("password");

    @Autowired
    TodoRepository todos;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        var latch = new CountDownLatch(1);
        this.todos.deleteAll()
            .doOnTerminate(latch::countDown)
            .subscribe(data -> log.debug("post data is removed"), err -> log.error("error:" + err));
        latch.await(500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testPostRepositoryExisted() {
        assertNotNull(todos);
    }

    @Test
    public void testInsertAndQuery() {
        var data = new Todo(null, "test title");

        todos.save(data)
            .thenMany(this.todos.findAll())
            .as(StepVerifier::create)
            .consumeNextWith(
                p -> {
                    log.info("saved post: {}", p);
                    assertThat(p.title()).isEqualTo("test title");
                }
            )
            .verifyComplete();
    }
}
