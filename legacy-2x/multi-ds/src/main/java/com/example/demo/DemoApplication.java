package com.example.demo;

import com.example.demo.customers.CustomerEntity;
import com.example.demo.orders.CustomerId;
import com.example.demo.orders.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        TransactionAutoConfiguration.class
})
@EnableJpaAuditing
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(JpaRepository<CustomerEntity, Long> customers, JpaRepository<OrderEntity, Long> orders) {
        return args -> {
            var savedCustomer = customers.save(CustomerEntity.builder().firstName("Hantsy").lastName("Bai").build());
            log.info("saved customer: {}", savedCustomer);

            var savedOrder = orders.save(OrderEntity.builder().customer(new CustomerId(savedCustomer.getId())).amount(1.2).build());
            log.info("saved order: {}", savedOrder);
        };
    }

}
