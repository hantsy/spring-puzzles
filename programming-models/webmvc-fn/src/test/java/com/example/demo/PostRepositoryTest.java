package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;


@JdbcTest
@Import({TestcontainersConfiguration.class, PostRepository.class})
public class PostRepositoryTest {

    @Autowired
    private PostRepository posts;

    @Test
    void testCurd() {
        var savedId = posts.create(new Post(null, "new title", "new content", null));

        assertThat(savedId).isNotNull();
        assertThat(posts.findAll().anyMatch(post -> post.title().equals("new title"))).isTrue();

        assertThat(posts.existsById(savedId)).isTrue();

        var byId = posts.findById(savedId);
        assertThat(byId.isPresent()).isTrue();
        assertThat(byId.get().title()).isEqualTo("new title");

        var updatedRow = posts.update(savedId, new Post(null, "updated title", "updated content", null));
        assertThat(updatedRow).isGreaterThan(0);

        posts.findById(savedId).ifPresent(
                post -> assertThat(post.title()).isEqualTo("updated title")
        );

        var deletedRow = posts.deleteById(savedId);
        assertThat(deletedRow).isGreaterThan(0);

        assertThat(posts.existsById(savedId)).isFalse();
    }
}
