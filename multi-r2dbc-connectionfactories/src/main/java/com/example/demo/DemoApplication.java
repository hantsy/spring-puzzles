package com.example.demo;

import com.example.demo.customers.Customer;
import com.example.demo.orders.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

@SpringBootApplication(exclude = R2dbcAutoConfiguration.class)
@Slf4j
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(
            R2dbcRepository<Customer, Long> customers,
            R2dbcRepository<Order, Integer> orders
    ) {
        return args -> {
            customers
                    .save(Customer.builder().firstName("Hantsy@DemoApplication").lastName("Bai").build())
                    .log()
                    //see: https://stackoverflow.com/questions/63878598/numeric-types-mapping-issue-in-r2dbc-postgres
                    // and https://docs.spring.io/spring-data/r2dbc/docs/1.1.3.RELEASE/reference/html/#r2dbc.multiple-databases
                    .flatMap(
                            c -> orders
                                    .save(Order.builder().customerId(c.getId()).amount(201.0).build())
                    )
                    .log()
                    .subscribe();
        };
    }
}
