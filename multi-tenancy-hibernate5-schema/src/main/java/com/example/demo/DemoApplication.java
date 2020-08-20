package com.example.demo;

import com.example.demo.model.CustomerEntity;
import com.example.demo.model.CustomerId;
import com.example.demo.model.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;

@SpringBootApplication()
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(JpaRepository<CustomerEntity, Long> customers, JpaRepository<OrderEntity, Long> orders) {
        log.info(">>> DemoApplication: starting initializing...");
        return args -> {
            var savedCustomer = customers.save(CustomerEntity.builder().firstName("Hantsy").lastName("Bai").build());
            log.info("saved customer: {}", savedCustomer);

            var savedOrder = orders.save(OrderEntity.builder().customer(new CustomerId(savedCustomer.getId())).amount(1.2).build());
            log.info("saved order: {}", savedOrder);
        };
    }

}
