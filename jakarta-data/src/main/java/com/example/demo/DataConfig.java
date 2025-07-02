package com.example.demo;

import com.example.demo.repository.PostRepository;
import com.example.demo.repository.PostRepository_;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
public class DataConfig {

    @Bean
    public StatelessSession statelessSession(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return entityManagerFactoryBean.getObject().unwrap(SessionFactory.class).openStatelessSession();
    }

    @Bean
    public PostRepository postRepository(StatelessSession statelessSession) {
        return new PostRepository_(statelessSession);
    }

    @Bean
    public Blogger blogger(StatelessSession statelessSession) {
        return new Blogger_(statelessSession);
    }

}
