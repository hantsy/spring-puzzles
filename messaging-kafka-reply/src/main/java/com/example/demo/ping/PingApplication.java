package com.example.demo.ping;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@SpringBootApplication
@Slf4j
public class PingApplication {

    public static final String TOPIC_PINGPONG = "pingpong";


    public static void main(String[] args) {
        SpringApplication.run(PingApplication.class, args);
    }

    @Bean
    ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(
            ProducerFactory<String, String> producerFactory,
            GenericMessageListenerContainer<String, String> listenerContainer
    ) {
        var template= new ReplyingKafkaTemplate<String, String, String>(producerFactory, listenerContainer);
//        template.setSharedReplyTopic(false);
//        template.setDefaultReplyTimeout(Duration.ofSeconds(5));
        return template;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> listenerContainer(
            ConcurrentKafkaListenerContainerFactory<String, String> containerFactory) {

        ConcurrentMessageListenerContainer<String, String> listenerContainer =
                containerFactory.createContainer("replies");
        listenerContainer.getContainerProperties().setGroupId("repliesGroup");
        listenerContainer.setAutoStartup(false);
        return listenerContainer;
    }

    @Bean
    @SneakyThrows
    RouterFunction<ServerResponse> router(ReplyingKafkaTemplate<String, String, String> template) {
        return route()
                .GET("/",
                        req -> {
                            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_PINGPONG, "ping");
                            RequestReplyFuture<String, String, String> replyFuture = template.sendAndReceive(record);
                            replyFuture.addCallback(
                                    result -> {
                                        log.info("callback result: {}", result);
                                    },
                                    ex -> {
                                        log.info("callback ex: {}", ex.getMessage());
                                    }
                            );
                            SendResult<String, String> sendResult = replyFuture.getSendFuture().get(10, TimeUnit.SECONDS);
                            log.info("Sent ok: {}", sendResult.toString());
                            ConsumerRecord<String, String> consumerRecord = replyFuture.get(10, TimeUnit.SECONDS);
                            log.info("Return value: {}->{}", consumerRecord.key(), consumerRecord.value());

                            return ok().body(consumerRecord.value());
                        }
                )
                .build();
    }

}



