package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;


@DataR2dbcTest
@Import({TestcontainersConfiguration.class, PostRepository.class})
public class PostRepositoryTest {

    @Autowired
    private PostRepository posts;

    @Test
    void testCurd() {
        long id = new Random(10).nextLong(10_000);
        posts.create(new Post(id, "new title", "new content", null))
                .as(StepVerifier::create)
                .consumeNextWith(savedId -> assertThat(savedId).isEqualTo(id))
                .verifyComplete();

        posts.findAll()
                .as(StepVerifier::create)
                .expectNextCount(3)
                .verifyComplete();

        posts.findById(id)
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertThat(p.id()).isEqualTo(id))
                .verifyComplete();

        posts.update(id, new Post(id, "updated title", "updated content", null))
                .as(StepVerifier::create)
                .consumeNextWith(c -> assertThat(c).isGreaterThan(0))
                .verifyComplete();

        posts.findById(id)
                .as(StepVerifier::create)
                .consumeNextWith(p -> assertThat(p.title()).isEqualTo("updated title"))
                .verifyComplete();

        posts.deleteById(id)
                .as(StepVerifier::create)
                .consumeNextWith(c -> assertThat(c).isGreaterThan(0))
                .verifyComplete();

        posts.findById(id)
                .as(StepVerifier::create)
                .verifyComplete();
    }
}
