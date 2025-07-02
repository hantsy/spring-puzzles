package com.example.demo;

import com.example.demo.model.Post;
import com.example.demo.model.PostSummary;
import com.example.demo.model.Status;
import com.example.demo.repository.PostRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author hantsy
 */
@Slf4j
@SpringJUnitConfig(
        classes = {
                DataSourceConfig.class,
                JpaConfig.class,
                DataConfig.class,
                PostRepositoryTest.TestConfig.class
        }
)
@ContextConfiguration(initializers = PostRepositoryTest.TestContainerInitializer.class)
public class PostRepositoryTest {
    static class TestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            final PostgreSQLContainer container = new PostgreSQLContainer("postgres:12");
            container.start();
            log.info(" container.getFirstMappedPort():: {}", container.getFirstMappedPort());
            configurableApplicationContext
                    .addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> container.stop());
            var env = configurableApplicationContext.getEnvironment();
            var props = env.getPropertySources();
            props.addFirst(
                    new MapPropertySource("testdatasource",
                            Map.of("datasource.url", container.getJdbcUrl(),
                                    "datasource.username", container.getUsername(),
                                    "datasource.password", container.getPassword()
                            )
                    )
            );

        }
    }

    //@Inject
    @Autowired
    PostRepository posts;

    @Autowired
    Blogger blogger;

    @Autowired
    TransactionTemplate txTemplate;

    @SneakyThrows
    @BeforeEach
    public void setup() {
//        this.posts.findAll().forEach(this.posts::delete);
//
//        txTemplate.executeWithoutResult(transactionStatus -> {
        var deleted = posts.deleteAll();
        log.debug("deleted posts: {}", deleted);
        //       });
    }

    @Test
    public void testSaveAll() {

        var data = List.of(
                Post.builder().title("test").content("content").status(Status.PENDING_MODERATION).build(),
                Post.builder().title("test1").content("content1").build());
        data.forEach(this.posts::insert);

        var results = posts.findAll();
        assertThat(results.toList().size()).isEqualTo(2);

        var resultsByKeyword = posts.byStatus(Status.PENDING_MODERATION);
        assertThat(resultsByKeyword.size()).isEqualTo(1);
    }

    @Test
    public void testInsertAndQuery() {
        var data = Post.builder().title("test").content("test content").status(Status.DRAFT).build();
        var saved = this.posts.insert(data);
        assertThat(this.posts.findById(saved.getId()).isPresent()).isTrue();
        assertThat(saved.getStatus()).isEqualTo(Status.DRAFT);
    }

    @Test
    public void testBlogger() {
        var data = Post.builder().title("test").content("test content").status(Status.DRAFT).build();
        var saved = blogger.newPost(data);
        assertThat(this.posts.findById(saved.getId()).isPresent()).isTrue();

        saved.setStatus(Status.PUBLISHED);
        posts.update(saved);

        List<PostSummary> allPublished = blogger.allPublishedPosts();
        assertThat(allPublished.size()).isEqualTo(1);
    }

    @Configuration
    @ComponentScan(basePackageClasses = JpaConfig.class)
    static class TestConfig {
    }

}
