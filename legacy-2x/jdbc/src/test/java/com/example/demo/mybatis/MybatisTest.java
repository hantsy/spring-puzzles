package com.example.demo.mybatis;

import com.example.demo.DataSourceConfig;
import com.example.demo.Post;
import com.example.demo.mybatis.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("h2")
@Slf4j
public class MybatisTest {

    @Autowired
    MybatisPostRepository mybatisPosts;

    @Autowired
    MybatisJdbcPostRepository postsRepository;

    @Autowired
    PostMapper mapper;

    @BeforeEach()
    public void setup() {
    }

    @Test
    public void testFindAll() {
        var posts = postsRepository.findAll();
        posts.forEach(post -> log.info("post: {}", post));

        // Create a List from the Iterable
        List<Post> list = StreamSupport
                .stream(posts.spliterator(), false)
                .collect(Collectors.toList());

        assertThat(list.size()).isEqualTo(1);
        log.info("list.get(0).getTitle(): {}", list.get(0).getTitle());
    }

    @Test
    public void testFindAllWithTemplate() {
        var result = mybatisPosts.findAll();
        result.forEach(post -> log.info("result: {}", result));
        assertThat(result.size()).isEqualTo(1);
        log.info("result.get(0).getTitle(): {}", result.get(0).getTitle());
        log.info("result.get(0).getBody(): {}", result.get(0).getBody());
        assertThat(result.get(0).getTitle()).isNotNull();
    }

    @Test
    public void testFindAllWithMapper() {
        var result = mapper.findAll();
        result.forEach(post -> log.info("result: {}", result));
        assertThat(result.size()).isEqualTo(1);
        log.info("result.get(0).getTitle(): {}", result.get(0).getTitle());
        log.info("result.get(0).getBody(): {}", result.get(0).getBody());
        assertThat(result.get(0).getTitle()).isNotNull();
    }

    @Test
    public void testFindAllSummariesWithMapper() {
        var result = mapper.findSummaries();
        result.forEach(post -> log.info("result: {}", result));
        assertThat(result.size()).isEqualTo(1);
        log.info("result.get(0).getTitle(): {}", result.get(0).getTitle());
        assertThat(result.get(0).getTitle()).isNotNull();
    }
}

@Configuration
@ComponentScan
@Import({DataSourceConfig.class, MybatisConfig.class})
class TestConfig {
    @Autowired
    DataSource dataSource;

    @PostConstruct
    public void init() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(
                new ClassPathResource("schema.sql"),
                new ClassPathResource("data.sql")
        );
        populator.execute(dataSource);
    }

}
