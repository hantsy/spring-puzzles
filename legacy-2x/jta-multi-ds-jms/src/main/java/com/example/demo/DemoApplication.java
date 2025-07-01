package com.example.demo;

import com.example.demo.customers.CustomerEntity;
import com.example.demo.orders.CustomerId;
import com.example.demo.orders.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.transaction.PlatformTransactionManagerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootApplication
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Autowired
    TransactionTemplate template;

    @Bean
    ApplicationRunner runner(JpaRepository<CustomerEntity, Long> customers, JpaRepository<OrderEntity, Long> orders) {
        return args -> {
            log.info("template tx: {}", template.getTransactionManager());
            template.executeWithoutResult(
                    transactionStatus -> {
                        var savedCustomer = customers.save(CustomerEntity.builder().firstName("Hantsy").lastName("Bai").build());
                        log.info("saved customer: {}", savedCustomer);

                        var savedOrder = orders.save(OrderEntity.builder().customer(new CustomerId(savedCustomer.getId())).amount(1.2).build());
                        log.info("saved order: {}", savedOrder);

                    }
            );

        };
    }

    @Bean
    PlatformTransactionManagerCustomizer<JtaTransactionManager> jtaTxManagerCustomizer() {
        return transactionManager -> {
            log.info("tx: {}", transactionManager.getTransactionManager());
        };
    }

}
