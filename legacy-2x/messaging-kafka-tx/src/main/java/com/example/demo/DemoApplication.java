package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.accepted;

@SpringBootApplication
@Slf4j
public class DemoApplication {
    public static final String TOPIC_NAME = "tx";

    public static final String TOPIC_LOGGER = "logger";

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    @SneakyThrows
    RouterFunction<ServerResponse> router(KafkaTemplate<String, Object> kafkaTemplate) {
        return route()
                .POST("/",
                        req -> {
                            var body = req.body(GreetingRequest.class);
                            var result = kafkaTemplate
                                    .executeInTransaction(
                                            operations -> operations.
                                                    send(TOPIC_NAME, body)
                                    );
                            log.info("rx result: {}", result);

                            return accepted().build();
                        }
                )
                .build();
    }


    @Bean
    public ChainedKafkaTransactionManager<Object, Object> chainedTm(
            KafkaTransactionManager<String, String> ktm,
            DataSourceTransactionManager dstm) {

        return new ChainedKafkaTransactionManager<>(ktm, dstm);
    }


    @Bean
    public DataSourceTransactionManager dstm(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory,
            ChainedKafkaTransactionManager<Object, Object> chainedTM) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.getContainerProperties().setTransactionManager(chainedTM);
        return factory;
    }

}

@RequiredArgsConstructor
@Component
@Slf4j
class HelloTxListener {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(id = "tx", topics = DemoApplication.TOPIC_NAME)
    public void listen1(GreetingRequest in) {
        var result = GreetingResult.builder().message("Hi, " + in.getName()).createdAt(LocalDateTime.now()).build();
        this.kafkaTemplate.send("logger", result);
        var updated = this.jdbcTemplate.update(
                "insert into REQUESTS(NAME, CREATED_AT) values (:name, :created)",
                Map.of("name", in.getName(), "created", LocalDateTime.now())
        );
        log.info("inserted requests into db: {}", updated);
    }
}

@Component
@Slf4j
class LoggerHandler {
    @KafkaListener(id = "log", topics = DemoApplication.TOPIC_LOGGER)
    public void handle(GreetingResult result) {
        log.info("Received result: {} in {}", result, this.getClass().getName());
    }
}


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class GreetingRequest {
    String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class GreetingResult {
    String message;
    LocalDateTime createdAt;
}