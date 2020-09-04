package com.example.demo.jdbc;

import com.example.demo.DataSourceConfig;
import com.example.demo.Post;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("h2")
@Slf4j
public class JdbcPostRepositoryTest {

    @Mock
    NamedParameterJdbcTemplate mockTemplate;

    @Autowired
    NamedParameterJdbcTemplate realTemplate;

    //@Spy
    NamedParameterJdbcTemplate spyTemplate;

    @InjectMocks
    JdbcPostRepository posts;


    JdbcPostRepository realPosts;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        spyTemplate = spy(realTemplate);
        realPosts = new JdbcPostRepository(spyTemplate);
    }

    @Test
    public void testMock() {
        when(mockTemplate.query(anyString(), isA(Map.class), isA(RowMapper.class)))
                .thenReturn(List.of(Post.builder().title("title").body("content").build()));

        var list = posts.findAll();
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0).getTitle()).isEqualTo("title");

        verify(mockTemplate, times(1)).query(anyString(), isA(Map.class), isA(RowMapper.class));
        verifyNoMoreInteractions(mockTemplate);

        //if no stubbing on the method `findById`, it will NOT hit database
        var post = posts.findById(1L);
        assertThat(post).isNull();
        verify(mockTemplate, times(1)).queryForObject(anyString(), isA(Map.class), isA(RowMapper.class));
        verifyNoMoreInteractions(mockTemplate);
    }

    @Test
    public void testSpy() {
        doReturn(List.of(Post.builder().title("title").body("content").build()))
                .when(spyTemplate)
                .query(anyString(), isA(Map.class), isA(RowMapper.class));

        var list = realPosts.findAll();
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0).getTitle()).isEqualTo("title");

        verify(spyTemplate, times(1)).query(anyString(), isA(Map.class), isA(RowMapper.class));
        verifyNoMoreInteractions(spyTemplate);

        //if no stubbing on the method `findById`, it will hit real database
        var post = realPosts.findById(1L);
        assertThat(post).isNotNull();
        assertThat(post.getTitle()).isEqualTo("test title");
        // can not verify the interactions here
    }
}

@Configuration
@ComponentScan
@Import({DataSourceConfig.class, JdbcConfig.class})
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