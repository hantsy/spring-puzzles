package com.example.demo;

import com.example.demo.jdbc.DataJdbcPostRepository;
import com.example.demo.jdbc.JdbcPostRepository;
import com.example.demo.jpa.DataJpaPostRepository;
import com.example.demo.jpa.JpaPostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.RequestPredicates.accept;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@EnableWebMvc
@Configuration
public class WebConfig implements WebMvcConfigurer{

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        var jackson2MessageConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        converters.add(jackson2MessageConverter);
    }

    @Bean
    RouterFunction<ServerResponse> routes(
            DataJpaPostRepository posts,
            JpaPostRepository jpaPosts,
            DataJdbcPostRepository dataJdbcPosts,
            JdbcPostRepository jdbcPosts
    ) {
        return route()
                .GET("/", accept(APPLICATION_JSON), req -> ok().body(posts.findAll()))
                .GET("/jpa", accept(APPLICATION_JSON), req -> ok().body(jpaPosts.findAll()))
                .GET("/datajdbc", accept(APPLICATION_JSON), req -> ok().body(dataJdbcPosts.findAll()))
                .GET("/jdbc", accept(APPLICATION_JSON), req -> ok().body(jdbcPosts.findAll()))
                //.after((req,res) ->logRequest(req, res))
                .build();
    }
}
