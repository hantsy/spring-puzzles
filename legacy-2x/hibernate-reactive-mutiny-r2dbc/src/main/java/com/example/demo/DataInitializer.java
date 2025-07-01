package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final static Logger LOGGER = Logger.getLogger(DataInitializer.class.getName());

    private final Mutiny.SessionFactory sessionFactory;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("Data initialization is starting...");

        Post first = Post.of(null, "Hello Spring", "My first post of Spring", null);
        Post second = Post.of(null, "Hello Hibernate Reactive", "My second Hibernate Reactive", null);

        sessionFactory
            .withTransaction(
                (conn, tx) -> conn.createQuery("DELETE FROM Post").executeUpdate()
                    .flatMap(r -> conn.persistAll(first, second))
                    .chain(conn::flush)
                    .flatMap(r -> conn.createQuery("SELECT p from Post p", Post.class).getResultList())
            )
            .subscribe()
            .with(
                data -> LOGGER.log(Level.INFO, "saved data:{0}", data),
                throwable -> LOGGER.warning("Data initialization is failed:" + throwable.getMessage())
            );
    }
}
